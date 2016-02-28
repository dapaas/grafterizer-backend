package main.java.rest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import main.java.dao.VocabularyDAO;
import main.java.prediction.EnumPredict;
import main.java.prediction.EnumType;
import main.java.prediction.Prediction;
import main.java.prediction.Selection;
import main.java.prediction.Prediction.PredictionProbability;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@Path("/vocabulary")
public class VocabularyAPI {
	
	static int SUCCESS = 200;
	static int FAIL = 500;
	private static Logger logger = Logger.getLogger(VocabularyAPI.class); 
	
	//add vocabulary
	@Path("/add")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)
	public Response addVocabulary(String data) throws Exception {
		
		logger.info( "invoke addVocabulary: " + data );
		String returnString = null;
		JSONObject jsonObject = new JSONObject();
		VocabularyDAO dao = new VocabularyDAO();
		
		try {
			
			JSONObject partsData = new JSONObject(data);
			
			if (dao.insertVocabulary(partsData.optString("name"), 
					partsData.optString("namespace"), 
					partsData.optString("path"), 
					partsData.optString("data"))) {
				jsonObject.put("http_code", "200");
				jsonObject.put("message", "Vocabulary has been added successfully");

				returnString = jsonObject.toString();
			} else {
				return Response.status(500).entity("Unable to enter Item").build();
			}
			
			logger.info( "returnString: " + returnString );
			
		} catch(Exception e) {
			return Response.status(500).entity(getStackTrace(e)).build();
		}
		
		return Response.ok(returnString).build();
	}
	
	//get class and property from vocabulary
	@Path("/getClassAndPropertyFromVocabulary")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassAndPropertyFromVocabulary(String data) throws Exception{
		logger.info( "invoke getClassAndPropertyFromVocabulary: " + data );
		
		JSONObject jsonObject = new JSONObject();
		JSONArray json = new JSONArray();
		String returnString = null;
		VocabularyDAO dao = new VocabularyDAO();
		
		try {
			JSONObject partsData = new JSONObject(data);
			
			List<String> classList = new ArrayList<String>();
			List<String> propertyList = new ArrayList<String>();
			
			//get a list of class and a list of property from file.
			if (dao.getClassAndPropertyFromVocabulary(
					partsData.optString("name"), 
					partsData.optString("namespace"), 
					partsData.optString("path"), 
					partsData.optString("data"),
					partsData.optString("islocal"),
					classList,
					propertyList)){
				jsonObject.put("message", "get class and property info sucessful");
				json = getJsonFromObject(classList.iterator(), true);
				jsonObject.put("classResult", json);
				json = getJsonFromObject(propertyList.iterator(), true);
				jsonObject.put("propertyResult", json);
			}else{
				jsonObject.put("message", "Some error happens in data access");
				jsonObject.put("result", json);
			}
			returnString = jsonObject.toString();
			
			logger.info( "returnString: " + returnString );
			
		} catch(Exception e) {
			return Response.status(500).entity(getStackTrace(e)).build();
		}
		
		return Response.ok(returnString).build();
	}
	
	//get class and property from vocabulary
	@Path("/getClassAndProperty")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassAndProperty(String data) throws Exception{
		logger.info( "invoke getClassAndPropertyFromVocabulary: " + data );

		JSONObject jsonObject = new JSONObject();
		JSONArray json = new JSONArray();
		String returnString = null;
		VocabularyDAO dao = new VocabularyDAO();

		try {
			JSONObject partsData = new JSONObject(data);

			List<String> classList = new ArrayList<String>();
			List<String> propertyList = new ArrayList<String>();

			//get a list of class and a list of property from file.
			if (dao.getClasses(
					partsData.optString("name"), 
					partsData.optString("namespace"),
					classList)){
				if (dao.getProperties(
						partsData.optString("name"), 
						partsData.optString("namespace"),
						propertyList)){
					jsonObject.put("message", "get class and property info sucessful");
					json = getJsonFromObject(classList.iterator(), true);
					jsonObject.put("classResult", json);
					json = getJsonFromObject(propertyList.iterator(), true);
					jsonObject.put("propertyResult", json);
				}
			}else{
				jsonObject.put("message", "Some error happens in data access");
				jsonObject.put("result", json);
			}
			returnString = jsonObject.toString();

			logger.info( "returnString: " + returnString );

		} catch(Exception e) {
			return Response.status(500).entity(getStackTrace(e)).build();
		}

		return Response.ok(returnString).build();
	}

	//get a list of vocabulary name
	@Path("/getAll")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response returnAllVocabulary() throws Exception {
		
		String returnString = null;
		Response response = null;	
		JSONObject jsonObject = new JSONObject();
		
		try {
			VocabularyDAO dao = new VocabularyDAO();
			
			Map<String, String> retMap = dao.getAllVocabularyName();
			
		    JSONArray jsonArray = new JSONArray();
		    
		    for(Map.Entry<String, String> entry : retMap.entrySet()) {
		        String name = entry.getKey();
		        String namespace = entry.getValue();
		        
		    	JSONObject formDetailsJson = new JSONObject();
		    	
				formDetailsJson.put("name", name);
				formDetailsJson.put("namespace", namespace);
				jsonArray.put(formDetailsJson).toString();
		    }

			jsonObject.put("Message", "Get Vocabulary successfully");
			jsonObject.put("result", jsonArray);
			
			returnString = jsonObject.toString();
			
			response = Response.ok(returnString).build();
			logger.info( "returnString: " + returnString );
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return response;
	}
	
	//search vocabulary based on keyword
	@Path("/search/{keyword}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchVocabulary(
			@PathParam("keyword") String keyword)
			throws Exception {
		
		logger.info( "invoke searchVocabulary: " + keyword );
		String returnString = null;
		JSONArray json = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		List<String> classList = new ArrayList<String>();
		List<String> propertyList = new ArrayList<String>();
		
		try {
			VocabularyDAO dao = new VocabularyDAO();
			
			//here we return a list of class and a list of properties for two kinds of search
			if (dao.searchVocabulary(keyword, classList, propertyList)){
				jsonObject.put("message", "Search success");
				json = getJsonFromObject(classList.iterator(), true);
				jsonObject.put("classResult", json);
				json = getJsonFromObject(propertyList.iterator(), true);
				jsonObject.put("propertyResult", json);
			}else{
				jsonObject.put("message", "Search failed due to data access problem");
			}

			returnString = jsonObject.toString();
		}
		catch (Exception e) {
			return Response.status(500).entity(getStackTrace(e)).build();
		}
		
		return Response.ok(returnString).build();
	}
	
	/*
	//auto complete
	@Path("/autocomplete/{firstletter}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response autoComplete(
			@PathParam("firstletter") String firstletter)
			throws Exception {
		
		logger.info( "invoke autoComplete: " + firstletter );
		String returnString = null;
		JSONArray json = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		
		try {
			VocabularyDAO main.java.dao = new VocabularyDAO();
			
			Iterator<String> it = main.java.dao.searchVocabulary(firstletter);
			
			json = getJsonFromObject(it, true);
			
			jsonObject.put("http_code", "200");
			jsonObject.put("message", "get autocomplete sucessful");
			jsonObject.put("result", json);
			
			returnString = jsonObject.toString();
		}
		catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).entity("Server was not able to process your request").build();
		}
		
		return Response.ok(returnString).build();
	}
	*/
	
	//auto complete
	@Path("/autocomplete")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response autoComplete()
			throws Exception {

		String returnString = null;
		JSONArray json = new JSONArray();
		JSONObject jsonObject = new JSONObject();

		try {
			VocabularyDAO dao = new VocabularyDAO();

			Iterator<String> it = dao.getAutoComplete();

			json = getJsonFromObject(it, true);

			jsonObject.put("message", "get autocomplete sucessful");
			jsonObject.put("result", json);

			returnString = jsonObject.toString();
		}
		catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).entity("Server was not able to process your request").build();
		}

		return Response.ok(returnString).build();
	}
	
	//delete vocabulary based on vocabulary name
	@Path("/delete")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteVocabulary(String data) throws Exception {
		logger.info( "invoke deleteVocabulary: " + data );
		String returnString = null;
		JSONObject jsonObject = new JSONObject();
		VocabularyDAO dao = new VocabularyDAO();
		
		try {	
			JSONObject partsData = new JSONObject(data);
			String name = partsData.optString("name") + "_" + partsData.optString("namespace");
			
			if (dao.deleteVocabulary(name)) {
				jsonObject.put("message", "Vocabulary has been deleted successfully");
			} else {
				return Response.status(500).entity("Server was not able to process your request").build();
			}
			
			returnString = jsonObject.toString();
			
		} catch(Exception e) {
			e.printStackTrace();
			return Response.status(500).entity("Server was not able to process your request").build();
		}
		return Response.ok(returnString).build();
	}
	
	//update vocabulary with a new uri
	@Path("/update")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateVocabulary(String data) throws Exception {
		
		logger.info( "invoke updateVocabulary: " + data );
		String returnString = null;
		VocabularyDAO dao = new VocabularyDAO();
		JSONObject jsonObject = new JSONObject();
		
		try {
			logger.info("incomingData: " + data);
			
			JSONObject partsData = new JSONObject(data);
			logger.info( "jsonData: " + partsData.toString() );
			
			if (dao.updataVocabulary(partsData.optString("name"), partsData.optString("namespace"), partsData.optString("path"))) {
				jsonObject.put("message", "Vocabulary has been updated successfully");
			} else {
				return Response.status(500).entity("Server was not able to process your request").build();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).entity("Server was not able to process your request").build();
		}
		
		returnString = jsonObject.toString();
		
		return Response.ok(returnString).build();
	}
	
	@Path("/parse")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)
	public Response Parse(String data)  throws Exception{
		Prediction prediction = new Prediction();
		JSONObject jsonObject = new JSONObject();
		
		try{
			JSONObject partsData = new JSONObject(data);
			
			String strPrediction = partsData.getString("prediction");
			String strType = partsData.getString("type");
			
			EnumPredict epredict = EnumPredict.valueOf(strType);
			
			String str = prediction.parseOperation(strPrediction, epredict);
			
			jsonObject.put("closure", str);
			
		}catch(Exception e){
			
		}
		
		String returnString = jsonObject.toString();
		
		return Response.ok(returnString).build();
	}
	/*
	{
		"selectedRow" : "1",
		"header" : ["id", "name", "address"],
		"data" : ["1", "xiangliy", "Sogn"]
	}
	*/
	@Path("/singleRowPrediction")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)
	public Response RowPredict(String data) throws Exception{
		logger.info( "rowPrediction: " + data );
		JSONObject jsonObject = new JSONObject();
		
		try{
			JSONObject jsonData = new JSONObject(data);
			Integer selectedRow = jsonData.getInt("selectedRow");
			
			//get and parse header
			JSONArray header = jsonData.getJSONArray("header");
			String[] headerArray = convertStringArray(header);
			
			//get and parse row data
			JSONArray rowData = jsonData.getJSONArray("data");
			String[] rowDataArray = convertStringArray(rowData);
			
			Selection s = new Selection();
			s.setType(EnumType.rowSingle);
			s.setSelectedRow(selectedRow);
			
			jsonObject = processPrediction(rowDataArray, null, s, headerArray);
			
		}catch(Exception e){
			logger.info( "exception rowPrediction: " + data );
		}
		
		return Response.ok(jsonObject.toString()).build();
	}
	
	/*
	{
		"selectedRows" : [1, 2, 3],
		"header" : ["id", "name", "address"],
	}
	*/
	@Path("/multipleRowPrediction")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)
	public Response RowsPredict(String data)  throws Exception{
		logger.info( "rowsPrediction: " + data );
		JSONObject jsonObject = new JSONObject();
		
		try{
			JSONObject jsonData = new JSONObject(data);
			
			//get and parse header
			JSONArray header = jsonData.getJSONArray("header");
			String[] headerArray = convertStringArray(header);
			
			//get and parse row data
			JSONArray rowArray = jsonData.getJSONArray("selectedRows");
			Integer[] selectedRows = convertIntArray(rowArray);
			
			Selection s = new Selection();
			s.setType(EnumType.rowMulti);
			s.setSelectedRows(selectedRows);
			
			jsonObject = processPrediction(null, null, s, headerArray);
		}catch(Exception e){
			logger.info( "exception rowPrediction: " + data );
		}
		
		return Response.ok(jsonObject.toString()).build();
	}
	
	/*
	{
		"selectedColumn" : "2",
		"header" : ["id", "name", "address"],
		"data" : ["kringsja", "sogn", "oslo"]
	}
	*/
	@Path("/singleColumnPrediction")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)
	public Response ColumnPredict(String data) throws Exception{
		logger.info( "columnPrediction: " + data );
		JSONObject jsonObject = new JSONObject();
		
		try{
			JSONObject jsonData = new JSONObject(data);
			Integer selectedColumn = jsonData.getInt("selectedColumn");
			
			//get and parse header
			JSONArray header = jsonData.getJSONArray("header");
			String[] headerArray = convertStringArray(header);
			
			//get and parse column data
			JSONArray columnData = jsonData.getJSONArray("data");
			String[] columnDataArray = convertStringArray(columnData);
			
			Selection s = new Selection();
			s.setType(EnumType.colSingle);
			s.setSelectedColumn(selectedColumn);
			
			jsonObject = processPrediction(columnDataArray, null, s, headerArray);
			
		}catch(Exception e){
			logger.info( "exception rowPrediction: " + data );
		}
		
		return Response.ok(jsonObject.toString()).build();
	}
	
	/*
	{
		"selectedColumns" : [1, 2, 3],
		"header" : ["id", "name", "address"],
	}
	*/
	@Path("/multipleColumnPrediction")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)
	public Response ColumnsPredict(String data)  throws Exception{
		logger.info( "columnsPrediction: " + data );
		JSONObject jsonObject = new JSONObject();
		
		try{
			JSONObject jsonData = new JSONObject(data);
			
			//get and parse header
			JSONArray header = jsonData.getJSONArray("header");
			String[] headerArray = convertStringArray(header);
			
			//get and parse column data
			JSONArray columnArray = jsonData.getJSONArray("selectedColumns");
			Integer[] selectedColumns = convertIntArray(columnArray);
			
			Selection s = new Selection();
			s.setType(EnumType.colMulti);
			s.setSelectedRows(selectedColumns);
			
			jsonObject = processPrediction(null, null, s, headerArray);
		}catch(Exception e){
			logger.info( "exception rowPrediction: " + data );
		}
		
		return Response.ok(jsonObject.toString()).build();
	}
	
	//get class and property from vocabulary
	@Path("/validate")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)
	public Response ValidateTriples(String data) throws Exception{
		logger.info( "invoke ValidateTriples: " + data );

		JSONObject jsonObject = new JSONObject();
		String returnString = null;
		VocabularyDAO dao = new VocabularyDAO();

		try {
			JSONObject partsData = new JSONObject(data);
			
			LinkedHashMap<String, String> errMap = new LinkedHashMap<String, String>();
			//0-no error, 1-warning, 2-error
			//validate new triple
			int errorLevel = dao.validateTriples("", partsData.optString("data"), errMap);

			switch(errorLevel){
			case 0:
				jsonObject.put("errorlevel", "Correct");
				break;
			case 1:
				jsonObject.put("errorlevel", "Warning");
				break;
			case 2:
				jsonObject.put("errorlevel", "Error");
				break;
			default:
				jsonObject.put("errorlevel", "Correct");
				break;	
			}
			
			JSONArray jsonArray = new JSONArray();
			
			for(Map.Entry<String, String> entry : errMap.entrySet()){
				JSONObject jsonItem = new JSONObject();
				jsonItem.put("key", entry.getKey());
				jsonItem.put("value", entry.getValue());
				
				jsonArray.put(jsonItem);
			}
			
			jsonObject.put("errorArray", jsonArray);
			
			returnString = jsonObject.toString();

			logger.info( "returnString: " + returnString );

		} catch(Exception e) {
			return Response.status(500).entity(getStackTrace(e)).build();
		}

		return Response.ok(returnString).build();
	}
	
	private JSONArray getJsonFromObject(Iterator<String> it, boolean bSearch) throws JSONException
	{
		JSONArray jsonArray = new JSONArray();
		if (it == null){
			return jsonArray;
		}
	    
	    while (it.hasNext()){
	    	String vocabulary = it.next();
	    	JSONObject formDetailsJson = new JSONObject();
	    	
			formDetailsJson.put("value", vocabulary);
			jsonArray.put(formDetailsJson).toString();
	    }
	    
	    return jsonArray;
	}
	
	JSONObject processPrediction(String[] curRowdata, String [] curColumnData, Selection s, String [] header) throws JSONException{
		
		JSONObject retObject = new JSONObject();
		JSONArray array = new JSONArray();
		Prediction prediction = new Prediction();
		
		Iterator<PredictionProbability> it = prediction.generateOperations(curRowdata, curColumnData, s, header).iterator();
		while(it.hasNext()){
			PredictionProbability p = it.next();
			JSONObject objectTmp = new JSONObject();
			objectTmp.put("value", p.getStrOp());
			objectTmp.put("type", p.getEnumpredict());
			array.put(objectTmp);
		}
		
		retObject.put("predictions", array);
		
		return retObject;
	}
	
	Integer[] convertIntArray(JSONArray jsonArray) throws JSONException{
		Integer[] array= null;
		if (jsonArray != null) {
			array = new Integer[jsonArray.length()];
			for (int i = 0; i < jsonArray.length(); ++i) {
				array[i] = jsonArray.getInt(i);
			}
		}
		return array;
	}
	
	String[] convertStringArray(JSONArray jsonArray) throws JSONException{
		String[] array= null;
		if (jsonArray != null) {
			array = new String[jsonArray.length()];
			for (int i = 0; i < jsonArray.length(); ++i) {
				array[i] = jsonArray.getString(i).toString();
			}
		}
		return array;
	}
	
	private String getStackTrace(Exception e){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String str = sw.toString();
		
		return str;
	}
}


