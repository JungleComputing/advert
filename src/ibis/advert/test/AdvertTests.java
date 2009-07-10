/*
 * Created 02 Apr. 2009 by Bas Boterman.
 */

package ibis.advert.test;

import ibis.advert.Advert;
import ibis.advert.MetaData;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to test the various functions present at the Advert server. Note that
 * the constant variables still need to be adapted to the new locations and 
 * credentials. 
 * 
 * @author Bas
 */

public class AdvertTests {

	private static final String SERVER = "google://jondoe.appspot.com/";
	private static final String USER   = "jondoe@gmail.com";

	final static Logger logger = LoggerFactory.getLogger(AdvertTests.class);
	
	/***
	 * Tests delete() function.
	 * 
	 * @param advert
	 * 		Advert server to connect to.
	 * @param path
	 * 		Pathname of object to be deleted.
	 * @throws Exception
	 * 		Something went wrong.
	 */
	private static void testDelete(Advert advert, String path) throws Exception {
		logger.info("Calling advert.delete()");
		advert.delete(path);
	}
	
	/***
	 * Tests find() function.
	 * 
	 * @param advert
	 * 		Advert server to connect to.
	 * @throws Exception
	 * 		Something went wrong.
	 */
	private static void testFind(Advert advert) throws Exception {
		MetaData metaData = new MetaData();
		String[] result   = null;
		
		metaData.put("key1", "value1");
		metaData.put("key3", "value3");
		
		logger.info("Calling advert.find()");
		result = advert.find(metaData);
		
		for (int i = 0; i < result.length; i++) {
			logger.debug("Find result: {}", result[i]);
		}
	}
	
	/***
	 * Tests getMD() function.
	 * 
	 * @param advert
	 * 		Advert server to connect to.
	 * @param path
	 * 		Pathname of meta data to be fetched.
	 * @throws Exception
	 * 		Something went wrong.
	 */
	private static void testGetMD(Advert advert, String path) throws Exception {
		MetaData metaData = null;
		
		logger.info("Calling advert.getMetaData()");
		metaData = advert.getMetaData(path);
		
		Iterator<String> itr  = metaData.getAllKeys().iterator();
		
		while (itr.hasNext()) {
			String key   = itr.next();
			String value = metaData.get(key);

			if (key == null) {
				continue; //key can't be null (value can)
			}
			
			logger.debug("GetMD result: {} - {}", key, value);
		}
	}
	
	/***
	 * Tests get() function.
	 * 
	 * @param advert
	 * 		Advert server to connect to.
	 * @param path
	 * 		Pathname of object to be retrieved.
	 * @throws Exception
	 * 		Something went wrong.
	 */
	private static void testGet(Advert advert, String path) throws Exception {
		byte[] b = null;
		
		logger.info("Calling advert.get()");
		b = advert.get(path);
		
		logger.debug("Get result: {}", b.toString());
	}
	
	/***
	 * Tests add() function.
	 * 
	 * @param advert
	 * 		Advert server to connect to.
	 * @param path
	 * 		Pathname where the object should be added.
	 * @param filename
	 * 		Filename of object to add to the datastore.
	 * @throws Exception
	 * 		Something went wrong.
	 */
	private static void testAdd(Advert advert, String path, String filename) throws Exception {
		MetaData metaData = new MetaData();
		File file = new File(filename);;

		byte[] b = new byte[(int) file.length()];
	    int size = 0;
	    DataInputStream is = null;
	    
	    try {
	    	 is = new DataInputStream(new FileInputStream(file));
	    }
	    catch (FileNotFoundException nfe) {
	    	logger.error("File not found: {}", nfe);
	    }
	    
	    try {
	    	size = is.read(b);
	        logger.debug("Bytes read: {}", size);
		} 
	    catch (EOFException eof) {
			logger.error("EOF reached."); 
		}
		catch (IOException ioe) {
			logger.error("IO error: {}", ioe);
		}
		
		metaData.put("key1", "value1");
		metaData.put("key2", "value2");
		metaData.put("key3", "value3");
		
		logger.info("Calling advert.add()");
		advert.add(b, metaData, path);
	}
	
	/**
	 * Main function for starting AdvertTests. 
	 * 
	 * @param argv
	 * 		Usage: AdvertTests passwd pathname filename
	 * @throws Exception
	 * 		Something went wrong.
	 */
	public static void main(String argv[]) throws Exception {
		logger.info("Starting test class...");
		Advert advert   = null;
		String path     = null;
		String filename = null;
		
		if (argv.length < 2) {
			logger.error("***Usage: AdvertTests (passwd) <pathname> <filename>");
			logger.error("\tIf password is present, authenticated server will be used");
		}
		else {
			/* Create a new Advert object. */
			try {
				URI advertUri = new URI(SERVER);
				if (argv.length == 2) {
					advert = new Advert(advertUri); /* Public server */
				}
				else {
					advert = new Advert(advertUri, USER, argv[0]); /* Private */
				}
				logger.info("Advert object created.");
			}
			catch (Exception e) {
				System.err.println("Malformed URI");
				System.exit(-1);
			}
			
			path     = argv[1];
			filename = argv[2];
			logger.debug("Path: {}", path);
			logger.debug("File: {}", filename);
		}

		/* add() */
		logger.info("Testing add()...");
		testAdd(advert, path, filename);
		
		/* get() */
		logger.info("Testing get()...");
		testGet(advert, path);
		
		/* getMetaData() */
		logger.info("Testing getMetaData()...");
		testGetMD(advert, path);
		
		/* find() */
		logger.info("Testing find()...");
		testFind(advert);
		
		/* delete() */
		logger.info("Testing delete()...");
		testDelete(advert, path);
	}
}
