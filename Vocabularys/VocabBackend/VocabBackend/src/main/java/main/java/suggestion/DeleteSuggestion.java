package main.java.suggestion;

import java.util.ArrayList;
import java.util.List;

import main.java.prediction.EnumPredict;
import main.java.prediction.EnumType;
import main.java.prediction.ProbabilityFile;
import main.java.prediction.Selection;

public class DeleteSuggestion extends Suggestion{
	
	@Override
	List<SuggestionItem> generateSuggestion(String[] selectedRowData, String[] selectedColumnData, Selection selection, String [] columnhead){
		List<SuggestionItem> oplist = new ArrayList<SuggestionItem>();
		
		String opStr = "";
		if(selection.getType() == EnumType.rowSingle){
			int index = 0;
			
			//delete empty rows
			if(isRowEmpty(selectedRowData, selection.getSelectedRow())){
				opStr = "Delete empty rows ";
				String closure = "";
				AddItem(oplist, opStr, EnumPredict.SingleRowDeleteEmpty, closure);
			}
			
			//delete rows where column1 = "data in column1"
			while(index < columnhead.length){
				opStr = "Delete rows where " + columnhead[index] + " = " + selectedRowData[index];
				String closure = "";
				AddItem(oplist, opStr, EnumPredict.SingleRowDeleteBasic, closure);
				index++;
			}
		}
		
		JSONARRY{
			text: merge rows 1,2
			key: 
			
			
		}
		
		//delete rows 1,2
		if(selection.getType() == EnumType.rowMulti){
			
			opStr = "Delete rows " + getSelectedRows(selection);
			String closure = "";
			AddItem(oplist, opStr, EnumPredict.MultiRowDeleteBasic, closure);
		}
		
		//delete column1
		if(selection.getType() == EnumType.colSingle){
			opStr = "Delete " + columnhead[selection.getSelectedColumn()];
			String closure = "";
			AddItem(oplist, opStr, EnumPredict.SingleColumnDeleteBasic, closure);
		}
		
		//delete column1,column2
		if(selection.getType() == EnumType.colMulti){
			
			opStr = "Delete " + getSelectedColumns(selection, columnhead);
			String closure = "";
			AddItem(oplist, opStr, EnumPredict.MultiColumnDeleteBasic, closure);
		}
		
		//delete rows where column1 contains selectedstring
		if(selection.getType() == EnumType.Text){
			opStr = "Delete rows where " + columnhead[selection.getTextcorY()] + " contains " + selection.getSelectedText();
		}
		
		return oplist;
	}
}
