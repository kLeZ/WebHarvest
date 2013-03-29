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

package org.webharvest.runtime.scripting;

import org.testng.annotations.Test;

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

/**
 * Created by IntelliJ IDEA.
 * User: awajda
 * Date: Sep 26, 2010
 * Time: 6:02:52 PM
 */
public class ScriptingLanguageTest {

    @Test
    public void testRecognize_ok() {
        assertSame(ScriptingLanguage.recognize(" beanshell "), ScriptingLanguage.BEANSHELL);
        assertSame(ScriptingLanguage.recognize(" BEANSHELL "), ScriptingLanguage.BEANSHELL);
        assertSame(ScriptingLanguage.recognize(" BeAnShElL "), ScriptingLanguage.BEANSHELL);

        assertSame(ScriptingLanguage.recognize(" javascript "), ScriptingLanguage.JAVASCRIPT);
        assertSame(ScriptingLanguage.recognize(" JAVASCRIPT "), ScriptingLanguage.JAVASCRIPT);
        assertSame(ScriptingLanguage.recognize(" jAvAsCrIpT "), ScriptingLanguage.JAVASCRIPT);

        assertSame(ScriptingLanguage.recognize(" groovy "), ScriptingLanguage.GROOVY);
        assertSame(ScriptingLanguage.recognize(" GROOVY "), ScriptingLanguage.GROOVY);
        assertSame(ScriptingLanguage.recognize(" GrOOvY "), ScriptingLanguage.GROOVY);
    }

    @Test
    public void testRecognize_fail() {
        assertNull(ScriptingLanguage.recognize(null));
        assertNull(ScriptingLanguage.recognize(""));
        assertNull(ScriptingLanguage.recognize(" "));
        assertNull(ScriptingLanguage.recognize("obviously wrong language"));
    }
}
