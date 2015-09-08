package dao;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.text.EntityDefinition;
import org.apache.jena.query.text.TextDatasetFactory;
import org.apache.jena.query.text.TextQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDFS;
import org.apache.log4j.Logger;

import validation.TripleValidation;

public class VocabularyDAO {

	private String getStackTrace(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String str = sw.toString();

		return str;
	}

	private static Logger logger = Logger.getLogger(VocabularyDAO.class);

	// add new vocabulary
	public boolean insertVocabulary(String prefix, String namespace,
			String location, String fileContent) throws Exception {
		if (prefix.isEmpty() || namespace.isEmpty()) {
			return false;
		}

		try {
			// get dataset and model
			Dataset datasetVocab = getDataset();
			Model model = ModelFactory.createDefaultModel();

			if (location.isEmpty()) {
				model.read(new ByteArrayInputStream(fileContent.getBytes()),
						null, getFileExtension(location));
			} else {
				FileManager.get().readModel(model, location,
						getFileExtension(location));
			}

			Dataset datasetSearch = getDatasetSearch();
			Model searchModel = ModelFactory.createDefaultModel();

			datasetSearch.begin(ReadWrite.WRITE);
			datasetVocab.begin(ReadWrite.WRITE);

			// create a search model and put all class name, property name in
			// it, as a index for search.
			// vocabularyModel is used for save all the triple about a
			// vocabulary.
			extractClassesandProperties(model, searchModel, prefix);

			datasetSearch
					.addNamedModel(namespace + "___" + prefix, searchModel);
			datasetVocab.addNamedModel(namespace, model);

			datasetVocab.commit();
			datasetVocab.end();
			datasetVocab.close();

			datasetSearch.commit();
			// testDataset(datasetSearch);
			datasetSearch.end();
			datasetSearch.close();

			model.close();
			searchModel.close();
		} catch (Exception e) {
			String strErr = getStackTrace(e);
			System.out.println(strErr);
			return false;
		}

		return true;
	}
	
	//get classes of a vocabulary
	public Boolean getClasses(String name, String namespace, List<String> classList) throws Exception{
		
		String key = namespace + "___" + name;
		
		Dataset d = getDatasetSearch();
		
		d.begin(ReadWrite.READ);
		
		if(!d.containsNamedModel(key))
		{
			return false;
		}
		
		Model m = d.getNamedModel(key);
		
		if (m == null){
			return false;
		}
		
		String pre = StrUtils.strjoinNL(
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>");
		String qsclass = StrUtils.strjoinNL("SELECT ?label { ?s rdf:type 'class'. ?s rdfs:label ?label.}");

		Query qclass = QueryFactory.create(pre + "\n" + qsclass);

		QueryExecution qeclass = QueryExecutionFactory.create(qclass, m);
		try {
			ResultSet res = qeclass.execSelect();
			while (res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode label = soln.get("?label");

				if (null != label) {
					String str = label.toString();
					str = str.substring(str.indexOf(":") + 1, str.length());
					classList.add(str);
				}
			}
		} finally {
			qeclass.close();
		}
		
		d.end();
		d.close();
		
		return true;
	}
	
	//get classes of a vocabulary
	public Boolean getProperties(String name, String namespace, List<String> propertyList) throws Exception{

		String key = namespace + "___" + name;

		Dataset d = getDatasetSearch();
		
		d.begin(ReadWrite.READ);
		
		if(!d.containsNamedModel(key))
		{
			return false;
		}

		Model m = d.getNamedModel(key);

		if (m == null){
			return false;
		}

		String pre = StrUtils.strjoinNL(
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>");
		String qsproperty = StrUtils.strjoinNL("SELECT ?label {?s rdf:type 'property'. ?s rdfs:label ?label.}");

		Query qproperty = QueryFactory.create(pre + "\n" + qsproperty);

		QueryExecution qeproperty = QueryExecutionFactory.create(qproperty, m);
		try {
			ResultSet res = qeproperty.execSelect();
			while (res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode label = soln.get("?label");

				if (null != label) {
					String str = label.toString();
					str = str.substring(str.indexOf(":") + 1, str.length());
					propertyList.add(str);
				}
			}
		} finally {
			qeproperty.close();
		}

		d.end();
		d.close();
		return true;
	}
	
	

	public Boolean getClassAndPropertyFromVocabulary(String name,
			String namespace, String location, String fileContent,
			String islocal, List<String> classlist, List<String> propertylist) {
		Model model = ModelFactory.createDefaultModel();

		if (islocal == "true") {
			model.read(new ByteArrayInputStream(fileContent.getBytes()), null,
					getFileExtension(location));
		} else {
			FileManager.get().readModel(model, location,
					getFileExtension(location));
		}

		ResultSet res;

		String pre = StrUtils.strjoinNL(
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>");
		// get rdfs classes
		String qs = StrUtils.strjoinNL("SELECT DISTINCT ?classname WHERE{ ",
				" ?classname a rdfs:Class. }");

		Query q = QueryFactory.create(pre + "\n" + qs);
		QueryExecution qe = QueryExecutionFactory.create(q, model);

		try {
			res = qe.execSelect();

			while (res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode a = soln.get("?classname");

				classlist.add(name + ":" + a.asResource().getLocalName());
			}
		} finally {
			qe.close();
		}

		// get owl classes
		String qsOwlClass = StrUtils.strjoinNL(
				"SELECT DISTINCT ?classname WHERE{ ",
				" ?classname a owl:Class. }");

		Query qOwlClass = QueryFactory.create(pre + "\n" + qsOwlClass);
		QueryExecution qeOwlClass = QueryExecutionFactory.create(qOwlClass,
				model);

		try {
			res = qeOwlClass.execSelect();

			while (res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode a = soln.get("?classname");
				if (classlist.contains(name + ":"
						+ a.asResource().getLocalName()) == false) {
					classlist.add(name + ":" + a.asResource().getLocalName());
				}
			}
		} finally {
			qe.close();
		}

		// get rdf properties
		String qsProperty = StrUtils.strjoinNL(
				"SELECT DISTINCT ?classname WHERE{ ",
				" ?classname a rdf:Property. }");

		Query qProperty = QueryFactory.create(pre + "\n" + qsProperty);
		QueryExecution qeProperty = QueryExecutionFactory.create(qProperty,
				model);

		try {
			res = qeProperty.execSelect();

			while (res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode a = soln.get("?classname");
				propertylist.add(name + ":" + a.asResource().getLocalName());
			}
		} finally {
			qe.close();
		}

		// get owl properties
		String qsOwlProperty = StrUtils.strjoinNL(
				"SELECT DISTINCT ?classname WHERE{ ",
				" ?classname a owl:ObjectProperty. }");

		Query qOwlProperty = QueryFactory.create(pre + "\n" + qsOwlProperty);
		QueryExecution qeOwlProperty = QueryExecutionFactory.create(
				qOwlProperty, model);

		try {
			res = qeOwlProperty.execSelect();

			while (res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode a = soln.get("?classname");
				if (propertylist.contains(name + ":"
						+ a.asResource().getLocalName()) == false) {
					propertylist
							.add(name + ":" + a.asResource().getLocalName());
				}
			}
		} finally {
			qe.close();
		}

		String qsOwlDataProperty = StrUtils.strjoinNL(
				"SELECT DISTINCT ?classname WHERE{ ",
				" ?classname a owl:DatatypeProperty. }");

		Query qOwlDataProperty = QueryFactory.create(pre + "\n"
				+ qsOwlDataProperty);
		QueryExecution qeOwlDataProperty = QueryExecutionFactory.create(
				qOwlDataProperty, model);

		try {
			res = qeOwlDataProperty.execSelect();

			while (res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode a = soln.get("?classname");
				if (propertylist.contains(name + ":"
						+ a.asResource().getLocalName()) == false) {
					propertylist
							.add(name + ":" + a.asResource().getLocalName());
				}
			}
		} finally {
			qe.close();
		}

		return true;
	}

	// delete vocabulary based on vocabulary name
	public boolean deleteVocabulary(String name) throws Exception {

		if (name.isEmpty()) {
			return false;
		}

		Dataset dataset = getDataset();
		Dataset datasetSearch = getDatasetSearch();

		datasetSearch.begin(ReadWrite.WRITE);

		if (datasetSearch.containsNamedModel(name)) {
			datasetSearch.removeNamedModel(name);
		}

		datasetSearch.commit();
		datasetSearch.end();
		datasetSearch.close();

		String namespace = name;

		if (name.indexOf("___") > 0) {
			namespace = name.substring(0, name.indexOf("___"));
		}

		dataset.begin(ReadWrite.WRITE);

		if (dataset.containsNamedModel(namespace)) {
			dataset.removeNamedModel(namespace);
		}

		dataset.commit();
		dataset.end();
		dataset.close();

		return true;
	}

	// update vocabulary with a new uri
	public boolean updataVocabulary(String name, String namespace,
			String newPath) throws Exception {

		if (name.isEmpty() || newPath.isEmpty() || namespace.isEmpty()) {
			return false;
		}

		Dataset dataset = getDataset();

		dataset.begin(ReadWrite.WRITE);

		if (dataset.containsNamedModel(name)) {
			Model model = ModelFactory.createDefaultModel();
			org.apache.jena.util.FileManager.get().readModel(model, newPath,
					"RDF/XML");

			dataset.replaceNamedModel(name + "___" + namespace, model);
		}

		dataset.commit();
		dataset.end();
		dataset.close();

		Dataset datasetSearch = getDataset();

		datasetSearch.begin(ReadWrite.WRITE);

		if (datasetSearch.containsNamedModel(name)) {
			Model model = ModelFactory.createDefaultModel();
			org.apache.jena.util.FileManager.get().readModel(model, newPath,
					"RDF/XML");

			dataset.replaceNamedModel(namespace, model);
		}

		datasetSearch.commit();
		datasetSearch.end();
		datasetSearch.close();

		return true;
	}

	// search vocabulary based on keyword
	public Boolean searchVocabulary(String keyword, List<String> classList,
			List<String> propertyList) throws Exception {

		String prefix = "";
		String name = keyword;
		if (keyword.contains(":")) {
			prefix = keyword.substring(0, keyword.indexOf(":"));
			name = keyword
					.substring(keyword.indexOf(":") + 1, keyword.length());
		}

		// get dataset connection
		Dataset dataset = getDatasetSearch();
		dataset.begin(ReadWrite.READ);

		ResultSet res;

		String pre = StrUtils.strjoinNL(
				"PREFIX text: <http://jena.apache.org/text#>",
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
		String qsclass = StrUtils.strjoinNL("SELECT ?label ",
				"{{ ?s text:query (rdfs:label '*" + name + "*'). ",
				"?s rdfs:label ?label. ?s rdf:type 'class'.} UNION ",
				"{ GRAPH ?g {", "?s text:query (rdfs:label '*" + name + "*').",
				"?s rdfs:label ?label.", "?s rdf:type 'class'.", "}}}");

		Query qclass = QueryFactory.create(pre + "\n" + qsclass);

		QueryExecution qeclass = QueryExecutionFactory.create(qclass, dataset);
		try {
			res = qeclass.execSelect();
			while (res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode label = soln.get("?label");

				if (null != label) {
					String str = label.toString();
					if (str.contains(prefix)) {
						classList.add(str);
					}
				}
			}
		} finally {
			qeclass.close();
		}

		String qsproperty = StrUtils.strjoinNL("SELECT ?label ",
				"{{ ?s text:query (rdfs:label '*" + name + "*'). ",
				"?s rdfs:label ?label. ?s rdf:type 'property'.} UNION ",
				"{ GRAPH ?g {", "?s text:query (rdfs:label '*" + name + "*').",
				"?s rdfs:label ?label.", "?s rdf:type 'property'.", "}}}");

		Query qproperty = QueryFactory.create(pre + "\n" + qsproperty);

		QueryExecution qeproperty = QueryExecutionFactory.create(qproperty,
				dataset);
		try {
			res = qeproperty.execSelect();
			while (res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode label = soln.get("?label");

				if (null != label) {
					String str = label.toString();
					if (str.contains(prefix)) {
						propertyList.add(str);
					}
				}
			}
		} finally {
			qeproperty.close();
		}

		dataset.commit();

		// testDataset(dataset);
		dataset.end();
		dataset.close();

		return true;
	}

	// get a list of vocabulary name
	public Map<String, String> getAllVocabularyName() throws Exception {

		Dataset datasetsearch = getDatasetSearch();

		Iterator<String> it;
		Map<String, String> ret = new HashMap<>();

		datasetsearch.begin(ReadWrite.READ);

		try {
			it = datasetsearch.listNames();
			datasetsearch.commit();
		} finally {
			datasetsearch.end();
		}
		// testDataset(datasetsearch);
		datasetsearch.close();

		while (it.hasNext()) {
			String str = it.next();
			if (str.indexOf("___") > 0) {
				String namespace = str.substring(0, str.indexOf("___"));
				String name = str.substring(str.indexOf("___") + 3,
						str.length());
				ret.put(name, namespace);
			}
		}

		return ret;
	}

	// get a list of vocabulary name
	public Iterator<String> getAutoComplete() throws Exception {

		// get dataset connection
		Dataset dataset = getDatasetSearch();
		dataset.begin(ReadWrite.READ);

		List<String> resultList = new ArrayList<String>();
		ResultSet res;

		String qs = StrUtils
				.strjoinNL("SELECT ?label {{?s ?o ?label. } UNION { GRAPH ?g { ?s ?o ?label } }}");

		Query q = QueryFactory.create(qs);

		QueryExecution qe = QueryExecutionFactory.create(q, dataset);
		try {
			res = qe.execSelect();
			while (res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode label = soln.get("?label");

				if (null != label) {
					System.out.println(label.toString());
					resultList.add(label.toString());
				}
			}
		} finally {
			qe.close();
		}

		dataset.commit();

		// testDataset(dataset);
		dataset.end();
		dataset.close();

		return resultList.iterator();
	}
	
	private String formatTriple(Statement statement, Model model){
		//get a statement which has a easy readable format
		Resource subject = statement.getSubject();
		String prefix = model.getNsURIPrefix(subject.getNameSpace());
		String name = subject.getLocalName();
		String subjectValue = prefix + ":" + name;
		
		Property predicate = statement.getPredicate();
		prefix = model.getNsURIPrefix(predicate.getNameSpace());
		name = predicate.getLocalName();
		String predicateValue = prefix + ":" + name;
		
		String objectValue = "";
		RDFNode objectnode = statement.getObject();
		if(objectnode.isLiteral()){
			objectValue = objectnode.asLiteral().toString();
		}else if(objectnode.isResource()){
			Resource object = objectnode.asResource();
			prefix = model.getNsURIPrefix(object.getNameSpace());
			name = object.getLocalName();
			objectValue = prefix + ":" + name;
		}
		
		String strStatement = subjectValue + " " + predicateValue + " " + objectValue;
		
		return strStatement;
	}

	public int validateTriples(String path, String data, LinkedHashMap<String, String> errorMap) throws Exception{
		int errorLevel = 0;
		
		Dataset dataset = getDataset();
		Dataset datasetSearch = getDatasetSearch();
		
		//get a map of prefix and namespace from dataset
		datasetSearch.begin(ReadWrite.READ);
		Map<String, String> namespaceMap = new HashMap<String, String>();
		Iterator<String> it = datasetSearch.listNames();
		while(it.hasNext())
		{
			String str = it.next();
			String strNamespace = str.substring(0, str.indexOf("___"));
			String strPrefix= str.substring(str.indexOf("___") + 3, str.length());
			namespaceMap.put(strPrefix, strNamespace);
		}
		
		TripleValidation validation = new TripleValidation();
		//data += "sim:yexl rdf:type foaf:Person.\n";
		//data += "sim:dine rdf:type foaf:Person.\n";
		//data += "sim:yexl foaf:knows sim:sam.\n";
		//data += "sim:yexl foaf:knows sim:dine.\n";
		data = data.replace(".", ".\n");

		Iterator<String> itPrefix = validation.getAllPrefix(data);
		
		String strRDF = "";
		
		//@prefix rdf: <http://www.w3.org/>.

		// add prefix definition to mapping
		while(itPrefix.hasNext()){
			String prefix = itPrefix.next();
			
			if(namespaceMap.containsKey(prefix)){
				strRDF += "@prefix " + prefix + ":<" + namespaceMap.get(prefix) + ">.\n";
			}
			else{
				if(prefix.isEmpty()){
					strRDF += "@prefix :<http://temp/>.\n";
				}
				else{
					strRDF += "@prefix " + prefix + ":<http://temp/>.\n";
				}
				
			}
		}
		
		path = "/home/yexl/Desktop/sims.ttl";
		Model model = ModelFactory.createDefaultModel();
		//if(data.isEmpty())
		{
			//model.read(path, "TURTLE") ;
		}
		//else
		{
			strRDF += data;
			model.read(new ByteArrayInputStream(strRDF.getBytes()), null, "TURTLE");
		}
		
		//get new added triple
		List<Statement> listStatement = new ArrayList<Statement>();
		StmtIterator iterStatement = model.listStatements();
		if( iterStatement != null ){
			while( iterStatement.hasNext() ){
				listStatement.add(iterStatement.nextStatement());
			}
		}
		
		//FileManager.get().readModel( model, "http://www.w3.org/1999/02/22-rdf-syntax-ns#", "RDF/XML" );
		
		Iterator<Statement> iter = listStatement.iterator();
		if( iter != null ){
			while( iter.hasNext() ){
				Statement statement = iter.next();
				
				StringBuilder err = new StringBuilder();
				
				String strStatement = formatTriple(statement, model);
				
				//System.out.print(model.toString());
				if( validation.validateDefinition(dataset, model, statement, err) ){
					errorLevel = 0;
					System.out.print(err.toString());
				}
				else{
					errorLevel = 1;
					System.out.print(err.toString());
					errorMap.put(strStatement, err.toString());
					continue;
				}
				
				OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_DL_MEM_RULE_INF);
				OntModel ontModel = ModelFactory.createOntologyModel(spec, model);
				
				if( validation.validateMisplacedProperty(dataset, model, statement, ontModel, err) ){
					errorLevel = 0;
				}
				else{
					errorLevel = 1;
					System.out.print(err.toString());
					errorMap.put(strStatement, err.toString());
					continue;
				}
				
				if( validation.validateDomain(model, statement, ontModel, err) ){
					errorLevel = 0;
				}
				else{
					errorLevel = 1;
					System.out.print(err.toString());
					errorMap.put(strStatement, err.toString());
					continue;
				}
				
				if( validation.validateRange(model, statement, ontModel, err) ){
					errorLevel = 0;
				}
				else{
					errorLevel = 1;
					System.out.print(err.toString());
					errorMap.put(strStatement, err.toString());
					continue;
				}
			}
			System.out.print("pass");
		}
		
		datasetSearch.commit();
		datasetSearch.end();
		datasetSearch.close();
		return errorLevel;
	}

	private static Dataset getDataset() throws Exception {
		return TDBFactory.createDataset("VocabDataset");
	}

	// get a dataset with lucene index, lucene index is used for search.
	private static Dataset getDatasetSearch() throws Exception {
		

		TextQuery.init();

		Dataset datasetSearch = TDBFactory.createDataset("VocabDatasetSearch");

		// Define the index mapping
		EntityDefinition entDef = new EntityDefinition("uri", "text",
				RDFS.label.asNode());

		Directory dir = null;
		try {
			// Lucene, index file
			dir = FSDirectory.open(new File("index-directory"));
		} catch (Exception e) {

		}

		// Join together into a dataset
		datasetSearch = TextDatasetFactory.createLucene(datasetSearch, dir,
				entDef, null);

		
		return datasetSearch;
	}

	static int teststatistic = 0;

	// test code: get all data from dataset
	private void testDataset(Dataset dataset) {
		dataset.begin(ReadWrite.READ);
		String qstest = StrUtils
				.strjoinNL("SELECT *{ { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } }}");

		Query qtest = QueryFactory.create(qstest);

		teststatistic++;

		ResultSet restest;

		QueryExecution qetest = QueryExecutionFactory.create(qtest, dataset);

		try {

			restest = qetest.execSelect();
			while (restest.hasNext()) {
				QuerySolution soln = restest.next();
				RDFNode s = soln.get("?s");
				RDFNode p = soln.get("?p");
				RDFNode o = soln.get("?o");

				System.out.println(s.toString() + "  " + p.toString() + "  "
						+ o.toString());
				logger.info(s.toString() + "  " + p.toString() + "  "
						+ o.toString());
			}
		} finally {
			qetest.close();
			dataset.end();
			logger.info("______________________________" + teststatistic
					+ " _________________________");
		}
	}

	// get file extension
	private String getFileExtension(String fileName) {
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

	// get all the classes and properties from one model. and create another
	// model to save it. used for search.
	private boolean extractClassesandProperties(Model originModel,
			Model resultModel, String vocabulary_name) {

		ResultSet res;
		Property label = resultModel
				.createProperty("http://www.w3.org/2000/01/rdf-schema#label");
		Property type = resultModel
				.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

		String pre = StrUtils.strjoinNL(
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>");
		// get rdfs classes
		String qs = StrUtils.strjoinNL("SELECT DISTINCT ?classname WHERE{ ",
				" ?classname a rdfs:Class. }");

		Query q = QueryFactory.create(pre + "\n" + qs);
		QueryExecution qe = QueryExecutionFactory.create(q, originModel);

		try {
			res = qe.execSelect();

			while (res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode a = soln.get("?classname");

				Resource subject = resultModel.createResource(a.asResource()
						.getNameSpace()
						+ vocabulary_name
						+ "___"
						+ a.asResource().getLocalName());
				Literal object = resultModel.createLiteral(vocabulary_name
						+ ":" + a.asResource().getLocalName());
				rdfclassNr++;
				Literal classLiteral = resultModel.createLiteral("class");
				subject.addProperty(label, object);
				subject.addProperty(type, classLiteral);
			}
		} finally {
			qe.close();
		}

		// get owl classes
		String qs1 = StrUtils.strjoinNL("SELECT DISTINCT ?classname WHERE{ ",
				" ?classname a owl:Class. }");

		Query q1 = QueryFactory.create(pre + "\n" + qs1);
		QueryExecution qe1 = QueryExecutionFactory.create(q1, originModel);

		try {
			res = qe1.execSelect();

			while (res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode a = soln.get("?classname");

				Resource subject = resultModel.createResource(a.asResource()
						.getNameSpace()
						+ vocabulary_name
						+ "___"
						+ a.asResource().getLocalName());
				Literal object = resultModel.createLiteral(vocabulary_name
						+ ":" + a.asResource().getLocalName());
				owlclassNr++;
				subject.addProperty(label, object);
				Literal classLiteral = resultModel.createLiteral("class");
				subject.addProperty(label, object);
				subject.addProperty(type, classLiteral);
			}
		} finally {
			qe.close();
		}

		// get rdf properties
		String qs2 = StrUtils.strjoinNL("SELECT DISTINCT ?classname WHERE{ ",
				" ?classname a rdf:Property. }");

		Query q2 = QueryFactory.create(pre + "\n" + qs2);
		QueryExecution qe2 = QueryExecutionFactory.create(q2, originModel);

		try {
			res = qe2.execSelect();

			while (res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode a = soln.get("?classname");

				Resource subject = resultModel.createResource(a.asResource()
						.getNameSpace()
						+ vocabulary_name
						+ "___"
						+ a.asResource().getLocalName());
				Literal object = resultModel.createLiteral(vocabulary_name
						+ ":" + a.asResource().getLocalName());
				rdfpropertyNr++;
				subject.addProperty(label, object);

				Literal propertyLiteral = resultModel.createLiteral("property");
				subject.addProperty(label, object);
				subject.addProperty(type, propertyLiteral);
			}
		} finally {
			qe.close();
		}

		// get owl properties
		String qs3 = StrUtils.strjoinNL("SELECT DISTINCT ?classname WHERE{ ",
				" ?classname a owl:ObjectProperty. }");

		Query q3 = QueryFactory.create(pre + "\n" + qs3);
		QueryExecution qe3 = QueryExecutionFactory.create(q3, originModel);

		try {
			res = qe3.execSelect();

			while (res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode a = soln.get("?classname");

				Resource subject = resultModel.createResource(a.asResource()
						.getNameSpace()
						+ vocabulary_name
						+ "___"
						+ a.asResource().getLocalName());
				Literal object = resultModel.createLiteral(vocabulary_name
						+ ":" + a.asResource().getLocalName());
				owlobjectPropertyNr++;
				subject.addProperty(label, object);

				Literal propertyLiteral = resultModel.createLiteral("property");
				subject.addProperty(label, object);
				subject.addProperty(type, propertyLiteral);
			}
		} finally {
			qe.close();
		}

		String qs4 = StrUtils.strjoinNL("SELECT DISTINCT ?classname WHERE{ ",
				" ?classname a owl:DatatypeProperty. }");

		Query q4 = QueryFactory.create(pre + "\n" + qs4);
		QueryExecution qe4 = QueryExecutionFactory.create(q4, originModel);

		try {
			res = qe4.execSelect();

			while (res.hasNext()) {
				QuerySolution soln = res.next();
				RDFNode a = soln.get("?classname");

				Resource subject = resultModel.createResource(a.asResource()
						.getNameSpace()
						+ vocabulary_name
						+ "___"
						+ a.asResource().getLocalName());
				Literal object = resultModel.createLiteral(vocabulary_name
						+ ":" + a.asResource().getLocalName());
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
