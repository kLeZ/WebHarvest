package org.webharvest;

/**
 * A component which is capable of transforming object from one type to another
 * type.
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 *
 * @param <I>
 *            type of input object
 * @param <O>
 *            type of output object
 */
public interface Transformer<I, O> {

    /**
     * Transforms input object into output object. It accepts not {@code null}
     * input.
     *
     * @param input
     *            an object which should be transformed
     * @return result of transformation process
     * @throws TransformationException
     *             when transformation process failed
     */
    O transform(I input) throws TransformationException;

}
