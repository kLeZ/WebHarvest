package org.webharvest.gui.settings.validation;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertEquals;
import static org.easymock.EasyMock.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.EasyMockUnitils;
import org.unitils.easymock.annotation.RegularMock;
import org.unitils.inject.annotation.InjectInto;
import org.unitils.inject.annotation.TestedObject;
import org.webharvest.definition.validation.SchemaResolver;
import org.webharvest.gui.Settings;

public class XmlSchemasPresenterTest extends UnitilsTestNG {

    private static final String SAMPLE_LOCATION = "/aaa.xsd";
    private static final String SAMPLE_OTHER_LOCATION = "/bbb.xsd";
    private static final String SAMPLE_ANOTHER_LOCATION = "/ccc.xsd";

    @RegularMock
    @InjectInto(property = "schemaResolver")
    private SchemaResolver mockSchemaResolver;

    @RegularMock
    private XmlSchemasView mockView;

    @TestedObject
    private XmlSchemasPresenter presenter;

    @BeforeMethod
    public void setUp() throws Exception {
        presenter = new XmlSchemasPresenter(mockView);

        final Set<XmlSchemaDTO> dtoSet = new HashSet<XmlSchemaDTO>(
                Arrays.asList(new XmlSchemaDTO(SAMPLE_LOCATION)));
        final Field field = presenter.getClass().getDeclaredField("schemaDTOs");
        field.setAccessible(true);
        field.set(presenter, dtoSet);
    }

    @AfterMethod
    public void tearDown() {
        presenter = null;
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testRegisterSchemaIfNullSchema() {
        presenter.registerSchema(null);
    }

    @Test
    public void testRegisterExistingSchema() {
        EasyMockUnitils.replay();

        presenter.registerSchema(new XmlSchemaDTO(SAMPLE_LOCATION));
    }

    @Test
    public void testRegisterSchema() {
        final XmlSchemaDTO dto = new XmlSchemaDTO(SAMPLE_OTHER_LOCATION);

        mockView.addToList(same(dto));
        expectLastCall();

        EasyMockUnitils.replay();

        presenter.registerSchema(dto);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testUnregisterSchemaIfNullSchema() {
        presenter.unregisterSchema(null);
    }

    @Test
    public void testUnregisterUnexistingSchema() {
        EasyMockUnitils.replay();

        presenter.unregisterSchema(new XmlSchemaDTO(SAMPLE_OTHER_LOCATION));
    }

    @Test
    public void testUnregisterSchema() {
        final XmlSchemaDTO dto = new XmlSchemaDTO(SAMPLE_LOCATION);

        mockView.removeFromList(same(dto));
        expectLastCall();

        EasyMockUnitils.replay();

        presenter.unregisterSchema(dto);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testOnLoadIfNullSettings() {
        presenter.onLoad(null);
    }

    @Test
    public void testOnLoad() {
        final XmlSchemaDTO dto1 = new XmlSchemaDTO(SAMPLE_OTHER_LOCATION);
        final XmlSchemaDTO dto2 = new XmlSchemaDTO(SAMPLE_ANOTHER_LOCATION);
        final Settings mockSettings = new MockSettings(dto1, dto2);

        mockView.addToList(same(dto1));
        expectLastCall();
        mockView.addToList(same(dto2));
        expectLastCall();

        EasyMockUnitils.replay();

        presenter.onLoad(mockSettings);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testOnUpdateIfNullSettings() {
        presenter.onUpdate(null);
    }

    @Test
    public void testOnUpdate() {
        final Settings mockSettings = new MockSettings();

        mockSchemaResolver.refresh();
        expectLastCall();

        EasyMockUnitils.replay();

        presenter.onUpdate(mockSettings);

        final XmlSchemaDTO[] schemas = mockSettings.getXmlSchemas();
        assertNotNull("Returned schema array is null.", schemas);
        assertEquals("Incorrect size of schema array", 1, schemas.length);
        assertEquals("Unexpected location of the schema.", SAMPLE_LOCATION,
                schemas[0].getLocation());
    }

    private class MockSettings extends Settings {

        private XmlSchemaDTO[] schemas;

        public MockSettings(XmlSchemaDTO...schemas) {
            this.schemas = schemas;
        }

        @Override
        public XmlSchemaDTO[] getXmlSchemas() {
            return schemas;
        }

        @Override
        public void setXmlSchemas(final XmlSchemaDTO[] xmlSchemas) {
            this.schemas = xmlSchemas;
        }

    }

}
