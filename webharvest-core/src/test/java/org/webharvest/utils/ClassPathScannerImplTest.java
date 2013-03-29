package org.webharvest.utils;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ClassPathScannerImplTest {

    private ClassPathScannerImpl scanner;

    @BeforeMethod
    public void setUp() {
        this.scanner = new ClassPathScannerImpl(
                this.getClass().getPackage().getName());
    }

    @AfterMethod
    public void tearDown() {
        this.scanner = null;
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void doesNotAllowNullPackage() {
        new ClassPathScannerImpl(null);
    }

    @Test
    public void scanAnnotatedTypes() {
        final Set<Class< ? >> results = scanner
                .getTypesAnnotatedWith(MyAnnotation.class);
        assertNotNull("Null set of results", results);
        assertEquals("One element expected", 1, results.size());
        assertEquals("Unexpected element", AnnotatedClass.class, results
                .iterator().next());
    }

    @Test
    public void scanAnnotatedTypesAtUnknownPackage() {
        final Set<Class< ? >> results = new ClassPathScannerImpl(
                "not.existing.package")
                .getTypesAnnotatedWith(MyAnnotation.class);

        assertNotNull("Null set of results", results);
        assertTrue("Empty set expected", results.isEmpty());
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface MyAnnotation {
    }

    @MyAnnotation
    static class AnnotatedClass {
    }

    static class NotAnnotatedClass {
    }
}
