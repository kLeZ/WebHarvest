package org.webharvest;

import net.sf.saxon.om.NamespaceResolver;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.webharvest.XmlNamespaceUtils.getNamespaceResolverFromBrokenXml;

public class XmlNamespaceUtilsTest {

    @Test
    public void testGetNamespaceResolverFromBrokenXml_root() throws Exception {
        assertEquals(getNamespaceResolverFromBrokenXml("<root xmlns:a='foo'").getURIForPrefix("a", false), "foo");
        assertEquals(getNamespaceResolverFromBrokenXml("<root xmlns:a='bar' ").getURIForPrefix("a", false), "bar");
        assertEquals(getNamespaceResolverFromBrokenXml("<root xmlns:a='baz' b='").getURIForPrefix("a", false), "baz");
        assertEquals(getNamespaceResolverFromBrokenXml("<root xmlns:a='qux' b=\"").getURIForPrefix("a", false), "qux");
    }

    @Test
    public void testGetNamespaceResolverFromBrokenXml_in_scope() throws Exception {
        final NamespaceResolver nsResolver = getNamespaceResolverFromBrokenXml("" +
                "<root xmlns:a='aaa' xmlns:b='bbb'>" +
                "<foo xmlns:b='bbb2' xmlns:c='ccc'>" +
                "<bar");
        assertEquals(nsResolver.getURIForPrefix("a", false), "aaa");
        assertEquals(nsResolver.getURIForPrefix("b", false), "bbb2");
        assertEquals(nsResolver.getURIForPrefix("c", false), "ccc");
    }

    @Test
    public void testGetNamespaceResolverFromBrokenXml_out_scope() throws Exception {
        final NamespaceResolver nsResolver = getNamespaceResolverFromBrokenXml("" +
                "<root xmlns:a='aaa' xmlns:b='bbb'>" +
                "<foo xmlns:b='bbb2'></foo>" +
                "<bar");
        assertEquals(nsResolver.getURIForPrefix("a", false), "aaa");
        assertEquals(nsResolver.getURIForPrefix("b", false), "bbb");
        assertNull(nsResolver.getURIForPrefix("c", false));
    }

    @Test
    public void testGetNamespaceResolverFromBrokenXml_default_ns() throws Exception {
        assertNull(getNamespaceResolverFromBrokenXml("<root>").getURIForPrefix("", true));
        assertNull(getNamespaceResolverFromBrokenXml("<root xmlns='xxx'>").getURIForPrefix("", false));
        assertEquals(getNamespaceResolverFromBrokenXml("<root xmlns='xxx'>").getURIForPrefix("", true), "xxx");
    }

    @Test
    public void testGetNamespaceResolverFromBrokenXml_in_scope_with_default() throws Exception {
        final NamespaceResolver nsResolver = getNamespaceResolverFromBrokenXml("" +
                "<root xmlns='xxx'>" +
                "<foo xmlns='yyy'>" +
                "<bar");
        assertEquals(nsResolver.getURIForPrefix("", true), "yyy");
    }

    @Test
    public void testGetNamespaceResolverFromBrokenXml_out_scope_with_default() throws Exception {
        final NamespaceResolver nsResolver = getNamespaceResolverFromBrokenXml("" +
                "<root xmlns='xxx'>" +
                "<foo xmlns='yyy'></foo>" +
                "<bar");
        assertEquals(nsResolver.getURIForPrefix("", true), "xxx");
    }
}
