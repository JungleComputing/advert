/*
 * Created 02 Feb. 2009 by Bas Boterman.
 */

package ibis.advert;

/**
 * This class contains the main functionality for using the service which runs
 * at the Google App Engine.
 * 
 * @author bbn230
 */

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.util.Iterator;
import java.util.NoSuchElementException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Advert {
	
	final static Logger logger = LoggerFactory.getLogger(Advert.class);
	
	private Communication comm = null;

	/**
	 * Constructor for a new Advert client, which dynamically decides to
	 * connects to a private or public (i.e. a server with or without
	 * authentication, respectively) Advert server. This choice is made
	 * depending on <code>userinfo</code> being present in the server
	 * {@link URI}. Note that connecting to a public server does not guarantee
	 * the server being alive/existing. The client will notice this once any of
	 * the functions below are called. This approach saves startup time.
	 * 
	 * @param server
	 * 		{@link URI} containing the location of Advert server to connect to.
	 * @throws AuthenticationException 
	 * 		Authentication to Advert server failed.
	 * @throws IOException
	 * 		I/O to Advert server failed.
	 * @throws ProtocolException 
	 * 		Wrong protocol applied.
	 * @throws MalformedURLException
	 * 		URL was malformed. 
	 * @throws UriNotSupportedException
	 * 		The server {@link URI} given does not adhere to:
	 * 		<code>google://hostname/</code> or <code>any://hostname/</code>
	 */
	public Advert(URI server) 
			throws MalformedURLException, ProtocolException, IOException,
			AuthenticationException, UriNotSupportedException {
		if (server.getScheme() == null || !(server.getScheme().equals("any") || 
				server.getScheme().equals("google"))) {
			throw new UriNotSupportedException(
					"Scheme not supported, should be google:// or any://");
		}
		if (server.getHost() == null) {
			throw new UriNotSupportedException("Hostname can't be null");
		}
		if (server.getUserInfo() != null) { //user info found
			//Parse user info
			String[] userpass = server.getUserInfo().split(":");
			if (userpass != null && userpass.length == 2) {
				comm = new Communication(server.getHost(), userpass[0], userpass[1]);
			}
		}
		logger.debug("Connecting to public server at: {}", server.getHost());
		comm = new Communication(server.getHost());
		logger.info("Communications class created.");
	}
	
	/**
	 * Constructor for a new Advert client, which connects to a private (i.e. a
	 * server with authentication) Advert server.
	 * 
	 * @param server
	 * 		{@link URI} containing the location of Advert server to connect to.
	 * @param user
	 * 		{@link String} containing the Google Account to be authenticated with.
	 * @param passwd
	 * 		{@link String} containing the corresponding password.
	 * @throws AuthenticationException 
	 * 		Authentication to Advert server failed.
	 * @throws IOException
	 * 		I/O to Advert server failed.
	 * @throws ProtocolException 
	 * 		Wrong protocol applied.
	 * @throws MalformedURLException
	 * 		URL was malformed. 
	 * @throws UriNotSupportedException
	 * 		The server {@link URI} given does not adhere to:
	 * 		<code>google://hostname/</code> or <code>any://hostname/</code>
	 */
	public Advert(URI server, String user, String passwd)
			throws MalformedURLException, ProtocolException, IOException,
			AuthenticationException, UriNotSupportedException {
		if (server.getScheme() == null || !(server.getScheme().equals("any") || 
				server.getScheme().equals("google"))) {
			throw new UriNotSupportedException(
					"Scheme not supported, should be google:// or any://");
		}
		if (server.getHost() == null) {
			throw new UriNotSupportedException("Hostname can't be null");
		}
		logger.debug("Connecting to private server at: {}", server.getHost());
		logger.debug("User/Pass found: {}/********", user);
		comm = new Communication(server.getHost(), user, passwd);
		logger.info("Communications class created.");
	}
	
	/**
	 * Add a {@link byte[]} to the App Engine, at an absolute path, with
	 * {@link MetaData} included, to the datastore. If an entry exists at the
	 * specified path, that entry gets overwritten, and a warning is issued.
	 * 
	 * @param bytes
	 *            {@link byte[]} to be stored.
	 * @param metaData
	 *            {@link MetaData} to be associated with the passed bytes.
	 * @param path
	 *            {@link String} containing the absolute path of the new entry.
	 * @throws AppEngineResourcesException
	 *             This exception is thrown when the App Engine runs out of
	 *             resources.
	 */
	public void add(byte[] object, MetaData metaData, String path) 
	  throws MalformedURLException, IOException, AuthenticationException,
	  AppEngineResourcesException, NoSuchElementException, 
	  RequestTooLargeException, Exception {
		if (path == null || path.equals("")) {
			logger.warn("Throwing NullPointerException");
			throw new NullPointerException("Path can't be null.");
		}
		if (path.endsWith("/")) {
			logger.warn("Throwing IlligalPathException");
			throw new IllegalPathException("Path cannot be a directory.");
		}
		
		JSONArray  jsonarr = new JSONArray();
		JSONObject jsonobj = null;
		
		if (metaData != null) {
			jsonobj = new JSONObject();
			
			Iterator<String> itr  = metaData.getAllKeys().iterator();
		
			while (itr.hasNext()) {
				String key   = itr.next();
				String value = metaData.get(key);
	
				if (key == null) {
					continue; //key can't be null (value can)
				}
				
				jsonobj.put(key, value);
			}
		}
		
		String base64 = null;
		if (object != null) {
			base64 = new String(new Base64().encode(object)); 
		}
		
		jsonarr.add(path);
		jsonarr.add(jsonobj);
		jsonarr.add(base64);
		
		logger.info("Calling httpSend() /add...");
		comm.httpSend("/add", jsonarr.toString());
	}
	

	/**
	 * Remove an instance and related {@link MetaData} from the datastore at an
	 * absolute path.
	 * 
	 * @param path
	 *            {@link String} containing the path (an absolute entry to be deleted).
	 * @throws NoSuchElementException
	 *             The path is incorrect.
	 */
	public void delete(String path) 
	  throws MalformedURLException, IOException, AuthenticationException,
	  AppEngineResourcesException, NoSuchElementException, 
	  RequestTooLargeException, Exception {
		if (path == null || path.equals("")) {
			logger.warn("Throwing NullPointerException");
			throw new NullPointerException("Path can't be null.");
		}
		if (path.endsWith("/")) {
			logger.warn("Throwing IlligalPathException");
			throw new IllegalPathException("Path cannot be a directory.");
		}
		
		logger.info("Calling httpSend() /del...");
		comm.httpSend("/del", path);
	}

	/**
	 * Gets an instance from the datastore at a given (absolute) path.
	 * 
	 * @param path
	 *            {@link String} containing the absolute path of the entry.
	 * @return The instance at the given path.
	 * @throws NoSuchElementException
	 *             The path is incorrect.
	 */
	public byte[] get(String path) 
	  throws MalformedURLException, IOException, AuthenticationException,
	  AppEngineResourcesException, NoSuchElementException, 
	  RequestTooLargeException,Exception {
		if (path == null || path.equals("")) {
			logger.warn("Throwing NullPointerException");
			throw new NullPointerException("Path can't be null.");
		}
		if (path.endsWith("/")) {
			logger.warn("Throwing IlligalPathException");
			throw new IllegalPathException("Path cannot be a directory.");
		}
		
		logger.info("Calling httpSend() /get...");
		String base64 = comm.httpSend("/get", path);
		
		return new Base64().decode(base64.getBytes());
	}

	/**
	 * Gets the {@link MetaData} of an instance from the given (absolute) path.
	 * 
	 * @param path
	 *            {@link String} containing the absolute path of the entry.
	 * @return A {@link MetaData} object containing the meta data.
	 * @throws NoSuchElementException
	 *             The path is incorrect.
	 */
	public MetaData getMetaData(String path) 
	  throws MalformedURLException, IOException, AuthenticationException,
	  AppEngineResourcesException, NoSuchElementException, 
	  RequestTooLargeException, Exception {
		if (path == null || path.equals("")) {
			logger.warn("Throwing NullPointerException");
			throw new NullPointerException("Path can't be null.");
		}
		if (path.endsWith("/")) {
			logger.warn("Throwing IlligalPathException");
			throw new IllegalPathException("Path cannot be a directory.");
		}
		
		MetaData   metadata = new MetaData();
		
		logger.info("Calling httpSend() /getmd...");
		String result = comm.httpSend("/getmd", path);
		
		JSONObject jsonobj = JSONObject.fromObject(result);
		Iterator<?> itr    = jsonobj.keys();
		
		while (itr.hasNext()) {
			String key   = (String)itr.next();
			String value = (String)jsonobj.get(key);

			if (key == null) {
				continue; //key can't be null (value can)
			}
			
			metadata.put(key,value);
		}

		return metadata;
	}

	/**
	 * Query the App Engine for entries matching the specified set of
	 * {@link MetaData}.
	 * 
	 * @param metaData
	 *            {@link MetaData} describing the entries to be searched for.
	 *            No wildcards allowed.
	 * @return a {@link String}[] of absolute paths, each pointing to a
	 *         matching entry. If no matches are found, null is returned.
	 */
	public String[] find(MetaData metaData)
	  throws MalformedURLException, IOException, AuthenticationException,
	  AppEngineResourcesException, NoSuchElementException, 
	  RequestTooLargeException, Exception {
		if (metaData == null) {
			logger.warn("Throwing NullPointerException");
			throw new NullPointerException("MetaData can't be null.");
		}
		
		JSONObject metadata = new JSONObject();
		JSONArray  jsonarr  = new JSONArray();
		
		Iterator<String> itr  = metaData.getAllKeys().iterator();
		
		while (itr.hasNext()) {
			String key   = itr.next();
			String value = metaData.get(key);

			if (key == null) {
				continue; //key can't be null (value can)
			}
			
			metadata.put(key, value);
		}
		
		logger.info("Calling httpSend() /find...");
		String result = comm.httpSend("/find", metadata.toString());
		
		jsonarr = JSONArray.fromObject(result);
		
		try {
			return (String[]) jsonarr.toArray(new String[0]);
		}
		catch (ArrayStoreException ase) {
			logger.warn("Converting JSONArray to String[] failed.");
			String[] ret = new String[jsonarr.size()];
			
			for (int i = 0; i < jsonarr.size(); i++) {
				if (jsonarr.getString(i) != null) {
					ret[i] = jsonarr.getString(i);
				}
			}
			
			return ret;
		}
	}
}
