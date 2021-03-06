package it.unive.golisa.cfg.type.composite;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import it.unive.golisa.cfg.expression.literal.GoNil;
import it.unive.golisa.cfg.type.GoType;
import it.unive.lisa.program.SourceCodeLocation;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.type.PointerType;
import it.unive.lisa.type.Type;
import it.unive.lisa.type.Untyped;

public class GoSliceType implements GoType, PointerType {
	
	private Type contentType;

	private static final Set<GoSliceType> sliceTypes = new HashSet<>();

	public static GoSliceType lookup(GoSliceType type)  {
		if (!sliceTypes.contains(type))
			sliceTypes.add(type);
		return sliceTypes.stream().filter(x -> x.equals(type)).findFirst().get();
	}
	
	public GoSliceType(Type contentType) {
		this.contentType = contentType;
	}

	public Type getContentType() {
		return contentType;
	}

	@Override
	public boolean canBeAssignedTo(Type other) {
		return (other instanceof GoSliceType &&  ((GoSliceType) other).contentType.canBeAssignedTo(contentType)) || other.isUntyped();
	}

	@Override
	public Type commonSupertype(Type other) {
		return (other instanceof GoSliceType &&  ((GoSliceType) other).contentType.equals(contentType)) ? this : Untyped.INSTANCE;
	}

	@Override
	public String toString() {
		return "[]" + contentType.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((contentType == null) ? 0 : contentType.hashCode());
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
		GoSliceType other = (GoSliceType) obj;
		if (contentType == null) {
			if (other.contentType != null)
				return false;
		} else if (!contentType.equals(other.contentType))
			return false;
		return true;
	}

	@Override
	public Expression defaultValue(CFG cfg, SourceCodeLocation location) {
		return new GoNil(cfg, location);
	}
	
	@Override
	public Collection<Type> allInstances() {
		return Collections.singleton(this);
	}
}
