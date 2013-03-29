/*  Copyright (c) 2006-2007, Vladimir Nikic
    All rights reserved.

    Redistribution and use of this software in source and binary forms,
    with or without modification, are permitted provided that the following
    conditions are met:

    * Redistributions of source code must retain the above
      copyright notice, this list of conditions and the
      following disclaimer.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the
      following disclaimer in the documentation and/or other
      materials provided with the distribution.

    * The name of Web-Harvest may not be used to endorse or promote
      products derived from this software without specific prior
      written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.

    You can contact Vladimir Nikic by sending e-mail to
    nikic_vladimir@yahoo.com. Please include the word "Web-Harvest" in the
    subject line.
*/
package org.webharvest.runtime.processors;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webharvest.WHConstants;
import org.webharvest.definition.IElementDef;
import org.webharvest.events.ProcessorStartEvent;
import org.webharvest.events.ProcessorStopEvent;
import org.webharvest.ioc.DebugFileLogger;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;
import org.webharvest.utils.KeyValuePair;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

/**
 * Base processor that contains common processor logic.
 * All other processors extend this class.
 */
public abstract class AbstractProcessor<TDef extends IElementDef> implements Processor<TDef> {

    // TODO Consider making it a static logger
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractProcessor.class);

    @DebugFileLogger @Inject private Logger debugFileLogger;

    abstract protected Variable execute(DynamicScopeContext context) throws InterruptedException;

    protected TDef elementDef;

    private Map<String, Object> properties = new LinkedHashMap<String, Object>();

    private Processor parentProcessor;

    @Inject
    private EventBus eventBus;

    protected AbstractProcessor() {
    }

    /**
     * Wrapper for the execute method. Adds controlling and logging logic.
     */
    @Override
    public Variable run(DynamicScopeContext context) throws InterruptedException {

        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }

        final long startTime = System.currentTimeMillis();

        final String id = (this.elementDef != null) ? BaseTemplater.evaluateToString(this.elementDef.getId(), null, context) : null;

        setProperty("ID", id);

        //TODO: mczapiewski Information about beginning and finishing of
        //processing should not be logged for BodyProcessor, because it only
        //delegates processing to other processors.
        if (LOG.isInfoEnabled()) {
            LOG.info("{}{} starts processing...{}", new Object[]{
                    CommonUtil.indent(getRunningLevel()),
                    getClass().getSimpleName(),
                    id != null ? "[ID=" + id + "] " : ""});
        }

        eventBus.post(new ProcessorStartEvent(this));

        final Variable result = execute(context);
        final long executionTime = System.currentTimeMillis() - startTime;

        setProperty(WHConstants.EXECUTION_TIME_PROPERTY_NAME, executionTime);
        setProperty(WHConstants.VALUE_PROPERTY_NAME, result);

        eventBus.post(new ProcessorStopEvent(this, properties));

        writeDebugFile(id, result);

        //TODO: mczapiewski Information about beginning and finishing of
        //processing should not be logged for BodyProcessor, because it only
        //delegates processing to other processors.
        if (LOG.isInfoEnabled()) {
            LOG.info("{}{} processor executed in {}ms. {}", new Object[]{
                    CommonUtil.indent(getRunningLevel()),
                    getClass().getSimpleName(),
                    executionTime,
                    id != null ? "[ID=" + id + "] " : ""});
        }

        return result;
    }

    /**
     * Defines processor runtime property with specified name and value.
     *
     * @param name
     *            name of the property
     * @param value
     *            value of the property
     */
    protected void setProperty(String name, Object value) {
        if (name != null && !"".equals(name) && value != null) {
            this.properties.put(name, value);
        }
    }

    protected Variable getBodyTextContent(IElementDef elementDef, DynamicScopeContext context,
                                          boolean registerExecution, KeyValuePair properties[]) throws InterruptedException {
        if (elementDef.hasOperations()) {
            BodyProcessor bodyProcessor = new BodyProcessor.Builder(elementDef).
                setParentProcessor(this).build();
            if (properties != null) {
                for (KeyValuePair property : properties) {
                    bodyProcessor.setProperty(property.getKey(), property.getValue());
                }
            }
            return registerExecution ? bodyProcessor.run(context) : bodyProcessor.execute(context);
        }

        return EmptyVariable.INSTANCE;
    }

    protected Variable getBodyTextContent(IElementDef elementDef, DynamicScopeContext context, boolean registerExecution) throws InterruptedException {
        return getBodyTextContent(elementDef, context, registerExecution, null);
    }

    protected Variable getBodyTextContent(IElementDef elementDef, DynamicScopeContext context) throws InterruptedException {
        return getBodyTextContent(elementDef, context, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setElementDef(final TDef elementDef) {
        this.elementDef = elementDef;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TDef getElementDef() {
        return elementDef;
    }

    //TODO: can we remove elementDef parameter? (it is already accessible as a field)
    protected void debug(final IElementDef elementDef,
            final DynamicScopeContext context, final Variable variable) {
        final String id = (elementDef != null) ? BaseTemplater.evaluateToString(
                elementDef.getId(), null, context) : null;
        writeDebugFile(id, variable);

    }

    private void writeDebugFile(final String processorId, final Variable var) {
        if (processorId != null && var != null) {
            debugFileLogger.trace("[{}]\n{}\n\n", processorId, var.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParentProcessor(final Processor parentProcessor) {
        this.parentProcessor = parentProcessor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Processor getParentProcessor() {
        return parentProcessor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRunningLevel() {
        return parentProcessor == null ? 1 :
            parentProcessor.getRunningLevel() + 1;
    }

}