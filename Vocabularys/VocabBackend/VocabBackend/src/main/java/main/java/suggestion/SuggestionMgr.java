package main.java.suggestion;

import java.util.List;

import main.java.prediction.EnumOp;
import main.java.prediction.EnumPredict;
import main.java.prediction.Selection;
import main.java.suggestion.Suggestion.SuggestionItem;

public class SuggestionMgr {
	
	Suggestion suggestion;
	
	public SuggestionMgr(){
		
	}	
	
	public Suggestion getSuggestion() {
		return suggestion;
	}

	public void setSuggestion(Suggestion suggestion) {
		this.suggestion = suggestion;
	}

	public void setSuggestion(EnumOp type) {
		switch(type){
		case Copy:
			suggestion = new CopySuggestion();
			break;
		case Cut:
			suggestion = new CutSuggestion();
			break;
		case Delete:
			suggestion = new DeleteSuggestion();
			break;
		case Fill:
			suggestion = new FillSuggestion();
			break;
		case Fold:
			suggestion = new FoldSuggestion();
			break;
		case Merge:
			suggestion = new MergeSuggestion();
			break;
		case Split:
			suggestion = new SplitSuggestion();
			break;
		default:
			throw new IllegalArgumentException("Incorrect suggestion type");	
		}
	}
	
	public void setSuggestion(String type) {
		switch(type){
		case "Copy":
			suggestion = new CopySuggestion();
			break;
		case "Cut":
			suggestion = new CutSuggestion();
			break;
		case "Delete":
			suggestion = new DeleteSuggestion();
			break;
		case "Fill":
			suggestion = new FillSuggestion();
			break;
		case "Fold":
			suggestion = new FoldSuggestion();
			break;
		case "Merge":
			suggestion = new MergeSuggestion();
			break;
		case "Split":
			suggestion = new SplitSuggestion();
			break;
		default:
			throw new IllegalArgumentException("Incorrect suggestion type");	
		}
	}
	
	public List<SuggestionItem> getSuggestion(String[] tData, Selection selection, String [] columnhead){
		
		return suggestion.generateSuggestion(tData, selection, columnhead);
	}
	
	public String parseSuggestion(String strSuggestion, EnumPredict predictType){
		return suggestion.parseSuggestion(strSuggestion, predictType);
	}
}
