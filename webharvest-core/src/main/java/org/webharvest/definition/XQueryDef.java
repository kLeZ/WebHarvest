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
 * Definition of XQuery processor.
 */
public class XQueryDef extends WebHarvestPluginDef {

    private DefinitionResolver definitionResolver = DefinitionResolver.INSTANCE;

    private IElementDef xqDef;

    private XQueryExternalParamDef[] externalParamDefs;

    public XQueryDef(XmlNode xmlNode, Class<? extends Processor> processorClass) {
        super(xmlNode, processorClass);

        XmlNode xqDefNode = xmlNode.getFirstSubnode(new ElementName("xq-expression", xmlNode.getUri()));
        xqDef = xqDefNode == null ? null : new ElementDefProxy(xqDefNode);

        List<XmlNode> listOfExternalParamNodes = xmlNode.getSubnodes(new ElementName("xq-param", xmlNode.getUri()));

        int size = listOfExternalParamNodes == null ? 0 : listOfExternalParamNodes.size();
        externalParamDefs = new XQueryExternalParamDef[size];

        if (listOfExternalParamNodes != null) {
            Iterator it = listOfExternalParamNodes.iterator();
            int index = 0;
            while (it.hasNext()) {
                XmlNode currParamNode =  (XmlNode) it.next();
                externalParamDefs[index++] = (XQueryExternalParamDef) definitionResolver.createElementDefinition(currParamNode);
            }
        }
    }

    public IElementDef getXqDef() {
        return xqDef;
    }

    public XQueryExternalParamDef[] getExternalParamDefs() {
        return externalParamDefs;
    }

    public IElementDef[] getOperationDefs() {
        final int size = externalParamDefs == null ? 1 : externalParamDefs.length + 1;
        IElementDef[] result = new IElementDef[size];
        if (externalParamDefs != null) {
            System.arraycopy(externalParamDefs, 0, result, 0, externalParamDefs.length);
        }
        result[result.length - 1] = this.xqDef;

        return result;
    }

    public String getShortElementName() {
        return "xquery";
    }

}