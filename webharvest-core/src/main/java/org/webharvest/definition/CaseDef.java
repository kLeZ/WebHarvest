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

import java.util.Iterator;
import java.util.List;

import org.webharvest.runtime.processors.Processor;

/**
 * Definition of case processor.
 */
public class CaseDef extends WebHarvestPluginDef {

    private DefinitionResolver definitionResolver = DefinitionResolver.INSTANCE;

    private IfDef[] ifDefs;
    private IElementDef elseDef;

    public CaseDef(XmlNode xmlNode, Class<? extends Processor> processorClass) {
        super(xmlNode, processorClass);

        List<XmlNode> ifNodesList = xmlNode.getSubnodes(new ElementName("if", xmlNode.getUri()));
        int size = ifNodesList == null ? 0 : ifNodesList.size();
        ifDefs = new IfDef[size];

        if (ifNodesList != null) {
            Iterator it = ifNodesList.iterator();
            int index = 0;
            while (it.hasNext()) {
                XmlNode currParamNode = (XmlNode) it.next();
                ifDefs[index++] = (IfDef) definitionResolver.createElementDefinition(currParamNode);
            }
        }

        XmlNode elseDefNode = xmlNode.getFirstSubnode(new ElementName("else", xmlNode.getUri()));
        elseDef = elseDefNode == null ? null : new ElementDefProxy(elseDefNode);
    }

    public IfDef[] getIfDefs() {
        return ifDefs;
    }

    public IElementDef getElseDef() {
        return elseDef;
    }

    public String getShortElementName() {
        return "case";
    }

    public IElementDef[] getOperationDefs() {
        int size = (ifDefs == null ? 0 : ifDefs.length) + (elseDef == null ? 0 : 1);
        IElementDef[] result = new IElementDef[size];
        if (ifDefs != null) {
            System.arraycopy(ifDefs, 0, result, 0, ifDefs.length);
        }

        if (elseDef != null) {
            result[size - 1] = elseDef;
        }

        return result;
    }

}