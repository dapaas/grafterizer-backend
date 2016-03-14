package main.java.suggestion;

import java.util.ArrayList;
import java.util.List;

import main.java.prediction.EnumPredict;
import main.java.prediction.EnumType;
import main.java.prediction.Selection;

public class MergeSuggestion extends Suggestion{

	@Override
	List<SuggestionItem> generateSuggestion(String[] selectedRowData, String[] selectedColumnData, Selection selection, String [] columnhead){
		List<SuggestionItem> oplist = new ArrayList<SuggestionItem>();
		
		String opStr = "";
		if(selection.getType() == EnumType.rowSingle){
			/*opStr = "Merge " + selection.getSelectedRow();
			AddItem(oplist, opStr, EnumPredict.SingleRowMergeBasic, closure);
			*/
		}
		
		if(selection.getType() == EnumType.rowMulti){
			opStr = "Merge " + getSelectedRows(selection);
			Parameters p = getParameter("merge", false, null, 
					null, selection.getSelectedRows(), null);
			AddItem(oplist, opStr, EnumPredict.MultiRowMergeBasic, p);
		}
		
		if(selection.getType() == EnumType.colSingle){
			/*
			opStr = "Merge " + columnhead[selection.getSelectedColumn()];
			closure = "";
			AddItem(oplist, opStr, EnumPredict.SingleColumnMergeBasic, closure);
			*/
		}
		
		if(selection.getType() == EnumType.colMulti){
			opStr = "Merge " + getSelectedColumns(selection, columnhead);
			Parameters p = getParameter("merge", false, null, 
					null, null, selection.getSelectedColumns());
			AddItem(oplist, opStr, EnumPredict.MultiColumnMergeBasic, p);
		}
		
		return oplist;
	}
}
