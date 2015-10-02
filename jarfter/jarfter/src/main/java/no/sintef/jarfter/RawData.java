/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package no.sintef.jarfter;

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
@Path("rawData")
public class RawData {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of RawData
     */
    public RawData() {
    }

    
    /**
     * PUT method for updating or creating an instance of RawData
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    @Consumes("text/txt")
    @Produces("application/json")
    public Response storeRawData(@HeaderParam("files_filename") String filename,
            InputStream csvStream) {
        try {

            JsonObject transformationsUri;
            if (filename == null) {
                transformationsUri = new Jarfter().storeRawData(csvStream);
            } else {
                transformationsUri = new Jarfter().storeRawData(csvStream, filename);
            }
            
            // Can also be used: Response.created(URI_location)
            return Response.ok(transformationsUri).build();
        } catch(JarfterException je) {
            return je.getResponse();
        }
        
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
