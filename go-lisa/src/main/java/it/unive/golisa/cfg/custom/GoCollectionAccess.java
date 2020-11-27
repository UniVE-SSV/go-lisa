package it.unive.golisa.cfg.custom;

import java.util.Collection;

import it.unive.lisa.analysis.AnalysisState;
import it.unive.lisa.analysis.HeapDomain;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.ValueDomain;
import it.unive.lisa.analysis.callgraph.CallGraph;
import it.unive.lisa.cfg.CFG;
import it.unive.lisa.cfg.statement.Expression;
import it.unive.lisa.cfg.statement.NativeCall;
import it.unive.lisa.symbolic.SymbolicExpression;

public class GoCollectionAccess extends NativeCall {

	public GoCollectionAccess(CFG cfg, Expression container, Expression index) {
		this(cfg, "", -1, -1, container, index);
	}
	
	public GoCollectionAccess(CFG cfg, String sourceFile, int line, int col, Expression container, Expression index) {
		super(cfg, sourceFile, line, col, "[]", new Expression[] { container, index});
	}

	@Override
	public <H extends HeapDomain<H>, V extends ValueDomain<V>> AnalysisState<H, V> callSemantics(
			AnalysisState<H, V> current, CallGraph callGraph, Collection<SymbolicExpression>[] params)
			throws SemanticException {
		throw new UnsupportedOperationException("Semantics not supported yet");
	}
}
