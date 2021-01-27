package it.unive.golisa.analysis.rsubs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import it.unive.golisa.cfg.type.GoStringType;
import it.unive.lisa.analysis.FunctionalLattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.ValueDomain;
import it.unive.lisa.caches.Caches;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.BinaryOperator;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.PushAny;
import it.unive.lisa.symbolic.value.ValueExpression;

public class RelationalSubstringDomain extends FunctionalLattice<RelationalSubstringDomain, Identifier, StringUpperBound> implements ValueDomain<RelationalSubstringDomain> {

	public RelationalSubstringDomain() {
		this(new StringUpperBound(), null);
	}

	protected RelationalSubstringDomain(StringUpperBound lattice, Map<Identifier, StringUpperBound> function) {
		super(lattice, function);
	}

	@Override
	public RelationalSubstringDomain top() {
		return new RelationalSubstringDomain(lattice.top(), null);
	}

	@Override
	public RelationalSubstringDomain bottom() {
		return new RelationalSubstringDomain(lattice.bottom(), null);
	}

	@Override
	public boolean isTop() {
		return lattice.isTop() && function == null;
	}

	@Override
	public boolean isBottom() {
		return lattice.isBottom() && function == null;
	}

	@Override
	public RelationalSubstringDomain lubAux(RelationalSubstringDomain other) throws SemanticException {
		Map<Identifier, StringUpperBound> func = new HashMap<>();

		for (Identifier x : getKeys())
			if (other.function.containsKey(x))
				func.put(x, getState(x).lub(other.getState(x)));

		return new RelationalSubstringDomain(lattice, func);
	}


	private RelationalSubstringDomain glb(RelationalSubstringDomain other) throws SemanticException {
		return super.lub(other);
	}

	@Override
	public RelationalSubstringDomain assign(Identifier id, ValueExpression expression) throws SemanticException {
		Map<Identifier, StringUpperBound> func;
		if (function == null)
			func = new HashMap<>();
		else
			func = new HashMap<>(function);

		if (expression instanceof PushAny)
			return new RelationalSubstringDomain(lattice, func);

		
		// Remove phase
		for (Identifier x : func.keySet())
			for (ValueExpression xRel : func.get(x))
				if (appersIn(xRel, id))
					func.put(x, func.get(x).removeRelation(id));

		if (!appearsAtTopLevel(expression, id))
			func.remove(id);

		// Add phase
		func.put(id, func.get(id) == null ? getRelations(expression) : func.get(id).merge(getRelations(expression)));

		// Inter-asg phase
		for (Identifier y : func.keySet())
			if (!y.equals(id) && func.get(y).contains(func.get(id)))
				func.put(y, func.get(y).addRelation(id));

		// Closure phase
		return new RelationalSubstringDomain(lattice, func).closure();
	}

	private RelationalSubstringDomain closure() {
		if (isTop() || isBottom())
			return new RelationalSubstringDomain(lattice, function);


		Map<Identifier, StringUpperBound>  clos = new HashMap<>(function);


		for (Identifier x : clos.keySet())
			for (Identifier y : clos.keySet())
				for (Identifier z : clos.keySet())
					if (clos.get(y).contains(x) && clos.get(z).contains(y))
						clos.put(z, clos.get(z).addRelation(x));
		return new RelationalSubstringDomain(lattice, clos);
	}

	@Override
	public RelationalSubstringDomain smallStepSemantics(ValueExpression expression) throws SemanticException {
		return new RelationalSubstringDomain(lattice, function);
	}

	@Override
	public RelationalSubstringDomain assume(ValueExpression expression) throws SemanticException {
		// rsubs can assume contains, equals, and & or expressions (all binary expressions)

		if (isBottom())
			return bottom();

		HashMap<Identifier, StringUpperBound> func;
		if (function == null)
			func = new HashMap<>();
		else
			func = new HashMap<>(function);

		if (expression instanceof BinaryExpression) {
			BinaryExpression binary = (BinaryExpression) expression;
			RelationalSubstringDomain leftState = smallStepSemantics((ValueExpression) binary.getLeft());
			RelationalSubstringDomain rightState = smallStepSemantics((ValueExpression) binary.getRight());

			switch(binary.getOperator()) {
			
			case COMPARISON_EQ:
				ValueExpression left = (ValueExpression) binary.getLeft();
				ValueExpression right = (ValueExpression) binary.getRight();
				
				if (!left.getDynamicType().isStringType() || !right.getDynamicType().isStringType())
					break;

				if (left instanceof Identifier && right instanceof Identifier) {
					Identifier x = (Identifier) left;
					Identifier y = (Identifier) right;
					
					StringUpperBound relsForX = getRelations(y);
					StringUpperBound relsForY = getRelations(x);
					
					func.put(x, func.get(x) == null ? relsForX : func.get(x).merge(relsForX));
					func.put(y, func.get(y) == null ? relsForY : func.get(y).merge(relsForY));
					return new RelationalSubstringDomain(lattice, func);
				}
				
				if (left instanceof Identifier) {
					Identifier x = (Identifier) left;
					StringUpperBound rels = getRelations(right);
					func.put(x, func.get(x) == null ? rels : func.get(x).merge(rels));
					return new RelationalSubstringDomain(lattice, func);
				}

				if (right instanceof Identifier) {
					Identifier x = (Identifier) right;
					StringUpperBound rels = getRelations(left);
					func.put(x, func.get(x) == null ? rels : func.get(x).merge(rels));
					return new RelationalSubstringDomain(lattice, func);
				}
			case COMPARISON_GE:
				break;
			case COMPARISON_GT:
				break;
			case COMPARISON_LE:
				break;
			case COMPARISON_LT:
				break;
			case COMPARISON_NE:
				break;
			case LOGICAL_AND:
				return leftState.lub(rightState);
			case LOGICAL_OR:
				return leftState.glb(rightState);
			case STRING_STARTS_WITH: // can be assumed but not integrated in the paper
			case STRING_ENDS_WITH: // can be assumed but not integrated in the paper
			case STRING_CONTAINS:
				if (binary.getLeft() instanceof Identifier) {
					Identifier x = (Identifier) binary.getLeft();
					StringUpperBound rels = getRelations((ValueExpression) binary.getRight());
					func.put(x, func.get(x) == null ? rels : func.get(x).merge(rels));
					return new RelationalSubstringDomain(lattice, func);
				}
			case STRING_EQUALS:
				if (binary.getLeft() instanceof Identifier) {
					Identifier x = (Identifier) binary.getLeft();
					StringUpperBound rels = getRelations((ValueExpression) binary.getRight());
					func.put(x, func.get(x) == null ? rels : func.get(x).merge(rels));
					return new RelationalSubstringDomain(lattice, func);
				}

				if (binary.getRight() instanceof Identifier) {
					Identifier x = (Identifier) binary.getRight();
					StringUpperBound rels = getRelations((ValueExpression) binary.getLeft());
					func.put(x, func.get(x) == null ? rels : func.get(x).merge(rels));
					return new RelationalSubstringDomain(lattice, func);
				}
			default:
				return new RelationalSubstringDomain(lattice, function);
			}
		}

		return new RelationalSubstringDomain(lattice, function);
	}

	@Override
	public RelationalSubstringDomain forgetIdentifier(Identifier id) throws SemanticException {
		if (function == null)
			return new RelationalSubstringDomain(lattice, null);

		RelationalSubstringDomain result = new RelationalSubstringDomain(lattice, new HashMap<>(function));
		if (result.function.containsKey(id))
			result.function.remove(id);

		return result;
	}

	@Override
	public Satisfiability satisfies(ValueExpression expression) throws SemanticException {
		// rsubs can satisfy contains, equals, and & or expressions (all binary expressions)

		if (isTop())
			return Satisfiability.UNKNOWN;
		if (isBottom())
			return Satisfiability.BOTTOM;
		
		if (expression instanceof BinaryExpression) {
			BinaryExpression binary = (BinaryExpression) expression;

			switch(binary.getOperator()) {
			case COMPARISON_EQ:
				break;
			case COMPARISON_GE:
				break;
			case COMPARISON_GT:
				break;
			case COMPARISON_LE:
				break;
			case COMPARISON_LT:
				break;
			case COMPARISON_NE:
				break;
			case LOGICAL_AND:
				return satisfies((ValueExpression) binary.getLeft()).and(satisfies((ValueExpression) binary.getRight()));
			case LOGICAL_OR:
				return satisfies((ValueExpression) binary.getLeft()).or(satisfies((ValueExpression) binary.getRight()));
			case STRING_CONTAINS:
				if (binary.getLeft() instanceof Identifier) {
					Identifier x = (Identifier) binary.getLeft();
					return getState(x).contains((ValueExpression) binary.getRight()) ? Satisfiability.SATISFIED : Satisfiability.NOT_SATISFIED;
				}
			case STRING_EQUALS:
				if (binary.getLeft() instanceof Identifier && binary.getRight() instanceof Identifier) {
					Identifier x = (Identifier) binary.getLeft();
					Identifier y = (Identifier) binary.getRight();
					return getState(x).contains(y) && getState(y).contains(x) ? Satisfiability.SATISFIED : Satisfiability.NOT_SATISFIED;
				}
			default:
				return Satisfiability.UNKNOWN;
			}
		}

		return Satisfiability.UNKNOWN;
	}

	@Override
	public String toString() {
		return representation();
	}

	@Override
	public String representation() {
		if (isTop())
			return "TOP";

		if (isBottom())
			return "BOTTOM";

		StringBuilder builder = new StringBuilder();
		for (Entry<Identifier, StringUpperBound> entry : function.entrySet())
			builder.append(entry.getKey()).append(": ").append(entry.getValue().toString()).append("\n");

		return builder.toString().trim();
	}

	private ValueExpression[] flat(ValueExpression expression) {

		if (expression instanceof BinaryExpression) {
			BinaryExpression binary = (BinaryExpression) expression;

			if (binary.getOperator() == BinaryOperator.STRING_CONCAT)
				return ArrayUtils.addAll(flat((ValueExpression) binary.getLeft()), flat((ValueExpression) binary.getRight()));

		}
		return new ValueExpression[] {expression};
	}

	private StringUpperBound getRelations(ValueExpression expression) {
		ValueExpression[] exps = flat(expression);
		Set<ValueExpression> result = new HashSet<ValueExpression>();

		for (int i = 0; i < exps.length; i++) {
			ValueExpression partial = exps[i];

			if (exps[i] instanceof Constant && ((Constant) exps[i]).getValue() instanceof String) 
				result.addAll(getAllSubstrings((String) ((Constant) exps[i]).getValue()));
			else 
				result.add(exps[i]);		

			for (int j = i + 1; j < exps.length; j++) {

				if (exps[j] instanceof Constant && ((Constant) exps[j]).getValue() instanceof String) 
					result.addAll(getAllSubstrings((String) ((Constant) exps[j]).getValue()));
				else 
					result.add(exps[j]);

				partial = new BinaryExpression(Caches.types().mkSingletonSet(GoStringType.INSTANCE), partial, exps[j], BinaryOperator.STRING_CONCAT);
				result.add(partial);
			}
		}

		return new StringUpperBound(result);
	} 

	private Set<Constant> getAllSubstrings(String str) {
		HashSet<Constant> res = new HashSet<Constant>();

		for (int i = 0; i < str.length(); i++) 
			for (int j = i+1; j <= str.length(); j++) 
				res.add(new Constant(GoStringType.INSTANCE, str.substring(i,j)));

		return res;
	}

	private boolean appersIn(ValueExpression expression, ValueExpression search) {
		HashSet<ValueExpression> singleton = new HashSet<>();
		singleton.add(search);
		return getRelations(expression).contains(new StringUpperBound(singleton));
	}

	private boolean appearsAtTopLevel(ValueExpression expression, Identifier id) {
		if (expression.equals(id))
			return true;

		if (expression instanceof BinaryExpression && ((BinaryExpression) expression).getOperator() == BinaryOperator.STRING_CONCAT) {
			BinaryExpression binary = (BinaryExpression) expression;
			return appearsAtTopLevel((ValueExpression) binary.getLeft(), id) || appearsAtTopLevel((ValueExpression) binary.getRight(), id);
		}

		return false;
	}
}
