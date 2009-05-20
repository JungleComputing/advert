package ibis.advert;

/**
 * This exception is thrown when a URI scheme is not supported.
 * 
 * @author bbn230
 * 
 */

@SuppressWarnings("serial")
public class SchemeNotSupportedException extends Exception {
	public SchemeNotSupportedException() {
		super();
	}
	
	public SchemeNotSupportedException(String message) {
		super(message);
	}
}
