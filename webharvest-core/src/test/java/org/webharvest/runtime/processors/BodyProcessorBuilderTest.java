package org.webharvest.runtime.processors;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.mock.Mock;
import org.webharvest.UnitilsTestNGExtension;
import org.webharvest.definition.ElementDefProxy;
import org.webharvest.definition.IElementDef;
import org.webharvest.definition.XmlNodeTestUtils;
import org.webharvest.runtime.processors.BodyProcessor.Builder;

public class BodyProcessorBuilderTest extends UnitilsTestNGExtension {

        private Mock<Processor> parentProcessor;

        @Test
        public void testBuilder() {
            final Processor parent = parentProcessor.getMock();
            final IElementDef def = new MockElementDef();

            final Builder builder = new Builder(def).
                setParentProcessor(parent);
            final BodyProcessor processor = builder.build();

            Assert.assertNotNull(processor, "Processor is null.");
            Assert.assertNotNull(processor.getElementDef(),
                "Elemenet definition is null.");
            Assert.assertSame(processor.getElementDef(), def,
                "Unexpected element definition.");
            Assert.assertNotNull(processor.getParentProcessor(),
                "Parent processor is null.");
            Assert.assertSame(processor.getParentProcessor(), parent,
                "Unexpected parent processor.");
        }

        private class MockElementDef extends ElementDefProxy {

            protected MockElementDef() {
                super(XmlNodeTestUtils.createXmlNode("<empty/>",
                        XmlNodeTestUtils.NAMESPACE_21));
            }

        }


}
