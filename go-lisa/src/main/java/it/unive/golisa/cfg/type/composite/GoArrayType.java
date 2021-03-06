package it.unive.golisa.cfg.type.composite;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import it.unive.golisa.cfg.expression.literal.GoNonKeyedLiteral;
import it.unive.golisa.cfg.type.GoType;
import it.unive.lisa.program.SourceCodeLocation;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.type.PointerType;
import it.unive.lisa.type.Type;
import it.unive.lisa.type.Untyped;

public class GoArrayType implements GoType, PointerType {

	private GoType contentType;
	private Integer length;

	private static final Set<GoArrayType> arrayTypes = new HashSet<>();

	public static GoArrayType lookup(GoArrayType type)  {
		if (!arrayTypes.contains(type))
			arrayTypes.add(type);

		return arrayTypes.stream().filter(x -> x.equals(type)).findFirst().get();
	}

	public GoArrayType(GoType contentType, Integer length) {
		this.contentType = contentType;
		this.length = length;
	}

	public GoType getContentType() {
		return contentType;
	}

	public Integer getLength() {
		return length;
	}

	@Override
	public boolean canBeAssignedTo(Type other) {
		if (other instanceof GoArrayType)
			return contentType.canBeAssignedTo(((GoArrayType) other).contentType) && length.equals(((GoArrayType) other).length);
		return false;
	}

	@Override
	public Type commonSupertype(Type other) {
		if (other instanceof GoArrayType)
			if (contentType.canBeAssignedTo(((GoArrayType) other).contentType) && length.equals(((GoArrayType) other).length))
				return other;
		return Untyped.INSTANCE;
	}

	@Override
	public String toString() {
		return "[" + length + "]" + contentType.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((contentType == null) ? 0 : contentType.hashCode());
		result = prime * result + ((length == null) ? 0 : length.hashCode());
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
		GoArrayType other = (GoArrayType) obj;
		if (contentType == null) {
			if (other.contentType != null)
				return false;
		} else if (!contentType.equals(other.contentType))
			return false;
		if (length == null) {
			if (other.length != null)
				return false;
		} else if (!length.equals(other.length))
			return false;
		return true;
	}

	@Override
	public Expression defaultValue(CFG cfg, SourceCodeLocation location) {
		Expression[] result = new Expression[length];
		for (int i = 0; i < length; i++)
			result[i] = contentType.defaultValue(cfg, location);

		return new GoNonKeyedLiteral(cfg, location, result, this);
	}
	
	@Override
	public boolean isPointerType() {
		return true;
	}
	
	@Override
	public boolean isArrayType() {
		return true;
	}

	public static Collection<Type> all() {
		Collection<Type> instances = new HashSet<>();
		for (GoArrayType in : arrayTypes)
			instances.add(in);
		return instances;	
	}
	
	
	@Override
	public Collection<Type> allInstances() {
		Collection<Type> instances = new HashSet<>();
		for (GoArrayType in : arrayTypes)
			instances.add(in);
		return instances;
	}
	
	public static void clearAll() {
		arrayTypes.clear();
	}
}
