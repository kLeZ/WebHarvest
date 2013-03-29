package org.webharvest.definition;

import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.mock.Mock;

public class AbstractRefreshableResolverTest extends UnitilsTestNG {

    private Mock<ResolverPostProcessor> postProcessor;

    private AbstractRefreshableResolver resolver =
        new MockAbstractConfigurableResolver();

    @Test
    public void postProcessorExecutedOnRefresh() {
        resolver.addPostProcessor(postProcessor.getMock());

        resolver.refresh();

        postProcessor.assertInvoked().postProcess(resolver);
        postProcessor.assertNotInvoked().postProcess(null);
    }

    @Test
    public void createsNewElementsRegistryOnRefresh() {
        final ElementsRegistry previous = resolver.getElementsRegistry();
        resolver.refresh();
        final ElementsRegistry current = resolver.getElementsRegistry();

        assertFalse("New instance of registry expected", previous == current);
    }

    private class MockAbstractConfigurableResolver
        extends AbstractRefreshableResolver {

        @Override
        public void registerPlugin(final ElementInfo elementInfo,
                final String namespace) {
            throw new UnsupportedOperationException("TEST MOCK");
        }
    }
}
