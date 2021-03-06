package it.unive.golisa.cfg.runtime.strings;

import it.unive.golisa.cfg.type.GoStringType;
import it.unive.golisa.cfg.type.numeric.signed.GoIntType;
import it.unive.lisa.analysis.AbstractState;
import it.unive.lisa.analysis.AnalysisState;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.heap.HeapDomain;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.caches.Caches;
import it.unive.lisa.interprocedural.InterproceduralAnalysis;
import it.unive.lisa.program.CompilationUnit;
import it.unive.lisa.program.SourceCodeLocation;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.CFGDescriptor;
import it.unive.lisa.program.cfg.NativeCFG;
import it.unive.lisa.program.cfg.Parameter;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.PluggableStatement;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.UnaryNativeCall;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.UnaryOperator;
import it.unive.lisa.type.Type;
import it.unive.lisa.util.collections.externalSet.ExternalSet;

public class GoLen extends NativeCFG {

	public GoLen(SourceCodeLocation location, CompilationUnit stringUnit) {
		super(new CFGDescriptor(location, stringUnit, false, "Len", GoIntType.INSTANCE,
				new Parameter(location, "this", GoStringType.INSTANCE)),
				Len.class);
	}
	
	public static class Len extends UnaryNativeCall implements PluggableStatement {
		
		private Statement original;

		@Override
		public void setOriginatingStatement(Statement st) {
			original = st;
		}
		
		public Len(CFG cfg, SourceCodeLocation location, Expression arg) {
			super(cfg, location, "Len", GoIntType.INSTANCE, arg);
		}

		@Override
		protected <A extends AbstractState<A, H, V>, H extends HeapDomain<H>, V extends ValueDomain<V>> AnalysisState<A, H, V> unarySemantics(
				AnalysisState<A, H, V> entryState, InterproceduralAnalysis<A, H, V> interprocedural,
				AnalysisState<A, H, V> exprState, SymbolicExpression expr) throws SemanticException {
			ExternalSet<Type> intType = Caches.types().mkSingletonSet(GoIntType.INSTANCE);
			return exprState.smallStepSemantics(new UnaryExpression(intType, expr, UnaryOperator.STRING_LENGTH, getLocation()), original);
		}

		
	}
}
