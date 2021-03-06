package it.unive.golisa.cfg.runtime.conversion;

import it.unive.golisa.cfg.type.GoStringType;
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
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.BinaryOperator;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.type.Type;
import it.unive.lisa.type.TypeTokenType;
import it.unive.lisa.type.Untyped;
import it.unive.lisa.util.collections.externalSet.ExternalSet;

public class GoToString extends NativeCFG {

	public GoToString(SourceCodeLocation location, CompilationUnit pkgUnit) {
		super(new CFGDescriptor(location, pkgUnit, false, "string", GoStringType.INSTANCE,
				new Parameter(location, "this", Untyped.INSTANCE)),
				ToString.class);
	}

	public static class ToString extends UnaryNativeCall implements PluggableStatement {

		private Statement original;

		@Override
		public void setOriginatingStatement(Statement st) {
			original = st;
		}

		public ToString(CFG cfg, SourceCodeLocation location, Expression arg) {
			super(cfg, location, "string", GoStringType.INSTANCE, arg);
		}

		@Override
		protected <A extends AbstractState<A, H, V>, H extends HeapDomain<H>, V extends ValueDomain<V>> AnalysisState<A, H, V> unarySemantics(
				AnalysisState<A, H, V> entryState, InterproceduralAnalysis<A, H, V> interprocedural,
				AnalysisState<A, H, V> exprState, SymbolicExpression expr) throws SemanticException {			
			ExternalSet<Type> castType = Caches.types().mkSingletonSet(GoStringType.INSTANCE);
			Constant typeCast = new Constant(new TypeTokenType(castType), GoStringType.INSTANCE, getLocation());
			return entryState.smallStepSemantics(new BinaryExpression(castType, expr, typeCast, BinaryOperator.TYPE_CONV, original.getLocation()), original);
		}
	}
}
