/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package no.sintef.jarfter.Exceptions;


import java.sql.Timestamp;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 *
 * @author havahol
 */
public class JarfterException extends RuntimeException {

    /***
     *   Enumerator describing all possible errors in a type safe and easily readable manner
     */
    public enum Error {
        
        // Database related request errors
        // Database related request errors
// Database related request errors
        // Database related request errors
        DATABASE_LOGIN_ERROR(451, Status.INTERNAL_SERVER_ERROR,
                "Invalid database username and password"),
        SQL_NO_DATABASE_ENTRY(452, Status.INTERNAL_SERVER_ERROR, 
                "Could not find database entry"),
        DATABASE_JSON_ERROR(453, Status.INTERNAL_SERVER_ERROR,
                "Could not find JSON element for logging into database"),
        
        // Web service request errors
        MISSING_HEADERS(456, Status.BAD_REQUEST, 
                "Missing required headers"),
        MISSING_FORM_PARAM(457, Status.BAD_REQUEST,
                "Missing form parameters"),
        
        // Grafter user errors
        GRAFTER_COMPILE_ERROR(461, Status.BAD_REQUEST,
                "Could not compile the clojure code"),
        GRAFTER_BAD_INPUT_FORMAT(462, Status.BAD_REQUEST,
                "Illegal input data file format - should have .csv extension"),        
        GRAFTER_BAD_OUTPUT_FORMAT(463, Status.BAD_REQUEST,
                "Illegal output file format - choose between {nt|rdf|n3|ttl}"),
        GRAFTER_CORRUPT_CSV_FILE(464, Status.BAD_REQUEST,
                "Corrupt csv file given as input to transformation"),
        
        // Database related server errors
        SQL_UNKNOWN_ERROR(551, Status.INTERNAL_SERVER_ERROR,
                "Unknown SQL error"),
        SQL_NO_DATABASE(552, Status.INTERNAL_SERVER_ERROR,
                "Could not find database with the given name"),
        SQL_NO_ENDPOINT(553, Status.INTERNAL_SERVER_ERROR,
                "Could not find database at the given endpoint"),
        SQL_NO_DRIVER(554, Status.INTERNAL_SERVER_ERROR,
                "Could not load database driver org.postgresql.Driver"),
        SQL_NOT_CONNECTED(555, Status.INTERNAL_SERVER_ERROR,
                "Not connected to database"),
        SQL_DUPLICATED_KEY(556, Status.INTERNAL_SERVER_ERROR,
                "Duplicated key for new database entry"),
        
        // Grafter related
        CLOJURE_COMPILE_ERROR(562, Status.INTERNAL_SERVER_ERROR,
                "Got error when compiling the clojure project"),
        IO_PROCESS_OUTPUT(563, Status.INTERNAL_SERVER_ERROR,
                "Something went wrong while reading process stdout/stderr"),
        GRAFTER_UNKNOWN_COMPILE_ERROR(564, Status.INTERNAL_SERVER_ERROR,
                "Unknown grafter compilation error"),
        GRAFTER_UNKOWN_RUNTIME_ERROR(565, Status.INTERNAL_SERVER_ERROR,
                "Unknown grafter runtime error"),
        
        
        // FileSystem errors
        IO_NO_TEMP_FOLDER(566, Status.INTERNAL_SERVER_ERROR, 
                "Could not find temporary folder"),
        IO_WRITE_FILE(567, Status.INTERNAL_SERVER_ERROR,
                "Could not write file to temporary disk"),
        IO_NO_TEMP_JAR(568, Status.INTERNAL_SERVER_ERROR, 
                "Could not find jar file on temporary disk"),
        IO_NO_TEMP_CLJ(569, Status.INTERNAL_SERVER_ERROR,
                "Could not find clojure file on temporary disk"),
        IO_NO_TEMP_RAWDATA(570, Status.INTERNAL_SERVER_ERROR,
                "Could not find raw data file on temporary disk"),
        IO_NO_TEMP_FILE(571, Status.INTERNAL_SERVER_ERROR,
                "Could not find file on temporary disk"),
        IO_NO_TEMP_WAR(572, Status.INTERNAL_SERVER_ERROR,
                "Could not find war file on temporary disk"),
        TMP_CLEAN_UP(573, Status.INTERNAL_SERVER_ERROR,
                "Failed clean up the tmp directory"),
        
        UNKNOWN_ERROR(-1, Status.INTERNAL_SERVER_ERROR,
                "Unknown error");
            
        
        private final int error_code;
        private final String error_message;
        private final Status error_status;
        private final String error_timestamp;
        
        private Error(int code, Status status, String message) {
            this.error_code = code;
            this.error_status = status;
            java.util.Date date= new java.util.Date();
            this.error_timestamp = new Timestamp(date.getTime()).toString();
            this.error_message = "(" + this.error_code + " - " + error_timestamp + ") " + message + "\n";
        }
        
        private int getCode() {return error_code;}
        private Status getStatus() {return error_status;}
        private String getMessage() {return error_message;}
    }
    
    
    private int jarfterErrorCode;
    private String errorMessage;
    private Status status;
        
    public JarfterException() {
        this(Error.UNKNOWN_ERROR);
    }
    
    // TO BE REMOVED
    public JarfterException(String message) {
        super(message);
        errorMessage = message;
        jarfterErrorCode = -1;
        status = Status.INTERNAL_SERVER_ERROR;
    }
    
    public JarfterException(String className, String localizedMessage) {
        this(Error.UNKNOWN_ERROR);
        appendToMessage("Unexpected " + className + " with message \"" + localizedMessage + "\"");
    }
    
    public JarfterException(Error error) {
        jarfterErrorCode = error.getCode();
        errorMessage = error.getMessage();
        status = error.getStatus();
    }
    
    public JarfterException(Error error, String additionalMessage) {
        this(error);
        appendToMessage(additionalMessage);
    }
    
    public Response getResponse() {
        return Response.status(status).entity(errorMessage).build();
    } 
    
    
    private void setErrorMessage(String message) {
        errorMessage = "\n\nDepricated!!!\t\tOBS! OBS! \n(" + jarfterErrorCode + ") " + message + "\n";
    }
    
    private void appendToMessage(String additionalMessage) {
        errorMessage = errorMessage + additionalMessage + "\n";
    }
    
    private void createMessage() {
        switch (jarfterErrorCode) {
            case -1:
                setErrorMessage("Unkown error");
                status = Status.INTERNAL_SERVER_ERROR;
                break;
            case 1:
                setErrorMessage("one");
                status = Status.INTERNAL_SERVER_ERROR;
                break;
            case 2:
                setErrorMessage("two");
                status = Status.INTERNAL_SERVER_ERROR;
                break;
            case 451:
                setErrorMessage("Invalid database username and password");
                status = Status.UNAUTHORIZED;
                break;
            case 452:
                setErrorMessage("Could not find database entry");
                status = Status.BAD_REQUEST;
                break;
            case 456:
                setErrorMessage("Missing required headers");
                status = Status.BAD_REQUEST;
                break;
            case 551:
                setErrorMessage("Unknown SQL error");
                status = Status.INTERNAL_SERVER_ERROR;
                break;
            case 552:
                setErrorMessage("Could not find database with the given name");
                status = Status.INTERNAL_SERVER_ERROR;
                break;
            case 553:
                setErrorMessage("Could not find database at the given endpoint");
                status = Status.INTERNAL_SERVER_ERROR;
                break;
            case 554:
                setErrorMessage("Could not load database driver org.postgresql.Driver");
                status = Status.INTERNAL_SERVER_ERROR;
                break;
            case 555:
                setErrorMessage("Not connected to database");
                status = Status.INTERNAL_SERVER_ERROR;
                break;
            default:
                setErrorMessage("Unknown error - no matching error code");
                status = Status.INTERNAL_SERVER_ERROR;
                break;
        }
    }

    
    
}