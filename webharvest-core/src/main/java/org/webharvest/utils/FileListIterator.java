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

import java.io.File;
import java.util.Iterator;

public class FileListIterator implements Iterator<File> {

    private final boolean recursive;
    private final Stack<FileTraverser> traverserStack = new Stack<FileTraverser>();

    private FileTraverser traverser;
    private File curr;
    private File next;

    public FileListIterator(File dir, boolean recursive) {
        this.recursive = recursive;
        this.traverser = new FileTraverser(dir);
    }

    private void step() {
        if (recursive && curr != null && curr.isDirectory()) {
            traverserStack.push(traverser);
            traverser = new FileTraverser(curr);
        }
        while ((next = traverser.next()) == null && !traverserStack.isEmpty()) {
            traverser = traverserStack.pop();
        }
    }

    @Override public boolean hasNext() {
        if (next == curr) step();
        return next != null;
    }

    @Override public File next() {
        return hasNext() ? (curr = next) : null;
    }

    @Override public void remove() {
        throw new UnsupportedOperationException();
    }

    private static class FileTraverser {

        private File[] arr;
        private int i;

        FileTraverser(File dir) {
            arr = dir.listFiles();
            i = 0;
        }

        File next() {
            return i < arr.length ? arr[i++] : null;
        }
    }
}
