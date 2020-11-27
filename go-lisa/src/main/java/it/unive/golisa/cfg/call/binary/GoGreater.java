package it.unive.golisa.cfg.call.binary;

import java.util.Collection;

import it.unive.golisa.cfg.type.GoBoolType;
import it.unive.lisa.analysis.AnalysisState;
import it.unive.lisa.analysis.HeapDomain;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.ValueDomain;
import it.unive.lisa.analysis.callgraph.CallGraph;
import it.unive.lisa.cfg.CFG;
import it.unive.lisa.cfg.statement.Expression;
import it.unive.lisa.cfg.statement.NativeCall;
import it.unive.lisa.symbolic.SymbolicExpression;

/**
 * A Go greater function call (e1 > e2).
 * The static type of this expression is definitely {@link GoBoolType}.
 * 
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 */
public class GoGreater extends NativeCall {
	
	/**
	 * Builds a Go greater expression. 
	 * The location where this expression appears is unknown 
	 * (i.e. no source file/line/column is available).
	 * 
	 * @param cfg	the cfg that this expression belongs to
	 * @param exp1	left-hand side operand
	 * @param exp2 	right-hand side operand 
	 */
	public GoGreater(CFG cfg, Expression exp1, Expression exp2) {
		super(cfg, null, -1, -1, ">", GoBoolType.INSTANCE, exp1, exp2);
	}

	/**
	 * Builds a Go greater expression at a given location in the program.
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
	public GoGreater(CFG cfg, String sourceFile, int line, int col, Expression exp1, Expression exp2) {
		super(cfg, sourceFile, line, col, ">", GoBoolType.INSTANCE,exp1, exp2);
	}

	@Override
	public <H extends HeapDomain<H>, V extends ValueDomain<V>> AnalysisState<H, V> callSemantics(
			AnalysisState<H, V> current, CallGraph callGraph, Collection<SymbolicExpression>[] params)
			throws SemanticException {
		throw new UnsupportedOperationException("Semantics not supported yet");
	}

}
