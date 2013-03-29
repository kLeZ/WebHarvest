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

import static org.webharvest.WHConstants.XMLNS_CORE;
import static org.webharvest.WHConstants.XMLNS_CORE_10;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.math.NumberUtils;
import org.webharvest.WHConstants;
import org.webharvest.annotation.Definition;
import org.webharvest.definition.IElementDef;
import org.webharvest.definition.RegexpDef;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.processors.plugins.Autoscanned;
import org.webharvest.runtime.processors.plugins.TargetNamespace;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

/**
 * Regular expression replace processor.
 */
//TODO Add unit test
//TODO Add javadoc
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition(value = "regexp", validAttributes = { "id", "replace", "max",
        "flag-caseinsensitive", "flag-multiline", "flag-dotall",
        "flag-unicodecase", "flag-canoneq" }, validSubprocessors = {
        "regexp-pattern", "regexp-source", "regexp-result" },
        requiredSubprocessors = { "regexp-pattern", "regexp-source"},
        definitionClass = RegexpDef.class )
public class RegexpProcessor extends AbstractProcessor<RegexpDef> {

    public Variable execute(final DynamicScopeContext context) throws InterruptedException {

        IElementDef patternDef = elementDef.getRegexpPatternDef();
        Variable patternVar = getBodyTextContent(patternDef, context, true);
        debug(patternDef, context, patternVar);

        IElementDef sourceDef = elementDef.getRegexpSourceDef();
        Variable source = new BodyProcessor.Builder(sourceDef).
            setParentProcessor(this).build().run(context);
        debug(sourceDef, context, source);

        String replace = BaseTemplater.evaluateToString(elementDef.getReplace(), null, context);
        final boolean isReplace = CommonUtil.isBooleanTrue(replace);

        boolean flagCaseInsensitive = CommonUtil.getBooleanValue(BaseTemplater.evaluateToString(elementDef.getFlagCaseInsensitive(), null, context), false);
        boolean flagMultiline = CommonUtil.getBooleanValue(BaseTemplater.evaluateToString(elementDef.getFlagMultiline(), null, context), false);
        boolean flagDotall = CommonUtil.getBooleanValue(BaseTemplater.evaluateToString(elementDef.getFlagDotall(), null, context), true);
        boolean flagUnicodecase = CommonUtil.getBooleanValue(BaseTemplater.evaluateToString(elementDef.getFlagUnicodecase(), null, context), true);
        boolean flagCanoneq = CommonUtil.getBooleanValue(BaseTemplater.evaluateToString(elementDef.getFlagCanoneq(), null, context), false);

        this.setProperty("Is replacing", String.valueOf(isReplace));
        this.setProperty("Flag CaseInsensitive", String.valueOf(flagCaseInsensitive));
        this.setProperty("Flag MultiLine", String.valueOf(flagMultiline));
        this.setProperty("Flag DotAll", String.valueOf(flagDotall));
        this.setProperty("Flag UnicodeCase", String.valueOf(flagUnicodecase));
        this.setProperty("Flag CanonEq", String.valueOf(flagCanoneq));

        final double maxLoops = NumberUtils.toDouble(BaseTemplater.evaluateToString(elementDef.getMax(), null, context), WHConstants.DEFAULT_MAX_LOOPS);

        this.setProperty("Max loops", String.valueOf(maxLoops));

        int flags = 0;
        if (flagCaseInsensitive) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        if (flagMultiline) {
            flags |= Pattern.MULTILINE;
        }
        if (flagDotall) {
            flags |= Pattern.DOTALL;
        }
        if (flagUnicodecase) {
            flags |= Pattern.UNICODE_CASE;
        }
        if (flagCanoneq) {
            flags |= Pattern.CANON_EQ;
        }

        final Pattern pattern = Pattern.compile(patternVar.toString(), flags);

        final List<NodeVariable> resultList = new ArrayList<NodeVariable>();

        List bodyList = source.toList();
        for (final Object currVar : bodyList) {

            String text = currVar.toString();

            Matcher matcher = pattern.matcher(text);
            int groupCount = matcher.groupCount();

            StringBuffer buffer = new StringBuffer();

            int index = 0;
            while (matcher.find()) {
                index++;

                // if index exceeds maximum number of matching sequences exists the loop
                if (maxLoops < index) {
                    break;
                }

                for (int i = 0; i <= groupCount; i++) {
                    context.setLocalVar("group" + i, new NodeVariable(matcher.group(i)));
                }

                IElementDef resultDef = elementDef.getRegexpResultDef();
                Variable result = getBodyTextContent(resultDef, context, true);
                debug(resultDef, context, result);

                String currResult = (result == null) ? matcher.group(0) : result.toString();
                if (isReplace) {
                    matcher.appendReplacement(buffer, currResult);
                } else {
                    resultList.add(new NodeVariable(currResult));
                }
            }

            if (isReplace) {
                matcher.appendTail(buffer);
                resultList.add(new NodeVariable(buffer.toString()));
            }

        }


        return new ListVariable(resultList);
    }

}