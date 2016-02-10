/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package no.sintef.jarfter;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
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
@Path("transformStandAlone")
public class TransformStandAlone {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of TransformStandAlone
     */
    public TransformStandAlone() {
    }

    
    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.TEXT_PLAIN)
    public Response Transform(
            @FormParam("clojure") String clojure,
            @FormParam("csv") String csv,
            @FormParam("transformed_filename") String transformed_filename) {
        log("::createJar - started");
        
        FileInputStream stream = null;
        try {
            
            if (empty(transformed_filename)) {
                throw new JarfterException(JarfterException.Error.MISSING_FORM_PARAM, 
                        "Missing form parameter 'transformed_filename'");
            }
            if (empty(clojure)) {
                throw new JarfterException(JarfterException.Error.MISSING_FORM_PARAM,
                        "Missing form parameter 'clojure'");
            }
            if (empty(csv)) {
                throw new JarfterException(JarfterException.Error.MISSING_FORM_PARAM,
                        "Missing form parameter 'csv'");
            }
            
            log("Transform - got clojure of length: " + clojure.length());
            log("Transform - got csv of length: " + csv.length());
            log("Transform - transformed_filename: " + transformed_filename);
        
            
            //InputStream clojureStream = new ByteArrayInputStream(clojure.getBytes(StandardCharsets.UTF_8));
            //InputStream csvStream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
            
            stream = new Jarfter().compileAndTransform(clojure, csv, transformed_filename);
        } catch (JarfterException e) {
            return e.getResponse();
        }      
        return Response.ok(stream, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment; filename=" + transformed_filename).build();
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
    
