/*
 Copyright (c) 2006-2012 the original author or authors.

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
*/

package org.webharvest.runtime.scripting;

/**
 * Factory creating {@link ScriptEngine} instances capable of executing
 * provided {@link ScriptSource}. Along with {@link ScriptEngine} interface
 * provides abstraction over scripting engine implementations.
 *
 * @see ScriptEngine
 *
 * @author Piotr Dyraga
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
public interface ScriptEngineFactory {

    /**
     * Returns {@link ScriptEngine} capable of executing provided
     * {@link ScriptSource}.
     *
     * @param scriptSource
     *            not {@code null} ScriptSource for which {@link ScriptEngine}
     *            will be returned
     * @return {@link ScriptEngine} capable of executing provided
     *         {@link ScriptSource}.
     */
    ScriptEngine getEngine(ScriptSource scriptSource);
}
