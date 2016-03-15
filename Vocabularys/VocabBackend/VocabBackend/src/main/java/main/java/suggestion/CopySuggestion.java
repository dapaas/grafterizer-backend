package main.java.suggestion;

import java.util.ArrayList;
import java.util.List;

import main.java.prediction.EnumPredict;
import main.java.prediction.EnumType;
import main.java.prediction.Selection;

public class CopySuggestion extends Suggestion {
	
	@Override
	List<SuggestionItem> generateSuggestion(String[] selectedRowData, String[] selectedColumnData, Selection selection, String [] columnhead){
		List<SuggestionItem> oplist = new ArrayList<SuggestionItem>();
		
		String opStr = "";
		if(selection.getType() == EnumType.colSingle){
			opStr = "Extract from " + "\"" + columnhead[selection.getSelectedColumn()] + "\"";
			//Extract from <column head>
			//eg: extract from telephone
			Parameters p = getParameter("extract", false, columnhead[selection.getSelectedColumn()], 
					null, null, null);
			
			AddItem(oplist, opStr, EnumPredict.SingleColumnCopyBasic, p);
		}
		
		if(selection.getType() == EnumType.colMulti){
			opStr = "Extract from " + getSelectedColumns(selection, columnhead);
			//Extract from <column index>, <column index>...
			//eg: extract from column 1,2
			Parameters p = getParameter("extract", false, columnhead[selection.getSelectedColumn()], 
					null, null, selection.getSelectedColumns());
			AddItem(oplist, opStr, EnumPredict.MultiColumnCopyBasic, p);
		}
		
		return oplist;
	}
}
