package it.unive.golisa.cfg.expression.unary;

import it.unive.golisa.cfg.type.GoBoolType;
import it.unive.lisa.analysis.AbstractState;
import it.unive.lisa.analysis.AnalysisState;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.heap.HeapDomain;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.caches.Caches;
import it.unive.lisa.callgraph.CallGraph;
import it.unive.lisa.program.SourceCodeLocation;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.UnaryNativeCall;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.UnaryOperator;

/**
 * Go unary not native function class (e.g., !(x > y)).
 * The static type of this expression is definite {@link GoBoolType}
 * and its operand must be instance of {@link GoBoolType}.
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 */
public class GoNot extends UnaryNativeCall {

	/**
	 * Builds a Go unary not expression. The location where 
	 * this expression appears is unknown 
	 * (i.e. no source file/line/column is available).
	 * 
	 * @param cfg	the cfg that this expression belongs to
	 * @param exp	operand
	 */
	public GoNot(CFG cfg, Expression exp) {
		this(cfg, null, -1, -1,  exp);
	}

	public GoNot(CFG cfg, String sourceFile, int line, int col, Expression exp) {
		super(cfg, new SourceCodeLocation(sourceFile, line, col), "!", GoBoolType.INSTANCE, exp);
	}
	
	@Override
	protected <A extends AbstractState<A, H, V>, H extends HeapDomain<H>, V extends ValueDomain<V>> AnalysisState<A, H, V> unarySemantics(
			AnalysisState<A, H, V> entryState, CallGraph callGraph, AnalysisState<A, H, V> exprState,
			SymbolicExpression expr) throws SemanticException {

		if (!expr.getDynamicType().isBooleanType() && !expr.getDynamicType().isUntyped())
			return entryState.bottom();

		return exprState.smallStepSemantics(
				new UnaryExpression(Caches.types().mkSingletonSet(GoBoolType.INSTANCE), expr, UnaryOperator.LOGICAL_NOT), this);
	}

}
