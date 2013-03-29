package org.webharvest.definition;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.webharvest.definition.FileConfigSource.FileLocation;
import org.webharvest.definition.URLConfigSource.URLLocation;
import org.webharvest.utils.CommonUtil;

// TODO Missing unit test
// TODO Missing documentation
public final class IncludeVisitor implements ConfigLocationVisitor {

    private final String path;

    private ConfigSource configSource;

    // TODO Missing unit test
    // TODO Missing documentation
    public IncludeVisitor(final String path) {
        // FIXME Moved directly form IncludeProcessor
        this.path = CommonUtil.adaptFilename(path);
    }

    // TODO Missing documentation
    // TODO Missing unit test
    public ConfigSource getConfigSource() {
        return configSource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // TODO Missing unit test
    public void visit(final URLLocation location) throws IOException {
        // FIXME Moved directly form IncludeProcessor
        final String newURL = CommonUtil.fullUrl(location.toString(), path);

        this.configSource = new URLConfigSource(new URL(newURL));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // TODO Missing unit test
    public void visit(final FileLocation location) throws IOException {
        // FIXME Moved directly form IncludeProcessor
         final String oldPath = CommonUtil.adaptFilename(location.toString());
         int index = oldPath.lastIndexOf('/');
         String newPath;
         if (index > 0) {
             final String workingPath = oldPath.substring(0, index);
             newPath = CommonUtil.getAbsoluteFilename(workingPath, path);
         } else {
             newPath = path;
         }

        this.configSource = new FileConfigSource(new File(newPath));
    }

}
