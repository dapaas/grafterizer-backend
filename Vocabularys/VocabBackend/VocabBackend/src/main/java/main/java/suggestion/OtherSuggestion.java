package main.java.suggestion;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.java.prediction.EnumPredict;
import main.java.prediction.Selection;
import main.java.suggestion.Suggestion.SuggestionItem;

public class OtherSuggestion extends Suggestion {

	@Override
	List<SuggestionItem> generateSuggestion(String[] selectedRowData,
			String[] selectedColumnData, Selection selection,
			String[] columnhead) {
		
		return null;
	}
	
	private List<SuggestionItem> SuggestedByEmail(String [] data){
		List<SuggestionItem> oplist = new ArrayList<SuggestionItem>();
		Pattern pattern = Pattern.compile("//^(//w)+(//.//w+)*@(//w)+((//.//w+)+)$//");
		
		List<String> lNonEmail = new ArrayList<String>();
		
		for(int i = 0; i < data.length; i++){
			Matcher matcher = pattern.matcher(data[i]);

			if (!matcher.find())
			{
				lNonEmail.add(data[i]);
			}
		}
		
		if((double)lNonEmail.size()/ (double)data.length > 0.7){
			String opStr = "Remove all items which is not email";
			String closure = "";
			AddItem(oplist, opStr, EnumPredict.MultiColumnCutBasic, closure);
		}
		
		return oplist;
	}

}
