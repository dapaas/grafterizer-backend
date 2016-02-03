/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package no.sintef.jarfter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response;
import no.sintef.jarfter.Exceptions.JarfterException;

/**
 * REST Web Service
 *
 * @author havahol
 */
@Path("jarCreator")
public class JarCreator {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of JarCreatorResource
     */
    public JarCreator() {
    }
    
    
    @GET
    @Path("/listEntries")
    @Produces("application/json")
    public Response listFromDataBase(@HeaderParam("transform_uri") String transform_uri)
    {
        JsonObject queryResult;
        try {
            // List database entries here. If (transform_uri == null) Select * From transformations
           queryResult = new Jarfter().selectFromTransformations(transform_uri);
        } catch(JarfterException je) {
            return je.getResponse();
        }   
        return Response.ok(queryResult).build();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getJar(@HeaderParam("transform_uri") String transform_uri) {
        
        InputStream jarStream = null;
        try {
            if (empty(transform_uri)) {
                throw new JarfterException(JarfterException.Error.MISSING_HEADERS, 
                        "Missing request header 'transform_uri'");
            }
        
           jarStream = new Jarfter().getJarStream(transform_uri);
        } catch(JarfterException je) {
            return je.getResponse();
        }       
        return Response.ok(jarStream, MediaType.APPLICATION_OCTET_STREAM).build();    
    }
    
    @PUT
    @Consumes("text/txt")
    @Produces("application/json")
    public Response jarCreator(@HeaderParam("transformations_name") String name,
            @HeaderParam("transformations_metadata") String metadata,
            InputStream clojureStream) {
        
        JsonObject transformationsUri;
        
        try {
            if (name == null) {
                transformationsUri = new Jarfter().storeTransformation(clojureStream);
            } else if (metadata == null) {
                transformationsUri = new Jarfter().storeTransformation(clojureStream, name);
            } else {
                transformationsUri = new Jarfter().storeTransformation(clojureStream, name, metadata);
            }
            
        } catch (JarfterException e) {
            return e.getResponse();
        }
    
        // Can also be used: Response.created(URI_location)
        return Response.ok(transformationsUri).build();
    }
    
    
    
    private boolean empty(String parameter) {
        if (parameter == null) {
            return true;
        }
        return (parameter.isEmpty());
    }
    
    private void log(String message) {
        Logger.getLogger(this.getClass().toString()).log(Level.INFO, this.getClass().getSimpleName() + "::" + message);
    }
    
    private void error(Exception ex) {
        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
    }
}
