package test.java.prediction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import main.java.suggestion.EnumPredict;
import main.java.suggestion.EnumType;
import main.java.suggestion.Prediction;
import main.java.suggestion.Selection;
import main.java.suggestion.Prediction.PredictionProbability;

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
		/*
		Selection s = new Selection();
		s.setSelectedColumn(1);
		s.setSelectedRow(1);
		Integer[] lcolumn = {0, 1};
		s.setSelectedColumns(lcolumn);
		
		Integer[] lrow = {1, 2};
		s.setSelectedRows(lrow);
		
		p.incProbability(EnumPredict.SingleColumnDeleteBasic);
		*/
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
	
	private String[] getSelectedColumnData(int columnid, List<String[]> data){
		List<String> l = new ArrayList<String>();
		for(int i = 0; i < data.size(); i++){
			if(columnid < data.get(i).length){
				l.add(data.get(i)[columnid]);
			}
		}
		
		return l.toArray(new String[l.size()]);
	}
	
	private void case1(){
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
			System.out.println(p.getPara().cellData);
			System.out.println(p.getPara().operation);
			System.out.println(p.getPara().relatedColumnHead);
			System.out.println(p.getPara().isEmpty);
			
			Integer [] columns = p.getPara().columns;
			if(columns != null){
				for(int i = 0; i < columns.length; i++){
					System.out.println(columns[i]);
				}
			}
			
			Integer [] rows = p.getPara().rows;
			if(rows != null){
				for(int i = 0; i < rows.length; i++){
					System.out.println(rows[i]);
				}
			}
		}
		
		System.out.println("Test 2------------------------------------------");
		//Test select row
		s.setSelectedRow(3);
		s.setType(EnumType.rowSingle);
				
		it = p.generateOperations(data[3], columnData, s, columnHead).iterator();
		while(it.hasNext()){
			PredictionProbability p = it.next();
			System.out.println(p.getStrOp());
			System.out.println(p.getPara().cellData);
			System.out.println(p.getPara().operation);
			System.out.println(p.getPara().relatedColumnHead);
			System.out.println(p.getPara().isEmpty);
			
			Integer [] columns = p.getPara().columns;
			if(columns != null){
				for(int i = 0; i < columns.length; i++){
					System.out.println(columns[i]);
				}
			}
			
			Integer [] rows = p.getPara().rows;
			if(rows != null){
				for(int i = 0; i < rows.length; i++){
					System.out.println(rows[i]);
				}
			}
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
			System.out.println(p.getPara().cellData);
			System.out.println(p.getPara().operation);
			System.out.println(p.getPara().relatedColumnHead);
			System.out.println(p.getPara().isEmpty);
			
			Integer [] columns = p.getPara().columns;
			if(columns != null){
				for(int i = 0; i < columns.length; i++){
					System.out.println(columns[i]);
				}
			}
			
			Integer [] rows = p.getPara().rows;
			if(rows != null){
				for(int i = 0; i < rows.length; i++){
					System.out.println(rows[i]);
				}
			}
		}
		
		System.out.println("Test 4------------------------------------------");
		
		//Test select column
		s.setSelectedColumn(2);
		s.setType(EnumType.colSingle);
				
		it = p.generateOperations(data[0], getSelectedColumnData(2, data), s, columnHead).iterator();
		while(it.hasNext()){
			PredictionProbability p = it.next();
			System.out.println(p.getStrOp());
			System.out.println(p.getPara().cellData);
			System.out.println(p.getPara().operation);
			System.out.println(p.getPara().relatedColumnHead);
			System.out.println(p.getPara().isEmpty);
			
			Integer [] columns = p.getPara().columns;
			if(columns != null){
				for(int i = 0; i < columns.length; i++){
					System.out.println(columns[i]);
				}
			}
			
			Integer [] rows = p.getPara().rows;
			if(rows != null){
				for(int i = 0; i < rows.length; i++){
					System.out.println(rows[i]);
				}
			}
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
			System.out.println(p.getPara().cellData);
			System.out.println(p.getPara().operation);
			System.out.println(p.getPara().relatedColumnHead);
			System.out.println(p.getPara().isEmpty);
			
			Integer [] columns = p.getPara().columns;
			if(columns != null){
				for(int i = 0; i < columns.length; i++){
					System.out.println(columns[i]);
				}
			}
			
			Integer [] rows = p.getPara().rows;
			if(rows != null){
				for(int i = 0; i < rows.length; i++){
					System.out.println(rows[i]);
				}
			}
		}
	}
	
	private void training(){
		//case 1
		p.incProbability(EnumPredict.SingleRowMakeDataset);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		
		//case 2
		p.incProbability(EnumPredict.SingleRowDeleteCurrent);
		p.incProbability(EnumPredict.SingleRowDeleteEmpty);
		p.incProbability(EnumPredict.SingleRowDeleteCurrent);
		p.incProbability(EnumPredict.MultiColumnMergeBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		
		//case 3
		p.incProbability(EnumPredict.SingleRowMakeDataset);
		
		//case 4
		p.incProbability(EnumPredict.SingleRowMakeDataset);
		p.incProbability(EnumPredict.SingleRowDeleteCurrent);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		
		/*
		//case 5
		p.incProbability(EnumPredict.SingleRowMakeDataset);
		p.incProbability(EnumPredict.SingleRowDeleteCurrent);
		p.incProbability(EnumPredict.SingleColumnSplitCommenWord);
		
		//case 6
		p.incProbability(EnumPredict.SingleRowMakeDataset);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		
		//case 7
		p.incProbability(EnumPredict.SingleRowMakeDataset);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		*/
		//case 8
		p.incProbability(EnumPredict.SingleRowMakeDataset);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		
		//case 9
		p.incProbability(EnumPredict.SingleRowDeleteCurrent);
		p.incProbability(EnumPredict.SingleColumnRename);
		p.incProbability(EnumPredict.SingleColumnRename);
		p.incProbability(EnumPredict.MultiColumnMergeBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		
		//case 10
		p.incProbability(EnumPredict.SingleRowMakeDataset);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);
		p.incProbability(EnumPredict.SingleColumnCopyBasic);		
	}

	@Test
	public void testGenerateOperations() {
		//training();
		
		
		List<String[]> data = importCsv("/home/yexl/test_data_original/test5 - ByggForAlle.csv");
		
		
		String [] columnHead = null;
		
		if(data.size() > 0){
			columnHead = data.get(0);
		}
		
		//Test select column
		Selection s = new Selection();
		s.setSelectedColumn(2);
		s.setType(EnumType.colSingle);
				
		Iterator<PredictionProbability> it = p.generateOperations(null, getSelectedColumnData(2, data), s, columnHead).iterator();
		while(it.hasNext()){
			PredictionProbability p = it.next();
			System.out.println(p.getStrOp());
			
			Integer [] columns = p.getPara().columns;
			if(columns != null){
				for(int i = 0; i < columns.length; i++){
					System.out.println(columns[i]);
				}
			}
			
			Integer [] rows = p.getPara().rows;
			if(rows != null){
				for(int i = 0; i < rows.length; i++){
					System.out.println(rows[i]);
				}
			}
		}		
		
		System.out.println("-------------------------------------------------------------------");
		//Test select row
	    s = new Selection();
		s.setSelectedRow(3);
		s.setType(EnumType.rowSingle);
				
		it = p.generateOperations(data.get(1), null, s, columnHead).iterator();
		while(it.hasNext()){
			PredictionProbability p = it.next();
			System.out.println(p.getStrOp());
					
			Integer [] columns = p.getPara().columns;
			if(columns != null){
				for(int i = 0; i < columns.length; i++){
					System.out.println(columns[i]);
				}
			}
					
			Integer [] rows = p.getPara().rows;
			if(rows != null){
				for(int i = 0; i < rows.length; i++){
					System.out.println(rows[i]);
				}
			}
		}		
	}
	
	public static List<String []> importCsv(String filename){
		List<String []> ret = new ArrayList<String []>();
		File file = new File(filename);

        BufferedReader br=null;
        FileReader f = null;
        try { 
        	f = new FileReader(file);
            br = new BufferedReader(f);
            String line = ""; 
            while ((line = br.readLine()) != null) { 
            	String[] arr = line.split(",");
                
            	ret.add(arr);
            }
        }catch (Exception e) {
             e.printStackTrace();
        }
 
        return ret;
    }

}
