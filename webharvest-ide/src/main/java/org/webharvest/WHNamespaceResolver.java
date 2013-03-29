package org.webharvest;

import net.sf.saxon.om.NamespaceResolver;
import org.webharvest.utils.Stack;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: awajda
 * Date: 11/21/11
 * Time: 12:55 AM
 */

public class WHNamespaceResolver implements NamespaceResolver {
    private final Map<String, Stack<String>> nsMap;

    public WHNamespaceResolver(Map<String, Stack<String>> nsMap) {
        this.nsMap = nsMap;
    }

    @Override
    public String getURIForPrefix(String prefix, boolean useDefault) {
        if (useDefault || prefix.length() > 0) {
            final Stack<String> stack = nsMap.get(prefix);
            return stack == null ? null : stack.peek();
        } else {
            return null;
        }
    }

    @Override
    public Iterator iteratePrefixes() {
        return nsMap.keySet().iterator();
    }

    public boolean isEmpty() {
        return nsMap.isEmpty();
    }

    public void addPrefix(final String prefix, final String uri) {
        nsMap.put(prefix, new Stack<String>() {{
            push(uri);
        }});
    }
}
