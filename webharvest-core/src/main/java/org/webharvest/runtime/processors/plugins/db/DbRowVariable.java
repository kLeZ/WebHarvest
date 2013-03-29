package org.webharvest.runtime.processors.plugins.db;

import org.webharvest.exception.PluginException;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Special variable used for database records
 */
// todo: review this class
public class DbRowVariable extends NodeVariable {

    private static final String ROW_TAG_NAME = "row";

    private DbColumnDescription columnDescription[] = null;
    private Object data[] = null;

    public DbRowVariable(DbColumnDescription columnDescription[], Object data[]) {
        super(data);
        this.columnDescription = columnDescription;
        this.data = data;
    }

    public int getColumnCount() {
        return columnDescription.length;
    }

    public String getColumnName(int index) {
        return columnDescription[index].getName();
    }

    public Variable get(Object var) {
        String s = var.toString();
        int index = CommonUtil.getIntValue(s, -1);
        if (index > 0) {
            return get(index - 1);
        } else {
            return get(s);
        }
    }

    public Variable get(int index) {
        return data[index] == null ? EmptyVariable.INSTANCE : new NodeVariable(data[index]);
    }

    public Variable get(String columnName) {
        int index = -1;
        for (int i = 0; i < columnDescription.length; i++) {
            if (columnName.equalsIgnoreCase(columnDescription[i].getName())) {
                index = i;
                break;
            }
        }

        if (index >= 0) {
            return get(index);
        } else {
            throw new PluginException("Invalid column name: " + columnName);
        }
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder("<" + ROW_TAG_NAME + ">");
        for (int i = 0; i < columnDescription.length; i++) {
            String colIdentifier = columnDescription[i].getIdentifier();
            buffer.append("<").append(colIdentifier).append(">");
            buffer.append(CommonUtil.escapeXml(CommonUtil.nvl(data[i], "")));
            buffer.append("</").append(colIdentifier).append(">");
        }
        buffer.append("</").append(ROW_TAG_NAME).append(">");
        return buffer.toString();
    }

    public String toString(String charset) {
        return toString();
    }

    public Object getWrappedObject() {
        return this.data;
    }

    public List<Variable> toList() {
        // todo: this method suppose to return a list of variables, no?
        return new ArrayList(Arrays.asList(data));
    }

}