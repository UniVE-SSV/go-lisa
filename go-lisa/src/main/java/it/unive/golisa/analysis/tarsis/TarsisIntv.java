package it.unive.golisa.analysis.tarsis;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticDomain.Satisfiability;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryOperator;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.TernaryOperator;
import it.unive.lisa.symbolic.value.UnaryOperator;
import it.unive.lisa.symbolic.value.ValueExpression;

public class TarsisIntv extends BaseNonRelationalValueDomain<TarsisIntv> {

	private static final TarsisIntv TOP = new TarsisIntv(null, null, true);
	private static final TarsisIntv BOTTOM = new TarsisIntv(null, null, false);

	private final boolean isTop;

	private final Integer low;
	private final Integer high;

	private TarsisIntv(Integer low, Integer high, boolean isTop) {
		this.low = low;
		this.high = high;
		this.isTop = isTop;
	}

	/**
	 * Builds an interval from its low bound and high bound. Call this
	 * constructor iff {@code low} and {@code high} are not both null. If you
	 * need to build top or bottom elements, call {@link TarsisIntv#top()} and
	 * {@link TarsisIntv#bottom()}, respectively.
	 * 
	 * @param low  the low bound
	 * @param high the high bound
	 */
	public TarsisIntv(Integer low, Integer high) {
		this(low, high, false);
	}

	/**
	 * Builds the top interval.
	 */
	public TarsisIntv() {
		this(null, null, true);
	}

	@Override
	public TarsisIntv top() {
		return TOP;
	}

	@Override
	public boolean isTop() {
		return isTop && low == null && low == high;
	}

	@Override
	public TarsisIntv bottom() {
		return BOTTOM;
	}

	@Override
	public boolean isBottom() {
		return !isTop && low == null && low == high;
	}

	/**
	 * Yields the high bound of this interval.
	 * 
	 * @return the high bound of this interval.
	 */
	public Integer getHigh() {
		return high;
	}

	/**
	 * Yields the low bound of this interval.
	 * 
	 * @return the low bound of this interval.
	 */
	public Integer getLow() {
		return low;
	}

	@Override
	public DomainRepresentation representation() {
		if (isBottom())
			return Lattice.BOTTOM_REPR;
		if (isTop())
			return Lattice.TOP_REPR;

		return new StringRepresentation("[" + (lowIsMinusInfinity() ? "-Inf" : low) + ", "
				+ (highIsPlusInfinity() ? "+Inf" : high) + "]");
	}

	@Override
	protected TarsisIntv evalNullConstant(ProgramPoint pp) {
		return top();
	}

	@Override
	protected TarsisIntv evalNonNullConstant(Constant constant, ProgramPoint pp) {
		if (constant.getValue() instanceof Integer) {
			Integer i = (Integer) constant.getValue();
			return new TarsisIntv(i, i);
		}

		return top();
	}

	@Override
	protected TarsisIntv evalUnaryExpression(UnaryOperator operator, TarsisIntv arg, ProgramPoint pp) {
		switch (operator) {
		case NUMERIC_NEG:
			if (arg.isTop())
				return top();
			return arg.mul(new TarsisIntv(-1, -1));
		case STRING_LENGTH:
			return new TarsisIntv(0, null);
		default:
			return top();
		}
	}

	private boolean is(int n) {
		if (low == null || high == null)
			return false;

		return low == n && high == n;
	}

	@Override
	protected TarsisIntv evalBinaryExpression(BinaryOperator operator, TarsisIntv left, TarsisIntv right, ProgramPoint pp) {
		switch (operator) {
		case NUMERIC_ADD:
			if (left.isTop() || right.isTop())
				return top();
			return left.plus(right);
		case NUMERIC_SUB:
			if (left.isTop() || right.isTop())
				return top();
			return left.diff(right);
		case NUMERIC_MUL:
			if (left.is(0) || right.is(0))
				return new TarsisIntv(0, 0);

			if (left.isTop() || right.isTop())
				return top();

			return left.mul(right);
		case NUMERIC_DIV:
			if (right.is(0))
				return bottom();

			if (left.is(0))
				return new TarsisIntv(0, 0);

			if (left.isTop() || right.isTop())
				return top();

			return left.div(right);
		case NUMERIC_MOD:
			return top();
		default:
			return top();
		}
	}

	@Override
	protected TarsisIntv evalTernaryExpression(TernaryOperator operator, TarsisIntv left, TarsisIntv middle, TarsisIntv right,
			ProgramPoint pp) {
		return top();
	}

	@Override
	protected TarsisIntv lubAux(TarsisIntv other) throws SemanticException {
		Integer newLow = lowIsMinusInfinity() || other.lowIsMinusInfinity() ? null : Math.min(low, other.low);
		Integer newHigh = highIsPlusInfinity() || other.highIsPlusInfinity() ? null : Math.max(high, other.high);
		return newLow == null && newLow == newHigh ? top() : new TarsisIntv(newLow, newHigh);
	}

	@Override
	public TarsisIntv glbAux(TarsisIntv other) {
		Integer newLow = lowIsMinusInfinity() ? other.low : other.lowIsMinusInfinity() ? low : Math.max(low, other.low);
		Integer newHigh = highIsPlusInfinity() ? other.high
				: other.highIsPlusInfinity() ? high : Math.min(high, other.high);

		if (newLow != null && newHigh != null && newLow > newHigh)
			return bottom();
		return newLow == null && newLow == newHigh ? top() : new TarsisIntv(newLow, newHigh);
	}

	@Override
	protected TarsisIntv wideningAux(TarsisIntv other) throws SemanticException {
		Integer newLow, newHigh;
		if (other.highIsPlusInfinity() || (!highIsPlusInfinity() && other.high > high))
			newHigh = null;
		else
			newHigh = other.high;

		if (other.lowIsMinusInfinity() || (!lowIsMinusInfinity() && other.low < low))
			newLow = null;
		else
			newLow = other.low;

		return newLow == null && newLow == newHigh ? top() : new TarsisIntv(newLow, newHigh);
	}

	@Override
	protected boolean lessOrEqualAux(TarsisIntv other) throws SemanticException {
		return geqLow(low, other.low) && leqHigh(high, other.high);
	}

	private boolean lowIsMinusInfinity() {
		return low == null;
	}

	private boolean highIsPlusInfinity() {
		return high == null;
	}

	public TarsisIntv plus(TarsisIntv other) {
		Integer newLow, newHigh;

		if (lowIsMinusInfinity() || other.lowIsMinusInfinity())
			newLow = null;
		else
			newLow = low + other.low;

		if (highIsPlusInfinity() || other.highIsPlusInfinity())
			newHigh = null;
		else
			newHigh = high + other.high;

		return newLow == null && newLow == newHigh ? top() : new TarsisIntv(newLow, newHigh);
	}

	private TarsisIntv diff(TarsisIntv other) {
		Integer newLow, newHigh;

		if (other.highIsPlusInfinity() || lowIsMinusInfinity())
			newLow = null;
		else
			newLow = low - other.high;

		if (other.lowIsMinusInfinity() || highIsPlusInfinity())
			newHigh = null;
		else
			newHigh = high - other.low;

		return newLow == null && newLow == newHigh ? top() : new TarsisIntv(newLow, newHigh);
	}

	private TarsisIntv mul(TarsisIntv other) {
		// this = [l1, h1]
		// other = [l2, h2]

		SortedSet<Integer> boundSet = new TreeSet<>();
		Integer l1 = low;
		Integer h1 = high;
		Integer l2 = other.low;
		Integer h2 = other.high;

		AtomicBoolean lowInf = new AtomicBoolean(false), highInf = new AtomicBoolean(false);

		// x1 * y1
		multiplyBounds(boundSet, l1, l2, lowInf, highInf);

		// x1 * y2
		multiplyBounds(boundSet, l1, h2, lowInf, highInf);

		// x2 * y1
		multiplyBounds(boundSet, h1, l2, lowInf, highInf);

		// x2 * y2
		multiplyBounds(boundSet, h1, h2, lowInf, highInf);

		TarsisIntv result = new TarsisIntv(lowInf.get() ? null : boundSet.first(), highInf.get() ? null : boundSet.last());
		if (result.low == null && result.high == result.low)
			return top();
		else
			return result;
	}

	private TarsisIntv div(TarsisIntv other) {
		// this = [l1, h1]
		// other = [l2, h2]

		SortedSet<Integer> boundSet = new TreeSet<>();
		Integer l1 = low;
		Integer h1 = high;
		Integer l2 = other.low;
		Integer h2 = other.high;

		AtomicBoolean lowInf = new AtomicBoolean(false), highInf = new AtomicBoolean(false);

		// l1 / l2
		divideBounds(boundSet, l1, l2, lowInf, highInf);

		// x1 / y2
		divideBounds(boundSet, l1, h2, lowInf, highInf);

		// x2 / y1
		divideBounds(boundSet, h2, l2, lowInf, highInf);

		// x2 / y2
		divideBounds(boundSet, h1, h2, lowInf, highInf);

		TarsisIntv result = new TarsisIntv(lowInf.get() ? null : boundSet.first(), highInf.get() ? null : boundSet.last());
		if (result.low == null && result.high == result.low)
			return top();
		else
			return result;
	}

	private boolean isSingleton() {
		return low != null && low == high;
	}

	@Override
	protected Satisfiability satisfiesBinaryExpression(BinaryOperator operator, TarsisIntv left, TarsisIntv right,
			ProgramPoint pp) {

		if (left.isTop() || right.isTop())
			return Satisfiability.UNKNOWN;

		switch (operator) {
		case COMPARISON_EQ:
			TarsisIntv glb = null;
			try {
				glb = left.glb(right);
			} catch (SemanticException e) {
				e.printStackTrace();
			}
			if (glb.isBottom())
				return Satisfiability.NOT_SATISFIED;
			else if (left.isSingleton() && left.equals(right))
				return Satisfiability.SATISFIED;
			return Satisfiability.UNKNOWN;
		case COMPARISON_GE:
			return satisfiesBinaryExpression(BinaryOperator.COMPARISON_LE, right, left, pp);
		case COMPARISON_GT:
			return satisfiesBinaryExpression(BinaryOperator.COMPARISON_LT, right, left, pp);
		case COMPARISON_LE:
			TarsisIntv firstBound = right.high == null ? top() : new TarsisIntv(null, right.high);
			TarsisIntv secondBound = left.low == null ? top() : new TarsisIntv(left.low, null);

			TarsisIntv firstCheck = null;
			TarsisIntv secondCheck = null;

			try {
				firstCheck = firstBound.glb(left);
				secondCheck = secondBound.glb(right);
			} catch (SemanticException e) {
				e.printStackTrace();
			}

			if (firstCheck.isBottom() || secondCheck.isBottom())
				return Satisfiability.NOT_SATISFIED;

			if (firstCheck.equals(left) && secondCheck.equals(right))
				return Satisfiability.SATISFIED;
			return Satisfiability.UNKNOWN;
		case COMPARISON_LT:
			firstBound = right.high == null ? top() : new TarsisIntv(null, right.high - 1);
			secondBound = left.low == null ? top() : new TarsisIntv(left.low + 1, null);

			firstCheck = null;
			secondCheck = null;

			try {
				firstCheck = firstBound.glb(left);
				secondCheck = secondBound.glb(right);
			} catch (SemanticException e) {
				e.printStackTrace();
			}

			if (firstCheck.isBottom() || secondCheck.isBottom())
				return Satisfiability.NOT_SATISFIED;
			if (firstCheck.equals(left) && secondCheck.equals(right))
				return Satisfiability.SATISFIED;
			return Satisfiability.UNKNOWN;
		case COMPARISON_NE:
			glb = null;
			try {
				glb = left.glb(right);
			} catch (SemanticException e) {
				e.printStackTrace();
			}
			if (glb.isBottom())
				return Satisfiability.SATISFIED;
			return Satisfiability.UNKNOWN;
		default:
			return Satisfiability.UNKNOWN;
		}
	}

	private void multiplyBounds(SortedSet<Integer> boundSet, Integer i, Integer j, AtomicBoolean lowInf,
			AtomicBoolean highInf) {
		if (i == null) {
			if (j == null)
				// -inf * -inf = +inf
				highInf.set(true);
			else {
				if (j > 0)
					// -inf * positive
					lowInf.set(true);
				else if (j < 0)
					// -inf * negative
					highInf.set(true);
				else
					boundSet.add(0);
			}
		} else if (j == null) {
			if (i > 0)
				// -inf * positive
				lowInf.set(true);
			else if (i < 0)
				// -inf * negative
				highInf.set(true);
			else
				boundSet.add(0);
		} else
			boundSet.add(i * j);
	}

	private void divideBounds(SortedSet<Integer> boundSet, Integer i, Integer j, AtomicBoolean lowInf,
			AtomicBoolean highInf) {
		if (i == null) {
			if (j == null)
				// -inf * -inf = +inf
				highInf.set(true);
			else {
				if (j > 0)
					// -inf * positive
					lowInf.set(true);
				else if (j < 0)
					// -inf * negative
					highInf.set(true);

				// division by zero!
			}
		} else if (j == null) {
			if (i > 0)
				// -inf * positive
				lowInf.set(true);
			else if (i < 0)
				// -inf * negative
				highInf.set(true);
			else
				boundSet.add(0);
		} else if (j != 0) {
			boundSet.add((int) Math.ceil(i / (double) j));
			boundSet.add((int) Math.floor(i / (double) j));
		}
		// division by zero!
	}

	/**
	 * Given two interval lower bounds, yields {@code true} iff l1 >= l2, taking
	 * into account -Inf values (i.e., when l1 or l2 is {@code null}.) This
	 * method is used for the implementation of {@link TarsisIntv#lessOrEqualAux}.
	 * 
	 * @param l1 the lower bound of the first interval.
	 * @param l2 the lower bounds of the second interval.
	 * 
	 * @return {@code true} iff iff l1 >= l2, taking into account -Inf values;
	 */
	private boolean geqLow(Integer l1, Integer l2) {
		if (l1 == null) {
			if (l2 == null)
				return true;
			else
				return false;
		} else {
			if (l2 == null)
				return true;
			else
				return l1 >= l2;
		}
	}

	/**
	 * Given two interval upper bounds, yields {@code true} iff h1 <= h2, taking
	 * into account +Inf values (i.e., when h1 or h2 is {@code null}.) This
	 * method is used for the implementation of {@link TarsisIntv#lessOrEqualAux}.
	 * 
	 * @param h1 the upper bound of the first interval.
	 * @param h2 the upper bounds of the second interval.
	 * 
	 * @return {@code true} iff iff h1 <= h2, taking into account +Inf values;
	 */
	private boolean leqHigh(Integer h1, Integer h2) {
		if (h1 == null) {
			if (h2 == null)
				return true;
			else
				return false;
		} else {
			if (h2 == null)
				return false;
			else
				return h1 <= h2;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((high == null) ? 0 : high.hashCode());
		result = prime * result + (isTop ? 1231 : 1237);
		result = prime * result + ((low == null) ? 0 : low.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TarsisIntv other = (TarsisIntv) obj;
		if (high == null) {
			if (other.high != null)
				return false;
		} else if (!high.equals(other.high))
			return false;
		if (isTop != other.isTop)
			return false;
		if (low == null) {
			if (other.low != null)
				return false;
		} else if (!low.equals(other.low))
			return false;
		return true;
	}

	@Override
	protected ValueEnvironment<TarsisIntv> assumeBinaryExpression(
			ValueEnvironment<TarsisIntv> environment, BinaryOperator operator, ValueExpression left,
			ValueExpression right, ProgramPoint pp) throws SemanticException {

		Map<Identifier, TarsisIntv> map = null;

		if (environment.getMap() == null)
			map = new HashMap<Identifier, TarsisIntv>();
		else
			map = new HashMap<>(environment.getMap());

		switch (operator) {
		case COMPARISON_EQ:
			if (left instanceof Identifier)
				environment = environment.assign((Identifier) left, right, pp);
			else if (right instanceof Identifier)
				environment = environment.assign((Identifier) right, left, pp);
			return environment;
		case COMPARISON_GE:
			if (left instanceof Identifier) {
				TarsisIntv rightEval = eval(right, environment, pp);
				if (rightEval.lowIsMinusInfinity())
					return environment;

				TarsisIntv bound = new TarsisIntv(rightEval.low, null);
				map.put((Identifier) left, bound);
				return new ValueEnvironment<TarsisIntv>(bottom(), map);
			} else if (right instanceof Identifier) {
				TarsisIntv leftEval = eval(left, environment, pp);
				TarsisIntv bound = leftEval.lowIsMinusInfinity() ? leftEval : new TarsisIntv(null, leftEval.low);
				map.put((Identifier) right, bound);
				return new ValueEnvironment<TarsisIntv>(bottom(), map);
			} else
				return environment;
		case COMPARISON_GT:
			if (left instanceof Identifier) {
				TarsisIntv rightEval = eval(right, environment, pp);
				if (rightEval.lowIsMinusInfinity())
					return environment;

				TarsisIntv bound = new TarsisIntv(rightEval.low + 1, null);
				map.put((Identifier) left, bound);
				return new ValueEnvironment<TarsisIntv>(bottom(), map);
			} else if (right instanceof Identifier) {
				TarsisIntv leftEval = eval(left, environment, pp);
				TarsisIntv bound = leftEval.lowIsMinusInfinity() ? leftEval : new TarsisIntv(null, leftEval.low - 1);
				map.put((Identifier) right, bound);
				return new ValueEnvironment<TarsisIntv>(bottom(), map);
			} else
				return environment;
		case COMPARISON_LE:
			if (left instanceof Identifier) {
				TarsisIntv rightEval = eval(right, environment, pp);
				TarsisIntv bound = rightEval.lowIsMinusInfinity() ? rightEval : new TarsisIntv(null, rightEval.low);
				map.put((Identifier) left, bound);
				return new ValueEnvironment<TarsisIntv>(bottom(), map);
			} else if (right instanceof Identifier) {
				TarsisIntv leftEval = eval(left, environment, pp);
				if (leftEval.lowIsMinusInfinity())
					return environment;

				TarsisIntv bound = new TarsisIntv(leftEval.low, null);
				map.put((Identifier) right, bound);
				return new ValueEnvironment<TarsisIntv>(bottom(), map);
			} else
				return environment;
		case COMPARISON_LT:
			if (left instanceof Identifier) {
				TarsisIntv rightEval = eval(right, environment, pp);
				TarsisIntv bound = rightEval.lowIsMinusInfinity() ? rightEval : new TarsisIntv(null, rightEval.low - 1);
				map.put((Identifier) left, bound);
				return new ValueEnvironment<TarsisIntv>(bottom(), map);
			} else if (right instanceof Identifier) {
				TarsisIntv leftEval = eval(left, environment, pp);
				if (leftEval.lowIsMinusInfinity())
					return environment;

				TarsisIntv bound = new TarsisIntv(leftEval.low + 1, null);
				map.put((Identifier) right, bound);
				return new ValueEnvironment<TarsisIntv>(bottom(), map);
			} else
				return environment;
		default:
			return environment;
		}
	}
	public boolean isFinite() {
		return !isTop() && !isBottom() && this.high != null && this.low != null;
	}
}
