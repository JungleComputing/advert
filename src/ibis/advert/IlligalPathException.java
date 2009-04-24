package ibis.advert;

/**
 * This exception is thrown when a given path name does not meet our
 * specifications.
 * 
 * @author bbn230
 * 
 */

@SuppressWarnings("serial")
public class IlligalPathException extends Exception {
	public IlligalPathException() {
		super();
	}
	
	public IlligalPathException(String message) {
		super(message);
	}
}
