package it.unive.golisa.cfg.expression;

import java.util.Collection;

import it.unive.golisa.cfg.type.GoType;
import it.unive.lisa.analysis.AbstractState;
import it.unive.lisa.analysis.AnalysisState;
import it.unive.lisa.analysis.HeapDomain;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.ValueDomain;
import it.unive.lisa.analysis.impl.types.TypeEnvironment;
import it.unive.lisa.caches.Caches;
import it.unive.lisa.callgraph.CallGraph;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.statement.NativeCall;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.heap.HeapAllocation;
import it.unive.lisa.type.Type;

public class GoNew extends NativeCall {

	public GoNew(CFG cfg, GoType type) {
		super(cfg, "new", type);
	}

	@Override
	public <A extends AbstractState<A, H, V>, H extends HeapDomain<H>, V extends ValueDomain<V>> AnalysisState<A, H, V> callSemantics(
			AnalysisState<A, H, V> entryState, CallGraph callGraph, AnalysisState<A, H, V>[] computedStates,
			Collection<SymbolicExpression>[] params) throws SemanticException {
		// Following the Golang reference:
		// The new built-in function allocates memory. The first argument is a type, not a value, 
		// and the value returned is a pointer to a newly allocated zero value of that type.
		HeapAllocation created = new HeapAllocation(Caches.types().mkSingletonSet((Type) getParameters()[0]));
		return entryState.smallStepSemantics(created);
	}

	@Override
	public <A extends AbstractState<A, H, TypeEnvironment>, H extends HeapDomain<H>> AnalysisState<A, H, TypeEnvironment> callTypeInference(
			AnalysisState<A, H, TypeEnvironment> entryState, CallGraph callGraph,
			AnalysisState<A, H, TypeEnvironment>[] computedStates, Collection<SymbolicExpression>[] params)
			throws SemanticException {

		// Following the Golang reference:
		// The new built-in function allocates memory. The first argument is a type, not a value, 
		// and the value returned is a pointer to a newly allocated zero value of that type.
		HeapAllocation created = new HeapAllocation(Caches.types().mkSingletonSet((Type) getParameters()[0]));
		return entryState.smallStepSemantics(created);
	}
}
