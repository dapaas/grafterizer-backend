package main.java.suggestion;

import java.util.ArrayList;
import java.util.List;

import main.java.prediction.EnumPredict;
import main.java.prediction.EnumType;
import main.java.prediction.Selection;

public class FillSuggestion extends Suggestion{
	
	@Override
	List<SuggestionItem> generateSuggestion(String[] selectedRowData, String[] selectedColumnData, Selection selection, String [] columnhead){
		List<SuggestionItem> oplist = new ArrayList<SuggestionItem>();
		
		String opStr = "";
		if(selection.getType() == EnumType.rowSingle){
			//fill empty rows with value from ...
			if(isRowEmpty(selectedRowData, selection.getSelectedRow())){
				
				opStr = "Fill empty rows with value from below";	
				Parameters p = getParameter("fill", true, null, 
						null, null, null);
				AddItem(oplist, opStr, EnumPredict.SingleRowFillEmpty, p);
				
				opStr = "Fill empty rows with value from above";
				p = getParameter("fill", true, null, 
						null, null, null);
				AddItem(oplist, opStr, EnumPredict.SingleRowFillEmpty, p);
			}
			
			//fill <row index> with value from ...
			opStr = "Fill row " + selection.getSelectedRow()  + " with value from below";
			Parameters p = getParameter("fill", false, null, 
					null, selection.getSelectedRows(), null);
			AddItem(oplist, opStr, EnumPredict.SingleRowFillBasic, p);
			opStr = "Fill row " + selection.getSelectedRow()  + " with value from above";
			p = getParameter("fill", false, null, 
					null, selection.getSelectedRows(), null);
			AddItem(oplist, opStr, EnumPredict.SingleRowFillBasic, p);
			
			//fill row where <column head> = <cell data> with value from ...
			int index = 0;
			while(index < columnhead.length){
				p = getParameter("fill", false, columnhead[index], 
						selectedRowData[index], null, null);
				opStr = "Fill rows where " + "\"" + columnhead[index] + "\"" + " = " + "\"" + selectedRowData[index] + "\"" + " with value from above";
				AddItem(oplist, opStr, EnumPredict.SingleRowFillAll, p);
				p = getParameter("fill", false, columnhead[index], 
						selectedRowData[index], null, null);
				opStr = "Fill rows where " + "\"" + columnhead[index] + "\"" + " = " + "\"" + selectedRowData[index] + "\"" + " with value from below";
				AddItem(oplist, opStr, EnumPredict.SingleRowFillAll, p); 
				index++;
			}
		}
		
		// fill rows <row index> with value from ...
		if(selection.getType() == EnumType.rowMulti){
			Parameters p = getParameter("fill", false, null, 
					null, selection.getSelectedRows(), null);
			opStr = "Fill rows" + getSelectedRows(selection) + " with value from below";	
			AddItem(oplist, opStr, EnumPredict.MultiRowFillBasic, p);
			p = getParameter("fill", false, null, 
					null, selection.getSelectedRows(), null);
			opStr = "Fill rows" + getSelectedRows(selection) + " with value from above";
			AddItem(oplist, opStr, EnumPredict.MultiRowFillBasic, p);
		}
		
		// fill <column head> with value from ...
		if(selection.getType() == EnumType.colSingle){
			Parameters p = getParameter("fill", false, columnhead[selection.getSelectedColumn()], 
					null, null, null);
			opStr = "Fill " + "\"" + columnhead[selection.getSelectedColumn()] + "\"" + " with value from left";
			AddItem(oplist, opStr, EnumPredict.SingleColumnFillBasic, p);
			
			p = getParameter("fill", false, columnhead[selection.getSelectedColumn()], 
					null, null, null);
			opStr = "Fill " + "\"" + columnhead[selection.getSelectedColumn()] + "\"" + " with value from right";
			AddItem(oplist, opStr, EnumPredict.SingleColumnFillBasic, p);
		}
		
		// fill <column head 1>, <column head 2> with value from ...
		if(selection.getType() == EnumType.colMulti){
			Parameters p = getParameter("fill", false, null, 
					null, null, selection.getSelectedColumns());
			opStr = "Fill " + getSelectedColumns(selection, columnhead) + " with value from left";
			AddItem(oplist, opStr, EnumPredict.MultiColumnFillBasic, p);
			p = getParameter("fill", false, null, 
					null, null, selection.getSelectedColumns());
			opStr = "Fill " + getSelectedColumns(selection, columnhead) + " with value from above";
			AddItem(oplist, opStr, EnumPredict.MultiColumnFillBasic, p);
		}

		return oplist;
	}
}
