/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package no.sintef.jarfter;

import java.io.FileInputStream;
import java.io.InputStream;
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
import no.sintef.jarfter.Exceptions.JarfterException;

/**
 * REST Web Service
 *
 * @author havahol
 */
@Path("transform")
public class Transform {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of Transform
     */
    public Transform() {
    }

    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response transform(@HeaderParam("transformations_uri") String transformation_uri,
            @HeaderParam("files_fileid") String files_fileid,
            @HeaderParam("transformed_filename") String transformed_filename) {
        try {
            log ("transform - Entered method");

            FileInputStream stream = null;
            if (empty(transformation_uri) || empty(files_fileid) || empty(transformation_uri)) {
                throw new JarfterException(JarfterException.Error.MISSING_HEADERS, 
                            "Missing headers 'transformations_uri', 'transformed_filename' and/or 'files_fileid'");
            }
            log("transform - headers accepted - calling Jarfter().executeTransformation");
            stream = new Jarfter().executeTransformation( transformation_uri, files_fileid, transformed_filename);
            
            // Can also be used: Response.created(URI_location)
            return Response.ok(stream, MediaType.TEXT_PLAIN).build();
            
        } catch(JarfterException je) {
            return je.getResponse();
        } 
    }

    
    @POST
    @Consumes("text/txt")
    @Produces(MediaType.TEXT_PLAIN)
    public Response storeAndTransformData(@HeaderParam("files_filename") String files_filename,
            @HeaderParam("transformations_uri") String transformations_uri,
            @HeaderParam("transformed_filename") String transformed_filename,
            InputStream csvStream)
    {
        try {
 
            if (empty(transformations_uri) || empty(transformed_filename)) {
                throw new JarfterException(JarfterException.Error.MISSING_HEADERS, 
                            "Missing headers 'transformations_uri', 'transformed_filename' and/or 'files_filename' (optional)");
            }
        
            log("storeAndTransformData - storing data");
            JsonObject filesId;
            if (files_filename == null) {
                filesId = new Jarfter().storeRawData(csvStream);
            } else {
                filesId = new Jarfter().storeRawData(csvStream, files_filename);
            }
            
            log("storeAndTransformData - transforming");
            InputStream transformedStream = new Jarfter().executeTransformation( transformations_uri, filesId.getString("fileid"), transformed_filename);
            return Response.ok(transformedStream, MediaType.TEXT_PLAIN).build();
        } catch (JarfterException je) {
            return je.getResponse();
        }
    }
    
    
    
    /**
     * PUT method for updating or creating an instance of Transform
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response putBinary(InputStream input) {
        try {
            new Jarfter().storeBinaryOnDisk(input);
            return Response.ok().build();
        } catch (Exception e) {
            error(e);
        }
        return Response.serverError().build();
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
