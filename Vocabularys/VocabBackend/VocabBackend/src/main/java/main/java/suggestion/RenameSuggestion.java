package main.java.suggestion;

import java.util.ArrayList;
import java.util.List;

import main.java.prediction.EnumPredict;
import main.java.prediction.EnumType;
import main.java.prediction.Selection;

public class RenameSuggestion extends Suggestion{
	@Override
	List<SuggestionItem> generateSuggestion(String[] selectedRowData, String[] selectedColumnData, Selection selection, String [] columnhead){
		List<SuggestionItem> oplist = new ArrayList<SuggestionItem>();
		
		String opStr = "";
		//rename <column head>
		if(selection.getType() == EnumType.colSingle){
			opStr = "Rename " + "\"" + columnhead[selection.getSelectedColumn()] + "\"";
			Parameters p = getParameter("Rename", false, columnhead[selection.getSelectedColumn()], 
					null, null, null);
			AddItem(oplist, opStr, EnumPredict.SingleColumnRename, p);
		}
		
		return oplist;
	}
}
