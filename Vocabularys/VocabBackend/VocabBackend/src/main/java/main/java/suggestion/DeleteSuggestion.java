package main.java.suggestion;

import java.util.ArrayList;
import java.util.List;

import main.java.prediction.EnumPredict;
import main.java.prediction.EnumType;
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
				Parameters p = getParameter("delete", true, null, 
						null, null, null);
				AddItem(oplist, opStr, EnumPredict.SingleRowDeleteEmpty, p);
			}
			
			//delete rows where <column head> = <cell data>
			while(index < columnhead.length){
				opStr = "Delete rows where " + columnhead[index] + " = " + selectedRowData[index];
				Parameters p = getParameter("delete", false, columnhead[index], 
						selectedRowData[index], null, null);
				AddItem(oplist, opStr, EnumPredict.SingleRowDeleteBasic, p);
				index++;
			}
		}
		
		//delete rows <row index 1>, <row index 2>
		if(selection.getType() == EnumType.rowMulti){
			
			opStr = "Delete rows " + getSelectedRows(selection);
			Parameters p = getParameter("delete", false, null, 
					null, selection.getSelectedRows(), null);
			AddItem(oplist, opStr, EnumPredict.MultiRowDeleteBasic, p);
		}
		
		//delete <column head>
		if(selection.getType() == EnumType.colSingle){
			opStr = "Delete " + columnhead[selection.getSelectedColumn()];
			Parameters p = getParameter("delete", false, columnhead[selection.getSelectedColumn()], 
					null, null, null);
			AddItem(oplist, opStr, EnumPredict.SingleColumnDeleteBasic, p);
		}
		
		//delete <column index 1>, <column index 2>
		if(selection.getType() == EnumType.colMulti){
			
			opStr = "Delete " + getSelectedColumns(selection, columnhead);
			Parameters p = getParameter("delete", false, null, 
					null, null, selection.getSelectedColumns());
			AddItem(oplist, opStr, EnumPredict.MultiColumnDeleteBasic, p);
		}
		
		/*
		//delete rows where column1 contains selected string
		if(selection.getType() == EnumType.Text){
			opStr = "Delete rows where " + columnhead[selection.getTextcorY()] + " contains " + selection.getSelectedText();
		}
		*/
		return oplist;
	}
}
