/*
 * Created 24 Mar. 2009 by Bas Boterman.
 */

package ibis.advert;

/**
 * This exception is thrown when Authentication to App Engine fails.
 * 
 * @author bbn230
 * 
 */

@SuppressWarnings("serial")
public class AuthenticationException extends Exception {
	public AuthenticationException() {
		super();
	}
	
	public AuthenticationException(String message) {
		super(message);
	}
}
