package main.java.suggestion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import main.java.prediction.EnumPredict;
import main.java.prediction.EnumType;
import main.java.prediction.Selection;

public class SplitSuggestion extends Suggestion{
	
	@Override
	List<SuggestionItem> generateSuggestion(String[] selectedRowData, String[] selectedColumnData, Selection selection, String [] columnhead){
		List<SuggestionItem> oplist = new ArrayList<SuggestionItem>();
		
		String opStr = "";
		if(selection.getType() == EnumType.rowSingle){
			/*
			opStr = "Split " + selection.getSelectedRow();
			closure = "";
			AddItem(oplist, opStr, EnumPredict.SingleRowSplitBasic, closure);
			*/
		}
		
		if(selection.getType() == EnumType.rowMulti){
			/*
			opStr = "Split " + getSelectedRows(selection);
			closure = "";
			AddItem(oplist, opStr, EnumPredict.MultiRowSplitBasic, closure);
			*/
		}
		
		if(selection.getType() == EnumType.colSingle){
			List<List<String>> data = new ArrayList<List<String>>();
			
			//put all word in data array
			for(int i = 0; i < selectedColumnData.length; i++){
				String current = selectedColumnData[i];
				
				String [] ret = current.split(" |\\.|-|@|,");
				List<String> innerList = new ArrayList<String>();
				for(int j = 0; j < ret.length; j++){
					innerList.add(ret[j].trim());
				}
				data.add(innerList);
			}
			
			List<String> commenWord = findCommenWords(data);
			
			Iterator<String> it = commenWord.iterator();
			while(it.hasNext()){
				String word = it.next();
				opStr = "Split on " + word;
				Parameters p = getParameter("delete", false, null, 
						null, null, null);
				AddItem(oplist, opStr, EnumPredict.SingleColumnSplitCommenWord, p);
				
				opStr = "Split after " + word;
				p = getParameter("delete", false, null, 
						null, null, null);
				AddItem(oplist, opStr, EnumPredict.SingleColumnSplitCommenWord, p);
				
				opStr = "Split before " + word;
				p = getParameter("delete", false, null, 
						null, null, null);
				AddItem(oplist, opStr, EnumPredict.SingleColumnSplitCommenWord, p);
			}
		}
		
		if(selection.getType() == EnumType.colMulti){
			opStr = "Split " + getSelectedColumns(selection, columnhead);
			Parameters p = getParameter("delete", false, null, 
					null, null, null);
			AddItem(oplist, opStr, EnumPredict.MultiColumnSplitBasic, p);
		}
		
		return oplist;
	}
	
	private List<String> findCommenWords(List<List<String>> data){
		List<String> ret = new ArrayList<String>();
		
		Set<String> wordsSet = new HashSet<String>();
		Iterator<List<String>> outerIt = data.iterator();
		
		//put all exist word in a set
		while(outerIt.hasNext()){
			Iterator<String> innerIt = outerIt.next().iterator();
			
			while(innerIt.hasNext()){
				wordsSet.add(innerIt.next());
			}
		}
		
		int listLength = data.size();
		
		
		Iterator<String> wordIt = wordsSet.iterator();
		
		// check whether list contains a word
		while(wordIt.hasNext()){
			String word = wordIt.next();
			int containsNumber = 0;
			Iterator<List<String>> it = data.iterator();
			while(it.hasNext()){
				if(it.next().contains(word)){
					containsNumber++;
				}
			}
			
			if(containsNumber > listLength/2){
				ret.add(word);
			}
		}
		
		return ret;
	}
}
