package main.java.suggestion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import main.java.suggestion.Suggestion.Parameters;
import main.java.suggestion.Suggestion.SuggestionItem;
import main.java.suggestion.SuggestionMgr;


public class Prediction {
	
	public class PredictionProbability {
		String strOp;
		Double probability;
		EnumPredict enumpredict;
		Parameters para;
		
		public Parameters getPara() {
			return para;
		}
		public void setPara(Parameters para) {
			this.para = para;
		}
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
		EnumOp [] singleRowList = {EnumOp.Delete, EnumOp.Copy, EnumOp.Cut, EnumOp.Split, EnumOp.Fill, EnumOp.Other};
		opMap.put(EnumType.rowSingle, singleRowList);
		
		EnumOp [] multiRowList = {EnumOp.Delete, EnumOp.Copy, EnumOp.Cut, EnumOp.Split, EnumOp.Fill, EnumOp.Fold, EnumOp.Other};
		opMap.put(EnumType.rowMulti, multiRowList);
		
		EnumOp [] singleColList = {EnumOp.Delete, EnumOp.Copy, EnumOp.Cut, EnumOp.Split, EnumOp.Fill, EnumOp.Other};
		opMap.put(EnumType.colSingle, singleColList);
		
		EnumOp [] multiColList = {EnumOp.Delete, EnumOp.Copy, EnumOp.Cut, EnumOp.Split, EnumOp.Fill, EnumOp.Fold, EnumOp.Other};
		opMap.put(EnumType.colMulti, multiColList);
		
		EnumOp [] textList = {EnumOp.Delete, EnumOp.Copy, EnumOp.Cut, EnumOp.Split, EnumOp.Fill, EnumOp.Other};
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
				AddPrediction(item.content, item.predictType, item.parameters);
			}
		}
		
		return suggestionList;
	}
	
	private Boolean AddPrediction(String opstr, EnumPredict e, Parameters para){
		Boolean insert = false;
		Double probability = 0.0;
		
		PredictionProbability p = new PredictionProbability();
		
		probability = ProbabilityFile.getProbability(e).doubleValue();
		p.setProbability(probability);
		p.setStrOp(opstr);
		p.setEnumpredict(e);
		p.setPara(para);
		
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
