package org.delia.rule;

import org.delia.type.DValue;
import org.delia.type.TypeReplaceSpec;

/**
 * A validation rule.
 * @author Ian Rae
 *
 */
public interface DRule {
	String getName();
	boolean validate(DValue dval, DRuleContext ctx);
	Object exec(DValue dval, DRuleContext ctx); //rules either do exec or validate
	boolean dependsOn(String fieldName); //does this rule evaluate or depend on the given field
	String getSubject(); //for error messages
	void setPolarity(boolean polarity);
	boolean executeGuard(DValue dval);
	void performTypeReplacement(TypeReplaceSpec spec);
}