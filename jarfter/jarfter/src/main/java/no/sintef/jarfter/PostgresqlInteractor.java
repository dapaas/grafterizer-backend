/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package no.sintef.jarfter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

import no.sintef.jarfter.Exceptions.JarfterException;
import org.json.simple.parser.ParseException;

/**
 *
 * @author havahol
 */
public class PostgresqlInteractor {
    
    private Connection conn;
    
    public PostgresqlInteractor() throws JarfterException {
        log("PostgresqlInteractor - Connecting to database...");
        
        try {
            // Loading the Driver
            Class.forName("org.postgresql.Driver");
            // Connecting to the database
            loginDB();
        } catch (ClassNotFoundException cnfe) {
            log("PostgresqlInteractor - Found ClassNotFoundException: " + cnfe.getLocalizedMessage());
            throw new JarfterException(JarfterException.Error.SQL_NO_DRIVER);
        }
        
        log("PostgresqlInteractor - Connected to database!");
    }
        
    private void loginDB()  {
        JSONParser parser = new JSONParser();
        String endpoint;
        String username;
        String password;
        try {
            Object object = parser.parse(new FileReader("/usr/local/var/jarfter_config.json"));
            JSONObject jsonObject = (JSONObject) object;
            endpoint = (String) jsonObject.get("db_endpoint");
            username = (String) jsonObject.get("db_username");
            password = (String) jsonObject.get("db_password");
            if (empty(endpoint) || empty(username) || empty(password)) {
                throw new IOException("Json file did not contain one of the following: endpoint, username, password (" +
                        empty(endpoint) + "," + empty(username) + "," +empty(password) + ")");
            }
            conn = DriverManager.getConnection(endpoint, username, password);
        } catch (SQLException sqle) {
            String sqleMessage = sqle.getLocalizedMessage();
            log("loginDB - Found SQLException: " + sqle.getErrorCode() + " - " + sqleMessage);
            error(sqle);
            if (sqleMessage.contains("password authentication failed")) {
                throw new JarfterException(JarfterException.Error.DATABASE_LOGIN_ERROR);
            }
            else if (sqleMessage.contains("FATAL: database") && sqleMessage.contains("does not exist")) {
                throw new JarfterException(JarfterException.Error.SQL_NO_DATABASE);
            }
            else if (sqleMessage.contains("Connection refused. Check that the hostname and port are correct")) {
                throw new JarfterException(JarfterException.Error.SQL_NO_ENDPOINT);
            }
            else {
                throw new JarfterException(sqle.getClass().getName(), sqle.getLocalizedMessage());
            }
        }   
        catch (IOException ioe) {
            log("loginDB - Found IOException");
            error(ioe);
            throw new JarfterException(JarfterException.Error.DATABASE_JSON_ERROR);
        } catch (ParseException pex) {
            log("loginDB - Found ParseException");
            error(pex);
            throw new JarfterException(JarfterException.Error.DATABASE_JSON_ERROR);
        }
    }
    
    
    public int addTransformationEntry(String uri, String name, String metadata, String clojureFileName, String jarFileName) 
            throws JarfterException
    {
        checkConnection();
        
        File jarFile = null;
        FileInputStream jarFileInputStream = null;
        try {
            jarFile = new File(jarFileName); // if jarFile does not exist, the next line will throw exception
            jarFileInputStream = new FileInputStream(jarFileName);
        } catch (FileNotFoundException nofile) {
            log("addTransformationEntry - Did not find jarFile");
            error(nofile);
            throw new JarfterException(JarfterException.Error.IO_NO_TEMP_JAR);
        }
        int rowsUpdated;
        try {
            PreparedStatement pst = conn.prepareStatement(
                    "INSERT INTO transformations (uri, name, metadata, clojure, executable) VALUES (?, ?, ?, ?, ?)");
            pst.setString(1, uri);
            pst.setString(2, name);
            pst.setString(3, metadata);
            pst.setString(4, fileToString(clojureFileName));
            pst.setBinaryStream(5, jarFileInputStream, (int)jarFile.length());
            rowsUpdated = pst.executeUpdate();
            pst.close();
            jarFileInputStream.close();
        } catch (SQLException sqle) {
            log("addTransformationEntry - got SQLException...");
            error(sqle);
            if (sqle.getLocalizedMessage().contains("duplicate key value")) {
                throw new JarfterException(JarfterException.Error.SQL_DUPLICATED_KEY);
            }
            throw new JarfterException(JarfterException.Error.SQL_UNKNOWN_ERROR, sqle.getLocalizedMessage());
        } catch (FileNotFoundException nofile) {
            log("addTransformationEntry - Did not find jarFile");
            error(nofile);
            throw new JarfterException(JarfterException.Error.IO_NO_TEMP_CLJ);
        }
        catch (IOException ioe) {
            log("addTransformationEntry - got IOException from jarFileInputStream.close()");
            error(ioe);
            throw new JarfterException(JarfterException.Error.UNKNOWN_ERROR, ioe.getLocalizedMessage());
        }
        log("addTransformationEntry - End of method");
        return rowsUpdated;
    }
    
    public InputStream getTransformationJar(String transform_uri) throws JarfterException {
        checkConnection();
        
        try {
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM transformations WHERE uri = ?");
            pst.setString(1, transform_uri);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                InputStream jarStream = rs.getBinaryStream("executable");
                return jarStream;
            }
        } catch (SQLException sqle) {
            error(sqle);
            throw new JarfterException(JarfterException.Error.SQL_UNKNOWN_ERROR, sqle.getMessage());
        }
        throw new JarfterException(JarfterException.Error.SQL_NO_DATABASE_ENTRY, "No transformations entry with uri " + transform_uri);
    }
    
    public JsonObject selectAllTransfomations(String filter_uri) throws JarfterException {
        checkConnection();
        
        String table = "transformations";
        String uri = "uri";
        String name = "name";
        String metadata = "metadata";
        String clojure = "clojure";
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        try {
            PreparedStatement st = conn.prepareStatement(
                    "SELECT " + uri + ", " + name + ", " + metadata + ", " + clojure + " FROM transformations WHERE uri ~ ?;");
            if (filter_uri == null) {
                st.setString(1, ".*");
            } else {
                st.setString(1, filter_uri);
            }
            log("selectAllTransfomations - calling executeQuery");
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                JsonObjectBuilder job = Json.createObjectBuilder();
                job.add(uri, rs.getString(uri));
                job.add(name, rs.getString(name));
                job.add(metadata, rs.getString(metadata));
                job.add(clojure, rs.getString(clojure));
                jsonArrayBuilder.add(job.build());
            }

            rs.close();
            st.close();
        } catch (SQLException sqle) {
            log("selectAllTransformations - got SQLException");
            error(sqle);
            throw new JarfterException(JarfterException.Error.SQL_UNKNOWN_ERROR, sqle.getLocalizedMessage());
        }
        return Json.createObjectBuilder().add(table, jsonArrayBuilder.build()).build();
    }
    
    
    public int addFilesEntry(String fileid, String filename, String rawDataFilename) 
            throws JarfterException {
        checkConnection();
        
        File rawDataFile = null;
        FileInputStream rawDataStream= null;
        try {
            rawDataFile = new File(rawDataFilename);
            rawDataStream = new FileInputStream(rawDataFile); // this constructor throws exception if file not exist
        } catch (FileNotFoundException nofile) {
            log("addFilesEntry - Did not find rawDataFile");
            error(nofile);
            throw new JarfterException(JarfterException.Error.IO_NO_TEMP_RAWDATA);
        }
        
        int rowsUpdated;
        try {
            PreparedStatement pst = conn.prepareStatement(
                    "INSERT INTO files (fileid, filename, file) VALUES (?, ?, ?)");
            pst.setString(1, fileid);
            pst.setString(2, filename);
            pst.setBinaryStream(3, rawDataStream, (int)rawDataFile.length());
            rowsUpdated = pst.executeUpdate();
            pst.close();
            log("addFilesEntry - End of method");
            return rowsUpdated;
        } catch (SQLException sqle) {
            log("addFilesEntry - got SQLException...");
            error(sqle);
            if (sqle.getLocalizedMessage().contains("duplicate key value")) {
                throw new JarfterException(JarfterException.Error.SQL_DUPLICATED_KEY);
            }
            throw new JarfterException(JarfterException.Error.SQL_UNKNOWN_ERROR, sqle.getLocalizedMessage());
        }
    }
    
    // Takes file uri as input, gives assosiated filename from database as output
    public String getFilesFilename(String fileid) throws JarfterException {
        checkConnection();
        
        try {
            PreparedStatement st = conn.prepareStatement("SELECT filename FROM files WHERE fileid = ?");
            st.setString(1, fileid);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                String filename = rs.getString("filename");
                return filename; // fileid should be unique.
            }
            throw new JarfterException(JarfterException.Error.SQL_NO_DATABASE_ENTRY, "No filename entry with fileid " + fileid);
        } catch (SQLException sqle) {
            log("getFileFilename - got SQLException...");
            error(sqle);
            throw new JarfterException(JarfterException.Error.SQL_UNKNOWN_ERROR, sqle.getLocalizedMessage());
        }
    }
    
    public InputStream getFilesFile(String fileid) throws JarfterException {
        checkConnection();
        
        try {
            PreparedStatement st = conn.prepareStatement("SELECT file FROM files WHERE fileid = ?");
            st.setString(1, fileid);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                InputStream fileStream = rs.getBinaryStream("file");
                return fileStream;
            }
            throw new JarfterException(JarfterException.Error.SQL_NO_DATABASE_ENTRY);
        } catch (SQLException sqle) {
            log("getFileFiles - got SQLException...");
            error(sqle);
            throw new JarfterException(JarfterException.Error.SQL_UNKNOWN_ERROR, sqle.getLocalizedMessage());
        }
    }
    
    // TEST Function - talking to a non-relavant table in DB 'havahol'
    public JsonObject selectAll_havahol(String filter_h_id) throws SQLException {
        checkConnection();
        
        String h_id = "h_id";
        String visual_name = "visual_name";
        String file = "file";
        PreparedStatement st = conn.prepareStatement("SELECT h_id, visual_name FROM havahol WHERE h_id ~ ?;");
        if (filter_h_id == null) {
            st.setString(1, ".*");
        } else {
            st.setString(1, filter_h_id);
        }
        ResultSet rs = st.executeQuery();
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        while (rs.next()) {
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add(h_id, rs.getString(h_id));
            job.add(visual_name, rs.getString(visual_name));
            
            jsonArrayBuilder.add(job.build());
        }
        
        rs.close();
        st.close();
        return Json.createObjectBuilder().add("havahol", jsonArrayBuilder.build()).build();
    }
    
    // TEST Function - talking to a non-relavant table in DB 'havahol'
    public boolean addEntry_havahol(String h_id, String visual_name) throws SQLException {
        checkConnection();
        
        PreparedStatement pst = conn.prepareStatement("INSERT INTO havahol (h_id, visual_name) VALUES(?, ?)");
        pst.setString(1, h_id);
        pst.setString(2, visual_name);
        int numRowsAffected = pst.executeUpdate();
        log("addEntry - Inserted " + numRowsAffected + " rows.");
        //Statement st = conn.createStatement();
        //st.executeQuery("INSERT INTO havahol (h_id, visual_name) VALUES('" + h_id + "', '" + visual_name + "');");
        pst.close();
        return true;
    }
    
    // TEST Function - talking to a non-relavant table in DB 'havahol'
    public int addFileEntry_havahol(String h_id, String filename) throws SQLException, IOException {
        checkConnection();
        
        File file = new File(filename);
        if (!file.exists()) {
            throw new IOException("File " + filename + " does not exist");
        }
        FileInputStream fis = new FileInputStream(file);
        PreparedStatement pst = conn.prepareStatement("INSERT INTO havahol (h_id, visual_name, file) VALUES (?, ?, ?)");
        pst.setString(1, h_id);
        pst.setString(2, filename);
        pst.setBinaryStream(3, fis, (int)file.length());
        int rowsUpdated = pst.executeUpdate();
        pst.close();
        fis.close();
        log("addFileEntry_havahol - End of method");
        return rowsUpdated;
    }
    
    // TEST Function - talking to a non-relavant table in DB 'havahol'
    public int addLargeFileEntry_havahol(String h_id, String filename) throws SQLException, IOException {
        checkConnection();
        
        throw new SQLException("addLargeFileEntry_havahol is not implemented");
        
        // A bytea field has a limitation of 1GB, and we should therefore not 
        // need to implement Large Object functionality. 
        // See https://jdbc.postgresql.org/documentation/80/binary-data.html
        // for example on storing and loading Large Objects in Java
        // If Large Objects should be used, we also have to alter the 
        // datatype in the database table. Therefore, we should forget about
        // it, but without forgetting that we did some reasoning around it.
        
    }
    
    // TEST Function - talking to a non-relavant table in DB 'havahol'
    public InputStream getFileAsStream_havahol(String transform_uri) throws SQLException {
        checkConnection();
        
        PreparedStatement pst = conn.prepareStatement("SELECT * FROM havahol WHERE h_id = ?");
        pst.setString(1, transform_uri);
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            InputStream jarStream = rs.getBinaryStream("file");
            return jarStream;
        }
        throw new SQLException("Found no stream...");
    }
    
    // TEST Function - talking to a non-relavant table in DB 'havahol'
    public String getFileAsString_havahol(String h_id) throws SQLException {
        checkConnection();
        
        PreparedStatement pst = conn.prepareStatement("SELECT * FROM havahol WHERE h_id = ?");
        pst.setString(1, h_id);
        ResultSet rs = pst.executeQuery();
        String fileAsString = "";
        while (rs.next()) {
            byte[] fileBytes = rs.getBytes("file");
            fileAsString = new String(fileBytes);
        }
        return fileAsString;
    }
    
    public void disconnect() {
        log("disconnect - Disconnecting from database!");
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log("disconnect - conn.close() threw SQLException");
                error(e);
                throw new JarfterException(JarfterException.Error.SQL_UNKNOWN_ERROR, e.getLocalizedMessage());
            }
        }
    }
    
    
    private String fileToString(String fileName) throws FileNotFoundException {
        FileInputStream fileStream = new FileInputStream(new File(fileName));
        java.util.Scanner scanner = new java.util.Scanner(fileStream).useDelimiter("\\A");
        String inputAsString = scanner.hasNext() ? scanner.next() : "";
        return inputAsString.replaceAll("[\uFEFF-\uFFFF]", "");
    }
    
    
    private void checkConnection() throws JarfterException {
        if (conn == null) {
            log("addTransformationEntry - Not connected to database!");
            throw new JarfterException(JarfterException.Error.SQL_NOT_CONNECTED);
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
