/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package no.sintef.jarfter;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
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
@Path("warfter")
public class Warfter {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of WarfterResource
     */
    public Warfter() {
    }


    /**@GET
    @Produces("text/plain")
    public Response getWarFile() {
        
        
    }*/

    

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response warfter(
            @FormParam("clojure") String clojure) {
                
        FileInputStream warStream = null;
        Jarfter jarfter = null;
        try {
            InputStream csvStream = new ByteArrayInputStream(clojure.getBytes(StandardCharsets.UTF_8));
            jarfter = new Jarfter();
            warStream = jarfter.getWarStreamFromClojureStream(csvStream);
            return Response.ok(warStream, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment; filename=warfter.war").build();

        } catch (JarfterException e) {
            return e.getResponse();
        }      
        finally {
            if (jarfter != null) {
                jarfter.cleanUp();
            }
        }
        
    }
}
