package test.java.prediction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import main.java.prediction.EnumPredict;
import main.java.prediction.EnumType;
import main.java.prediction.Prediction;
import main.java.prediction.Selection;
import main.java.prediction.Prediction.PredictionProbability;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PredictionTest {
	
	private Prediction p = new Prediction();

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testParseOperation() {
		Selection s = new Selection();
		s.setSelectedColumn(1);
		s.setSelectedRow(1);
		Integer[] lcolumn = {0, 1};
		s.setSelectedColumns(lcolumn);
		
		Integer[] lrow = {1, 2};
		s.setSelectedRows(lrow);
		
		p.incProbability(EnumPredict.SingleColumnDeleteBasic);
	}
	
	private String[] getSelectedColumnData(int columnid, String[][] data){
		List<String> l = new ArrayList<String>();
		for(int i = 0; i < data.length; i++){
			if(columnid < data[i].length){
				l.add(data[i][columnid]);
			}
		}
		
		return l.toArray(new String[l.size()]);
	}

	@Test
	public void testGenerateOperations() {
		String [][] data = {
				{"2004", "4029.3", "crime at Oslo"},
				{"2005", "3900", "crime at Bergen"},
				{"2006", "3937", "crime at Stavanger"},
				{"","",""},
				{"2007", "3934.9", "crime at Beijing"},
				{"2008", "4081.9", "crime at Shanghai"}
		};
		
		String [] columnData = {};
		
		String [] columnHead = {
				"year",
				"rate",
				"crime place"
		};
		
		//Test select row
		System.out.println("Test 1------------------------------------------");
		Selection s = new Selection();
		s.setSelectedRow(1);
		s.setType(EnumType.rowSingle);
		
		Iterator<PredictionProbability> it = p.generateOperations(data[1], columnData, s, columnHead).iterator();
		while(it.hasNext()){
			PredictionProbability p = it.next();
			System.out.println(p.getStrOp());
		}
		
		System.out.println("Test 2------------------------------------------");
		//Test select row
		s.setSelectedRow(3);
		s.setType(EnumType.rowSingle);
				
		it = p.generateOperations(data[3], columnData, s, columnHead).iterator();
		while(it.hasNext()){
			PredictionProbability p = it.next();
			System.out.println(p.getStrOp());
		}
				
		System.out.println("Test 3------------------------------------------");
		
		//Test select rows
		Integer[] selectedrow = {2,4};
		s.setSelectedRows(selectedrow);
		s.setType(EnumType.rowMulti);
				
		it = p.generateOperations(data[0], columnData, s, columnHead).iterator();
		while(it.hasNext()){
			PredictionProbability p = it.next();
			System.out.println(p.getStrOp());
		}
		
		System.out.println("Test 4------------------------------------------");
		
		//Test select column
		s.setSelectedColumn(2);
		s.setType(EnumType.colSingle);
				
		it = p.generateOperations(data[0], getSelectedColumnData(2, data), s, columnHead).iterator();
		while(it.hasNext()){
			PredictionProbability p = it.next();
			System.out.println(p.getStrOp());
		}
		
		System.out.println("Test 5------------------------------------------");
		
		//Test select columns
		Integer[] selectedcolumn = {2,4};
		s.setSelectedColumns(selectedcolumn);
		s.setType(EnumType.colMulti);
				
		it = p.generateOperations(data[0], columnData, s, columnHead).iterator();
		while(it.hasNext()){
			PredictionProbability p = it.next();
			System.out.println(p.getStrOp());
		}
	}

}
