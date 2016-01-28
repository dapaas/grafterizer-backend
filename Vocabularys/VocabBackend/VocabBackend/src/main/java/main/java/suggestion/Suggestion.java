package main.java.suggestion;

import java.util.List;

import main.java.prediction.EnumPredict;
import main.java.prediction.EnumType;
import main.java.prediction.Selection;


public abstract class Suggestion {
	
	public class SuggestionItem{
		public String content;
		public EnumPredict predictType;
	}
	
	abstract List<SuggestionItem> generateSuggestion(String[] selectedRowData, String[] selectedColumnData, Selection selection, String [] columnhead);
	
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
		
		Integer[] listrows = selection.getSelectedRows();
		
		if(listrows.length <= 0){
			return "";
		}
		
		String rows = "";
		
		for(int i = 0; i < listrows.length - 1; i++){
			rows += listrows[i];
			rows += ",";
		}
		
		rows += listrows[listrows.length - 1];
		
		return rows;
	}
	
	String getSelectedColumns(Selection selection, String [] columnhead){
		if(selection.getType() == EnumType.colSingle){
			return selection.getSelectedColumn().toString();
		}
		
		Integer [] listcolumns = selection.getSelectedColumns();
		
		if(listcolumns.length <= 0){
			return "";
		}
		
		String columns = "";
		
		for(int i = 0; i < listcolumns.length - 1; i++){
			columns += listcolumns[i];
			columns += ",";
		}
		
		columns += listcolumns[listcolumns.length - 1];
		
		return columns;
	}
	
	Boolean isRowEmpty(String[] tData, int row){
		
		String [] strlist = tData;
		
		for(int i = 0; i < strlist.length; i++){
			if(!strlist[i].isEmpty()){
				return false;
			}
		}
		
		return true;
	}
	
}
