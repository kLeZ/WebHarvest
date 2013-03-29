package org.webharvest.runtime.variables;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.*;
import org.webharvest.exception.VariableException;

public class VariableNameTest {

    private static final String VALID_NAME = "dummy";

    @Test(expectedExceptions=VariableException.class)
    public void testCreateWithNullName() {
        new VariableName(null);
    }

    @Test(expectedExceptions=VariableException.class)
    public void testCreateWithEmptyName() {
        new VariableName("");
    }

    @Test(expectedExceptions=VariableException.class)
    public void testCreateWithSeparatedName() {
        new VariableName("new value");
    }

    @Test(expectedExceptions=VariableException.class)
    public void testCreateWithNameSeparatedByMinus() {
        new VariableName("new-value");
    }

    @Test(expectedExceptions=VariableException.class)
    public void testCreateWithVarNameStartingWithNumber() {
        new VariableName("1SSSS");
    }

    @Test
    public void testCreateWithVarNameStartsWithUpper() {
        new VariableName("Dummy");
    }

    @Test
    public void testCreateWithVarNameWithCammelCase() {
        new VariableName("dummyVar");
    }

    @Test
    public void testCreateWithVarNameEndingWithNumber() {
        new VariableName("dummy1");
    }

    @Test
    public void testCreateWithVarNameWithNumberInMiddle() {
        new VariableName("dum12my");
    }

    @Test
    public void testGetValue() {
        final String value = new VariableName(VALID_NAME).getValue();
        assertNotNull(value);
        assertSame(VALID_NAME, value);
    }

}
