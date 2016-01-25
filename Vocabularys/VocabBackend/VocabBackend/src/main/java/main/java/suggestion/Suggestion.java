package main.java.suggestion;

import java.util.Iterator;
import java.util.List;

import main.java.prediction.EnumPredict;
import main.java.prediction.EnumType;
import main.java.prediction.Selection;


public abstract class Suggestion {
	
	public class SuggestionItem{
		public String content;
		public EnumPredict predictType;
	}
	
	abstract List<SuggestionItem> generateSuggestion(String[][] tData, Selection selection, String [] columnhead);
	
	abstract String parseSuggestion(String strSuggestion, EnumPredict predictType);
	
	void AddItem(List<SuggestionItem> l, String s, EnumPredict e){
		SuggestionItem item = new SuggestionItem();
		item.content = s;
		item.predictType = e;
		l.add(item);
	}
	
	String getSelectedRows(Selection selection){
		if(selection.getType() == EnumType.rowSingle){
			return selection.getSelectedRow().toString();
		}
		
		List<Integer> listrows = selection.getSelectedRows();
		Iterator<Integer> it = listrows.iterator();
		String rows = "";
		
		while(it.hasNext()){
			rows += it.next();
		}
		
		return rows;
	}
	
	String getSelectedColumns(Selection selection, String [] columnhead){
		if(selection.getType() == EnumType.colSingle){
			return selection.getSelectedColumn().toString();
		}
		
		List<Integer> listcolumns = selection.getSelectedColumns();
		Iterator<Integer> it = listcolumns.iterator();
		String columns = "";
		
		while(it.hasNext()){
			columns += columnhead[it.next()];
		}
		
		return columns;
	}
	
	Boolean isRowEmpty(String[][] tData, int row){
		
		String [] strlist = tData[row];
		
		for(int i = 0; i < strlist.length; i++){
			if(!strlist[i].isEmpty()){
				return false;
			}
		}
		
		return true;
	}
	
}
