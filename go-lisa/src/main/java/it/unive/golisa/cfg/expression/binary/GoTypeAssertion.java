package it.unive.golisa.cfg.expression.binary;

import it.unive.lisa.analysis.AbstractState;
import it.unive.lisa.analysis.AnalysisState;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.heap.HeapDomain;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.caches.Caches;
import it.unive.lisa.interprocedural.InterproceduralAnalysis;
import it.unive.lisa.program.SourceCodeLocation;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.UnaryNativeCall;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.PushAny;
import it.unive.lisa.type.Type;

public class GoTypeAssertion extends UnaryNativeCall {

	private final Type type;
	
	public GoTypeAssertion(CFG cfg, SourceCodeLocation location, Expression exp1, Type type) {
		super(cfg, location, ".", exp1);
		this.type = type;
	}

	@Override
	protected <A extends AbstractState<A, H, V>, H extends HeapDomain<H>, V extends ValueDomain<V>> AnalysisState<A, H, V> unarySemantics(
			AnalysisState<A, H, V> entryState, InterproceduralAnalysis<A, H, V> interprocedural, AnalysisState<A, H, V> exprState,
			SymbolicExpression expr) throws SemanticException {
		
		// TODO: Type assertion not handled yet, just returning top expression
		return exprState.smallStepSemantics(new PushAny(Caches.types().mkSingletonSet(type), getLocation()), this);
	}
}
