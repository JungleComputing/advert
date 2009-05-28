package ibis.advert;

/**
 * This exception is thrown when a given path name does not meet our
 * specifications.
 * 
 * @author bbn230
 * 
 */

@SuppressWarnings("serial")
public class IllegalPathException extends Exception {
	public IllegalPathException() {
		super();
	}
	
	public IllegalPathException(String message) {
		super(message);
	}
}
