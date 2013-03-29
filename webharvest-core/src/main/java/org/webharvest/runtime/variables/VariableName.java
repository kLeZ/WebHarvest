package org.webharvest.runtime.variables;

import static java.text.MessageFormat.format;

import org.apache.commons.lang.StringUtils;
import org.webharvest.exception.VariableException;

/**
 * Represents name of the {@link org.webharvest.runtime.variables.Variable}
 * that is set on {@link org.webharvest.deprecated.runtime.DynamicScopeContext}.
 * The variable name must conform the general rules that apply
 * to variable names of programming languages.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 * @see org.webharvest.runtime.variables.Variable
 * @see org.webharvest.runtime.DynamicScopeContext
 */
public final class VariableName {

    private final String name;

    /**
	 * Creates new {@link org.webharvest.runtime.variables.Variable} name.
	 *
	 * @param name variable name.
	 */
	public VariableName(final String name) {
	    if (StringUtils.isBlank(name)
	            || !name.matches("^[\\p{L}_$][\\p{L}\\p{N}-_\\.$]*$")) {
	        throw new VariableException(format("Invalid variable name ''{0}''",
	                name));
	    }
	
	    this.name = name;
	}

    /**
     * Gets {@link org.webharvest.runtime.variables.Variable} value.
     *
     * @return variable name.
     */
    public String getValue() {
        return name;
    }

}
