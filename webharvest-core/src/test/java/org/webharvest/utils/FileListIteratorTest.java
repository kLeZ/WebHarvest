/*
 Copyright (c) 2006-2007, Vladimir Nikic
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

package org.webharvest.utils;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.IteratorUtils;
import org.testng.annotations.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

public class FileListIteratorTest {

    @Test
    public void testHasNext_emptyDir_nonRecursive() throws Exception {
        final FileListIterator it = new FileListIterator(new MockFile("A", true), false);
        assertFalse(it.hasNext());
        assertFalse(it.hasNext());
    }

    @Test
    public void testHasNext_emptyDir_recursive() throws Exception {
        final FileListIterator it = new FileListIterator(new MockFile("A", true), true);
        assertFalse(it.hasNext());
        assertFalse(it.hasNext());
    }

    @Test
    public void testHasNext_nonRecursive() throws Exception {
        final FileListIterator it = createTestIterator(false);
        for (int i = 0; i < 4; i++, it.next()) {
            assertTrue(it.hasNext());
            assertTrue(it.hasNext());
        }
        assertFalse(it.hasNext());
        assertFalse(it.hasNext());
    }

    @Test
    public void testHasNext_recursive() throws Exception {
        final FileListIterator it = createTestIterator(true);
        for (int i = 0; i < 14; i++, it.next()) {
            assertTrue(it.hasNext());
            assertTrue(it.hasNext());
        }
        assertFalse(it.hasNext());
        assertFalse(it.hasNext());
    }

    @Test
    public void testNext_empty() throws Exception {
        assertNull(new FileListIterator(new MockFile("R", true), false).next());
        assertNull(new FileListIterator(new MockFile("R", true), true).next());
    }

    @Test
    public void testNext_nonRecursive() throws Exception {
        ReflectionAssert.assertReflectionEquals(
                Arrays.asList("A", "B", "x", "y"),
                IteratorUtils.toList(IteratorUtils.transformedIterator(createTestIterator(false), new BeanToPropertyValueTransformer("name"))));
    }

    @Test
    public void testNext_recursive() throws Exception {
        ReflectionAssert.assertReflectionEquals(
                Arrays.asList("A", "k", "l", "B", "C", "D", "f", "E", "E", "G", "m", "E", "x", "y"),
                IteratorUtils.toList(IteratorUtils.transformedIterator(createTestIterator(true), new BeanToPropertyValueTransformer("name"))));
    }

    private FileListIterator createTestIterator(boolean recursive) {
        return new FileListIterator(
                new MockFile("R",
                        new MockFile("A",
                                new MockFile("k"),
                                new MockFile("l")),
                        new MockFile("B",
                                new MockFile("C",
                                        new MockFile("D",
                                                new MockFile("f"),
                                                new MockFile("E", true)),
                                        new MockFile("E", true)),
                                new MockFile("G",
                                        new MockFile("m"),
                                        new MockFile("E", true))),
                        new MockFile("x"),
                        new MockFile("y")),
                recursive);
    }

    private static class MockFile extends File {

        private final boolean isDir;
        private final List<MockFile> children = new ArrayList<MockFile>();

        private MockFile(String name) {
            this(name, false);
        }

        public MockFile(String name, boolean isDir) {
            super(name);
            this.isDir = isDir;
        }

        private MockFile(String name, MockFile... children) {
            super(name);
            this.isDir = true;
            this.children.addAll(Arrays.asList(children));
        }

        @Override public boolean isDirectory() {
            return isDir;
        }

        @Override public boolean isFile() {
            return !isDir;
        }

        @Override public File[] listFiles() {
            if (!isDir) throw new IllegalStateException();
            return children.toArray(new File[children.size()]);
        }
    }
}
