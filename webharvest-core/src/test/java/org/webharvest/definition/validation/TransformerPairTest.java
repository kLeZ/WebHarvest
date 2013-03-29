package org.webharvest.definition.validation;

import static org.easymock.EasyMock.*;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertNotNull;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.webharvest.Transformer;
import org.unitils.easymock.EasyMockUnitils;
import org.unitils.easymock.annotation.RegularMock;

public class TransformerPairTest extends UnitilsTestNG {

    @RegularMock
    private Transformer<A, B> mockFirstTransformer;

    @RegularMock
    private Transformer<B, C> mockSecondTransformer;

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testConstructorIfNullFirstTransformer() {
        new TransformerPair<A, B, C>(null, mockSecondTransformer);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testConstructorIfNullSecondTransformer() {
        new TransformerPair<A, B, C>(mockFirstTransformer, null);
    }

    @Test
    public void testTransform() throws Exception {
        final A a = new A();
        final B b = new B();
        final C c = new C();
        final TransformerPair<A, B, C> transformer =
            new TransformerPair<A, B, C>(mockFirstTransformer,
                    mockSecondTransformer);

        expect(mockFirstTransformer.transform(same(a))).andReturn(b);
        expect(mockSecondTransformer.transform(same(b))).andReturn(c);

        EasyMockUnitils.replay();

        final C result = transformer.transform(a);
        assertNotNull("Unexpected null result.", result);
        assertSame("Unexpected result.", c, result);
    }



    private class A {}

    private class B {}

    private class C {}

}
