package dao;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.text.EntityDefinition;
import org.apache.jena.query.text.TextDatasetFactory;
import org.apache.jena.query.text.TextQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDFS;

import org.apache.log4j.Logger; 

import validation.TripleValidation;

public class VocabularyDAO {
	
	static int SUCCESS = 200;
	static int FAIL = 500;
	private static Logger logger = Logger.getLogger(VocabularyDAO.class); 
	
	// add new vocabulary
	public boolean insertVocabulary(String name, String prefix, String location) throws Exception{
		if (name.isEmpty() || location.isEmpty() || prefix.isEmpty()){
			return false;
		}
		
		//get dataset and model
		Dataset datasetVocab = getDataset();
		Model Vocabularymodel = ModelFactory.createDefaultModel();
		FileManager.get().readModel( Vocabularymodel, location, getFileExtension(location) );
		
		Dataset datasetSearch = getDatasetSearch(true);
		Model searchModel = ModelFactory.createDefaultModel();
		
		datasetSearch.begin(ReadWrite.WRITE);
		datasetVocab.begin(ReadWrite.WRITE);
		
		//create a search model and put all class name, property name in it, as a index for search.
		//vocabularyModel is used for save all the triple about a vocabulary.
		extractClassesandProperties(Vocabularymodel, searchModel, name);
		
		datasetSearch.addNamedModel(name + "_" + prefix, searchModel);
		datasetVocab.addNamedModel(name + "_" + prefix, Vocabularymodel);

		datasetVocab.commit();
		datasetVocab.end();
		datasetVocab.close();
		
		datasetSearch.commit();
		//testDataset(datasetSearch);
		datasetSearch.end();
		datasetSearch.close();
		
		Vocabularymodel.close();
		searchModel.close();
		
		return true;
	}
	
	public Boolean getClassAndPropertyFromVocabulary(String name, String namespace, String location, String fileContent, 
			List<String> classlist, List<String> propertylist){
		Model model = ModelFactory.createDefaultModel();
		
		if (location.isEmpty()){
			model.read(new ByteArrayInputStream(fileContent.getBytes()), null, getFileExtension(location));
		}
		else{
			FileManager.get().readModel( model, location, getFileExtension(location) );
		}

		ResultSet res;
		
		String pre = StrUtils.strjoinNL
	            ("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
	            , "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
	            , "PREFIX owl: <http://www.w3.org/2002/07/owl#>") ;
		//get rdfs classes
		String qs = StrUtils.strjoinNL
	            ( "SELECT DISTINCT ?classname WHERE{ "
	            , " ?classname a rdfs:Class. }") ;
        
		Query q = QueryFactory.create(pre+"\n"+qs) ;
		QueryExecution qe = QueryExecutionFactory.create(q, model);
		
		try {
			res = qe.execSelect();
			
			while (res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode a = soln.get("?classname");

				if (a.asResource().getNameSpace().equals(namespace)){
					classlist.add(name + ":" + a.asResource().getLocalName());
				}
			}
		} finally {
			qe.close();
		}
		
		//get owl classes
		String qsOwlClass = StrUtils.strjoinNL
	            ( "SELECT DISTINCT ?classname WHERE{ "
	            , " ?classname a owl:Class. }") ;
        
		Query qOwlClass = QueryFactory.create(pre+"\n"+qsOwlClass) ;
		QueryExecution qeOwlClass = QueryExecutionFactory.create(qOwlClass, model);
		
		try {
			res = qeOwlClass.execSelect();
			
			while (res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode a = soln.get("?classname");
				if (a.asResource().getNameSpace().equals(namespace)){
					if (classlist.contains(name + ":" + a.asResource().getLocalName()) == false){
						classlist.add(name + ":" + a.asResource().getLocalName());
					}
				}
			}
		} finally {
			qe.close();
		}
		
		//get rdf properties
		String qsProperty = StrUtils.strjoinNL
	            ( "SELECT DISTINCT ?classname WHERE{ "
	            , " ?classname a rdf:Property. }") ;
        
		Query qProperty = QueryFactory.create(pre+"\n"+qsProperty) ;
		QueryExecution qeProperty = QueryExecutionFactory.create(qProperty, model);
		
		try {
			res = qeProperty.execSelect();
			
			while (res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode a = soln.get("?classname");
				if (a.asResource().getNameSpace().equals(namespace)){
					propertylist.add(name + ":" + a.asResource().getLocalName());
				}
			}
		} finally {
			qe.close();
		}
		
		//get owl properties
		String qsOwlProperty = StrUtils.strjoinNL
	            ( "SELECT DISTINCT ?classname WHERE{ "
	            , " ?classname a owl:ObjectProperty. }") ;
        
		Query qOwlProperty = QueryFactory.create(pre+"\n"+qsOwlProperty) ;
		QueryExecution qeOwlProperty = QueryExecutionFactory.create(qOwlProperty, model);
		
		try {
			res = qeOwlProperty.execSelect();
			
			while (res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode a = soln.get("?classname");
				if (a.asResource().getNameSpace().equals(namespace)){
					if (propertylist.contains(name + ":" + a.asResource().getLocalName()) == false){
						propertylist.add(name + ":" + a.asResource().getLocalName());
					}
				}
			}
		} finally {
			qe.close();
		}
		
		String qsOwlDataProperty = StrUtils.strjoinNL
	            ( "SELECT DISTINCT ?classname WHERE{ "
	            , " ?classname a owl:DatatypeProperty. }") ;
        
		Query qOwlDataProperty = QueryFactory.create(pre+"\n"+qsOwlDataProperty) ;
		QueryExecution qeOwlDataProperty = QueryExecutionFactory.create(qOwlDataProperty, model);
		
		try {
			res = qeOwlDataProperty.execSelect();
			
			while (res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode a = soln.get("?classname");
				if (a.asResource().getNameSpace().equals(namespace)){
					if (propertylist.contains(name + ":" + a.asResource().getLocalName()) == false){
						propertylist.add(name + ":" + a.asResource().getLocalName());
					}
				}
			}
		} finally {
			qe.close();
		}
		
		return true;
	}
	
	//delete vocabulary based on vocabulary name
	public boolean deleteVocabulary(String name) throws Exception{
		
		if (name.isEmpty()){
			return false;
		}
		
		Dataset dataset = getDataset();
		
		dataset.begin(ReadWrite.WRITE);
		
		if (dataset.containsNamedModel(name)){
			dataset.removeNamedModel(name);	
		}
		
		dataset.commit();
		dataset.end();
		dataset.close();

		
		return true;
	}
	
	//update vocabulary with a new uri
	public boolean updataVocabulary(String name, String namespace, String newPath) throws Exception{
		
		if (name.isEmpty() || newPath.isEmpty() || namespace.isEmpty()){
			return false;
		}
		
		Dataset dataset = getDataset();
		
		dataset.begin(ReadWrite.WRITE);
		
		if (dataset.containsNamedModel(name)){
			Model model = ModelFactory.createDefaultModel();
			com.hp.hpl.jena.util.FileManager.get().readModel( model, newPath, "RDF/XML" );
			
			dataset.replaceNamedModel(name + "_" + namespace, model);
		}
		
		dataset.commit();
		dataset.end();
		dataset.close();

		return true;
	}
	
	//search vocabulary based on keyword
	public Boolean searchVocabulary(String keyword, List<String> classList, 
			List<String> propertyList) throws Exception{
		
		String prefix = "";
		String name = keyword;
		if( keyword.contains(":") ){
			 prefix = keyword.substring(0, keyword.indexOf(":") - 1);
			 name = keyword.substring(keyword.indexOf(":") + 1, keyword.length());
		}
		
		//get dataset connection
		Dataset dataset = getDatasetSearch(true);
		dataset.begin(ReadWrite.READ);
		
		ResultSet res;
		
		String pre = StrUtils.strjoinNL
	            ("PREFIX text: <http://jena.apache.org/text#>"
	            , "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
	            , "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>") ;
	    String qsclass = StrUtils.strjoinNL
	            ( "SELECT ?label "
	            , "{{ ?s text:query (rdfs:label '*" + name + "*'). "
	            , "?s rdfs:label ?label. ?s rdf:type 'class'.} UNION "
	            , "{ GRAPH ?g {"
	            , "?s text:query (rdfs:label '*" + name + "*')."
	            , "?s rdfs:label ?label."
	            , "?s rdf:type 'class'."
	            , "}}}") ;

		Query qclass = QueryFactory.create(pre+"\n"+qsclass) ;
		
		QueryExecution qeclass = QueryExecutionFactory.create(qclass, dataset);
		try {
			res = qeclass.execSelect();
			while( res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode label = soln.get("?label");
				
				if( null != label ){
					String str = label.toString();
					if(str.contains(prefix)){
						classList.add(str);
					}
				}
			}
		} finally {
			qeclass.close();
		}
		
		String qsproperty = StrUtils.strjoinNL
	            ( "SELECT ?label "
	            , "{{ ?s text:query (rdfs:label '*" + name + "*'). "
	            , "?s rdfs:label ?label. ?s rdf:type 'property'.} UNION "
	            , "{ GRAPH ?g {"
	            , "?s text:query (rdfs:label '*" + name + "*')."
	            , "?s rdfs:label ?label."
	            , "?s rdf:type 'property'."
	            , "}}}") ;

		Query qproperty = QueryFactory.create(pre+"\n"+qsproperty) ;
		
		QueryExecution qeproperty = QueryExecutionFactory.create(qproperty, dataset);
		try {
			res = qeproperty.execSelect();
			while( res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode label = soln.get("?label");
				
				if( null != label ){
					String str = label.toString();
					if(str.contains(prefix)){
						propertyList.add(str);
					}
				}
			}
		} finally {
			qeproperty.close();
		}
		
		dataset.commit();
		
		testDataset(dataset);
		dataset.end();
		dataset.close();
		
		return true;
	}
	
	//get a list of vocabulary name
	public Map<String, String> getAllVocabularyName() throws Exception{
		
		Dataset dataset = getDataset();
		Iterator<String> it;
		Map<String, String> ret = new HashMap<>();
		
		dataset.begin(ReadWrite.READ);
		
		try {
			 it = dataset.listNames();
			 dataset.commit() ;
	    } finally { dataset.end() ; }
		
		dataset.close();
		
		while( it.hasNext() ){
			String str = it.next();
			if(str.indexOf("_") > 0){
				String name = str.substring(0, str.indexOf("_"));
				String namespace = str.substring(str.indexOf("_") + 1, str.length());
				ret.put(name, namespace);
			}
		}

		return ret;
	}
	
	//get a list of vocabulary name
	public Iterator<String> getAutoComplete() throws Exception{
		
		//get dataset connection
		Dataset dataset = getDatasetSearch(true);
		dataset.begin(ReadWrite.READ);
		
		List<String> resultList = new ArrayList<String>();
		ResultSet res;
	    
		String qs = StrUtils.strjoinNL
	            ( "SELECT ?label {{?s ?o ?label. } UNION { GRAPH ?g { ?s ?o ?label } }}") ;
		
		Query q = QueryFactory.create(qs) ;
		
		QueryExecution qe = QueryExecutionFactory.create(q, dataset);
		try {
			res = qe.execSelect();
			while( res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode label = soln.get("?label");
				
				if( null != label ){
					System.out.println(label.toString());
					resultList.add(label.toString());
				}
			}
		} finally {
			qe.close();
		}
		
		dataset.commit();
		
		//testDataset(dataset);
		dataset.end();
		dataset.close();
		
		return resultList.iterator();
	}
	
	public int validateTriples(String data, String errMsg){
		int errorLevel = 0;
		
		TripleValidation validation = new TripleValidation();
		
		return errorLevel;
	}
	
	
	private static Dataset getDataset() throws Exception {
		return TDBFactory.createDataset("VocabDataset");
	}
	
	// get a dataset with lucene index, lucene index is used for search.
	private static Dataset getDatasetSearch(boolean addLuceneIndex) throws Exception {
		TextQuery.init();
		
		Dataset datasetSearch = TDBFactory.createDataset("VocabDatasetSearch");
			
	    // Define the index mapping 
	    EntityDefinition entDef = new EntityDefinition("uri", "text", RDFS.label.asNode());
	    
	    Directory dir = null;
	    try{
		    // Lucene, index file
	    	dir = FSDirectory.open(new File("index-directory"));
	    }
	    catch(Exception e){
	        	
	    }
	    
	    // Join together into a dataset
	    datasetSearch = TextDatasetFactory.createLucene(datasetSearch, dir, entDef, null) ;

		return datasetSearch;
	}
	
	
	static int teststatistic = 0;
	
	//test code: get all data from dataset
	private void testDataset(Dataset dataset){
		dataset.begin(ReadWrite.READ);
		String qstest = StrUtils.strjoinNL( "SELECT *{ { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } }}") ;
        
		Query qtest = QueryFactory.create(qstest) ;
		
		teststatistic++;
		
		ResultSet restest;
		
		QueryExecution qetest = QueryExecutionFactory.create(qtest, dataset);

		try {
			
			restest = qetest.execSelect();
			while( restest.hasNext()) {
				QuerySolution soln = restest.next();
				RDFNode s = soln.get("?s");
				RDFNode p = soln.get("?p");
				RDFNode o = soln.get("?o");

				System.out.println(s.toString() + "  " + p.toString() + "  " + o.toString());
				logger.info(s.toString() + "  " + p.toString() + "  " + o.toString());
			}
		} finally{
			qetest.close();
			dataset.end() ;
			logger.info("______________________________" + teststatistic +" _________________________");
		}
	}
	
	// get file extension
	private String getFileExtension(String fileName){
	        Map<String, String> extentionmap = new HashMap<String, String>();
		
	    	extentionmap.put("rdf", "RDF/XML");
	    	extentionmap.put("ttl", "TURTLE");
	    	extentionmap.put("n3", "N3");
	    	extentionmap.put("nt", "NTRIPLES");
	    	extentionmap.put("", "RDF/XML");
	    
		String extension = "";
		int i = fileName.lastIndexOf('.');
		if (i > 0) {
			extension = fileName.substring(i + 1);
		}
		
		return extentionmap.get(extension);
	}
	
	static int rdfclassNr = 0;
	static int owlclassNr = 0;
	static int rdfpropertyNr = 0;
	static int owlobjectPropertyNr = 0;
	static int owldataobjectPropertyNr = 0;
	//get all the classes and properties from one model. and create another model to save it. used for search.
	private boolean extractClassesandProperties(Model originModel, Model resultModel, String vocabulary_name){
		
		ResultSet res;
		Property label = resultModel.createProperty("http://www.w3.org/2000/01/rdf-schema#label");
		Property type = resultModel.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		
		String pre = StrUtils.strjoinNL
	            ("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
	            , "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
	            , "PREFIX owl: <http://www.w3.org/2002/07/owl#>") ;
		//get rdfs classes
		String qs = StrUtils.strjoinNL
	            ( "SELECT DISTINCT ?classname WHERE{ "
	            , " ?classname a rdfs:Class. }") ;
        
		Query q = QueryFactory.create(pre+"\n"+qs) ;
		QueryExecution qe = QueryExecutionFactory.create(q, originModel);
		
		try {
			res = qe.execSelect();
			
			while( res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode a = soln.get("?classname");

				Resource subject =  resultModel.createResource(a.asResource().getNameSpace() + vocabulary_name + "_" + a.asResource().getLocalName());
				Literal object = resultModel.createLiteral(vocabulary_name + ":" + a.asResource().getLocalName());
				rdfclassNr++;
				Literal classLiteral = resultModel.createLiteral("class");
				subject.addProperty(label, object);
				subject.addProperty(type, classLiteral);
			}
		} finally {
			qe.close();
		}
		
		//get owl classes
		String qs1 = StrUtils.strjoinNL
	            ( "SELECT DISTINCT ?classname WHERE{ "
	            , " ?classname a owl:Class. }") ;
        
		Query q1 = QueryFactory.create(pre+"\n"+qs1) ;
		QueryExecution qe1 = QueryExecutionFactory.create(q1, originModel);
		
		try {
			res = qe1.execSelect();
			
			while( res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode a = soln.get("?classname");

				Resource subject =  resultModel.createResource(a.asResource().getNameSpace() + vocabulary_name + "_" + a.asResource().getLocalName());
				Literal object = resultModel.createLiteral(vocabulary_name + ":" + a.asResource().getLocalName());
				owlclassNr++;
				subject.addProperty(label, object);
				Literal classLiteral = resultModel.createLiteral("class");
				subject.addProperty(label, object);
				subject.addProperty(type, classLiteral);
			}
		} finally {
			qe.close();
		}
		
		//get rdf properties
		String qs2 = StrUtils.strjoinNL
	            ( "SELECT DISTINCT ?classname WHERE{ "
	            , " ?classname a rdfs:Property. }") ;
        
		Query q2 = QueryFactory.create(pre+"\n"+qs2) ;
		QueryExecution qe2 = QueryExecutionFactory.create(q2, originModel);
		
		try {
			res = qe2.execSelect();
			
			while( res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode a = soln.get("?classname");

				Resource subject =  resultModel.createResource(a.asResource().getNameSpace() + vocabulary_name + "_" + a.asResource().getLocalName());
				Literal object = resultModel.createLiteral(vocabulary_name + ":" + a.asResource().getLocalName());
				rdfpropertyNr++;
				subject.addProperty(label, object);
				
				Literal propertyLiteral = resultModel.createLiteral("property");
				subject.addProperty(label, object);
				subject.addProperty(type, propertyLiteral);
			}
		} finally {
			qe.close();
		}
		
		//get owl properties
		String qs3 = StrUtils.strjoinNL
	            ( "SELECT DISTINCT ?classname WHERE{ "
	            , " ?classname a owl:ObjectProperty. }") ;
        
		Query q3 = QueryFactory.create(pre+"\n"+qs3) ;
		QueryExecution qe3 = QueryExecutionFactory.create(q3, originModel);
		
		try {
			res = qe3.execSelect();
			
			while( res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode a = soln.get("?classname");

				Resource subject =  resultModel.createResource(a.asResource().getNameSpace() + vocabulary_name + "_" + a.asResource().getLocalName());
				Literal object = resultModel.createLiteral(vocabulary_name + ":" + a.asResource().getLocalName());
				owlobjectPropertyNr++;
				subject.addProperty(label, object);
				
				Literal propertyLiteral = resultModel.createLiteral("property");
				subject.addProperty(label, object);
				subject.addProperty(type, propertyLiteral);
			}
		} finally {
			qe.close();
		}
		
		String qs4 = StrUtils.strjoinNL
	            ( "SELECT DISTINCT ?classname WHERE{ "
	            , " ?classname a owl:DatatypeProperty. }") ;
        
		Query q4 = QueryFactory.create(pre+"\n"+qs4) ;
		QueryExecution qe4 = QueryExecutionFactory.create(q4, originModel);
		
		try {
			res = qe4.execSelect();
			
			while( res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode a = soln.get("?classname");

				Resource subject =  resultModel.createResource(a.asResource().getNameSpace() + vocabulary_name + "_" + a.asResource().getLocalName());
				Literal object = resultModel.createLiteral(vocabulary_name + ":" + a.asResource().getLocalName());
				owldataobjectPropertyNr++;
				subject.addProperty(label, object);
				
				Literal propertyLiteral = resultModel.createLiteral("property");
				subject.addProperty(label, object);
				subject.addProperty(type, propertyLiteral);
			}
		} finally {
			qe.close();
		}
		
		logger.info("rdfclass number:" + rdfclassNr);
		logger.info("owlclass number:" + owlclassNr);
		logger.info("rdfProperty number:" + rdfpropertyNr);
		logger.info("owlobjectproperty number:" + owlobjectPropertyNr);
		logger.info("owldataobjectProperty number:" + owldataobjectPropertyNr);
		
		return true;
	}
}
