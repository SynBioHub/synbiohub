package org.synbiohub.frontend;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.TopLevel;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Provides a Java API to SynBioHub instances.
 * @author James McLaughlin
 * @author Chris Myers
 *
 */
public class SynBioHubFrontend
{
    PoolingHttpClientConnectionManager connectionManager;
    HttpClient client;
    String backendUrl;
    String uriPrefix;
    String user = "";
    String username = null;

    /**
     * Creates an instance of the SynBioHub API.
     * @param backendUrl - URL for the SynBioHub instance.
     * @param uriPrefix - prefix for all URIs stored in this repository
     */
    public SynBioHubFrontend(String backendUrl, String uriPrefix)
    {
        this.backendUrl = backendUrl;
        this.uriPrefix = uriPrefix;

        connectionManager = new PoolingHttpClientConnectionManager();
        client = HttpClients.custom().setConnectionManager(connectionManager).build();
    }

    /**
     * Creates an instance of the SynBioHub API.
     * @param backendUrl - URL for the SynBioHub instance.
     */
    public SynBioHubFrontend(String backendUrl)
    {
        this.backendUrl = backendUrl;
        this.uriPrefix = backendUrl;

        connectionManager = new PoolingHttpClientConnectionManager();
        client = HttpClients.custom().setConnectionManager(connectionManager).build();
    }
    
    /**
     * Creates an instance of the SynBioHub API.
     * @param backendUrl - URL for the SynBioHub instance.
     * @param timeout - timeout for connections in seconds
     */
    public SynBioHubFrontend(String backendUrl,int timeout)
    {
        this.backendUrl = backendUrl;
        this.uriPrefix = backendUrl;

        connectionManager = new PoolingHttpClientConnectionManager();
//        client = HttpClients.custom().setConnectionManager(connectionManager).build();
    	RequestConfig config = RequestConfig.custom()
    			.setConnectTimeout(timeout * 1000)
    			.setConnectionRequestTimeout(timeout * 1000)
    			.setSocketTimeout(timeout * 1000).build();
    	client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
    }

    /**
     * Creates an instance of the SynBioHub API.
     * @param backendUrl - URL for the SynBioHub instance.
     * @param uriPrefix - prefix for all URIs stored in this repository
     * @param timeout - timeout for connections in seconds
     */
    public SynBioHubFrontend(String backendUrl, String uriPrefix, int timeout)
    {
        this.backendUrl = backendUrl;
        this.uriPrefix = uriPrefix;

        connectionManager = new PoolingHttpClientConnectionManager();

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build();
        client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
    }


    /**
     * Returns the URL for the SynBioHub instance.
     * @return the URL for the SynBioHub instance.
     */
    public String getBackendUrl()
    {
        return this.backendUrl;
    }

    /**
     * Return the total number of objects of a specified type in the repository.
     *
     * @return the total number of objects of a specified type in the repository.
     *
     * @param objectType The object type to count 
     * (Collection, ComponentDefinition, Sequence, ModuleDefinition, Model, etc.).
     *
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     */ 
    public int getCount(String objectType) throws SynBioHubException
    {
        return fetchCount(backendUrl + "/" + objectType + "/count");
    }
    
    /**
     * Retrieve SBOL TopLevel object from a SynBioHub instance using its URI.
     *
     * @param topLevelUri The URI of the SBOL TopLevel
     *
     * @return A libSBOLj TopLevel instance corresponding to the TopLevel
     *
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     */
    public SBOLDocument getSBOL(URI topLevelUri) throws SynBioHubException
    {
         return getSBOL(topLevelUri,true);
    }
    
    /**
     * Retrieve SBOL TopLevel object from a SynBioHub instance using its URI.
     *
     * @param topLevelUri The URI of the SBOL TopLevel
     * @param recursive indicates if the complete SBOL document should be fetched recursively
     *
     * @return A libSBOLj TopLevel instance corresponding to the TopLevel
     *
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     */
    public SBOLDocument getSBOL(URI topLevelUri,boolean recursive) throws SynBioHubException
    {
    	if (topLevelUri==null) return null;
        if (!topLevelUri.toString().startsWith(uriPrefix)) {
        	throw new SynBioHubException("Object URI does not start with correct URI prefix for this repository.");
        }
        String url = topLevelUri + "/sbol";
        if (!recursive) {
        	url = topLevelUri + "/sbolnr";
        }
        url = url.replace(uriPrefix, backendUrl);

        SBOLDocument document = fetchFromSynBioHub(url);

        return document;
    }
    
    /**
     * Retrieve an attachment from a SynBioHub instance using its URI,
     * and save to the path provided.
     *
     * @param attachmentUri The URI of the SBOL Attachment object
     * @param path The path to store the downloaded attachment
     * @return the name of the file being downloaded
     *
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     * @throws IOException if there is an I/O error
     */
    public String getAttachment(URI attachmentUri, String path) throws SynBioHubException, IOException
    {
        if (!attachmentUri.toString().startsWith(uriPrefix)) {
        	throw new SynBioHubException("Object URI does not start with correct URI prefix for this repository.");
        }
        String url = attachmentUri + "/download";
        url = url.replace(uriPrefix, backendUrl);

        return fetchContentSaveToFile(url,null,path);
    }
    
    /**
     * Retrieve an attachment from a SynBioHub instance using its URI,
     * and save into the provided output stream.
     *
     * @param attachmentUri The URI of the SBOL Attachment object
     * @param outputStream The output stream to store the downloaded attachment
     * @return the name of the file being downloaded
     *
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     * @throws IOException if there is an I/O error
     */
    public String getAttachment(URI attachmentUri, OutputStream outputStream) throws SynBioHubException, IOException
    {
        if (!attachmentUri.toString().startsWith(uriPrefix)) {
        	throw new SynBioHubException("Object URI does not start with correct URI prefix for this repository.");
        }
        String url = attachmentUri + "/download";
        url = url.replace(uriPrefix, backendUrl);

        return fetchContentSaveToFile(url,outputStream,null);
    }
    
    /**
     * Remove SBOL TopLevel object from a SynBioHub instance using its URI.
     *
     * @param topLevelUri The URI of the SBOL TopLevel
     *
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     */
    public void removeSBOL(URI topLevelUri) throws SynBioHubException
    {
        if (!topLevelUri.toString().startsWith(uriPrefix)) {
        	throw new SynBioHubException("Object URI does not start with correct URI prefix for this repository.");
        }
        String url = topLevelUri + "/remove";
        url = url.replace(uriPrefix, backendUrl);

        removeFromSynBioHub(url);
    }
    
    /**
     * Remove SBOL TopLevel object from a SynBioHub instance using its URI,
     * but leave references to this object, since it is going to be replaced.
     *
     * @param topLevelUri The URI of the SBOL TopLevel
     *
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     */
    public void replaceSBOL(URI topLevelUri) throws SynBioHubException
    {
        if (!topLevelUri.toString().startsWith(uriPrefix)) {
        	throw new SynBioHubException("Object URI does not start with correct URI prefix for this repository.");
        }
        String url = topLevelUri + "/replace";
        url = url.replace(uriPrefix, backendUrl);

        removeFromSynBioHub(url);
    }

   /**
     * Search the default store for ComponentDefinition instances matching a name and/or a set of roles
     *
     * @param name The dcterms:title to search for, or null
     * @param roles A set of role URIs to search for, or null
     * @param types A set of type URIs to search for, or null
     * @param collections A set of Collection URIs to search for, or null
     * @param offset The offset of the results to begin at, or null to begin at 0
     * @param limit The maximum number of results to return, or null to return all results
     *
     * @return An ArrayList of ComponentDefinitionMetaData objects with a summary of all matching ComponentDefinitions.
     *
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     */
    public ArrayList<IdentifiedMetadata> getMatchingComponentDefinitionMetadata(String name, Set<URI> roles, 
    		Set<URI> types, Set<URI> collections, Integer offset, Integer limit)
            throws SynBioHubException
    {
    	SearchQuery query = new SearchQuery();

    	query.setOffset(offset);
    	query.setLimit(limit);

    	SearchCriteria objectCriteria = new SearchCriteria();
    	objectCriteria.setKey("objectType");
    	objectCriteria.setValue("ComponentDefinition");
    	query.addCriteria(objectCriteria);
    	if (roles != null) {
    		for(URI uri : roles)
    		{
    			SearchCriteria roleCriteria = new SearchCriteria();

    			roleCriteria.setKey("role");
    			roleCriteria.setValue(uri.toString());

    			query.getCriteria().add(roleCriteria);
        	}	
        }
        
        if (types != null) {
        	for(URI uri : types)
        	{
        		SearchCriteria typeCriteria = new SearchCriteria();

        		typeCriteria.setKey("type");
        		typeCriteria.setValue(uri.toString());

        		query.getCriteria().add(typeCriteria);
        	}
        }
        
        if (collections != null) {
        	for(URI uri : collections)
        	{
        		SearchCriteria collectionCriteria = new SearchCriteria();

        		collectionCriteria.setKey("collection");
        		collectionCriteria.setValue(uri.toString());

        		query.getCriteria().add(collectionCriteria);
        	}
        }

        if(name != null)
        {
            SearchCriteria nameCriteria = new SearchCriteria();

            nameCriteria.setKey("name");
            nameCriteria.setValue(name);

            query.getCriteria().add(nameCriteria);
        }
    	return search(query);
    }

    /**
     * Search this SynBioHub instance for objects matching a search query
     * 
     * @param query the search query
     *
     * @return An ArrayList of MetaData for objects that match the specified search query
     *
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     */
    public ArrayList<IdentifiedMetadata> search(SearchQuery query) throws SynBioHubException
    {
        String url = backendUrl + "/search/";

        //query.offset = offset;
        //query.limit = limit;

        String textQuery = "";
        boolean first = true;
        for (SearchCriteria criteria : query.getCriteria()) {
        	if (criteria.getKey().equals("objectType")) {
        		url += encodeUri(criteria.getKey()+"="+criteria.getValue()+"&");
        		continue;
        	}
        	if (criteria.getKey().equals("name")) {
        		if (first) first = false;
        		else textQuery = " ";
        		textQuery = criteria.getValue();
        		continue;
        	} 
        	if (criteria.getKey().startsWith("http")) {
        		url += encodeUri("<" + criteria.getKey() + ">=");
        	} else {
        		url += encodeUri(criteria.getKey()+"=");
        	}
        	if (criteria.getValue().startsWith("http")) {
        		url += encodeUri("<"+criteria.getValue()+">&");
        	} else {
        		url += encodeUri("'"+criteria.getValue()+"'&");
        	}
        }
        url += encodeUri(textQuery);
        if (query.getOffset()!=null && query.getLimit()!=null) {
        	url += "/?offset="+query.getOffset() + "&" + "limit="+query.getLimit();
        } else if (query.getOffset()!=null) {
        	url += "/?offset="+query.getOffset();
        } else if (query.getLimit()!=null) {
        	url += "/?limit="+query.getLimit();
        }

       	//System.out.println(url);
        Gson gson = new Gson();

        HttpGet request = new HttpGet(url);
        request.setHeader("X-authorization", user);
        request.setHeader("Accept", "text/plain");

        try
        {
            HttpResponse response = client.execute(request);

            checkResponseCode(response);

            InputStream inputStream = response.getEntity().getContent();

            ArrayList<IdentifiedMetadata> metadataList = gson.fromJson(
            		new InputStreamReader(inputStream),
            			new TypeToken<ArrayList<IdentifiedMetadata>>(){}.getType());
            
            return metadataList;
        }
        catch (Exception e)
        {
            throw new SynBioHubException(e);
        }
        finally
        {
            request.releaseConnection();
        }
    }
    
    /**
     * Search the default store for Collections that are not members of any other Collections
     *
     * @return An ArrayList of CollectionMetaData objects with a summary of all matching Collections.
     *
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     */    
    public ArrayList<IdentifiedMetadata> getRootCollectionMetadata()
            throws SynBioHubException
    {
        String url = backendUrl + "/rootCollections";

        Gson gson = new Gson();

        HttpGet request = new HttpGet(url);
        request.setHeader("X-authorization", user);
        request.setHeader("Accept", "text/plain");

        try
        {
            HttpResponse response = client.execute(request);

            checkResponseCode(response);

            InputStream inputStream = response.getEntity().getContent();

            ArrayList<IdentifiedMetadata> metadataList = gson.fromJson(
            		new InputStreamReader(inputStream),
            			new TypeToken<ArrayList<IdentifiedMetadata>>(){}.getType());
            
            return metadataList;
        }
        catch (Exception e)
        {
            throw new SynBioHubException(e);
        }
        finally
        {
            request.releaseConnection();
        }
    }
    
    /**
     * Fetch data about all registries in the web of registries.
     *
     * @return An ArrayList of WebOfRegistriesData describing each registry in the web of registries.
     *
     * @throws SynBioHubException if there was an error communicating with the WebOfRegistries
     */    
    public static ArrayList<WebOfRegistriesData> getRegistries() throws SynBioHubException
    {
    	return getRegistries("https://wor.synbiohub.org");
    }
    
    /**
     * Fetch data about all registries in the web of registries.
     * @param webOfRegistriesUrl The URL for the web-of-registries.
     *
     * @return An ArrayList of WebOfRegistriesData describing each registry in the web of registries.
     *
     * @throws SynBioHubException if there was an error communicating with the web-of-registries
     */    
    public static ArrayList<WebOfRegistriesData> getRegistries(String webOfRegistriesUrl) throws SynBioHubException
    {
        PoolingHttpClientConnectionManager connectionManager;
        HttpClient client;
        
        connectionManager = new PoolingHttpClientConnectionManager();
        client = HttpClients.custom().setConnectionManager(connectionManager).build();
        
        String url = webOfRegistriesUrl + "/instances/";

        Gson gson = new Gson();

        HttpGet request = new HttpGet(url);
        request.setHeader("Accept", "text/plain");

        try
        {
            HttpResponse response = client.execute(request);

            checkResponseCode(response);

            InputStream inputStream = response.getEntity().getContent();

            ArrayList<WebOfRegistriesData> metadataList = gson.fromJson(
            		new InputStreamReader(inputStream),
            			new TypeToken<ArrayList<WebOfRegistriesData>>(){}.getType());

            return metadataList;
         }
        catch (Exception e)
        {
            throw new SynBioHubException(e);
        }
        finally
        {
            request.releaseConnection();
        }
    }
    
    /**
     * Perform a SPARQL query
     * @param query SPARQL query string
     *
     * @return result as a JSON string
     *
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     */    
    public String sparqlQuery(String query) throws SynBioHubException
    {
        String url = backendUrl + "/sparql";

        url	+= "?query="+encodeUri(query);
        
        HttpGet request = new HttpGet(url);
        request.setHeader("X-authorization", user);
        request.setHeader("Accept", "application/json");

        try
        {
            HttpResponse response = client.execute(request);

            checkResponseCode(response);

            InputStream inputStream = response.getEntity().getContent();
            
            String result = inputStreamToString(inputStream);

            return result;
        }
        catch (Exception e)
        {
            throw new SynBioHubException(e);
        }
        finally
        {
            request.releaseConnection();
        }
    }
    
    /**
     * Perform a SPARQL admin query, must be logged in as an administrator
     * @param query SPARQL query string
     *
     * @return result as a JSON string
     *
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     */    
    public String sparqlAdminQuery(String query) throws SynBioHubException
    {
        String url = backendUrl + "/admin/sparql";

        url	+= "?query="+encodeUri(query);
        
        HttpGet request = new HttpGet(url);
        request.setHeader("X-authorization", user);
        request.setHeader("Accept", "application/json");

        try
        {
            HttpResponse response = client.execute(request);

            checkResponseCode(response);

            InputStream inputStream = response.getEntity().getContent();
            
            String result = inputStreamToString(inputStream);

            return result;
        }
        catch (Exception e)
        {
            throw new SynBioHubException(e);
        }
        finally
        {
            request.releaseConnection();
        }
    }
    
    /**
     * Search the default store for Collections that are members of the specified Collection
     *
     * @param parentCollectionUri URI for Collection to search for member Collections 
     * @return An ArrayList of CollectionMetaData objects with a summary of all matching Collections.
     *
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     */    
    public ArrayList<IdentifiedMetadata> getSubCollectionMetadata(URI parentCollectionUri)
            throws SynBioHubException
    {
        if (!parentCollectionUri.toString().startsWith(uriPrefix)) {
        	throw new SynBioHubException("Object URI does not start with correct URI prefix for this repository.");
        }
        String url = parentCollectionUri + "/subCollections";
        url = url.replace(uriPrefix, backendUrl);

        Gson gson = new Gson();
        HttpGet request = new HttpGet(url);
        request.setHeader("X-authorization", user);
        request.setHeader("Accept", "text/plain");

        try
        {
            HttpResponse response = client.execute(request);

            checkResponseCode(response);

            InputStream inputStream = response.getEntity().getContent();

            ArrayList<IdentifiedMetadata> metadataList = gson.fromJson(
            		new InputStreamReader(inputStream),
            			new TypeToken<ArrayList<IdentifiedMetadata>>(){}.getType());
            
            return metadataList;
        }
        catch (Exception e)
        {
            throw new SynBioHubException(e);
        }
        finally
        {
            request.releaseConnection();
        }
    }

//    /**
//     * Upload an SBOLDocument to the SynBioHub.
//     * 
//     * @param document The document to upload
//     *
//     * @throws SynBioHubException if there was an error communicating with the SynBioHub
//     */
//    public void upload(SBOLDocument document) throws SynBioHubException
//    {
//        String url = backendUrl;
//
//        HttpPost request = new HttpPost(url);
//                
//        try
//        {
//            request.setEntity(new StringEntity(serializeDocument(document)));
//            request.setHeader("Content-Type", "application/rdf+xml");
//            
//            HttpResponse response = client.execute(request);
//            
//            checkResponseCode(response);
//        }
//        catch (Exception e)
//        {
//            throw new SynBioHubException(e);
//        }
//        finally
//        {
//            request.releaseConnection();
//        }
//    }
    
    /**
	 * Sets the user to null to indicate that no user is logged in.
     */
    public void logout() 
    {
    	user = "";
    	username = null;
    }
    
    /**
     * Returns if a user is logged in
     * 
     * @return true if a user is logged in
     */
    public boolean isSetUsername()
    {
    	return (username!=null);
    }
    
    /**
     * Returns the username of the logged in user
     * 
     * @return the username of the logged in user
     */
    public String getUsername()
    {
    	return username;
    }

    /**
     * Login to the SynBioHub.
     * @param email The user's email
     * @param password The user's password
     * 
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     */
    public void login(String email, String password) throws SynBioHubException
    {    	
        String url = backendUrl + "/login";

        HttpPost request = new HttpPost(url);
        request.setHeader("Accept", "text/plain");

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("password", password));
                
        try
        {
            request.setEntity(new UrlEncodedFormEntity(params));
            request.setHeader("Content-Type", "application/x-www-form-urlencoded");
             
            HttpResponse response = client.execute(request);
            checkResponseCode(response);

            HttpEntity entity = response.getEntity();
            user = inputStreamToString(entity.getContent());
            username = email;
        }
        catch (Exception e)
        {
            throw new SynBioHubException(e);
            
        }
        finally
        {
            request.releaseConnection();
        }
    } 
    
	/**
	 * Remove all parts from this registry from a given SBOL document
	 * 
	 * @param document The document to remove all registry parts from
	 */
	public void removeRegistryParts(SBOLDocument document) {
		for (TopLevel topLevel : document.getTopLevels()) {
			if (topLevel.getIdentity().toString().startsWith(uriPrefix)) {
				try {
					document.removeTopLevel(topLevel);
				}
				catch (SBOLValidationException e) {
					// TODO: ignore for now
				}
			}	
		}
	}
	
    /**
     * Attach a file to an object in SynBioHub.
     * @param topLevelUri identity of the object to attach the file to
     * @param filename the name of the file to attach
     * 
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     * @throws FileNotFoundException  if the file is not found
     */
    public void attachFile(URI topLevelUri, String filename) throws SynBioHubException, FileNotFoundException
    {
    	attachFile(topLevelUri,new File(filename));
    }
    
    /**
     * Attach a file to an object in SynBioHub.
     * @param topLevelUri identity of the object to attach the file to
     * @param file the file to attach
     * 
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     * @throws FileNotFoundException if the file is not found
     */
    public void attachFile(URI topLevelUri, File file) throws SynBioHubException, FileNotFoundException
    {
    	String name = file.getName();
    	InputStream inputStream = new FileInputStream(file);
    	attachFile(topLevelUri,inputStream,name);
    }
    
    /**
     * Attach a file to an object in SynBioHub.
     * @param topLevelUri identity of the object to attach the file to
     * @param inputStream the inputStream to attach
     * 
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     */
    public void attachFile(URI topLevelUri, InputStream inputStream) throws SynBioHubException
    {
    	attachFile(topLevelUri,inputStream,"file");
    }
    
    /**
     * Attach a file to an object in SynBioHub.
     * @param topLevelUri identity of the object to attach the file to
     * @param inputStream the inputStream to attach
     * @param filename name of file to attach
     * 
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     */
    public void attachFile(URI topLevelUri, InputStream inputStream, String filename) throws SynBioHubException
    {
    	if (user.equals("")) {
    		Exception e = new Exception("Must be logged in to submit.");
    		throw new SynBioHubException(e);
    	}
        String url = topLevelUri + "/attach";
        url = url.replace(uriPrefix, backendUrl);

        HttpPost request = new HttpPost(url);
        request.setHeader("X-authorization", user);
        request.setHeader("Accept", "text/plain");
        
        MultipartEntityBuilder params = MultipartEntityBuilder.create();        

        /* example for setting a HttpMultipartMode */
        params.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        params.addTextBody("user", user);	
        params.addBinaryBody("file", inputStream, ContentType.DEFAULT_BINARY, filename);
	        
        try
        {
            request.setEntity(params.build());
            HttpResponse response = client.execute(request);
            checkResponseCode(response);
        }
        catch (Exception e)
        {
        	//e.printStackTrace();
            throw new SynBioHubException(e);
            
        }
        finally
        {
            request.releaseConnection();
        }
    }   
    
    /**
     * Add SBOL document to an existing private collection on SynBioHub
     * @param collectionUri Identity of the private collection
     * @param overwrite if object exists in collection, overwrite it
     * @param document the SBOL document to submit
     * 
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     */
    public void addToCollection(URI collectionUri, boolean overwrite, SBOLDocument document) throws SynBioHubException
    {
    	InputStream sbolDoc = new ByteArrayInputStream(serializeDocument(document).getBytes());
    	
        if (!collectionUri.toString().startsWith(uriPrefix)) {
        	throw new SynBioHubException("Collection URI does not start with correct URI prefix for this repository.");
        }
    	submit(collectionUri, "", "", "", "", "", overwrite?"3":"2", sbolDoc);
    }   
 
    /**
     * Add file to an existing private collection on SynBioHub
     * @param collectionUri Identity of the private collection
     * @param overwrite if object exists in collection, overwrite it
     * @param filename filename to submit to SynBioHub
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     * @throws IOException if there is an I/O error
     */
    public void addToCollection(URI collectionUri, boolean overwrite, String filename) throws SynBioHubException, IOException
    {
        if (!collectionUri.toString().startsWith(uriPrefix)) {
        	throw new SynBioHubException("Collection URI does not start with correct URI prefix for this repository.");
        }
    	submit(collectionUri, "", "", "", "", "", overwrite?"3":"2", new FileInputStream(filename));  
    }
    
    /**
     * Add file to an existing private collection on SynBioHub
     * @param collectionUri Identity of the private collection
     * @param overwrite if object exists in collection, overwrite it
     * @param file file to submit to SynBioHub
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     * @throws IOException if there is an I/O error
     */
    public void addToCollection(URI collectionUri, boolean overwrite, File file) throws SynBioHubException, IOException
    {
        if (!collectionUri.toString().startsWith(uriPrefix)) {
        	throw new SynBioHubException("Collection URI does not start with correct URI prefix for this repository.");
        }
    	submit(collectionUri, "", "", "", "", "", overwrite?"3":"2", new FileInputStream(file)); 
    }   
    

    /**
     * Add file to an existing private collection on SynBioHub
     * @param collectionUri Identity of the private collection
     * @param overwrite if object exists in collection, overwrite it
     * @param inputStream inputStream to submit to SynBioHub
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     */
    public void addToCollection(URI collectionUri, boolean overwrite, InputStream inputStream) throws SynBioHubException
    {
        if (!collectionUri.toString().startsWith(uriPrefix)) {
        	throw new SynBioHubException("Collection URI does not start with correct URI prefix for this repository.");
        }
    	submit(collectionUri,"","","","","",overwrite?"3":"2",inputStream);
    }
    
    /**
     * Create a new private collection on SynBioHub
     * @param id The submission identifier
     * @param version The submission version
     * @param name The submission name
     * @param description The submission description
     * @param citations The pubMedIds for this submission
     * @param overwrite if collection exists, overwrite it
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     */
    public void createCollection(String id, String version, String name, String description, String citations,
    		boolean overwrite) throws SynBioHubException
    {
    	submit(null, id,version,name,description,citations,overwrite?"1":"0",(InputStream)null);
	}
    
    /**
     * Create a new private collection on SynBioHub and add the contents of the 
     * SBOL document to this collection
     * @param id The submission identifier
     * @param version The submission version
     * @param name The submission name
     * @param description The submission description
     * @param citations The pubMedIds for this submission
     * @param overwrite if collection exists, overwrite it
     * @param document the SBOL document to submit
     * 
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     */
    public void createCollection(String id, String version, String name, String description, String citations,
    		boolean overwrite, SBOLDocument document) throws SynBioHubException
    {
    	InputStream sbolDoc = new ByteArrayInputStream(serializeDocument(document).getBytes());
    	
    	submit(null, id, version, name, description, citations, overwrite?"1":"0", sbolDoc);
    }   
    
    /**
     * Create a new private collection on SynBioHub and add the contents of the file to this collection
     * @param id The submission identifier
     * @param version The submission version
     * @param name The submission name
     * @param description The submission description
     * @param citations The pubMedIds for this submission
     * @param overwrite if collection exists, overwrite it
     * @param filename filename to submit to SynBioHub
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     * @throws IOException if there is an I/O error
     */
    public void createCollection(String id, String version, String name, String description, String citations,
    		boolean overwrite, String filename) throws SynBioHubException, IOException
    {
    	if(filename != null){
    		submit(null, id, version, name, description, citations, overwrite?"1":"0", new FileInputStream(filename));  
    	}
    }
    
    /**
     * Create a new private collection on SynBioHub and add the contents of the file to this collection
     * @param id The submission identifier
     * @param version The submission version
     * @param name The submission name
     * @param description The submission description
     * @param citations The pubMedIds for this submission
     * @param overwrite if collection exists, overwrite it
     * @param file file to submit to SynBioHub
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     * @throws IOException if there is an I/O error
     */
    public void createCollection(String id, String version, String name, String description, String citations,
    		boolean overwrite, File file) throws SynBioHubException, IOException
    {
    		if(file != null) {
    			submit(null, id, version, name, description, citations, overwrite?"1":"0", new FileInputStream(file)); 
    		}
    }   
    
    /**
     * Create a new private collection on SynBioHub and add the contents of the file to this collection
     * @param id The submission identifier
     * @param version The submission version
     * @param name The submission name
     * @param description The submission description
     * @param citations The pubMedIds for this submission
     * @param overwrite if collection exists, overwrite it
     * @param inputStream inputStream to submit to SynBioHub
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     */
    public void createCollection(String id, String version, String name, String description, String citations,
    		boolean overwrite, InputStream inputStream) throws SynBioHubException
    {
    	submit(null,id,version,name,description,citations,overwrite?"1":"0",inputStream);
    }

    /**
     * Submit file to a new private collection on SynBioHub
     * @param id The submission identifier
     * @param version The submission version
     * @param name The submission name
     * @param description The submission description
     * @param citations The pubMedIds for this submission
     * @param overwrite_merge '0' prevent, '1' overwrite, '2' merge and prevent, '3' merge and overwrite
     * @param inputStream inputStream to submit to SynBioHub
     * @throws SynBioHubException if there was an error communicating with the SynBioHub
     */
    private void submit(URI uri, String id, String version, String name, String description, String citations,
    		String overwrite_merge, InputStream inputStream) throws SynBioHubException
    {
    	if (user.equals("")) 
    	{
    		Exception e = new Exception("Must be logged in to submit.");
    		throw new SynBioHubException(e);
    	}
    	
        String url = backendUrl + "/submit";
        HttpPost request = new HttpPost(url);
        request.setHeader("X-authorization", user);
        request.setHeader("Accept", "text/plain");
        
        MultipartEntityBuilder params = MultipartEntityBuilder.create();        

        /* example for setting a HttpMultipartMode */
        params.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        if (uri==null) {
        	params.addTextBody("id", id);
        	params.addTextBody("version", version);
        	params.addTextBody("name", name);
        	params.addTextBody("description", description);
        	params.addTextBody("citations", citations);
        	params.addTextBody("collectionChoices", "");
        } else {
        	params.addTextBody("rootCollections", uri.toString());
        }
        params.addTextBody("overwrite_merge", overwrite_merge);
        params.addTextBody("user", user);
      
        if (inputStream != null) {
        	params.addBinaryBody("file", inputStream, ContentType.DEFAULT_BINARY, "file");
        } else {
        	params.addTextBody("file", "");
        }
	        
        try
        {
            request.setEntity(params.build());
            HttpResponse response = client.execute(request);
            checkResponseCode(response);
        }
        catch (Exception e)
        {
        	//e.printStackTrace();
            throw new SynBioHubException(e);
            
        }
        finally
        {
            request.releaseConnection();
        }
    }   
     
    private String serializeDocument(SBOLDocument document) throws SynBioHubException
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try
        {
            SBOLWriter.write(document,  outputStream); 
            return outputStream.toString("UTF-8");
        }
        catch(Exception e)
        {
            throw new SynBioHubException("Error serializing SBOL document", e);
        }
    }
    
    private void removeFromSynBioHub(String url) throws SynBioHubException
    {
		HttpGet request = new HttpGet(url);
        request.setHeader("X-authorization", user);
        request.setHeader("Accept", "text/plain");

    	try
    	{
			HttpResponse response = client.execute(request);
	
			checkResponseCode(response);
	        
			HttpStream res = new HttpStream();
			
			res.inputStream = response.getEntity().getContent();
			res.request = request;
    	}
    	catch(Exception e)
    	{
    		request.releaseConnection();

    		throw new SynBioHubException("Error connecting to SynBioHub endpoint", e);    		
    	}
    }

    private SBOLDocument fetchFromSynBioHub(String url) throws SynBioHubException
    {
    	HttpStream stream;
        
        try
        {
            stream = fetchContentAsInputStream(url);
        }
        catch (Exception e)
        {
            throw new SynBioHubException("Error connecting to SynBioHub endpoint", e);
        }
        
        SBOLDocument document;
        
        try
        {
            document = SBOLReader.read(stream.inputStream);
        }
        catch (Exception e)
        {
            throw new SynBioHubException("Error reading SBOL", e);
        }
        finally
        {
        	stream.request.releaseConnection();
        }
        
        //TopLevel topLevel = document.getTopLevel(topLevelUri);
        
        //if(topLevel == null)
        //{
        //    throw new SynBioHubException("Matching top-level not found in response");
        //}
        
        return document;
    }

    private int fetchCount(String url) throws SynBioHubException
    {
        try
        {
            return Integer.parseInt(fetchContentAsString(url));
        }
        catch(Exception e)
        {
            throw new SynBioHubException(e);    
        }
    }
    
    private String fetchContentSaveToFile(String url,OutputStream outputStream,String path) throws SynBioHubException, IOException
    {
		HttpGet request = new HttpGet(url);
        request.setHeader("X-authorization", user);
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
    	catch(SynBioHubException e)
    	{
    		request.releaseConnection();
    		
    		throw e;
    	}
    	catch(IOException e)
    	{
    		request.releaseConnection();
    		
    		throw e;
    	}
    }
    
    private String fetchContentAsString(String url) throws SynBioHubException, IOException
    {
    	HttpStream stream = fetchContentAsInputStream(url);
       
    	String str;
    	
    	try
    	{
    		str = inputStreamToString(stream.inputStream);
    	}
    	finally
    	{
    		stream.request.releaseConnection();        	
    	}
    	
    	return str;
    }

    private static String inputStreamToString(InputStream inputStream) throws IOException
    {
        StringWriter writer = new StringWriter();

        IOUtils.copy(inputStream, writer);
        
        return writer.toString();
    }
    
    class HttpStream
    {
    	public InputStream inputStream;
    	public HttpRequestBase request;
    }
    
    private HttpStream fetchContentAsInputStream(String url) throws SynBioHubException, IOException
    {
		HttpGet request = new HttpGet(url);
        request.setHeader("X-authorization", user);
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
    	catch(SynBioHubException e)
    	{
    		request.releaseConnection();
    		
    		throw e;
    	}
    	catch(IOException e)
    	{
    		request.releaseConnection();
    		
    		throw e;
    	}
    }

    private String encodeUri(String uri)
    {
        try
        {
            return URLEncoder.encode(uri, "UTF-8").replace("+", "%20");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("UTF-8 not supported?");
        }
    }
    
    private static void checkResponseCode(HttpResponse response) throws SynBioHubException
    {
        int statusCode = response.getStatusLine().getStatusCode();
                
        if(statusCode >= 300)
        {
            switch(statusCode)
            {
            case 401:
                throw new PermissionException();
            
            case 404:
                throw new NotFoundException();
            
            default:
            	HttpEntity entity = response.getEntity();
                try {
					throw new SynBioHubException(inputStreamToString(entity.getContent()));
				}
				catch (UnsupportedOperationException | IOException e) {
					throw new SynBioHubException(statusCode+"");
				}
            }
        }
    }

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}
}

