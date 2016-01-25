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
	
	Prediction(){
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
	 * table data, type
	 * output:
	 * ranked operation
	 */
	List<String> getOperationlist(String[][] tData, EnumType type, String [] columns, int row){
		List<String> oplist = new ArrayList<String>();
		
		return oplist;
	}
	
	/*
	 * input:
	 * operation
	 * output:
	 * closure code
	 */
	String parseOperation(String op, EnumPredict predictType){
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
	List<PredictionProbability> generateOperations(String[][] tData, Selection selection, String [] columnhead){
		List<PredictionProbability> oplist = new ArrayList<PredictionProbability>();
		
		EnumOp [] ops = opMap.get(selection.type);
		
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
		
		return oplist;
	}
	
	Boolean AddPrediction(String opstr, EnumPredict e){
		Boolean inserted = false;
		Double probability = 0.0;
		
		PredictionProbability p = new PredictionProbability();
		p.setProbability(probability);
		p.setStrOp(opstr);
		
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
