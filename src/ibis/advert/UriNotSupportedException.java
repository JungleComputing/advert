package ibis.advert;

/**
 * This exception is thrown when a URI is invalid.
 * 
 * @author bbn230
 * 
 */

@SuppressWarnings("serial")
public class UriNotSupportedException extends Exception {
	public UriNotSupportedException() {
		super();
	}
	
	public UriNotSupportedException(String message) {
		super(message);
	}
}
