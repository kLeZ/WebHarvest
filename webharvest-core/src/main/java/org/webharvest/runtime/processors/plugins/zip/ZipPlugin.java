package org.webharvest.runtime.processors.plugins.zip;

import static org.webharvest.WHConstants.XMLNS_CORE;
import static org.webharvest.WHConstants.XMLNS_CORE_10;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

import org.webharvest.annotation.Definition;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.processors.plugins.Autoscanned;
import org.webharvest.runtime.processors.plugins.TargetNamespace;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;

/**
 * ZIP processor
 */
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition("zip")
public class ZipPlugin extends WebHarvestPlugin {

    private ZipOutputStream zipOutStream = null;

    public String getName() {
        return "zip";
    }

    public Variable executePlugin(DynamicScopeContext context) throws InterruptedException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        zipOutStream = new ZipOutputStream(byteArrayOutputStream);
        executeBody(context);
        try {
            zipOutStream.close();
        } catch (IOException e) {
            throw new ZipPluginException(e);
        }
        return new NodeVariable(byteArrayOutputStream.toByteArray());
    }

    public String[] getValidAttributes() {
        return new String[] {};
    }

    public String[] getRequiredAttributes() {
        return new String[] {};
    }

    public Class[] getDependantProcessors() {
        return new Class[] {
            ZipEntryPlugin.class
        };
    }

    public ZipOutputStream getZipOutStream() {
        return zipOutStream;
    }

}