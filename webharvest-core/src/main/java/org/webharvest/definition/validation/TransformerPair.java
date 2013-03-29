package org.webharvest.definition.validation;

import org.webharvest.TransformationException;
import org.webharvest.Transformer;

/**
 * Implementation of {@link Transformer} interface connecting two other
 * {@link Transformer}s where the output type of the first one is the same as
 * input of the second one.
 *
 * In other words if first {@link Transformer} accepts object with type A and
 * returns object with type B and second {@link Transformer} accepts object with
 * type B and return object with type C, then {@link TransformerPair} allows to
 * convert object with type A to object with type C using these
 * {@link Transformer}s.
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 *
 * @param <I>
 *            type of input object of first {@link Transformer}
 * @param <T>
 *            type of input/output object of second/first {@link Transformer}
 * @param <O>
 *            type of output object of second {@link Transformer}
 */
public final class TransformerPair<I, T, O> implements Transformer<I, O> {

    private final Transformer<I, T> firstTransformer;
    private final Transformer<T, O> secondTransformer;

    /**
     * Default class constructor which accepts reference to two
     * {@link Transformer}s. If given {@link Transformer}s are {@code null},
     * then {@link IllegalArgumentException} is thrown.
     *
     * @param firstTransformer
     *            reference to the first {@link Transformer}
     * @param secondTransformer
     *            reference to the second {@link Transformer}
     */
    public TransformerPair(final Transformer<I, T> firstTransformer,
            final Transformer<T, O> secondTransformer) {
        if (firstTransformer == null) {
            throw new IllegalArgumentException(
                    "First transformer must not be null.");
        }
        if (secondTransformer == null) {
            throw new IllegalArgumentException(
                    "Second transformer must not be null.");
        }
        this.firstTransformer = firstTransformer;
        this.secondTransformer = secondTransformer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public O transform(I input) throws TransformationException {
        return secondTransformer.transform(firstTransformer.transform(input));
    }

}
