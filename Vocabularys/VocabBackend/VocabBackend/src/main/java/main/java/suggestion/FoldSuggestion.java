package main.java.suggestion;

import java.util.ArrayList;
import java.util.List;

import main.java.prediction.EnumPredict;
import main.java.prediction.EnumType;
import main.java.prediction.ProbabilityFile;
import main.java.prediction.Selection;

public class FoldSuggestion extends Suggestion{
	
	@Override
	List<SuggestionItem> generateSuggestion(String[] selectedRowData, String[] selectedColumnData, Selection selection, String [] columnhead){
		List<SuggestionItem> oplist = new ArrayList<SuggestionItem>();
		String closure = "";
		
		String opStr = "";
		if(selection.getType() == EnumType.rowSingle){
			opStr = "Fold using " + selection.getSelectedRow() + " as a key";
			closure = "";
			AddItem(oplist, opStr, EnumPredict.SingleRowFoldUsingRow, closure);
			
			int index = 0;
			while(index < columnhead.length){
				opStr = "Fold " + columnhead[index] + " using " + selection.getSelectedRow() + " as a key";
				closure = "";
				AddItem(oplist, opStr, EnumPredict.SingleRowFoldUsingColumn, closure);
				index++;
			}
		}
		
		if(selection.getType() == EnumType.rowMulti){
			opStr = "Fold using " + getSelectedRows(selection) + " as a key";
			closure = "";
			AddItem(oplist, opStr, EnumPredict.MultiRowFoldUsingRow, closure);
			int index = 0;
			while(index < columnhead.length){
				opStr = "Fold " + columnhead[index] + " using " + getSelectedRows(selection) + " as a key";
				closure = "";
				AddItem(oplist, opStr, EnumPredict.MultiRowFoldUsingColumn, closure);
				index++;
			}
		}
		
		if(selection.getType() == EnumType.colSingle){
			opStr = "Fold " + columnhead[selection.getSelectedColumn()] + " using header as a key";
			closure = "";
			AddItem(oplist, opStr, EnumPredict.SingleColumnFoldBasic, closure);
			opStr = "Fold " + columnhead[selection.getSelectedColumn()] + " using header 1 as a key";
			closure = "";
			AddItem(oplist, opStr, EnumPredict.SingleColumnFoldBasic, closure);
			opStr = "Fold " + columnhead[selection.getSelectedColumn()] + " using header 1,2 as a key";
			closure = "";
			AddItem(oplist, opStr, EnumPredict.SingleColumnFoldBasic, closure);
			opStr = "Fold " + columnhead[selection.getSelectedColumn()] + " using header 1,2,3 as a key";
			closure = "";
			AddItem(oplist, opStr, EnumPredict.SingleColumnFoldBasic, closure);
		}
		
		if(selection.getType() == EnumType.colMulti){
			opStr = "Fold " + getSelectedColumns(selection, columnhead) + " using header as a key";
			closure = "";
			AddItem(oplist, opStr, EnumPredict.MultiColumnFoldBasic, closure);
			opStr = "Fold " + getSelectedColumns(selection, columnhead) + " using header 1 as a key";
			closure = "";
			AddItem(oplist, opStr, EnumPredict.MultiColumnFoldBasic, closure);
			opStr = "Fold " + getSelectedColumns(selection, columnhead) + " using header 1,2 as a key";
			closure = "";
			AddItem(oplist, opStr, EnumPredict.MultiColumnFoldBasic, closure);
			opStr = "Fold " + getSelectedColumns(selection, columnhead) + " using header 1,2,3 as a key";
			closure = "";
			AddItem(oplist, opStr, EnumPredict.MultiColumnFoldBasic, closure);
		}
		
		return oplist;
	}
}
