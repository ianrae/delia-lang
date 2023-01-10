package org.delia.varevaluator;

public interface CustomVarEvaluatorFactory {
    CustomVarEvaluator create(VarEvaluator inner);
}
