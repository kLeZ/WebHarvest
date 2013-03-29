package org.webharvest.definition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractElementDef implements IElementDef {

    // TODO Make it private
    protected final XmlNode xmlNode;
    // sequence of operation definitions
    private final List<IElementDef> operationDefs = new ArrayList<IElementDef>();

    // TODO Do we really need createBodyDefs parameter? If not remove this constructor
    protected AbstractElementDef(XmlNode node) {
        if (node == null) {
            throw new IllegalArgumentException("XmlNode must not be null.");
        }
        this.xmlNode = node;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasOperations() {
        return operationDefs != null && operationDefs.size() > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public IElementDef[] getOperationDefs() {
        IElementDef[] defs = new IElementDef[operationDefs.size()];
        Iterator<IElementDef> it = operationDefs.iterator();
        int index = 0;
        while (it.hasNext()) {
            defs[index++] = it.next();
        }

        return defs;
    }

    @Override
    public String getId() {
        return xmlNode.getAttribute("id");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getShortElementName() {
        return xmlNode.getQName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLineNumber() {
        return xmlNode.getLineNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnNumber() {
        return xmlNode.getColumnNumber();
    }

    public void add(IElementDef element) {
        operationDefs.add(element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IElementDef> getElementDefs() {
        return Arrays.asList(getOperationDefs());
    }

}
