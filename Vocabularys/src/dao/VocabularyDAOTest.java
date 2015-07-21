package dao;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class VocabularyDAOTest {
	
	private static VocabularyDAO dao = new VocabularyDAO();

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testInsertVocabulary() {
		boolean ret = false;
		try{
			//ret = dao.insertVocabulary("foaf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#", "http://www.w3.org/1999/02/22-rdf-syntax-ns");
		}catch(Exception e){
			
		}
		
		//assertEquals(true, ret);
	}
	
	@Test
	public void testAutoComplete() {
		Iterator<String> ret;
		try{
			//ret = dao.getAutoComplete();
		}catch(Exception e){
			
		}
	}

	@Test
	public void testDeleteVocabulary() {
		boolean ret = false;
		try{
			//ret = dao.deleteVocabulary("foaf");
		}catch(Exception e){
			
		}
		
		//assertEquals(true, ret);
	}

	@Test
	public void testUpdataVocabulary() {
		boolean ret = false;
		try{
			//ret = dao.updataVocabulary("foaf", "http://www.w3.org/2003/01/geo/wgs84_pos");
		}catch(Exception e){
			
		}
		
		//assertEquals(200, ret);
	}

	@Test
	public void testSearchVocabulary() {
		try{
			//dao.searchVocabulary("f");
		}catch(Exception e){
			
		}
	}

	@Test
	public void testGetAllVocabularyName() {
		try{
			//Map<String, String> strArray= dao.getAllVocabularyName();
		}catch(Exception e){
			
		}
	}
	
	@Test
	public void testValidation() {
		try{
			String data = "@prefix foaf: <http://xmlns.com/foaf/0.1/>." +
			"@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>." +
			"@prefix sim: <http://www.ifi.uio.no/INF3580/simpsons#>." +
			//"sim:yexl rdf:type foaf:Person. " +
			"sim:yexl foaf:knows sim:tom. ";
			StringBuilder errMsg = new StringBuilder();
			int errLevel = dao.validateTriples(data, errMsg);
			System.out.print(errMsg.toString());
			
			//System.out.print(errLevel);
		}catch(Exception e){
		}
	}

}
