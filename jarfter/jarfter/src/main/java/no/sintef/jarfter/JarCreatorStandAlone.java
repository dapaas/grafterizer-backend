/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package no.sintef.jarfter;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import no.sintef.jarfter.Exceptions.JarfterException;


/**
 *
 * @author havahol
 */
@Path("/jarCreatorStandAlone")
public class JarCreatorStandAlone {

    @GET
    public Response getTest() {
        log("getTest - Confirming that we reach GET");
        return Response.ok().build();
    }

    
 
    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response createJar(
            @FormParam("clojure") String clojure) {
        log("::createJar - started");
        
        FileInputStream stream = null;
        
        Jarfter jarfter = null;
        try {
            
            jarfter = new Jarfter();
            InputStream input = new ByteArrayInputStream(clojure.getBytes(StandardCharsets.UTF_8));

            stream = jarfter.getJarStreamFromClojureStream(input);
            return Response.ok(stream, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment; filename=transformation.jar").build();

        } catch (JarfterException e) {
            return e.getResponse();
        }      
        finally {
            if (jarfter != null) {
                jarfter.cleanUp();
            }
        }
    }
    
    
    @POST
    @Path("reflector")
    @Produces(MediaType.TEXT_PLAIN)
    public Response reflector(InputStream input) {
        return Response.ok(input, MediaType.TEXT_PLAIN).build();
    }
    
    private void log(String message) {
        Logger.getLogger(this.getClass().toString()).log(Level.INFO, this.getClass().getSimpleName() + "::" + message);
    }
    
    private void error(Exception ex) {
        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
    }
}
