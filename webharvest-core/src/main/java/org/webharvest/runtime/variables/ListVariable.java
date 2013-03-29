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
package org.webharvest.runtime.variables;

import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * List variable - String wrapper.
 */
public class ListVariable extends Variable implements Iterable {

    private List<Variable> list;

    private String cachedStringRepresentation = null;

    public ListVariable() {
        this.list = new ArrayList<Variable>();
    }

    public ListVariable(Iterable list) {
        this.list = new ArrayList<Variable>();

        if (list != null) {
            for (Object object : list) {
                final Variable var = (object instanceof Variable) ? (Variable) object : new NodeVariable(object);
                if (!var.isEmpty()) {
                    this.list.add(var);
                }
            }
        }
    }

    public String toString() {
        if (cachedStringRepresentation == null) {
            StringBuilder buffer = new StringBuilder();
            for (Variable var : list) {
                final String value = var.toString();
                if (value.length() != 0) {
                    if (buffer.length() != 0) {
                        buffer.append('\n');
                    }
                    buffer.append(value);
                }
            }
            cachedStringRepresentation = buffer.toString();
        }
        return cachedStringRepresentation;
    }

    public String toString(String charset, String delimiter) {
        final StringBuilder buffer = new StringBuilder();
        for (Variable var : list) {
            final String value = var.toString(charset);
            if (value.length() != 0) {
                if (buffer.length() != 0) {
                    buffer.append(delimiter);
                }
                buffer.append(value);
            }
        }
        return buffer.toString();
    }

    public String toString(String charset) {
        return toString(charset, "\n");
    }

    public byte[] toBinary(String charset) {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (Variable currVar : list) {
                byte[] curr = (charset == null ? currVar.toBinary() : currVar.toBinary(charset));
                if (curr == null) {
                    continue;
                }
                baos.write(curr);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new AssertionError("This should never happen");
        }

    }

    public byte[] toBinary() {
        return toBinary(null);
    }

    public List<Variable> toList() {
        return list;
    }

    public boolean isEmpty() {
        for (Variable var : list) {
            if (!var.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void addVariable(Variable variable) {
        // in order string value needs to be recached
        cachedStringRepresentation = null;

        if (variable instanceof ListVariable) {
            list.addAll(((ListVariable) variable).getList());
        } else {
            list.add(variable == null ? EmptyVariable.INSTANCE : variable);
        }
    }

    public Collection<Variable> getList() {
        return this.list;
    }

    /**
     * Checks if list contains specified object's string representation
     *
     * @param item
     */
    public boolean contains(Object item) {
        final String itemAsString = item.toString();
        for (Variable currVar : list) {
            if (currVar != null && currVar.toString().equals(itemAsString)) {
                return true;
            }
        }
        return false;
    }

    public Object getWrappedObject() {
        return this.list;
    }

    @Override public Iterator toIterator() {
        return list.iterator();
    }

    public Variable get(int index) {
        return list.get(index);
    }

    @Override
    public Iterator iterator() {
        return list.iterator();
    }
}