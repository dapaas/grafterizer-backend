package validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class TripleValidation {
	
	/*private Model getLoadedOntology(String namespace, Map<String, Model> map){
		if(map.containsKey(namespace)){
			Model model = map.get(namespace);
			return model;
		}
		else{
			return null;
		}
	}*/
	
	public boolean loadVocabulary(Dataset dataset, Model model, Statement st, StringBuilder errMsg){
		Resource subject = st.getSubject();
		Resource predicate = st.getPredicate();
		RDFNode object = st.getObject();
		//Map<String, Model> ontologyMap = new HashMap<String, Model>();
		
		Model vocabularymodel = ModelFactory.createDefaultModel();
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
					errMsg.append(subject.toString() + " is not a class or property");
					return false;
				}
			}
			else{
				errMsg.append(subject.toString() + " is not a class or property");
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
					errMsg.append(predicate.toString() + " is not a class or property");
					return false;
				}
			}
			else{
				errMsg.append(predicate.toString() + " is not a class or property");
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
						errMsg.append(object.asResource().toString() + " is not a class or property");
						return false;
					}
				}
				else{
					errMsg.append(object.asResource().toString() + " is not a class or property");
					return false;
				}
			}
		}
		
		errMsg.append("No definition error");
		return true;
	}
	
	//find out undefined classes and properties, and misplaced class
	public boolean validateDefinition(Dataset dataset, Model model, Statement st, StringBuilder errMsg){
		Resource subject = st.getSubject();
		Resource predicate = st.getPredicate();
		RDFNode object = st.getObject();
		
		//reasoning to detect error
		OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_DL_MEM_RULE_INF);
		OntModel ontModel = ModelFactory.createOntologyModel(spec, model);
		
		OntResource subjectOnt = ontModel.getOntResource(subject);
		if( !subjectOnt.isClass() && !subjectOnt.isProperty() ){
			errMsg.append(subject.toString() + " is not a class or property"); 
			return false;
		}
		// predicate can not be class
		OntResource predicateOnt = ontModel.getOntResource(predicate);
		if( !predicateOnt.isProperty() ){
			errMsg.append(predicate.toString() + " is not a class or property"); 
			return false;
		}
		
		if( object.isURIResource() ){
			OntResource objectOnt = ontModel.getOntResource(object.asResource());
			
			if(!objectOnt.isClass() && !objectOnt.isProperty()){
				errMsg.append(object.asResource().toString() + " is not a class or property");
				return false;
			}
		}
		
		return true;
	}
	
	//whether domain of a property is compatible? or range?
	public boolean validateDomain(Model model, Statement st, StringBuilder errMsg){
		Resource subject = st.getSubject();
		Property predicate = st.getPredicate();
		
		//reasoning to detect error
		OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_DL_MEM_RULE_INF);
		OntModel ontModel = ModelFactory.createOntologyModel(spec, model);
				
		OntProperty predicateOnt = ontModel.getOntProperty(predicate.getURI());
		if( !predicateOnt.hasDomain(subject) ){
			errMsg.append("domain of " + predicate.toString() + " may not valid"); 
			return false;
		}
		
		return true;
	}
	
	public boolean validateRange(Model model, Statement st, StringBuilder errMsg){
		Property predicate = st.getPredicate();
		Resource object = st.getObject().asResource();
		
		//reasoning to detect error
		OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_DL_MEM_RULE_INF);
		OntModel ontModel = ModelFactory.createOntologyModel(spec, model);
				
		OntProperty predicateOnt = ontModel.getOntProperty(predicate.getURI());
		if( !predicateOnt.hasRange(object) ){
			errMsg.append("range of " + predicate.toString() + " may not valid"); 
			return false;
		}
		
		return true;
	}
	
	/*
	//get a list of rdfs:domain for a property
	private List<String> getDomainList(Model model, Property p){
		List<String> list= new ArrayList<String>();
		
		OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_DL_MEM_RULE_INF);
		OntModel ontModel = ModelFactory.createOntologyModel(spec, model);

		ontModel.setStrictMode(false);
  
		OntProperty prop = ontModel.getOntProperty(p.toString());
		ExtendedIterator <OntClass> opDomains = (ExtendedIterator <OntClass>) prop.listDomain();                        
		while(opDomains.hasNext()){
		    OntClass domain = opDomains.next();
		    list.add(domain.getLocalName());
		    System.out.println("DOMAIN: " + domain.getURI());
		}

		
		return list;
	}
	
	//get a list of rdfs:range for a property
	private List<String> getRangeList(Model model, Property p){
		List<String> list= new ArrayList<String>();
		
		OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_DL_MEM_RULE_INF);
		OntModel ontModel = ModelFactory.createOntologyModel(spec, model);

		ontModel.setStrictMode(false);
		
		OntProperty prop = ontModel.getOntProperty(p.toString());
   
		ExtendedIterator <OntClass> opRanges = (ExtendedIterator <OntClass>) prop.listRange();
		while(opRanges.hasNext()){
		    OntClass range = opRanges.next();
		    list.add(range.getLocalName());
		    System.out.println("RANGE: " + range.getURI());
		}
		return list;
	}
	*/
}
