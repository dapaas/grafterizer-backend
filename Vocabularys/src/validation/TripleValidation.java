package validation;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class TripleValidation {
	//find out undefined classes and properties
	public boolean validateDefinition(String errMsg){
		return true;
	}
	
	//predicate of triple should not be class
	public boolean validateMisplacedClass(String errMsg){
		return true;
	}
	
	//whether domain of a property is compatible? or range?
	public boolean validateDomain(String errMsg){
		return true;
	}
	
	public boolean validateRange(String errMsg){
		return true;
	}
	
	//get a list of rdfs:domain for a property
	private List<String> getDomainList(Model model, Property p){
		List<String> list= new ArrayList<String>();
		
		OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_DL_MEM_RULE_INF);
		OntModel ontModel = ModelFactory.createOntologyModel(spec, model);

		ontModel.setStrictMode(false);
  
		OntProperty prop = (OntProperty)p;
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
   
		OntProperty prop = (OntProperty)p;
		ExtendedIterator <OntClass> opRanges = (ExtendedIterator <OntClass>) prop.listRange();
		while(opRanges.hasNext()){
		    OntClass range = opRanges.next();
		    list.add(range.getLocalName());
		    System.out.println("RANGE: " + range.getURI());
		}
		return list;
	}
	
}
