package it.unive.golisa.cfg.expression.binary;

import it.unive.golisa.cfg.type.GoBoolType;
import it.unive.lisa.analysis.AbstractState;
import it.unive.lisa.analysis.AnalysisState;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.heap.HeapDomain;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.caches.Caches;
import it.unive.lisa.interprocedural.InterproceduralAnalysis;
import it.unive.lisa.program.SourceCodeLocation;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.statement.BinaryNativeCall;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.BinaryOperator;


/**
 * A Go less or equal function call (e1 < e2).
 * The static type of this expression is definitely {@link GoBoolType}.
 * The semantics of Go less or equal expression follows the Golang specification:
 * {@link https://golang.org/ref/spec#Comparison_operators}
 * 
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 */
public class GoLessOrEqual extends BinaryNativeCall {


	/**
	 * Builds a Go less or equal expression at a given location in the program.
	 * 
	 * @param cfg           the cfg that this expression belongs to
	 * @param sourceFile    the source file where this expression happens. If
	 *                      unknown, use {@code null}
	 * @param line          the line number where this expression happens in the
	 *                      source file. If unknown, use {@code -1}
	 * @param col           the column where this expression happens in the source
	 *                      file. If unknown, use {@code -1}
	 * @param exp1		    left-hand side operand
	 * @param exp2		    right-hand side operand
	 */
	public GoLessOrEqual(CFG cfg, SourceCodeLocation location, Expression exp1, Expression exp2) {
		super(cfg, location, "<=", GoBoolType.INSTANCE, exp1, exp2);
	}

	@Override
	protected <A extends AbstractState<A, H, V>, H extends HeapDomain<H>, V extends ValueDomain<V>> AnalysisState<A, H, V> binarySemantics(
			AnalysisState<A, H, V> entryState, InterproceduralAnalysis<A, H, V> interprocedural, AnalysisState<A, H, V> leftState,
			SymbolicExpression leftExp, AnalysisState<A, H, V> rightState, SymbolicExpression rightExp)
					throws SemanticException {

		// following the Golang specification:
		// in any comparison, the first operand must be assignable to the type of the second operand, or vice versa.
		if (leftExp.getDynamicType().canBeAssignedTo(rightExp.getDynamicType()) || rightExp.getDynamicType().canBeAssignedTo(leftExp.getDynamicType())) {
			// only, integer, floating point values, strings are ordered
			if (leftExp.getDynamicType().isNumericType() && leftExp.getDynamicType().isNumericType())
				return rightState.smallStepSemantics(new BinaryExpression(Caches.types().mkSingletonSet(GoBoolType.INSTANCE), leftExp, rightExp, BinaryOperator.COMPARISON_LE, getLocation()), this);
			//TODO: string comparisong: missing lexicographical order in LiSa binary operator
		}

		return entryState.bottom();
	}
}
