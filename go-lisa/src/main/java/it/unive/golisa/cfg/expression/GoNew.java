package it.unive.golisa.cfg.expression;

import it.unive.golisa.cfg.type.GoType;
import it.unive.lisa.analysis.AbstractState;
import it.unive.lisa.analysis.AnalysisState;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.heap.HeapDomain;
import it.unive.lisa.analysis.lattices.ExpressionSet;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.caches.Caches;
import it.unive.lisa.interprocedural.InterproceduralAnalysis;
import it.unive.lisa.program.SourceCodeLocation;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.statement.NativeCall;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.heap.HeapAllocation;
import it.unive.lisa.type.Type;

public class GoNew extends NativeCall {

	
	public GoNew(CFG cfg, SourceCodeLocation location, GoType type) {
		super(cfg, location, "new", type);
	}

	@Override
	public <A extends AbstractState<A, H, V>, H extends HeapDomain<H>, V extends ValueDomain<V>> AnalysisState<A, H, V> callSemantics(
			AnalysisState<A, H, V> entryState, InterproceduralAnalysis<A, H, V> interprocedural, AnalysisState<A, H, V>[] computedStates,
			ExpressionSet<SymbolicExpression>[] params) throws SemanticException {
		// Following the Golang reference:
		// The new built-in function allocates memory. The first argument is a type, not a value, 
		// and the value returned is a pointer to a newly allocated zero value of that type.
		HeapAllocation created = new HeapAllocation(Caches.types().mkSingletonSet((Type) getParameters()[0]), getLocation());
		return entryState.smallStepSemantics(created, this);
	}
}
