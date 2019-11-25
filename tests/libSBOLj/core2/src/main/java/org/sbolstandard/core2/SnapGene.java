package org.sbolstandard.core2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * Provides a Java API to a SnapGene Server instance.
 * @author Chris Myers
 *
 */
public class SnapGene {
	
	private static String SNAPGENE_POSTTEXTURL = "http://song.ece.utah.edu/examples/pages/acceptNewText.php";	
	private static String SNAPGENE_POSTFILEURL = "http://song.ece.utah.edu/examples/pages/acceptNewFile.php";
	private static String SNAPGENE_GETURLPREFIX = "http://song.ece.utah.edu/dnafiles/";

	static void read(SBOLDocument document, InputStream inputStream,
			String uriPrefix,String displayId,String version) throws SBOLConversionException {
		
		PoolingHttpClientConnectionManager connectionManager;
		HttpClient client;
		connectionManager = new PoolingHttpClientConnectionManager();
        client = HttpClients.custom().setConnectionManager(connectionManager).build();
        
		HttpPost request = new HttpPost(SNAPGENE_POSTFILEURL);
		request.setHeader("Accept", "text/plain");
	        
		MultipartEntityBuilder params = MultipartEntityBuilder.create();        

		params.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		try
		{
			String filename = RandomStringUtils.randomAlphanumeric(8);
			params.addTextBody("detectFeatures", "false");
	        params.addBinaryBody("fileToUpload", inputStream, ContentType.DEFAULT_BINARY, filename + ".dna");
			request.setEntity(params.build());
			HttpResponse response = client.execute(request);
			checkResponseCode(response);
			inputStream = fetchContentAsInputStream(client,SNAPGENE_GETURLPREFIX + filename + ".gb",null).inputStream;
			String oldUriPrefix = SBOLReader.getURIPrefix();
			String oldDisplayId = SBOLReader.getDisplayId();
			String oldVersion = SBOLReader.getVersion();
			SBOLReader.setURIPrefix(uriPrefix);
			SBOLReader.setDisplayId(displayId);
			SBOLReader.setVersion(version);
			SBOLDocument result = SBOLReader.read(inputStream);
			document.createCopy(result);
			// TODO: set name and perhaps description
			SBOLReader.setURIPrefix(oldUriPrefix);
			SBOLReader.setDisplayId(oldDisplayId);
			SBOLReader.setVersion(oldVersion);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new SBOLConversionException("SnapGene DNA file failed to convert to SBOL.");
		}
		finally
		{
			request.releaseConnection();
		}
	}

	static void write(SBOLDocument document, OutputStream out) throws SBOLConversionException, IOException {
		if (document.getRootComponentDefinitions().size()!=1) {
			throw new SBOLConversionException("Currently only SBOLDocuments with a single root ComponentDefinition can be converted to SnapGene DNA format.");
		}
		ComponentDefinition rootCD = document.getRootComponentDefinitions().iterator().next();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		document.write(baos,SBOLDocument.GENBANK);
		InputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
		HttpPost request = new HttpPost(SNAPGENE_POSTFILEURL);
		try
		{
			PoolingHttpClientConnectionManager connectionManager;
			HttpClient client;
			connectionManager = new PoolingHttpClientConnectionManager();
	        client = HttpClients.custom().setConnectionManager(connectionManager).build();
			request.setHeader("Accept", "text/plain");
			MultipartEntityBuilder params = MultipartEntityBuilder.create();        
			params.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			params.addTextBody("detectFeatures", "false");
	        params.addBinaryBody("fileToUpload", inputStream, ContentType.DEFAULT_BINARY, rootCD.getDisplayId()+".gb");
			request.setEntity(params.build());
			HttpResponse response = client.execute(request);
			checkResponseCode(response);
			fetchContentSaveToFile(client,null,SNAPGENE_GETURLPREFIX + rootCD.getDisplayId() + ".dna",out,null);
		}
		catch (Exception e)
		{
			throw new SBOLConversionException("ComponentDefinition " + rootCD.getIdentity() +
					" failed to convert to SnapGene DNA format.");
		}
		finally
		{
			request.releaseConnection();
		}
		return;
	}
	
	/**
	 * Detect features in a sequence using a SnapGene Server
	 * @param sequence The sequence to detect features on
	 * @param uriPrefix The uriPrefix for the generated SBOL object
	 * @param displayId The displayId for the generated SBOL object
	 * @param version The version for the generated SBOL object
	 * @param pngFileName The filename for the PNG file generated by the SnapGene Server
	 * @param name The name for the generated SBOL object
	 * @return An SBOLDocument for the annotated sequence
	 */
	public static SBOLDocument detectFeatures(String sequence,String uriPrefix,String displayId,String version,String pngFileName,String name) {
		return detectFeatures(sequence,uriPrefix,displayId,version,pngFileName,name,true);
	}
	
	/**
	 * Detect features in a sequence using a SnapGene Server
	 * @param sequence The sequence to detect features on
	 * @param uriPrefix The uriPrefix for the generated SBOL object
	 * @param displayId The displayId for the generated SBOL object
	 * @param version The version for the generated SBOL object
	 * @param pngFileName The filename for the PNG file generated by the SnapGene Server
	 * @param name The name for the generated SBOL object
	 * @param circular Set topology to be circular
	 * @return An SBOLDocument for the annotated sequence
	 */
	public static SBOLDocument detectFeatures(String sequence,String uriPrefix,String displayId,String version,String pngFileName,String name,boolean circular) {
		PoolingHttpClientConnectionManager connectionManager;
		HttpClient client;
		SBOLDocument result = null;
		connectionManager = new PoolingHttpClientConnectionManager();
        client = HttpClients.custom().setConnectionManager(connectionManager).build();
        
		HttpPost request = new HttpPost(SNAPGENE_POSTTEXTURL);
		request.setHeader("Accept", "text/plain");
	        
		MultipartEntityBuilder params = MultipartEntityBuilder.create();        

		params.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		try
		{
			params.addTextBody("textToUpload", sequence);
			params.addTextBody("textId", displayId);
			if (name!=null && !name.equals("")) {
				params.addTextBody("textName", name);
			}
			params.addTextBody("detectFeatures", "true");
			if (circular) {
				params.addTextBody("topology", "circular");
			} else {
				params.addTextBody("topology", "linear");
			}
			request.setEntity(params.build());
			HttpResponse response = client.execute(request);
			checkResponseCode(response);
			InputStream inputStream = fetchContentAsInputStream(client,SNAPGENE_GETURLPREFIX + displayId + ".gb",null).inputStream;
			String oldUriPrefix = SBOLReader.getURIPrefix();
			String oldDisplayId = SBOLReader.getDisplayId();
			String oldVersion = SBOLReader.getVersion();
			SBOLReader.setURIPrefix(uriPrefix);
			SBOLReader.setDisplayId(displayId);
			SBOLReader.setVersion(version);
			result = SBOLReader.read(inputStream);
			// TODO: set name and perhaps description
			SBOLReader.setURIPrefix(oldUriPrefix);
			SBOLReader.setDisplayId(oldDisplayId);
			SBOLReader.setVersion(oldVersion);
			if (pngFileName !=  null) {
				File pngFile = new File(pngFileName);
				OutputStream outStream = new FileOutputStream(pngFile);
				fetchContentSaveToFile(client,null,SNAPGENE_GETURLPREFIX + displayId + ".png",outStream,null);
			}
		}
		catch (Exception e)
		{
			//e.printStackTrace();
		}
		finally
		{
			request.releaseConnection();
		}
		return result;
	}
	
	private static String inputStreamToString(InputStream inputStream) throws IOException
	{
		StringWriter writer = new StringWriter();

		IOUtils.copy(inputStream, writer);

		return writer.toString();
	}

	static class HttpStream
	{
		public InputStream inputStream;
		public HttpRequestBase request;
	}

	private static HttpStream fetchContentAsInputStream(HttpClient client, String url, String user) throws Exception
	{
		HttpGet request = new HttpGet(url);
		if (user!=null) {
			request.setHeader("X-authorization", user);
		}
		request.setHeader("Accept", "text/plain");

		try
		{
			HttpResponse response = client.execute(request);

			checkResponseCode(response);

			HttpStream res = new HttpStream();

			res.inputStream = response.getEntity().getContent();
			res.request = request;

			return res;
		}
		catch(Exception e)
		{
			request.releaseConnection();

			throw e;
		}
	}

	private static String fetchContentSaveToFile(HttpClient client,String user,String url,OutputStream outputStream,String path) throws Exception
	{
		HttpGet request = new HttpGet(url);
		if (user!=null) {
			request.setHeader("X-authorization", user);
		}
		request.setHeader("Accept", "text/plain");

		try
		{
			HttpResponse response = client.execute(request);

			checkResponseCode(response);

			String filename = "default";
			if (response.getFirstHeader("Content-Disposition")!=null) {
				String dispositionValue = response.getFirstHeader("Content-Disposition").getValue();
				int index = dispositionValue.indexOf("filename=");
				if (index > 0) {
					filename = dispositionValue.substring(index + 10, dispositionValue.length() - 1);
				}
			}
			if (outputStream==null) {
				outputStream = new FileOutputStream(path+filename);
			}
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				entity.writeTo(outputStream);
			}
			return filename;
		}
		catch(Exception e)
		{
			request.releaseConnection();

			throw e;
		}
	}

	private static void checkResponseCode(HttpResponse response) throws Exception
	{
		int statusCode = response.getStatusLine().getStatusCode();

		if(statusCode >= 300)
		{
            switch(statusCode)
            {
            case 401:
                throw new Exception("Permission exception");
            
            case 404:
                throw new Exception("Not found exception");
            
            default:
            	HttpEntity entity = response.getEntity();
                try {
					throw new Exception(inputStreamToString(entity.getContent()));
				}
				catch (UnsupportedOperationException | IOException e) {
					throw new Exception(statusCode+"");
				}
            }
        }
    }
}
