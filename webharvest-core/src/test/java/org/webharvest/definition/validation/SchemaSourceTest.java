package org.webharvest.definition.validation;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import java.io.InputStream;

import javax.xml.transform.Source;

import org.apache.tools.ant.filters.StringInputStream;

public class SchemaSourceTest {

    private static final InputStream INPUT_STREAM =
        new StringInputStream("dummy content");
    private static final String SYSTEM_ID = "dummy id";

    private SchemaSource schemaSource;

    @BeforeMethod
    public void setUp() {
        schemaSource = new SchemaSource(INPUT_STREAM, SYSTEM_ID);
    }

    @AfterMethod
    public void tearDown() {
        schemaSource = null;
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testConstructorIfNullInputStream() {
        new SchemaSource(null, SYSTEM_ID);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testConstructorIfNullSystemId() {
        new SchemaSource(INPUT_STREAM, null);
    }

    @Test
    public void testGetSource() {
        final Source source = schemaSource.getSource();
        assertNotNull("Returned source is null.", source);
        assertEquals("Unexpected system id of the source.", SYSTEM_ID,
                source.getSystemId());
    }

    @Test
    public void testToString() {
        final String toString = schemaSource.toString();
        assertNotNull("String from the schema source is null.", toString);
        assertEquals("Unexpected value of schema source's string.", SYSTEM_ID,
                toString);
    }


    @Test
    public void equalsIsReflexive() {
        assertEquals("Equals is not reflexive", schemaSource, schemaSource);
    }

    @Test
    public void equalsIsSymmetric() {
        final SchemaSource schemaSource2 = new SchemaSource(INPUT_STREAM,
                SYSTEM_ID);

        assertTrue("Equals is not symmetric",
                schemaSource.equals(schemaSource2)
                && schemaSource2.equals(schemaSource));
    }

    @Test
    public void equalsIsTransitive() {
        final SchemaSource schemaSource2 = new SchemaSource(INPUT_STREAM,
                SYSTEM_ID);
        final SchemaSource schemaSource3 = new SchemaSource(INPUT_STREAM,
                SYSTEM_ID);

        assertTrue("Equals is not transitive",
                schemaSource.equals(schemaSource2)
                && schemaSource2.equals(schemaSource3)
                && schemaSource.equals(schemaSource3));
    }

    @Test
    public void equalsIsCautious() {
        assertFalse("Comparison with null should always return false",
                schemaSource.equals(null));
    }

    @Test
    public void equalsIsTypeAware() {
        assertFalse("Comparison with different type should always return false",
                schemaSource.equals(this));
    }

    @Test
    public void equalsConsiderSchemaSource() {
        final SchemaSource schemaSource2 = new SchemaSource(INPUT_STREAM,
                "another id");
        assertFalse("Schema source is not considered by equals",
                schemaSource.equals(schemaSource2));
    }

    @Test
    public void testHashCode() {
        final int hashCode = schemaSource.hashCode();
        assertEquals("Unexpected hash code value.", SYSTEM_ID.hashCode(),
                hashCode);
    }

    @Test
    public void hashCodeRepeatable() {
        final int hashCode = schemaSource.hashCode();
        for (int i = 0; i < 10; i++) {
            assertEquals("Hash code is not repeatable",
                    hashCode, schemaSource.hashCode());
        }
    }

    @Test
    public void hashCodeConsistentWithEquals() {
        final SchemaSource schemaSource2 = new SchemaSource(INPUT_STREAM,
            "another id");
        assertFalse("Expected different hash codes for not equal objects",
                schemaSource.hashCode() == schemaSource2.hashCode());
    }

}
