package it.unive.golisa.cfg.expression.binary;

import it.unive.golisa.cfg.type.GoStringType;
import it.unive.lisa.analysis.AbstractState;
import it.unive.lisa.analysis.AnalysisState;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.heap.HeapDomain;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.caches.Caches;
import it.unive.lisa.callgraph.CallGraph;
import it.unive.lisa.program.SourceCodeLocation;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.statement.BinaryNativeCall;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.BinaryOperator;
import it.unive.lisa.type.Type;
import it.unive.lisa.util.collections.externalSet.ExternalSet;

/**
 * A Go numerical sum function call (e1 + e2).
 * 
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 */
public class GoSum extends BinaryNativeCall implements GoBinaryNumericalOperation {

	/**
	 * Builds a Go sum expression. The location where 
	 * this expression appears is unknown 
	 * (i.e. no source file/line/column is available).
	 * 
	 * @param cfg	the cfg that this expression belongs to
	 * @param exp1	left-hand side operand
	 * @param exp2 	right-hand side operand 
	 */
	public GoSum(CFG cfg, Expression exp1, Expression exp2) {
		this(cfg, null, -1, -1, exp1, exp2);
	}
	
	public GoSum(CFG cfg, String filePath, int line, int col, Expression exp1, Expression exp2) {
		super(cfg, new SourceCodeLocation(filePath, line, col), "+", exp1, exp2);
	}

	@Override
	protected <A extends AbstractState<A, H, V>, H extends HeapDomain<H>, V extends ValueDomain<V>> AnalysisState<A, H, V> binarySemantics(
			AnalysisState<A, H, V> entryState, CallGraph callGraph, AnalysisState<A, H, V> leftState,
			SymbolicExpression leftExp, AnalysisState<A, H, V> rightState, SymbolicExpression rightExp)
			throws SemanticException {
		BinaryOperator op;
		ExternalSet<Type> types;
		
		Type leftType = leftExp.getDynamicType();
		Type rightType = rightExp.getDynamicType();
		
		if (leftType.isStringType() || rightType.isStringType()) {
			op = BinaryOperator.STRING_CONCAT;
			types = Caches.types().mkSingletonSet(GoStringType.INSTANCE);
		} else if (leftType.isNumericType() || rightType.isNumericType()) {
			op = BinaryOperator.NUMERIC_ADD;
			types = resultType(leftExp, rightExp);
		} else {
			// Rewrite as string concatenation symbolic expression
			op = BinaryOperator.STRING_CONCAT;
			types = Caches.types().mkSingletonSet(GoStringType.INSTANCE);
			AnalysisState<A, H, V> stringConcat = rightState
					.smallStepSemantics(new BinaryExpression(types, leftExp, rightExp, op), this);
			// Rewrite as numeric add symbolic expression
			op = BinaryOperator.NUMERIC_ADD;
			types = resultType(leftExp, rightExp);
			AnalysisState<A, H, V> numericAdd = rightState
					.smallStepSemantics(new BinaryExpression(types, leftExp, rightExp, op), this);
			
			// Least upper bound of the two results
			return stringConcat.lub(numericAdd);
		}
		
		return rightState
				.smallStepSemantics(new BinaryExpression(types, leftExp, rightExp, op), this);
	}
	
}
