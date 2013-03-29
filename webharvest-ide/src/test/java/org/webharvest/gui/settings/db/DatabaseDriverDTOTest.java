package org.webharvest.gui.settings.db;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DatabaseDriverDTOTest {

    private DatabaseDriverDTO x;
    private DatabaseDriverDTO y;
    private DatabaseDriverDTO z;
    private DatabaseDriverDTO notX;

    @BeforeMethod
    public void setUp() {
        this.x = new DatabaseDriverDTO("/tmp/somewhere");
        this.y = new DatabaseDriverDTO("/tmp/somewhere");
        this.z = new DatabaseDriverDTO("/tmp/somewhere");

        this.notX = new DatabaseDriverDTO("/tmp/somewhere/else");
    }

    @AfterMethod
    public void tearDown() {
        this.x = null;
        this.y = null;
        this.z = null;

        this.notX = null;
    }


    @Test(expectedExceptions = IllegalArgumentException.class)
    public void nullLocationDisallowed() {
        new DatabaseDriverDTO(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void emptyLocationDisallowed() {
        new DatabaseDriverDTO("");
    }

    @Test
    public void equalsIsReflexive() {
        assertEquals("Equals is not reflexive", x, x);
    }

    @Test
    public void equalsIsSymmetric() {
        assertTrue("Equals is not symmetric", x.equals(y) && y.equals(x));
    }

    @Test
    public void equalsIsTransitive() {
        assertTrue("Equals is not transitive",
                x.equals(y) && y.equals(z) && x.equals(z));
    }

    @Test
    public void equalsIsCautious() {
        assertFalse("Comparision with null should always return false",
                x.equals(null));
    }

    @Test
    public void equalsIsTypeAware() {
        assertFalse("Comparison with different type should always return false",
                x.equals(this));
    }

    @Test
    public void notEqualForDifferentLocations() {
        assertFalse("Location is not considered by equals", x.equals(notX));
    }

    @Test
    public void hashCodeIsRepeatable() {
        final int hashCode = x.hashCode();
        for (int i = 0; i < 10; i++) {
            assertEquals("Hash code is not repeatable", hashCode, x.hashCode());
        }
    }

    @Test
    public void hashCodeConsistentWithEquals() {
        assertFalse("Expected different hash codes for not equal objects",
                x.hashCode() == notX.hashCode());
    }
}
