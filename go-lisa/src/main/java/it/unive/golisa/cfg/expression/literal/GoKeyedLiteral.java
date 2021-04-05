package it.unive.golisa.cfg.expression.literal;

import java.util.Collection;
import java.util.Map;

import it.unive.golisa.cfg.type.GoType;
import it.unive.golisa.cfg.type.composite.GoArrayType;
import it.unive.golisa.cfg.type.numeric.signed.GoIntType;
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
import it.unive.lisa.program.cfg.statement.NativeCall;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.heap.AccessChild;
import it.unive.lisa.symbolic.heap.HeapAllocation;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.HeapLocation;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.Variable;
import it.unive.lisa.type.Untyped;

public class GoKeyedLiteral extends NativeCall {

	private final Map<Expression, Expression> keyedValues;

	public GoKeyedLiteral(CFG cfg, Map<Expression, Expression> keyedValues, GoType staticType) {
		this(cfg, null, -1 , -1, keyedValues, staticType);
	}

	public GoKeyedLiteral(CFG cfg, String sourceFile, int line, int col, Map<Expression, Expression> keyedValues, GoType staticType) {
		super(cfg, new SourceCodeLocation(sourceFile, line, col), "keyedLiteral(" + staticType + ")", staticType, new Expression[]{});
		this.keyedValues = keyedValues;
	}

	@Override
	public <A extends AbstractState<A, H, V>, H extends HeapDomain<H>, V extends ValueDomain<V>> AnalysisState<A, H, V> callSemantics(
			AnalysisState<A, H, V> entryState, CallGraph callGraph, AnalysisState<A, H, V>[] computedStates,
			Collection<SymbolicExpression>[] params) throws SemanticException {
		// it corresponds to the analysis state after the evaluation of all the
		// parameters of this call
		// (the semantics of this call does not need information about the
		// intermediate analysis states)
		AnalysisState<A, H, V> lastPostState = computedStates.length == 0 ? entryState : computedStates[computedStates.length - 1];
		HeapAllocation created = new HeapAllocation(Caches.types().mkSingletonSet(getStaticType()));

		// Allocates the new heap allocation 
		AnalysisState<A, H, V> containerState = lastPostState.smallStepSemantics(created, this);
		Collection<SymbolicExpression> containerExps = containerState.getComputedExpressions();

		if (getStaticType() instanceof GoArrayType) {

			GoArrayType arrayType = (GoArrayType) getStaticType();
			int arrayLength = arrayType.getLength();

			for (SymbolicExpression containerExp : containerExps) {
				if (!(containerExp instanceof HeapLocation))
					continue;

				HeapLocation hid = (HeapLocation) containerExp;

				// Assign the len property to this hid
				Variable lenProperty = new Variable(Caches.types().mkSingletonSet(Untyped.INSTANCE), "len");
				AccessChild lenAccess = new AccessChild(Caches.types().mkSingletonSet(GoIntType.INSTANCE), hid, lenProperty);
				AnalysisState<A, H, V> lenState = containerState.smallStepSemantics(lenAccess, this);

				AnalysisState<A, H, V> lenResult = null;
				for (SymbolicExpression lenId : lenState.getComputedExpressions())
					if (lenResult == null)
						lenResult = lenState.assign((Identifier) lenId, new Constant(GoIntType.INSTANCE, arrayLength), this);
					else
						lenResult = lenResult.lub(lenState.assign((Identifier) lenId, new Constant(GoIntType.INSTANCE, arrayLength), this));

				// Assign the cap property to this hid
				Variable capProperty = new Variable(Caches.types().mkSingletonSet(Untyped.INSTANCE), "cap");
				AccessChild capAccess = new AccessChild(Caches.types().mkSingletonSet(GoIntType.INSTANCE), hid, capProperty);
				AnalysisState<A, H, V> capState = lenResult.smallStepSemantics(capAccess, this);

				AnalysisState<A, H, V> capResult = null;
				for (SymbolicExpression lenId : capState.getComputedExpressions())
					if (capResult == null)
						capResult = capState.assign((Identifier) lenId, new Constant(GoIntType.INSTANCE, arrayLength), this);
					else
						capResult = capResult.lub(capState.assign((Identifier) lenId, new Constant(GoIntType.INSTANCE, arrayLength), this));

				if (getParameters().length == 0)
					return capResult;
			}
		}

		// TODO: to handle the other cases (maps...)
		return entryState.top();
	}
}
