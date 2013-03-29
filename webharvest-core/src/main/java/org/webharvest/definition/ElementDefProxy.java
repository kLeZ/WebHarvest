package org.webharvest.definition;

import java.util.List;

import org.webharvest.runtime.processors.Processor;

public class ElementDefProxy implements IElementDef {

    private final XmlNode node;

    private IElementDef delegate;

    public ElementDefProxy(final XmlNode node) {
        this.node = node;
    }

    public XmlNode getNode() {
        return node;
    }

    @Override
    @Deprecated
    public IElementDef[] getOperationDefs() {
         return getDelegate().getOperationDefs();
    }

    @Override
    public String getShortElementName() {
        // FIXME We can get the same information from xmlnode
        return getDelegate().getShortElementName();
    }

    @Override
    public Processor createPlugin() {
        return getDelegate().createPlugin();
    }

    @Override
    public boolean hasOperations() {
        return getDelegate().hasOperations();
    }

    private IElementDef getDelegate() {
        if (delegate == null) {
            this.delegate = DefinitionResolver.INSTANCE.createElementDefinition(node);
        }

        return delegate;
    }

    @Override
    public String getId() {
         return node.getAttribute("id");
    }

    @Override
    public int getLineNumber() {
        return node.getLineNumber();
    }

    @Override
    public int getColumnNumber() {
        return node.getColumnNumber();
    }

    @Override
    public List<IElementDef> getElementDefs() {
        return getDelegate().getElementDefs();
    }

    public String getNamespaceURI() {
        return getNode().getUri();
    }

}