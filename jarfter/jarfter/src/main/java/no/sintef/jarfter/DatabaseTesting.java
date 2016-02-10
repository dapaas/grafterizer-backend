/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package no.sintef.jarfter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonObject;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;


/**
 *
 * @author havahol
 */
@WebService(serviceName = "DatabaseTesting")
public class DatabaseTesting {

    /**
     * This is a sample web service operation
     */
    @WebMethod(operationName = "selectAll")
    public String selectAll(
        ) throws SQLException, ClassNotFoundException {
        PostgresqlInteractor pi = null;
        try {
            pi = new PostgresqlInteractor();
            return pi.selectAll_havahol(null).toString();
        } catch (SQLException e) {
            error(e);
            throw e;
        } finally {
            if (pi != null) {
                pi.disconnect();
            }
        }
    }
    
    @WebMethod(operationName = "addEntry") 
    public boolean addEntry(
            @WebParam(name = "h_id") String h_id,
            @WebParam(name = "visual_name") String visual_name
        ) throws SQLException, ClassNotFoundException {
        PostgresqlInteractor pi = null;
        try {
            pi = new PostgresqlInteractor();
            return pi.addEntry_havahol(h_id, visual_name);
        } catch (SQLException e) {
            error(e);
            throw e;
        } finally {
             if (pi != null) {
                 pi.disconnect();
             }
        }
    }
    
    @WebMethod(operationName = "addFileEntry") 
    public int addFileEntry(
            @WebParam(name = "h_id") String h_id,
            @WebParam(name = "filename") String filename
        ) throws SQLException, ClassNotFoundException, IOException {
        PostgresqlInteractor pi = null;
        try {
            pi = new PostgresqlInteractor();
            return pi.addFileEntry_havahol(h_id, filename);
        } catch (SQLException |IOException e) {
            error(e);
            throw e;
        } finally {
             if (pi != null) {
                 pi.disconnect();
             }
        }
    }
    
    @WebMethod(operationName = "getFileAsString")
    public String getFileAsString(
            @WebParam(name = "h_id") String h_id
        ) throws SQLException, ClassNotFoundException {
        PostgresqlInteractor pi = null;
        try {
            pi = new PostgresqlInteractor();
            return pi.getFileAsString_havahol(h_id);
        } catch (SQLException e) {
            error(e);
            throw e;
        } finally {
            if (pi != null) {
                pi.disconnect();
            }
        }
        
    }
    
    private void log(String message) {
        Logger.getLogger(this.getClass().toString()).log(Level.INFO, this.getClass().getSimpleName() + "::" + message);
    }

    private void error(Exception ex) {
        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
    }    
}
