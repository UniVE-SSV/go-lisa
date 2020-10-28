package it.unive.golisa.cfg.type;

import it.unive.lisa.cfg.type.NumericType;

/**
 * Go int type. The int type, in Go, is a machine dependent type 
 * since his size (32 or 64 bits) depends on the type of architecture that it is used.
 * 
 * @link https://www.golang-book.com/books/intro/3
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 */
public class GoIntType implements NumericType {

	/**
	 * Unique instance of GoString type. 
	 */
	public static final GoIntType INSTANCE = new GoIntType();
	
	private GoIntType() {}

	@Override
	public String toString() {
		return "int";
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof GoIntType;
	}
	
	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}
	@Override
	public boolean is8Bits() {
		return false;
	}

	@Override
	public boolean is32Bits() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean is64its() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isUnsigned() {
		return false;
	}
}
