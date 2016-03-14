package main.java.suggestion;

import java.util.ArrayList;
import java.util.List;

import main.java.prediction.EnumPredict;
import main.java.prediction.EnumType;
import main.java.prediction.ProbabilityFile;
import main.java.prediction.Selection;

public class MergeSuggestion extends Suggestion{

	@Override
	List<SuggestionItem> generateSuggestion(String[] selectedRowData, String[] selectedColumnData, Selection selection, String [] columnhead){
		List<SuggestionItem> oplist = new ArrayList<SuggestionItem>();
		String closure = "";
		
		String opStr = "";
		if(selection.getType() == EnumType.rowSingle){
			opStr = "Merge " + selection.getSelectedRow();
			closure = "";
			AddItem(oplist, opStr, EnumPredict.SingleRowMergeBasic, closure);
		}
		
		if(selection.getType() == EnumType.rowMulti){
			opStr = "Merge " + getSelectedRows(selection);
			closure = "";
			AddItem(oplist, opStr, EnumPredict.MultiRowMergeBasic, closure);
		}
		
		if(selection.getType() == EnumType.colSingle){
			opStr = "Merge " + columnhead[selection.getSelectedColumn()];
			closure = "";
			AddItem(oplist, opStr, EnumPredict.SingleColumnMergeBasic, closure);
		}
		
		if(selection.getType() == EnumType.colMulti){
			opStr = "Merge " + getSelectedColumns(selection, columnhead);
			closure = "";
			AddItem(oplist, opStr, EnumPredict.MultiColumnMergeBasic, closure);
		}
		
		return oplist;
	}
}
