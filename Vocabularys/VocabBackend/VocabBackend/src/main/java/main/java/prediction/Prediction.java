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
		String strClosure;
		
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
		public String getStrClosure() {
			return strClosure;
		}
		public void setStrClosure(String strClosure) {
			this.strClosure = strClosure;
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
	public void incProbability(EnumPredict predictType){
		SuggestionMgr suggestionMgr = new SuggestionMgr();
		suggestionMgr.parseSuggestion(predictType);
	}

	/*
	 * input:
	 * table data, operation
	 * output:
	 * operations
	 */
	public List<PredictionProbability> generateOperations(String[] selectedRowData, String [] selectedColumnData, Selection selection, String [] columnhead){
		EnumOp [] ops = opMap.get(selection.type);
		
		suggestionList.clear();
		
		for(int i = 0; i < ops.length; i++){
			SuggestionMgr suggestionMgr = new SuggestionMgr();
			suggestionMgr.setSuggestion(ops[i]);
			List<SuggestionItem> l = suggestionMgr.getSuggestion(selectedRowData, selectedColumnData, selection, columnhead);
			
			Iterator<SuggestionItem> it = l.iterator();
			
			while(it.hasNext()){
				SuggestionItem item = it.next();
				AddPrediction(item.content, item.predictType, item.closureCode);
			}
		}
		
		return suggestionList;
	}
	
	private Boolean AddPrediction(String opstr, EnumPredict e, String closure){
		Boolean insert = false;
		Double probability = 0.0;
		
		PredictionProbability p = new PredictionProbability();
		p.setProbability(probability);
		p.setStrOp(opstr);
		p.setEnumpredict(e);
		p.setStrClosure(closure);
		
		probability = ProbabilityFile.getProbability(e).doubleValue();
		
		Iterator<PredictionProbability> it = suggestionList.iterator();
		
		int size = suggestionList.size();
		int index = 0;
		
		for(int i = 0; i < size; i++){
			PredictionProbability tempPre = it.next();
			if(probability > tempPre.probability){
				index = suggestionList.indexOf(tempPre);
				
				insert = true;
			}
		}
		
		if(insert){
			suggestionList.add(index, p);
		}
		else{
			suggestionList.add(p);
		}
		
		return true;
	}

}
