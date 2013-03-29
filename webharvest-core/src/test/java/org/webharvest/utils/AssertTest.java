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

import org.testng.annotations.Test;

import static org.webharvest.utils.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: awajda
 * Date: Sep 26, 2010
 * Time: 10:13:39 PM
 */
public class AssertTest {

    private static final Object DUMMY = new Object();

    @Test
    public void testIsNull_Ok() throws Exception {
        isNull(null);
        isNull(null, "msg");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testIsNull_Fail() throws Exception {
        isNull(DUMMY);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testIsNull_Fail_withMsg() throws Exception {
        isNull(DUMMY, "msg");
    }

    @Test
    public void testNotNull_Ok() throws Exception {
        notNull(DUMMY);
        notNull(DUMMY, "msg");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNotNull_Fail() throws Exception {
        notNull(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNotNull_Fail_withMsg() throws Exception {
        notNull(null, "msg");
    }

    @Test
    public void testIsTrue_Ok() throws Exception {
        isTrue(true, "msg");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testIsTrue_Fail() throws Exception {
        isTrue(false, "msg");
    }

    @Test
    public void testIsFalse_Ok() throws Exception {
        isFalse(false, "msg");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testIsFalse_Fail() throws Exception {
        isFalse(true, "msg");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    public void testShouldNeverHappen() throws Exception {
        shouldNeverHappen(null);
    }
}
