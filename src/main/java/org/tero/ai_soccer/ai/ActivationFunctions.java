package org.tero.ai_soccer.ai;

import java.util.function.Function;

import org.ejml.simple.SimpleMatrix;

public enum ActivationFunctions {
    LOGISTIC(m -> sigmoid(m)), TANH(m -> {
	for (int i = 0; i < m.getNumElements(); i++)
	    m.set(i, 2 * elemSigmoid(2 * m.get(i)) - 1);
	return m;
    }), DEFAULT(TANH.getFunction());

    private Function<SimpleMatrix, SimpleMatrix> function;

    ActivationFunctions(Function<SimpleMatrix, SimpleMatrix> func) {
	function = func;
    }

    public Function<SimpleMatrix, SimpleMatrix> getFunction() {
	return function;
    }

    public SimpleMatrix apply(SimpleMatrix inputColumn) {
	return function.apply(inputColumn);
    }

    private static SimpleMatrix sigmoid(SimpleMatrix m) {
	for (int i = 0; i < m.getNumElements(); i++)
	    m.set(i, elemSigmoid(m.get(i)));
	return m;
    }

    private static double elemSigmoid(double x) {
	return 1.0 / (1.0 + Math.exp(-x));
    }

}
