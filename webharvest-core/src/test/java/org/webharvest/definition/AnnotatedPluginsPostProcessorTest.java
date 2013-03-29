package org.webharvest.definition;

import static org.unitils.mock.ArgumentMatchers.notNull;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.mock.Mock;
import org.webharvest.annotation.Definition;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.processors.plugins.Autoscanned;
import org.webharvest.runtime.processors.plugins.TargetNamespace;
import org.webharvest.runtime.variables.Variable;

public class AnnotatedPluginsPostProcessorTest extends UnitilsTestNG {

    private static final String TARGET_NAMESPACE =
        "http://web-harvest.sourceforge.net/schema/dummy";

    private Mock<ConfigurableResolver> mockConfigurbleResolver;

    private AnnotatedPluginsPostProcessor postProcessor;

    @BeforeMethod
    public void setUp() {
        this.postProcessor = new AnnotatedPluginsPostProcessor(
                this.getClass().getPackage().getName());
    }

    @AfterMethod
    public void tearDown() {
        this.postProcessor = null;
    }

    @Test
    public void testPostProcess() {
        postProcessor.postProcess(mockConfigurbleResolver.getMock());
        mockConfigurbleResolver.assertInvoked().registerPlugin(notNull(ElementInfo.class), TARGET_NAMESPACE);
        mockConfigurbleResolver.assertNotInvoked().registerPlugin((ElementInfo) null, null);
    }

    @Autoscanned
    @TargetNamespace(TARGET_NAMESPACE)
    @Definition("foo")
    static class ValidPlugin extends MockAbstractPlugin {
    }

    @Autoscanned
    static class MissingNamespacePlugin extends MockAbstractPlugin {
    }

    @Autoscanned
    @TargetNamespace(TARGET_NAMESPACE)
    class NotWebHarvestPlugin {
    }

    @Autoscanned
    @TargetNamespace(TARGET_NAMESPACE)
    static class MissingDefinitionForPlugin extends MockAbstractPlugin {

    }

    abstract static class MockAbstractPlugin extends WebHarvestPlugin {

        @Override
        public Variable executePlugin(final DynamicScopeContext context)
                throws InterruptedException {
            throw new UnsupportedOperationException("not supported by mock");
        }
    }
}
