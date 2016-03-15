package main.java.validation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.ExtendedIterator;

public class TripleValidation {
	
	public boolean validateDefinition(Dataset dataset, Model model, Statement st, StringBuilder errMsg){
		Resource subject = st.getSubject();
		Resource predicate = st.getPredicate();
		RDFNode object = st.getObject();
		//Map<String, Model> ontologyMap = new HashMap<String, Model>();
		
		Model vocabularymodel = ModelFactory.createDefaultModel();
		
		if(predicate.toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
		{
			return true;
		}
		
		// load vocabulary based on the prefix of subject or prefix of object in (?subject rdf:type ?object)
		// verify subject
		String namespace = subject.getNameSpace();

		if( dataset.containsNamedModel(namespace) ){
			vocabularymodel = dataset.getNamedModel(namespace);
			model.add(vocabularymodel);
		}
		else{
			Property type = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
			Statement s = model.getProperty(subject, type);
			if( s != null ){
				namespace = s.getObject().asResource().getNameSpace();
				if( dataset.containsNamedModel(namespace) ){
					vocabularymodel = dataset.getNamedModel(namespace);
					model.add(vocabularymodel);
				}
				else{
					errMsg.append(subject.getLocalName() + " is not a class or property");
					return false;
				}
			}
			else{
				errMsg.append(subject.getLocalName() + " is not a class or property");
				return false;
			}
		}

		//verify predicate
		namespace = predicate.getNameSpace();
		
		if( dataset.containsNamedModel(namespace) ){
			if( !model.containsAll(vocabularymodel) ){
				vocabularymodel = dataset.getNamedModel(namespace);
				model.add(vocabularymodel);
			}
		}
		else{
			Property type = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
			Statement s = model.getProperty(predicate, type);
			if( s != null ){
				namespace = s.getObject().asResource().getNameSpace();
				if( dataset.containsNamedModel(namespace) ){
					if( !model.containsAll(vocabularymodel) ){
						vocabularymodel = dataset.getNamedModel(namespace);
						model.add(vocabularymodel);
					}
				}
				else{
					errMsg.append(predicate.getLocalName() + " is not a class or property");
					return false;
				}
			}
			else{
				errMsg.append(predicate.getLocalName() + " is not a class or property");
				return false;
			}
		}
		
		//verify object
		if( object.isURIResource() ){
			namespace = object.asResource().getNameSpace();
			if( dataset.containsNamedModel(namespace) ){
				if( !model.containsAll(vocabularymodel) ){
					vocabularymodel = dataset.getNamedModel(namespace);
					model.add(vocabularymodel);
				}
			}
			else{
				Property type = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
				Statement s = model.getProperty(object.asResource(), type);
				if( s != null ){
					namespace = s.getObject().asResource().getNameSpace();
					if( dataset.containsNamedModel(namespace) ){
						if( !model.containsAll(vocabularymodel) ){
							vocabularymodel = dataset.getNamedModel(namespace);
							model.add(vocabularymodel);
						}
					}
					else{
						errMsg.append(object.asResource().getLocalName() + " is not a class or property");
						return false;
					}
				}
				else{
					errMsg.append(object.asResource().getLocalName() + " is not a class or property");
					return false;
				}
			}
		}
		
		return true;
	}
	
	//find out undefined classes and properties, and misplaced class
	public boolean validateMisplacedProperty(Dataset dataset, Model model, Statement st, OntModel ontModel, StringBuilder errMsg){
		Resource predicate = st.getPredicate();
		
		if(predicate.toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
		{
			return true;
		}
		
		// predicate can not be class
		OntResource predicateOnt = ontModel.getOntResource(predicate);
		if( predicateOnt.isClass() ){
			errMsg.append(predicate.getLocalName() + " is a class but used as a property"); 
			return false;
		}
		
		return true;
	}
	
	//whether domain of a property is compatible? or range?
	public boolean validateDomain(Model model, Statement st, OntModel ontModel, StringBuilder errMsg){
		System.out.print(model.toString());
		Resource subject = st.getSubject();
		Property predicate = st.getPredicate();
		//Property type = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		//Statement s = model.getProperty(subject.asResource(), type);
		
		if(predicate.toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
		{
			return true;
		}
		
		Iterator<String> domainIter = getDomainList(ontModel, predicate);
	
		if(domainIter == null){
			errMsg.append("domain of " + predicate.getLocalName() + " may not valid"); 
			return false;
		}
		
		while(domainIter.hasNext()){
			String str = domainIter.next();
			
			OntClass ontclass = ontModel.getOntClass(str);
			
			Property typeproperty = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
			Statement statement = model.getProperty(subject, typeproperty);
			if( statement != null ){
				if(ontclass.hasSubClass(statement.getResource())){
					return true;
				}
			}
		}
		errMsg.append("domain of " + predicate.getLocalName() + " may not valid"); 
		return false;
	}
	
	public boolean validateRange(Model model, Statement st, OntModel ontModel, StringBuilder errMsg){
		Property predicate = st.getPredicate();
		
		if(predicate.toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
		{
			return true;
		}
		
		if(st.getObject().isResource()){
			Resource object = st.getObject().asResource();
			Property type = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
			Statement s = model.getProperty(object.asResource(), type);
			Resource rangetype = s.getObject().asResource();
			
			Iterator<String> rangeIter = getRangeList(ontModel, predicate);
			
			if(rangeIter == null){
				errMsg.append("range of " + predicate.getLocalName() + " may not valid"); 
				return false;
			}
			
			while(rangeIter.hasNext()){
				String str = rangeIter.next();
				
				OntClass ontclass = ontModel.getOntClass(str);
				
				Property typeproperty = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
				Statement statement = model.getProperty(object, typeproperty);
				if( statement != null ){
					if(ontclass.hasSubClass(statement.getResource())){
						return true;
					}
				}

				if(str.equals(rangetype.toString())){
					return true;
				}
			}
		}
		else if(st.getObject().isLiteral()){
			OntProperty p = ontModel.getOntProperty(predicate.toString());
			
			OntResource r = p.getRange();
			
			if(r.toString().equals("http://www.w3.org/2000/01/rdf-schema#Literal")){
				return true;
			}
		}
		
		
		
		errMsg.append("range of " + predicate.getLocalName() + " may not valid"); 
		return false;
	}
	
	public boolean validateCardinality(Model model, Statement st, OntModel ontModel, StringBuilder errMsg){
		Property predicate = st.getPredicate();
		
		if(predicate.toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
		{
			return true;
		}
		
		
		return true;
	}
	
	
	//get a list of rdfs:domain for a property
	@SuppressWarnings("unchecked")
	private Iterator<String> getDomainList(OntModel ontModel, Property p){
		List<String> list= new ArrayList<String>();
  
		OntProperty prop = ontModel.getOntProperty(p.toString());
		ExtendedIterator <OntClass> opDomains = (ExtendedIterator <OntClass>) prop.listDomain();                        
		while(opDomains.hasNext()){
		    OntClass domain = opDomains.next();
		    list.add(domain.toString());
		}

		return list.iterator();
	}
	
	//get a list of rdfs:range for a property
	@SuppressWarnings("unchecked")
	private Iterator<String> getRangeList(OntModel ontModel, Property p){
		List<String> list= new ArrayList<String>();

		OntProperty prop = ontModel.getOntProperty(p.toString());

		ExtendedIterator <OntClass> opRanges = (ExtendedIterator <OntClass>) prop.listRange();
		while(opRanges.hasNext()){
		    OntClass range = opRanges.next();
		    list.add(range.toString());
		}
		return list.iterator();
	}
	
	public Iterator<String> getAllPrefix(String data){
		List<String> prefixes = new ArrayList<String>();
		String[] strlist = data.split(" ");
		
		for(int i = 0; i < strlist.length; i++){
			if(strlist[i].indexOf(':') != -1){
				String str = strlist[i].substring(0, strlist[i].indexOf(':'));
				if(!prefixes.contains(str)){
					prefixes.add(str);
				}
			}
		}
		
		return prefixes.iterator();
	}
	
	
}

