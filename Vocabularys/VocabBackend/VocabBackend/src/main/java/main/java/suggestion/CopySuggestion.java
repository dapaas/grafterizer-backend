package main.java.suggestion;

import java.util.ArrayList;
import java.util.List;

import main.java.prediction.EnumPredict;
import main.java.prediction.EnumType;
import main.java.prediction.ProbabilityFile;
import main.java.prediction.Selection;

public class CopySuggestion extends Suggestion {
	
	@Override
	List<SuggestionItem> generateSuggestion(String[] selectedRowData, String[] selectedColumnData, Selection selection, String [] columnhead){
		List<SuggestionItem> oplist = new ArrayList<SuggestionItem>();
		
		String opStr = "";
		if(selection.getType() == EnumType.colSingle){
			opStr = "Copy from " + columnhead[selection.getSelectedColumn()];
			//copy 
			String closure = ClosureCode.copyRowSimple;
			AddItem(oplist, opStr, EnumPredict.SingleColumnCopyBasic, closure);
		}
		
		if(selection.getType() == EnumType.colMulti){
			opStr = "Copy from " + getSelectedColumns(selection, columnhead);
			//copy 
			String closure = ClosureCode.copyColSimple;
			AddItem(oplist, opStr, EnumPredict.MultiColumnCopyBasic, closure);
		}
		
		return oplist;
	}
}
