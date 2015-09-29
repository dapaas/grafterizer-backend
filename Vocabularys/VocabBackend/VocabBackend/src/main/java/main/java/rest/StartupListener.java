package main.java.rest;

import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import main.java.dao.VocabularyDAO;

public class StartupListener implements ServletContextListener {
	
	static int vocabNr = 15;
	static String [][] vocablist = {
		{"rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#","http://www.w3.org/1999/02/22-rdf-syntax-ns"},
		{"rdfs","http://www.w3.org/2000/01/rdf-schema#","http://www.w3.org/2000/01/rdf-schema"},
		{"owl","http://www.w3.org/2002/07/owl#","http://www.w3.org/2002/07/owl"},
		{"foaf","http://xmlns.com/foaf/0.1/","http://xmlns.com/foaf/spec/index.rdf"},
		{"dc","http://purl.org/dc/terms/","http://dublincore.org/2012/06/14/dcterms.rdf"},
		{"skos","http://www.w3.org/2004/02/skos/core#","http://www.w3.org/2009/08/skos-reference/skos.rdf"},
		{"geo","http://www.w3.org/2003/01/geo/wgs84_pos#","http://www.w3.org/2003/01/geo/wgs84_pos"},
		{"rss","http://purl.org/rss/1.0/","http://purl.org/rss/1.0/schema.rdf"},
		{"sioc","http://rdfs.org/sioc/ns","http://rdfs.org/sioc/ns#"},
		{"vann","http://purl.org/vocab/vann/","http://vocab.org/vann/.rdf"},
		{"prov","http://www.w3.org/ns/prov#","http://www.w3.org/ns/prov.owl"},
		{"bibo","http://purl.org/ontology/bibo/","http://purl.org/ontology/bibo/"},
		{"org","http://www.w3.org/ns/org#","http://www.w3.org/ns/org"},
		{"time","http://www.w3.org/2006/time","http://www.w3.org/2006/time"},
		{"frbr","http://purl.org/vocab/frbr/core#","http://vocab.org/frbr/core.rdf"},
	};
	

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		VocabularyDAO dao = new VocabularyDAO();
		try{
			Map<String, String> map = dao.getAllVocabularyName();
			if(map.isEmpty()){
				for(int i = 0; i < vocabNr; i++){
					if(false == dao.insertVocabulary(vocablist[i][0], vocablist[i][1], vocablist[i][2], "")){
						System.out.println("add "+ vocablist[i][0] + "  error");
					}else{
						System.out.println("add "+ vocablist[i][0] + "  success");
					}
				}
			}
		}
		catch(Exception e){
			
		}
	}

}
