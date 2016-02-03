/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package no.sintef.jarfter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import org.apache.commons.io.FileUtils;

import no.sintef.jarfter.Exceptions.JarfterException;

/**
 *
 * @author havahol
 */
public class Jarfter {
    
    private final String sessionId;
    
    public Jarfter() {
        // Intentionally empty constructor
        long timestamp = new Timestamp(System.currentTimeMillis()).getTime();
        sessionId = "a_" + timestamp;
    }
    
    public void cleanUp() throws JarfterException {
        String dir_name = "/tmp/" + sessionId;
        log("cleanUp - Deleting folder " + dir_name);
        File directory = new File(dir_name);
        if (directory.exists()) {
            try {
                FileUtils.deleteDirectory(directory);
            } catch (IOException ioe) {
                log("cleanUp - Something bad happened");
                error(ioe);
                throw new JarfterException(JarfterException.Error.TMP_CLEAN_UP);
            }
        }
    }
 
    public FileInputStream getJarStreamFromClojureStream(InputStream input) throws JarfterException {
        log("getJarStreamFromClojureStream - started");
        
        String tmpFolderName = createTmpFolder();
        String clojureFileName = tmpFolderName + "/clojureInput.clj";
        String jarFileName = tmpFolderName + "/transformation.jar";
         
        FileInputStream stream = null;
        File clojureFile = new File( clojureFileName );
            
        // Write to file
        writeFile(clojureFile, input);
        
        // Compile clojure code
        compileClj2Jar(tmpFolderName, clojureFileName, jarFileName);
            
        log("getJarStreamFromClojureStream - Reading jar file...\n");
        
        try {
            // Read from file
            stream = new FileInputStream(jarFileName);
        } catch (FileNotFoundException noFile) {
            throw new JarfterException(JarfterException.Error.IO_NO_TEMP_JAR);
        }
    
        return stream;
    }
    
    public FileInputStream getWarStreamFromClojureStream(InputStream input) throws JarfterException {
        log("getWarStreamFromClojureStream - started");
        
        String tmpFolderName = createTmpFolder();
        String clojureFileName = tmpFolderName + "/clojureInput.clj";
        String warFileName = tmpFolderName + "/warfter.war";
         
        FileInputStream stream = null;
        File clojureFile = new File( clojureFileName );
            
        // Write to file
        writeFile(clojureFile, input);
        
        // Compile clojure code
        compileClj2War(tmpFolderName, clojureFileName, warFileName);
            
        log("getWarStreamFromClojureStream - Reading war file...\n");
        
        try {
            // Read from file
            stream = new FileInputStream(warFileName);
        } catch (FileNotFoundException noFile) {
            throw new JarfterException(JarfterException.Error.IO_NO_TEMP_WAR);
        }
    
        return stream;
    }
    
    public FileInputStream compileAndTransform(String clojure, String csv, String outputName) throws JarfterException {
        log("compileAndTransform - started");
        
        String tmpFolderName = createTmpFolder();
        String clojureFileName = tmpFolderName + "/clojureInput.clj";
        String jarFileName = tmpFolderName + "/transformation.jar";
        String csvFileName = tmpFolderName + "/" + "csv.csv";
        String transformedFileName = tmpFolderName + "/" + outputName;
        
        FileInputStream transformedStream = null;
        
        File clojureFile = new File( clojureFileName );
        File csvFile = new File( csvFileName );
            
        // Write clojure source code to file
        writeFile(clojureFile, clojure);
        
        // Compile clojure code
        compileClj2Jar(tmpFolderName, clojureFileName, jarFileName);
        log("compileAndTransform - Compile successful");
        
        // write csv raw data to file
        writeFile(csvFile, csv);
        
	// Run the transformation jarFileName with csv as input and transformedFileName as output
        log("compileAndTransform - Starting transformation with the following inputs:\n" +
                jarFileName + "\n" +
                csvFileName + "\n" + 
                transformedFileName);
        runTransformation(jarFileName, csvFileName, transformedFileName);
        log("compileAndTransform - Transform successful");
        try {
            // Read from file
            transformedStream = new FileInputStream(transformedFileName);
        } catch (FileNotFoundException noFile) {
            log("compileAndRun - got FileNotFoundException when reading transformed data...");
            error(noFile);
            throw new JarfterException(JarfterException.Error.IO_NO_TEMP_FILE);
        }
    
        return transformedStream;
    }
    
    
    public JsonObject storeTransformation(InputStream clojureStream)
            throws JarfterException {
        return storeTransformation(clojureStream, "");
    }
    
    public JsonObject storeTransformation(InputStream clojureStream, String name) 
            throws JarfterException {
        return storeTransformation(clojureStream, name, "");
    }
    
    public JsonObject storeTransformation(InputStream clojureStream, String name, String metadata) 
            throws JarfterException {
        
        log("storeTransformation - start of method");
        
        PostgresqlInteractor pi = null;
        
        String tmpFolderName = createTmpFolder();
        String clojureFileName = tmpFolderName + "/clojureInput.clj";
        String jarFileName = tmpFolderName + "/transformation.jar";
        File clojureFile = new File( clojureFileName );
        try {
            // Write to file
            writeFile(clojureFile, clojureStream);
            // Compile clojure code
            compileClj2Jar(tmpFolderName, clojureFileName, jarFileName);
           
            pi = new PostgresqlInteractor();
            pi.addTransformationEntry(sessionId, name, metadata, clojureFileName, jarFileName);
            //pi.addFileEntry_havahol(sessionId, jarFileName);
            return jsonWrappedTransformUri(sessionId);
            
        } catch (JarfterException je) {
            throw je;
        } finally {
            if (pi != null) {
                pi.disconnect();
            }
        }
    }
    
    
    public InputStream getJarStream(String transform_uri) throws JarfterException {
        // Checking if this jar is already in the tmp folder
        String searchJar = createTmpFolder(transform_uri) + "/transformation.jar";
        try {
            if (new File(searchJar).exists()) {
                return new FileInputStream(new File(searchJar));
            }
        } catch (FileNotFoundException nofile) {
            log("getJarStream - did not find jar, even though it should exist... Below is stack trace, but we load it from DB");
            error(nofile);
        }
        return getJarStreamFromDatabase(transform_uri);
    }
    
    public InputStream getJarStreamFromDatabase(String transform_uri) 
        throws JarfterException 
    {
        PostgresqlInteractor pi = null;
        try {
            pi = new PostgresqlInteractor();
            return pi.getTransformationJar(transform_uri);            
        } catch(JarfterException jex) {
            error(jex);
            throw jex;
        } finally {
            if (pi != null) {
                pi.disconnect();
            }
        }
    }
    
    public JsonObject selectFromTransformations(String transform_uri)
        throws JarfterException {
        PostgresqlInteractor pi = null;
        try {
            pi = new PostgresqlInteractor();
            return pi.selectAllTransfomations(transform_uri);            
        } catch(JarfterException jex) {
            throw jex;
        } finally {
            if (pi != null) {
                pi.disconnect();
            }
        }
    }
    
    
    public JsonObject storeRawData(InputStream rawDataStream) 
           throws JarfterException {
        return storeRawData(rawDataStream, "");
    }
    
     public JsonObject storeRawData(InputStream rawDataStream, String filename) 
            throws JarfterException {
        log("storeRawData - beginning of method");
        //String uri = streamToString(clojureStream);
        
        PostgresqlInteractor pi = null;
        String tmpFolderName = createTmpFolder();
        String rawDataFileName = tmpFolderName + "/rawData.dat";
        File rawDataFile = new File(rawDataFileName);
        try {
            writeFile(rawDataFile, rawDataStream);
            
            pi = new PostgresqlInteractor();
            pi.addFilesEntry(sessionId, filename, rawDataFileName);
            return jsonWrap("fileid", sessionId);
            
        } catch(JarfterException jex) {
            throw jex;
        } finally {
            if (pi != null) {
                pi.disconnect();
            }
        }
    }
    
    public FileInputStream executeTransformation(String transformation_uri, 
            String files_fileid, String transformedDataFileName) 
            throws JarfterException 
    {
        log("executeTransformation - began");
        
       PostgresqlInteractor pi = null;
        try {
            pi = new PostgresqlInteractor();
            InputStream rawDataStream = pi.getFilesFile(files_fileid);
            String rawDataFileName = pi.getFilesFilename(files_fileid);
            return executeTransformation(transformation_uri, rawDataFileName, rawDataStream, transformedDataFileName);
        } catch(JarfterException jex) {
            throw jex;
        } finally {
            if (pi != null) {
                pi.disconnect();
            }
        }
    }
     
    
    public FileInputStream executeTransformation(String transformation_uri,
            String rawDataFileName, InputStream rawDataStream, String transformedDataFileName) 
            throws JarfterException
    {
        String tmpFolderName = createTmpFolder(transformation_uri);
        String jarFileName = tmpFolderName + "/transformation.jar";
        rawDataFileName = tmpFolderName + "/" + rawDataFileName;
        transformedDataFileName = tmpFolderName + "/" + transformedDataFileName;
        try {
            if (! new File(jarFileName).exists()) {
                InputStream jarStream = getJarStreamFromDatabase(transformation_uri);
            
                // Write jarStream to file in tmpFolder
                writeFileBinary(new File(jarFileName), jarStream);
            }
            
            // Write rawDataStream to file in tmpFolder
            writeFileBinary(new File(rawDataFileName), rawDataStream);
            
            // Run transformation and create transformedDataFile in tmpFolder
            runTransformation(jarFileName, rawDataFileName, transformedDataFileName);
            
            // Return FileInputStream of transformedDataFile
            return new FileInputStream(transformedDataFileName);
        
        } catch (JarfterException jex) {
            throw jex;
        }
        catch (FileNotFoundException nofile) {
            log("executeTransformation - FileNotFoundException found...");
            error(nofile);
            throw new JarfterException(JarfterException.Error.IO_NO_TEMP_FILE);
        }
    }
    
    public void storeBinaryOnDisk(InputStream input) throws JarfterException {
        String tmpFolderName = createTmpFolder();
        File outputFile = new File(tmpFolderName + "/transformation.jar");
        writeFileBinary(outputFile, input);
        log("storeBinaryOnDisk - check file " + outputFile.getAbsolutePath());
    }
    
    
    private String createTmpFolder() {
        return createTmpFolder(sessionId);
    }
    
    private String createTmpFolder(String id) {
        String tmpFolderName = "/tmp/" + id;
        File tmpFolder = new File(tmpFolderName);
        
        if(!tmpFolder.exists()) {
            tmpFolder.mkdir();
        }
        log("createTmpFolder - created folder " + tmpFolderName);
        return tmpFolderName;
    }
    
    
    private void runTransformation(String jarFileName, String rawDataFileName, String transformedDataFileName) throws JarfterException {
        if (!rawDataFileName.endsWith(".csv")) {
            // This won't work, so we throw the exception here
            log("runTransformation - rawDataFileName does not have .csv extension");
            throw new JarfterException(JarfterException.Error.GRAFTER_BAD_INPUT_FORMAT);
        }
        runCommand("java", "-jar", jarFileName, rawDataFileName, transformedDataFileName);
    }
    
    private void compileClj2Jar(String folderName, String clojureFileName, String jarFileName) throws JarfterException {
        // Call script to generate jar.
        // Script need to be available in $PATH
        String applicationFileName = "clj2jar.sh";           
        runCommand(applicationFileName, folderName, clojureFileName, jarFileName);
    }
    
    public void compileClj2War(String folderName, String clojureFileName, String warFileName) throws JarfterException {
        // Call script to generate war.
        // Script need to be available in $PATH
        String applicationFileName = "clj2war.sh";
        runCommand(applicationFileName, folderName, clojureFileName, warFileName);
        if (fileContains(folderName + "/log.txt", "Subprocess failed")) {
            throw new JarfterException(JarfterException.Error.CLOJURE_COMPILE_ERROR);
        }        
    }
            
    
    /**
     * Encapsulates the use of ProccessBuilder
     * @param command
     * @param arguments
     * @throws IOException
     * @throws InterruptedException 
     */
    private void runCommand(String command, String... arguments) throws JarfterException {
        log("runCommand - Starting " + command + "...\n");
        List<String> commandList = new ArrayList<String>();
        commandList.add(command);
        for (String argument : arguments) {
            commandList.add(argument);
        }
        
        ProcessBuilder procBuilder = new ProcessBuilder(commandList);
        Process detachedProc = null;
        try {
            detachedProc = procBuilder.start();
        } catch (IOException ioe) {
            log("runCommand - Could not start the detachedProc...");
            error(ioe);
            throw new JarfterException();
        }
        
        
        String line;
        String stdout = "";
        String stderr = "";
        
        try {
            // Reading output
            BufferedReader outputReader = new BufferedReader(new InputStreamReader(detachedProc.getInputStream()));
            while ((line = outputReader.readLine()) != null) {
                stdout += line;
            }
            outputReader.close();
            
            // Reading error
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(detachedProc.getErrorStream()));
            while((line = errorReader.readLine()) != null) {
                stderr += line;
            }
            errorReader.close();
            
        } catch (IOException ioe) {
            log("runCommand - caught exception while reading stdout and stderr...");
            error(ioe);
            throw new JarfterException(JarfterException.Error.IO_PROCESS_OUTPUT);
        } 
        
        log("runCommand - stdout:\n" + stdout);
        log("runCommand - stderr:\n" + stderr);
                
        try {
            detachedProc.waitFor();
        } catch (InterruptedException interruption) {
            log("runCommand - caught InterruptedException from detachedProc.waitFor()...");
            error(interruption);
            throw new JarfterException(interruption.getClass().getName(), interruption.getLocalizedMessage());
        }
        detachedProc.destroy();
        
        if (!stderr.equals("")) {
            runCommandAnalyzeStderr(command, stderr);
        }
        
    }
    
    private void runCommandAnalyzeStderr(String command, String stderr) throws JarfterException {
        // At the time of writing, runCommand is used with two kind of programs

        if (command.equals("clj2jar.sh")) {
            // This is the stderr from successful clj2jar.sh
            if (stderr.endsWith("Compiling sintef-jarfter-template.coreCompiling sintef-jarfter-template.core")) {
                return;
            }
            else if (stderr.contains("lang.RuntimeException")) {
                throw new JarfterException(JarfterException.Error.GRAFTER_COMPILE_ERROR);
            }
            else {
                throw new JarfterException(JarfterException.Error.GRAFTER_UNKNOWN_COMPILE_ERROR);
            }
        }
        if (command.equals("java")) {
            if (stderr.equals("log4j:WARN No appenders could be found for logger (org.openrdf.rio.RDFParserRegistry).log4j:WARN Please initialize the log4j system properly.log4j:WARN See http://logging.apache.org/log4j/1.2/faq.html#noconfig for more info.")) {
                return;
            }
            else if (stderr.equals("SLF4J: Failed to load class \"org.slf4j.impl.StaticLoggerBinder\".SLF4J: Defaulting to no-operation (NOP) logger implementationSLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.")) {
                return;
            }
            if (stderr.contains("UnsupportedRDFormatException:")) {
                throw new JarfterException(JarfterException.Error.GRAFTER_BAD_OUTPUT_FORMAT);
            }
            throw new JarfterException(JarfterException.Error.GRAFTER_UNKOWN_RUNTIME_ERROR);
        }
    }
    
    
    private boolean fileContains(String filename, String searchString) {
        File file = new File(filename);
        try {
            FileInputStream fileStream = new FileInputStream(file);
            java.util.Scanner scanner = new java.util.Scanner(fileStream).useDelimiter("\\A");
            String fileAsString = scanner.hasNext() ? scanner.next() : "";
            return fileAsString.contains(searchString);
        } catch (FileNotFoundException fnfe) {
            throw new JarfterException(JarfterException.Error.IO_NO_TEMP_FILE);
        }
    }
    
    /**
     * Writes a input stream to a given file.
     * Also handles windows characters on Linux platforms.
     * @param outputFile
     * @param input
     * @throws IOException 
     */
    private void writeFile(File outputFile, InputStream input) throws JarfterException {
        if (outputFile.exists()) {
            outputFile.delete();
        }
        
        try {
            log("writeFile(File, InputStream) - Writing to file " + outputFile.getName());
            // Writing to a string before writing to file
            String inputAsString = streamToString(input);
            
            //String inputAsString = IOUtils.toString(input, encoding);
            log("writeFile(File, InputStream) - InputStream consisted of the following:\n" + inputAsString);
            writeFile(outputFile, inputAsString);
            input.close();
            log("writeFile(File, InputStream) - File written!");
        } catch (IOException ex) {
            log("writeFile(File, InputStream) - Exception in writeFile...");
            error(ex);
            throw new JarfterException(JarfterException.Error.IO_WRITE_FILE);
        }
    }
    
    private void writeFile(File outputFile, String content) throws JarfterException
    {
        try {
            String encoding = "UTF-8";
            FileUtils.writeStringToFile(outputFile, content, encoding);
        } catch (IOException ex) {
            log("writeFile(File, String) - Exception in writeFile...");
            error(ex);
            throw new JarfterException(JarfterException.Error.IO_WRITE_FILE);
        }
    }
    
    private void writeFileBinary(File outputFile, InputStream input) throws JarfterException {
        if (outputFile.exists()) {
            outputFile.delete();
        }
        
        try {
            log("writeFileBinary - Writing to file " + outputFile.getName());
            OutputStream output = new FileOutputStream(outputFile);
            byte[] buffer = new byte[8*1024];
            int bytesRead;
            while((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            output.close();    
        } catch (FileNotFoundException nofile) {
            log("writeFileBinary - FileNotFoundException found...");
            error(nofile);
            throw new JarfterException(JarfterException.Error.IO_NO_TEMP_FILE);
        } catch (IOException ioex) {
            log("writeFileBinary - IOException in writeFileBinary...");
            error(ioex);
            throw new JarfterException(JarfterException.Error.IO_WRITE_FILE);
        } finally {
        }
    }
    
    private String streamToString(InputStream input) {
        String encoding = "UTF-8";
        java.util.Scanner scanner = new java.util.Scanner(input).useDelimiter("\\A");
        String inputAsString = scanner.hasNext() ? scanner.next() : "";
        return inputAsString.replaceAll("[\uFEFF-\uFFFF]", "");
    }    

    private JsonObject jsonWrappedTransformUri (String transformation_uri) {
        return Json.createObjectBuilder().add("transformation_uri", transformation_uri).build();
    }
    
    private JsonObject jsonWrap(String field, String value) {
        return Json.createObjectBuilder().add(field, value).build();
    }
    
    
    private void log(String message) {
        Logger.getLogger(this.getClass().toString()).log(Level.INFO, this.getClass().getSimpleName() + "::" + message);
    }
    
    private void error(Exception ex) {
        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
    }    
    
}
