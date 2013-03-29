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
package org.webharvest.definition;

import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.webharvest.WHConstants;
import org.webharvest.runtime.processors.Processor;

/**
 * @author Vladimir Nikic
 * Date: May 24, 2007
 */
public class ElementInfo {

    // properties containing suggested attribute values

    private String name;
    private Class<? extends IElementDef> definitionClass;
    private Class<? extends Processor> processorClass;
    private String validTags;

    private Set<String> tagsSet = new TreeSet<String>();
    private Set<String> requiredTagsSet = new TreeSet<String>();
    private Set<String> attsSet = new TreeSet<String>();
    private Set<String> requiredAttsSet = new TreeSet<String>();

    // denotes any attribute belonging to specif namespace
    private Set<String> nsAttsSet = new TreeSet<String>();

    private boolean allTagsAllowed;

    private boolean isInternal;

    private Class[] dependantProcessors;

    public ElementInfo(String name,
                       Class<? extends IElementDef> definitionClass,
                       Class<? extends Processor> processorClass,
                       String validTags, String validAtts,
                       final boolean isInternal,
                       final Class[] dependantProcessors) {
        this.name = name;
        this.definitionClass = definitionClass;
        this.processorClass = processorClass;
        this.validTags = validTags;

        this.allTagsAllowed = validTags == null;

        if (validTags != null) {
            StringTokenizer tokenizer = new StringTokenizer(validTags, ",");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken().toLowerCase();
                if (token.startsWith("!")) {
                    token = token.substring(1);
                    this.requiredTagsSet.add(token);
                }
                this.tagsSet.add(token);
            }
        }

        if (validAtts != null) {
            StringTokenizer tokenizer = new StringTokenizer(validAtts, ",");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken().toLowerCase();
                if ("p:*".equals(token)) {
                    nsAttsSet.add(WHConstants.XMLNS_PARAM);
                } else if ("v:*".equals(token)) {
                    nsAttsSet.add(WHConstants.XMLNS_VAR);
                } else if (token.startsWith("!")) {
                    token = token.substring(1);
                    this.requiredAttsSet.add(token);
                }
                this.attsSet.add(token);
            }
        }

        this.isInternal = isInternal;
        this.dependantProcessors = dependantProcessors;
    }

    /**
     * @param onlyRequiredAtts only required attributes
     * @return Template with allowed attributes.
     */
    public String getTemplate(boolean onlyRequiredAtts) {
        StringBuilder result = new StringBuilder().append("<{0}").append(this.name);

        for (String att : (onlyRequiredAtts ? this.requiredAttsSet : this.attsSet)) {
            result.append(' ').append(att).append("=\"\"");
        }

        // if no valid subtags
        if (this.validTags != null && "".equals(this.validTags.trim())) {
            result.append("/>");
        } else {
            result.append("></{0}").append(name).append('>');
        }

        return result.toString();
    }

    public Class<? extends IElementDef> getDefinitionClass() {
        return definitionClass;
    }

    public Class<? extends Processor> getProcessorClass() {
        return processorClass;
    }

    public String getName() {
        return name;
    }

    public Set<String> getTagsSet() {
        return tagsSet;
    }

    public Set<String> getAttsSet() {
        return attsSet;
    }

    public Set<String> getRequiredAttsSet() {
        return requiredAttsSet;
    }

    public Set<String> getNsAttsSet() {
        return nsAttsSet;
    }

    public Set<String> getRequiredTagsSet() {
        return requiredTagsSet;
    }

    public boolean areAllTagsAllowed() {
        return allTagsAllowed;
    }

    public boolean isInternal() {
        return isInternal;
    }

    public Class[] getDependantProcessors() {
        return dependantProcessors;
    }

}