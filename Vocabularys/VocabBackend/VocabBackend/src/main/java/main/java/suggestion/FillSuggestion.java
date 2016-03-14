package main.java.suggestion;

import java.util.ArrayList;
import java.util.List;

import main.java.prediction.EnumPredict;
import main.java.prediction.EnumType;
import main.java.prediction.ProbabilityFile;
import main.java.prediction.Selection;

public class FillSuggestion extends Suggestion{
	
	@Override
	List<SuggestionItem> generateSuggestion(String[] selectedRowData, String[] selectedColumnData, Selection selection, String [] columnhead){
		List<SuggestionItem> oplist = new ArrayList<SuggestionItem>();
		String closure = "";
		
		String opStr = "";
		if(selection.getType() == EnumType.rowSingle){
			//fill empty rows with value from ...
			if(isRowEmpty(selectedRowData, selection.getSelectedRow())){
				opStr = "Fill empty rows with value from left";
				closure = "";
				AddItem(oplist, opStr, EnumPredict.SingleRowFillEmpty, closure);
				opStr = "Fill empty rows with value from below";	
				closure = "";
				AddItem(oplist, opStr, EnumPredict.SingleRowFillEmpty, closure);
				opStr = "Fill empty rows with value from above";
				closure = "";
				AddItem(oplist, opStr, EnumPredict.SingleRowFillEmpty, closure);
			}
			
			//fill row X with value from ...
			opStr = "Fill row " + selection.getSelectedRow() + " with value from left";	
			closure = "";
			AddItem(oplist, opStr, EnumPredict.SingleRowFillBasic, closure);
			opStr = "Fill row " + selection.getSelectedRow()  + " with value from below";
			closure = "";
			AddItem(oplist, opStr, EnumPredict.SingleRowFillBasic, closure);
			opStr = "Fill row " + selection.getSelectedRow()  + " with value from above";
			closure = "";
			AddItem(oplist, opStr, EnumPredict.SingleRowFillBasic, closure);
			
			//fill row where column1='***' with value from left
			int index = 0;
			while(index < columnhead.length){
				closure = "";
				opStr = "Fill rows where " + columnhead[index] + " = " + selectedRowData[index] + " with value from left";
				AddItem(oplist, opStr, EnumPredict.SingleRowFillAll, closure);
				closure = "";
				opStr = "Fill rows where " + columnhead[index] + " = " + selectedRowData[index] + " with value from above";
				AddItem(oplist, opStr, EnumPredict.SingleRowFillAll, closure);
				closure = "";
				opStr = "Fill rows where " + columnhead[index] + " = " + selectedRowData[index] + " with value from below";
				AddItem(oplist, opStr, EnumPredict.SingleRowFillAll, closure);
				index++;
			}
		}
		
		if(selection.getType() == EnumType.rowMulti){
			closure = "";
			opStr = "Fill rows" + getSelectedColumns(selection, columnhead) + " with value from left";	
			AddItem(oplist, opStr, EnumPredict.MultiRowFillBasic, closure);
			closure = "";
			opStr = "Fill rows" + getSelectedColumns(selection, columnhead) + " with value from below";	
			AddItem(oplist, opStr, EnumPredict.MultiRowFillBasic, closure);
			closure = "";
			opStr = "Fill rows" + getSelectedColumns(selection, columnhead) + " with value from above";
			AddItem(oplist, opStr, EnumPredict.MultiRowFillBasic, closure);
		}
		
		if(selection.getType() == EnumType.colSingle){
			closure = "";
			opStr = "Fill " + columnhead[selection.getSelectedColumn()] + " with value from left";
			AddItem(oplist, opStr, EnumPredict.SingleColumnFillBasic, closure);
			opStr = "Fill " + columnhead[selection.getSelectedColumn()] + " with value from above";
			closure = "";
			AddItem(oplist, opStr, EnumPredict.SingleColumnFillBasic, closure);
			opStr = "Fill " + columnhead[selection.getSelectedColumn()] + " with value from below";
			closure = "";
			AddItem(oplist, opStr, EnumPredict.SingleColumnFillBasic, closure);
		}
		
		if(selection.getType() == EnumType.colMulti){
			closure = "";
			opStr = "Fill " + getSelectedColumns(selection, columnhead) + " with value from left";
			AddItem(oplist, opStr, EnumPredict.MultiColumnFillBasic, closure);
			closure = "";
			opStr = "Fill " + getSelectedColumns(selection, columnhead) + " with value from above";
			AddItem(oplist, opStr, EnumPredict.MultiColumnFillBasic, closure);
			closure = "";
			opStr = "Fill " + getSelectedColumns(selection, columnhead) + " with value from below";
			AddItem(oplist, opStr, EnumPredict.MultiColumnFillBasic, closure);
		}

		return oplist;
	}
}
