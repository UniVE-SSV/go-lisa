package it.unive.golisa.cfg.expression.unary;

import it.unive.golisa.cfg.expression.literal.GoInteger;
import it.unive.golisa.cfg.type.numeric.signed.GoIntType;
import it.unive.lisa.analysis.AbstractState;
import it.unive.lisa.analysis.AnalysisState;
import it.unive.lisa.analysis.HeapDomain;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.ValueDomain;
import it.unive.lisa.caches.Caches;
import it.unive.lisa.callgraph.CallGraph;
import it.unive.lisa.cfg.CFG;
import it.unive.lisa.cfg.statement.Expression;
import it.unive.lisa.cfg.statement.UnaryNativeCall;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.BinaryOperator;
import it.unive.lisa.symbolic.value.Constant;

/**
 * Go unary minus native function class (e.g., -(5 + 3), -5).
 * 
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 */
public class GoMinus extends UnaryNativeCall {

	/**
	 * Builds a Go unary minus expression. The location where 
	 * this expression appears is unknown 
	 * (i.e. no source file/line/column is available).
	 * 
	 * @param cfg	the cfg that this expression belongs to
	 * @param exp	operand
	 */
	public GoMinus(CFG cfg, Expression exp) {
		super(cfg, null, -1, -1, "-", exp);
	}

	@Override
	protected <A extends AbstractState<A, H, V>, H extends HeapDomain<H>, V extends ValueDomain<V>> AnalysisState<A, H, V> unarySemantics(
			AnalysisState<A, H, V> entryState, CallGraph callGraph, AnalysisState<A, H, V> exprState,
			SymbolicExpression expr) throws SemanticException {
		if (!expr.getDynamicType().isNumericType() && !expr.getDynamicType().isUntyped())
			return entryState.bottom();

		Constant zero = new Constant(GoIntType.INSTANCE, new GoInteger(getCFG(), 0));
		return exprState.smallStepSemantics(
				new BinaryExpression(Caches.types().mkSingletonSet(zero.getDynamicType()), zero, expr, BinaryOperator.NUMERIC_SUB));
	}
}
