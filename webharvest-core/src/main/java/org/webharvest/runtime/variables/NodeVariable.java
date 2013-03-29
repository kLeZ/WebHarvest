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

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.iterators.EmptyIterator;
import org.webharvest.exception.VariableException;
import org.webharvest.utils.CommonUtil;
import org.webharvest.utils.XmlNodeWrapper;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Node variable - Single node wrapper.
 */
public class NodeVariable extends Variable {

    private Object data;

    public NodeVariable(Object data) {
        this.data = data;
    }

    public String toString() {
        if (data == null) {
            return "";
        } else if (data instanceof byte[]) {
            return new String((byte[]) data);
        } else {
            return data.toString();
        }
    }

    public String toString(String charset) {
        if (data == null) {
            return "";
        } else if (data instanceof byte[]) {
            try {
                return new String((byte[]) data, charset);
            } catch (UnsupportedEncodingException e) {
                throw new VariableException(e);
            }
        } else {
            return data.toString();
        }
    }

    public byte[] toBinary() {
        if (data == null) {
            return new byte[]{};
        } else if (data instanceof byte[]) {
            return (byte[]) data;
        } else {
            return data.toString().getBytes();
        }
    }

    public byte[] toBinary(String charset) {
        if (charset == null || data == null || data instanceof byte[]) {
            return toBinary();
        } else {
            try {
                return data.toString().getBytes(charset);
            } catch (UnsupportedEncodingException e) {
                throw new VariableException(e);
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    public List<Variable> toList() {
        return (isEmpty()) ? Collections.<Variable>emptyList() : IteratorUtils.toList(toIterator());
    }

    public boolean isEmpty() {
        return data == null
                || data instanceof Iterator && !((Iterator) data).hasNext()
                || data instanceof Iterable && !((Iterable) data).iterator().hasNext()
                || data instanceof XmlNodeWrapper && ((XmlNodeWrapper) data).isEmpty()
                || data instanceof CharSequence && ((CharSequence) data).length() == 0;
    }

    public Object getWrappedObject() {
        return this.data;
    }

    @SuppressWarnings({"unchecked"})
    @Override public Iterator<Variable> toIterator() {
        return (isEmpty()) ? EmptyIterator.INSTANCE
                : IteratorUtils.transformedIterator(
                (data instanceof Iterator) ? (Iterator) data
                        : (data instanceof Iterable) ? ((Iterable) data).iterator()
                        : (data instanceof Object[]) ? IteratorUtils.arrayIterator(data)
                        : Arrays.asList((Variable) this).iterator(),
                new Transformer() {
                    @Override public Object transform(Object o) {
                        return CommonUtil.createVariable(o);
                    }
                });
    }

}