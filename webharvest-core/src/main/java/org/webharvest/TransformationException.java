package org.webharvest;

/**
 * Checked exception thrown if transformation process has failed.
 *
 * @see Transformer
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public final class TransformationException extends Exception {

    /**
     * Serialization requirement.
     */
    private static final long serialVersionUID = 8231302592651245429L;

    /**
     * {@link TransformationException} constructor accepting cause of the
     * exception.
     *
     * @param cause
     *            exception cause
     */
    public TransformationException(final Throwable cause) {
        super(cause);
    }
}
