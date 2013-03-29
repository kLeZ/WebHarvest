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
package org.webharvest.runtime.processors;

import static org.webharvest.WHConstants.XMLNS_CORE;
import static org.webharvest.WHConstants.XMLNS_CORE_10;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Predicate;
import org.webharvest.annotation.Definition;
import org.webharvest.definition.FileDef;
import org.webharvest.exception.FileException;
import org.webharvest.ioc.WorkingDir;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.processors.plugins.Autoscanned;
import org.webharvest.runtime.processors.plugins.TargetNamespace;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Types;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;
import org.webharvest.utils.FileListIterator;

import com.google.inject.Inject;

/**
 * File processor.
 */
// TODO Add unit test
// TODO Add javadoc
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition(value = "file", validAttributes = { "id", "path", "action", "type",
        "charset", "listfilter", "listfiles", "listdirs", "listrecursive" },
        requiredAttributes="path", definitionClass = FileDef.class)
public class FileProcessor extends AbstractProcessor<FileDef> {

    @Inject
    @WorkingDir
    private String workingDir;

    public Variable execute(DynamicScopeContext context)
            throws InterruptedException {
        String action = BaseTemplater.evaluateToString(elementDef.getAction(),
                null, context);
        String filePath = BaseTemplater.evaluateToString(elementDef.getPath(),
                null, context);
        String type = BaseTemplater.evaluateToString(elementDef.getType(),
                null, context);
        String charset = BaseTemplater.evaluateToString(
                elementDef.getCharset(), null, context);
        if (charset == null) {
            charset = context.getCharset();
        }
        String listFilter = BaseTemplater.evaluateToString(
                elementDef.getListFilter(), null, context);
        String listFiles = BaseTemplater.evaluateToString(
                elementDef.getListFiles(), null, context);
        boolean isListFiles = CommonUtil.getBooleanValue(listFiles, true);
        String listDirs = BaseTemplater.evaluateToString(
                elementDef.getListDirs(), null, context);
        boolean isListDirs = CommonUtil.getBooleanValue(listDirs, true);
        String listRecursive = BaseTemplater.evaluateToString(
                elementDef.getListRecursive(), null, context);
        boolean isListRecursive = CommonUtil.getBooleanValue(listRecursive,
                false);

        this.setProperty("Action", action);
        this.setProperty("File Path", filePath);
        this.setProperty("Type", type);
        this.setProperty("Charset", charset);
        this.setProperty("List Files", isListFiles);
        this.setProperty("List Directories", isListDirs);
        this.setProperty("List Recursive", isListRecursive);

        String fullPath = CommonUtil.getAbsoluteFilename(workingDir, filePath);

        // depending on file acton calls appropriate method
        if ("write".equalsIgnoreCase(action)) {
            return executeFileWrite(false, context, fullPath, type,
                    charset);
        } else if ("append".equalsIgnoreCase(action)) {
            return executeFileWrite(true, context, fullPath, type,
                    charset);
        } else if ("list".equalsIgnoreCase(action)) {
            return executeFileList(filePath, listFilter, isListFiles,
                    isListDirs, isListRecursive);
        } else {
            return executeFileRead(fullPath, type, charset);
        }
    }

    private Variable executeFileList(String filePath, final String listFilter,
            final boolean listFiles, final boolean listDirs,
            final boolean listRecursive) {
        final File dir = new File(filePath);
        if (!dir.exists()) {
            throw new FileException("Directory \"" + dir + "\" doesn't exist!");
        } else if (!dir.isDirectory()) {
            throw new FileException("\"" + dir + "\" is not directory!");
        }

        return new NodeVariable(IteratorUtils.filteredIterator(
                new FileListIterator(dir, listRecursive), new Predicate() {
                    private final CommandPromptFilenameFilter filenameFilter = new CommandPromptFilenameFilter(
                            listFilter);

                    @Override
                    public boolean evaluate(Object object) {
                        final File file = (File) object;
                        return (file.isDirectory() && listDirs || file.isFile()
                                && listFiles)
                                && filenameFilter.accept(null, file.getName());
                    }
                }));
    }

    private Collection<File> listFiles(File directory, FilenameFilter filter,
            boolean recurse) {
        List<File> files = new ArrayList<File>();
        File[] entries = directory.listFiles();
        if (entries != null) {
            for (File entry : entries) {
                if (filter == null || filter.accept(directory, entry.getName())) {
                    files.add(entry);
                }
                if (recurse && entry.isDirectory()) {
                    files.addAll(listFiles(entry, filter, recurse));
                }
            }
        }
        return files;
    }

    /**
     * Writing content to the specified file. If parameter "append" is true,
     * then append content, otherwise write
     */
    private Variable executeFileWrite(boolean append,
            DynamicScopeContext context, String fullPath, String type,
            String charset) throws InterruptedException {
        Variable result;

        try {
            // ensure that target directory exists
            new File(CommonUtil.getDirectoryFromPath(fullPath)).mkdirs();

            FileOutputStream out = new FileOutputStream(fullPath, append);
            byte[] data;

            if (Types.TYPE_BINARY.equalsIgnoreCase(type)) {
                Variable bodyListVar = new BodyProcessor.Builder(elementDef).
                    setParentProcessor(this).build().execute(context);
                result = appendBinary(bodyListVar);
                data = result.toBinary();
            } else {
                Variable body = getBodyTextContent(elementDef, context);
                String content = body.toString();
                data = content.getBytes(charset);
                result = new NodeVariable(content);
            }

            out.write(data);
            out.flush();
            out.close();

            return result;
        } catch (IOException e) {
            throw new FileException("Error writing data to file: " + fullPath,
                    e);
        }
    }

    /**
     * Reading the specified file.
     */
    private Variable executeFileRead(String fullPath, String type,
            String charset) {
        if (Types.TYPE_BINARY.equalsIgnoreCase(type)) {
            try {
                byte[] data = CommonUtil.readBytesFromFile(new File(fullPath));
                LOG.info("Binary file read processor: {} bytes read.",
                        data.length);
                return new NodeVariable(data);
            } catch (IOException e) {
                throw new FileException("Error reading file: " + fullPath, e);
            }
        } else {
            try {
                String content = CommonUtil.readStringFromFile(new File(
                        fullPath), charset);
                LOG.info("Text file read processor: {} characters read.",
                        charCount(content));
                return new NodeVariable(content);
            } catch (IOException e) {
                throw new FileException("Error reading the file: " + fullPath,
                        e);
            }
        }
    }

    private int charCount(final String content) {
        return (content == null ? 0 : content.length());
    }

    public NodeVariable appendBinary(Variable body) {
        if (body == null) {
            return new NodeVariable("");
        }

        byte[] result = null;

        for (Object var : body.toList()) {
            final Variable currVariable = (Variable) var;
            byte bytes[] = currVariable.toBinary();
            if (bytes != null) {
                if (result == null) {
                    result = bytes;
                } else {
                    byte[] newResult = new byte[result.length + bytes.length];
                    System.arraycopy(result, 0, newResult, 0, result.length);
                    System.arraycopy(bytes, 0, newResult, result.length,
                            bytes.length);
                    result = newResult;
                }
            }
        }

        return new NodeVariable(result);
    }

    private class CommandPromptFilenameFilter implements FilenameFilter {

        Pattern pattern = Pattern.compile(".*");

        private CommandPromptFilenameFilter(String filter) {
            if (!CommonUtil.isEmpty(filter)) {
                StringBuilder buffer = new StringBuilder();
                for (int i = 0; i < filter.length(); i++) {
                    char ch = filter.charAt(i);
                    switch (ch) {
                    case '.':
                        buffer.append("\\.");
                        break;
                    case '*':
                        buffer.append(".*");
                        break;
                    case '?':
                        buffer.append(".");
                        break;
                    default:
                        buffer.append(ch);
                        break;
                    }
                }
                try {
                    pattern = Pattern.compile(buffer.toString());
                } catch (PatternSyntaxException e) {
                    pattern = Pattern.compile("");
                }
            }
        }

        public boolean accept(File dir, String name) {
            return pattern.matcher(name).matches();
        }
    }

}