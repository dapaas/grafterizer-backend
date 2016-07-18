package test.java.prediction;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.java.RDFSuggestion.HybridSTModelHandler;
import main.java.RDFSuggestion.SemanticTypeLabel;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class HybridSTModelHandlerTest {
	
	HybridSTModelHandler hybridPredict = new HybridSTModelHandler("TestCase");

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAddType() {
		
		
		//test 1
		List<List<String>> examples = importCsv("/home/yexl/test_data/test1-Persons data.csv");
/*
		hybridPredict.addType("foaf:Person foaf:name", examples.get(0));
		hybridPredict.addType("foaf:Person foaf:gender", examples.get(1));
		hybridPredict.addType("foaf:Person foaf:age", examples.get(2));
*/		
		//test 2
		examples = importCsv("/home/yexl/test_data/test2 - Claylab employed persons.csv");
		
		hybridPredict.addType("label base-prefix:location", examples.get(0));
		hybridPredict.addType("label base-prefix:involvedIn", examples.get(1));
		hybridPredict.addType("label dbpedia-owl:gender", examples.get(2));
		hybridPredict.addType("label dbpedia-owl:Year", examples.get(4));
		hybridPredict.addType("label base-prefix:Number", examples.get(5));
		hybridPredict.addType("label base-age:min", examples.get(9));
		hybridPredict.addType("label base-age:max", examples.get(10));
		/*
		//test 3
		examples = importCsv("/home/yexl/test_data/test3 - Infrarisk floods.csv");
		
		hybridPredict.addType("Event:Number infrarisk:occurredAtLoc", examples.get(0));
		hybridPredict.addType("Event:Number infrarisk:occurredOnDate", examples.get(3));
		hybridPredict.addType("Event:Number infrarisk:hasDuration", examples.get(5));
		hybridPredict.addType("Event:Number infrarisk:hasDescription", examples.get(19));
		hybridPredict.addType("SocietalLoss:Number infrarisk:hasFatalitiesSocietalLoss", examples.get(6));
		hybridPredict.addType("MonetaryLoss:Number infrarisk:isOfAmountMoney", examples.get(8));
		hybridPredict.addType("Point:Number geo:lat", examples.get(18));
		hybridPredict.addType("Point:Number geo:long", examples.get(17));
		hybridPredict.addType("Flood:Number infrarisk:hasExtent", examples.get(13));
		*/
		//test 4
		examples = importCsv("/home/yexl/test_data/test4 - ARPA protected sites sample.csv");
				
		hybridPredict.addType("arpa-base-au dbpedia-owl:nutsCode", examples.get(0));
		hybridPredict.addType("arpa-base-geometry geo:asWKT", examples.get(16));
		hybridPredict.addType("arpa-document dc:bibliographicCitation", examples.get(11));
		hybridPredict.addType("arpa-base-so rdfs:label", examples.get(2));
		hybridPredict.addType("arpa-base-so ps:legalFoundationDate", examples.get(22));
		hybridPredict.addType("arpa-base-so smod:areaHa", examples.get(13));
		hybridPredict.addType("arpa-base-so smod:lengthKm", examples.get(14));
		hybridPredict.addType("arpa-base-so dc:description", examples.get(21));
		hybridPredict.addType("arpa-base-so smod:ecologicalQuality", examples.get(19));
		hybridPredict.addType("_ geo-wgs84:lat", examples.get(17));
		hybridPredict.addType("_ geo-wgs84:long", examples.get(16));
		/*		
		//test 5
		examples = importCsv("/home/yexl/test_data/test5 - ByggForAlle.csv");
		
		hybridPredict.addType("mkk:matrikkel-nr bygg:hasDisabilityFriendlyMainEntrance", examples.get(12));
		hybridPredict.addType("mkk:matrikkel-nr bygg:numberOfFloors", examples.get(11));
		hybridPredict.addType("mkk:matrikkel-nr prodm:hasArea", examples.get(9));
		hybridPredict.addType("mkk:matrikkel-nr bygg:hasDisabilityFriendlySideEntrance", examples.get(13));
		hybridPredict.addType("mkk:matrikkel-nr bygg:hasElevator", examples.get(14));
		hybridPredict.addType("mkk:matrikkel-nr bygg:hasDisabledBathroom", examples.get(15));
		hybridPredict.addType("mkk:matrikkel-nr bygg:hasDisabledParking", examples.get(16));
	*/	
		//test 6
		examples = importCsv("/home/yexl/test_data/test6 - Statsbygg data with Geospatial old.csv");
		
		hybridPredict.addType("matrikkel-uri sbig:PMANSVARSSTEDKODE", examples.get(0));
		hybridPredict.addType("matrikkel-uri sbig:PMANSVARSSTED", examples.get(1));
		hybridPredict.addType("matrikkel-uri prodm:hasNumber", examples.get(2));
		hybridPredict.addType("matrikkel-uri prodm:hasName", examples.get(3));
		hybridPredict.addType("_ prodm:hasZipCode", examples.get(5));
		hybridPredict.addType("_ prodm:hasPostLocation", examples.get(6));
		hybridPredict.addType("_ prodm:hasDistrict", examples.get(9));		
		hybridPredict.addType("_ prodm:hasAddress", examples.get(4));
		hybridPredict.addType("_ prodm:hasCommercialAddress", examples.get(4));
		hybridPredict.addType("_ prodm:hasPostAddress", examples.get(4));		
		hybridPredict.addType("matrikkel-uri prodm:hasBuildingType", examples.get(7));
		hybridPredict.addType("sbig-complexes:PMKOMPLNR prodm:hasNumber", examples.get(15));
		hybridPredict.addType("sbig-complexes:PMKOMPLNR prodm:hasName", examples.get(16));	
	
		/*
		//test 7-1
		examples = importCsv("/home/yexl/test_data/test7 - Statsbygg property data simple - subset 1.csv");
		
		hybridPredict.addType("matrikkel-uri prodm:hasName", examples.get(1));
		hybridPredict.addType("_ prodm:hasZipCode", examples.get(3));
		hybridPredict.addType("_ prodm:hasPostLocation", examples.get(5));
		hybridPredict.addType("_ prodm:hasDistrict", examples.get(4));
		hybridPredict.addType("_ prodm:hasAddress", examples.get(2));
		
		//test 7-2
		examples = importCsv("/home/yexl/test_data/test7 - Statsbygg property data simple - subset 2.csv");
		
		hybridPredict.addType("matrikkel-uri prodm:hasName", examples.get(1));
		hybridPredict.addType("_ prodm:hasZipCode", examples.get(3));
		hybridPredict.addType("_ prodm:hasPostLocation", examples.get(5));
		hybridPredict.addType("_ prodm:hasDistrict", examples.get(4));
		hybridPredict.addType("_ prodm:hasAddress", examples.get(2));
			
		*/
		//test 8
		examples = importCsv("/home/yexl/test_data/test8 - Statsbygg property data all.csv");
		
		hybridPredict.addType("matrikkel-uri prodm:hasName", examples.get(1));
		hybridPredict.addType("_ prodm:hasZipCode", examples.get(3));
		hybridPredict.addType("_ prodm:hasPostLocation", examples.get(5));
		hybridPredict.addType("_ prodm:hasDistrict", examples.get(4));
		hybridPredict.addType("_ prodm:hasAddress", examples.get(2));		
		hybridPredict.addType("sbig-complexes prodm:hasNumber", examples.get(6));
		hybridPredict.addType("sbig-complexes prodm:hasBuildingComplexType", examples.get(7));
		/*
		//test 9
		examples = importCsv("/home/yexl/test_data/test9 - Citisense sensor data Oslo.csv");
		
		hybridPredict.addType("obs-uri rdfs:label", examples.get(17));
		hybridPredict.addType("obs-uri base-vocab:sensor", examples.get(6));
		hybridPredict.addType("obs-uri qb:dataSet", examples.get(11));
		hybridPredict.addType("obs-uri base-dimens:parameter", examples.get(15));
		hybridPredict.addType("obs-uri sdmx-dimens:refTime", examples.get(5));		
		hybridPredict.addType("obs-uri sdmx-measure:obsValue", examples.get(3));
		hybridPredict.addType("obs-uri sdmx-attribute:unitMeasure", examples.get(12));
		hybridPredict.addType("obs-uri sdmx-attribute:obsStatus", examples.get(13));
*/
		//test 10
		examples = importCsv("/home/yexl/test_data/test 10 - Turin census sections and coordinates.csv");
		
		hybridPredict.addType("sez:joinedld geosparql:asWKT", examples.get(3));
		hybridPredict.addType("sez:joinedld rdfs:label", examples.get(5));
		hybridPredict.addType("obs-uri ter:haSuperficie", examples.get(4));
		hybridPredict.addType("Torino_1272 ter:haCodIstat", examples.get(6));

	}
	
	@Test
	public void testPredictType() {
		int count = 0;
		List<SemanticTypeLabel> ret = null;
		
		Map<String, List<String>> sampleMap = new HashMap<String, List<String>>();
		
		List<List<String>> examples = importCsv("/home/yexl/test_data/test1-Persons data.csv");

		sampleMap.put("foaf:Person foaf:name", examples.get(0));
		sampleMap.put("foaf:Person foaf:gender", examples.get(1));
		/*
		examples = importCsv("/home/yexl/test_data/test2 - Claylab employed persons.csv");
		
		sampleMap.put("label dbpedia-owl:gender", examples.get(2));
		sampleMap.put("label dbpedia-owl:Year", examples.get(4));
		*/
		examples = importCsv("/home/yexl/test_data/test3 - Infrarisk floods.csv");

		sampleMap.put("Event:Number infrarisk:hasDuration", examples.get(5));
		sampleMap.put("Point:Number geo:lat", examples.get(18));
		/*
		//test 4
		examples = importCsv("/home/yexl/test_data/test4 - ARPA protected sites sample.csv");
				
		sampleMap.put("arpa-base-au dbpedia-owl:nutsCode", examples.get(0));
		sampleMap.put("_ geo-wgs84:long", examples.get(16));
*/
		//test 5
		examples = importCsv("/home/yexl/test_data/test5 - ByggForAlle.csv");
		
		sampleMap.put("mkk:matrikkel-nr prodm:hasArea", examples.get(9));
		sampleMap.put("mkk:matrikkel-nr bygg:hasDisabilityFriendlySideEntrance", examples.get(13));
		/*
		examples = importCsv("/home/yexl/test_data/test6 - Statsbygg data with Geospatial old.csv");
		
		sampleMap.put("matrikkel-uri prodm:hasNumber", examples.get(2));
		sampleMap.put("matrikkel-uri prodm:hasName", examples.get(3));	
		*/
		//test 7-1
		examples = importCsv("/home/yexl/test_data/test7 - Statsbygg property data simple - subset 1.csv");
		
		sampleMap.put("_ prodm:hasZipCode", examples.get(3));
		sampleMap.put("_ prodm:hasAddress", examples.get(2));
		
		//test 7-2
		examples = importCsv("/home/yexl/test_data/test7 - Statsbygg property data simple - subset 2.csv");
		
		sampleMap.put("_ prodm:hasZipCode", examples.get(3));
		sampleMap.put("_ prodm:hasPostLocation", examples.get(5));
/*
		//test 8
		examples = importCsv("/home/yexl/test_data/test8 - Statsbygg property data all.csv");
		
		sampleMap.put("matrikkel-uri prodm:hasName", examples.get(1));
		sampleMap.put("_ prodm:hasZipCode", examples.get(3));
		sampleMap.put("_ prodm:hasPostLocation", examples.get(5));
		sampleMap.put("_ prodm:hasDistrict", examples.get(4));
		sampleMap.put("_ prodm:hasAddress", examples.get(2));		
		sampleMap.put("sbig-complexes prodm:hasNumber", examples.get(6));
		sampleMap.put("sbig-complexes prodm:hasBuildingComplexType", examples.get(7));	
*/
		//test 9
		examples = importCsv("/home/yexl/test_data/test9 - Citisense sensor data Oslo.csv");
				
		sampleMap.put("obs-uri rdfs:label", examples.get(17));
		sampleMap.put("obs-uri sdmx-dimens:refTime", examples.get(5));		
		/*
		//test 10
		examples = importCsv("/home/yexl/test_data/test 10 - Turin census sections and coordinates.csv");
				
		sampleMap.put("sez:joinedld geosparql:asWKT", examples.get(3));
		sampleMap.put("obs-uri ter:haSuperficie", examples.get(4));
*/
		for (Map.Entry<String, List<String>> entry : sampleMap.entrySet())
		{
			int numPrediction = 5;
			ret = hybridPredict.predictType(entry.getValue(), numPrediction);
			System.out.println("-----------------------------------------------------------------------");
			System.out.println(entry.getKey());
			
			
			List<String> tempList = new ArrayList<String>();
			
			for(SemanticTypeLabel s : ret){
				tempList.add(s.getLabel());
				//System.out.println("--------------------");
				//System.out.println(s.getLabel());
				//System.out.println(s.getScore());
			}
			
			if(tempList.contains(entry.getKey())){
				count++;
				
				System.out.print("match");
			}
			
			System.out.println();
		}
	}

	@Test
	public void testGetIndexDirectory() {
		fail("Not yet implemented");
	}

	@Test
	public void testReadModelFromFileStringBoolean() {
		fail("Not yet implemented");
	}

	@Test
	public void testReadModelFromFileString() {
		fail("Not yet implemented");
	}

	public static List<List<String>> importCsv(String filename){
		List<List<String>> ret = new ArrayList<List<String>>();
		File file = new File(filename);
        
        BufferedReader br=null;
        FileReader f = null;
        try { 
        	f = new FileReader(file);
            br = new BufferedReader(f);
            String line = ""; 
            int length = 0;
            while ((line = br.readLine()) != null) { 
            	String[] arr = line.split(",");
                length = arr.length;
                break;
            }
            
            for(int index = 0; index < length; index++){
            	List<String> l = new ArrayList<String>();
            	ret.add(l);
            }
            
            while ((line = br.readLine()) != null) { 
            	String[] arr = line.split(",");
                
            	for(int index = 0; index < length; index++){
            		ret.get(index).add(arr[index]); 
            	}
            }
            
        }catch (Exception e) {
             e.printStackTrace();
        }
 
        return ret;
    }
}
