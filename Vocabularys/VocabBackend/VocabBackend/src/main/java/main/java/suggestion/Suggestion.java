package main.java.suggestion;

import java.util.List;


public abstract class Suggestion {
	
	public class Parameters{
		public String operation;
		public Boolean isEmpty;
		public String relatedColumnHead;
		public String cellData;
		public Integer [] rows;
		public Integer [] columns;
	}
	
	public class SuggestionItem{
		public String content;
		public EnumPredict predictType;
		public Parameters parameters;
	}
	
	abstract List<SuggestionItem> generateSuggestion(String[] selectedRowData, String[] selectedColumnData, Selection selection, String [] columnhead);
	
	void AddItem(List<SuggestionItem> l, String content, EnumPredict e, Parameters p){
		SuggestionItem item = new SuggestionItem();
		item.content = content;
		item.predictType = e;
		item.parameters = p;
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
			columns += "\"" + columnhead[i] + "\"";
			columns += ",";
		}
		
		columns += "\"" + columnhead[listcolumns.length - 1] + "\"";
		
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
	
	Parameters getParameter(String operation, Boolean isEmpty, String relatedColumnHead
			, String cellData, Integer [] columns, Integer [] rows){
		Parameters p = new Parameters();
		p.operation = operation;
		p.isEmpty = isEmpty;
		p.relatedColumnHead = relatedColumnHead;
		p.cellData = cellData;
		p.columns = columns;
		p.rows = rows;
		
		return p;
	}
}
