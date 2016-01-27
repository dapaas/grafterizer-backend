package main.java.prediction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import main.java.suggestion.Suggestion.SuggestionItem;
import main.java.suggestion.SuggestionMgr;


public class Prediction {
	
	public class PredictionProbability {
		String strOp;
		Double probability;
		EnumPredict enumpredict;
		
		public EnumPredict getEnumpredict() {
			return enumpredict;
		}
		public void setEnumpredict(EnumPredict enumpredict) {
			this.enumpredict = enumpredict;
		}
		public String getStrOp() {
			return strOp;
		}
		public void setStrOp(String strOp) {
			this.strOp = strOp;
		}
		public Double getProbability() {
			return probability;
		}
		public void setProbability(Double probability) {
			this.probability = probability;
		}
	}

	List<PredictionProbability> suggestionList = new ArrayList<PredictionProbability>();

	Map<EnumType, EnumOp []> opMap = new HashMap<EnumType, EnumOp []>();
	
	public Prediction(){
		EnumOp [] singleRowList = {EnumOp.Delete, EnumOp.Copy, EnumOp.Cut, EnumOp.Split, EnumOp.Fill};
		opMap.put(EnumType.rowSingle, singleRowList);
		
		EnumOp [] multiRowList = {EnumOp.Delete, EnumOp.Copy, EnumOp.Cut, EnumOp.Split, EnumOp.Fill, EnumOp.Fold};
		opMap.put(EnumType.rowMulti, multiRowList);
		
		EnumOp [] singleColList = {EnumOp.Delete, EnumOp.Copy, EnumOp.Cut, EnumOp.Split, EnumOp.Fill};
		opMap.put(EnumType.colSingle, singleColList);
		
		EnumOp [] multiColList = {EnumOp.Delete, EnumOp.Copy, EnumOp.Cut, EnumOp.Split, EnumOp.Fill, EnumOp.Fold};
		opMap.put(EnumType.colMulti, multiColList);
		
		EnumOp [] textList = {EnumOp.Delete, EnumOp.Copy, EnumOp.Cut, EnumOp.Split, EnumOp.Fill};
		opMap.put(EnumType.Text, textList);
	}
	
	/*
	 * input:
	 * operation
	 * output:
	 * closure code
	 */
	public String parseOperation(String op, EnumPredict predictType){
		String [] l = op.split(" ");
		
		if(l.length <= 0){
			return "";
		}
		
		SuggestionMgr suggestionMgr = new SuggestionMgr();
		suggestionMgr.setSuggestion(l[0]);
		suggestionMgr.parseSuggestion(op, predictType);
		
		return op;
	}

	/*
	 * input:
	 * table data, operation
	 * output:
	 * operations
	 */
	public List<PredictionProbability> generateOperations(String[] tData, Selection selection, String [] columnhead){
		EnumOp [] ops = opMap.get(selection.type);
		
		suggestionList.clear();
		
		for(int i = 0; i < ops.length; i++){
			SuggestionMgr suggestionMgr = new SuggestionMgr();
			suggestionMgr.setSuggestion(ops[i]);
			List<SuggestionItem> l = suggestionMgr.getSuggestion(tData, selection, columnhead);
			
			Iterator<SuggestionItem> it = l.iterator();
			
			while(it.hasNext()){
				SuggestionItem item = it.next();
				AddPrediction(item.content, item.predictType);
			}
		}
		
		return suggestionList;
	}
	
	private Boolean AddPrediction(String opstr, EnumPredict e){
		Boolean inserted = false;
		Double probability = 0.0;
		
		PredictionProbability p = new PredictionProbability();
		p.setProbability(probability);
		p.setStrOp(opstr);
		p.setEnumpredict(e);
		
		probability = ProbabilityFile.getProbability(e);
		
		Iterator<PredictionProbability> it = suggestionList.iterator();
		
		while(it.hasNext()) {
			PredictionProbability tempPre = it.next();
			if(probability > tempPre.probability){
				int index = suggestionList.indexOf(tempPre);
				suggestionList.add(index, p);
				inserted = true;
			}
		}
		
		if(inserted == false){
			suggestionList.add(p);
		}
		
		return true;
	}

}
