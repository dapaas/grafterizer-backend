package main.java.prediction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.lang.Math;

public class ProbabilityFile {
	static Map<EnumPredict, Double> singleRowProbability = new HashMap<EnumPredict, Double>();
	static Map<EnumPredict, Double> multiRowProbability = new HashMap<EnumPredict, Double>();
	static Map<EnumPredict, Double> singleColumnProbability = new HashMap<EnumPredict, Double>();
	static Map<EnumPredict, Double> multiColumnProbability = new HashMap<EnumPredict, Double>();
	
	public static Double getProbability(EnumPredict e){
		Double d = getSingleRowProbability(e);
		if(d != Double.MIN_VALUE){
			 return d;
		}
		
		d = getMultiRowProbability(e);
		if(d != Double.MIN_VALUE){
			 return d;
		}
		
		d = getSingleColumnProbability(e);
		if(d != Double.MIN_VALUE){
			 return d;
		}
		
		d = getMultiColumnProbability(e);
		if(d != Double.MIN_VALUE){
			 return d;
		}
		
		return 0.0;
	}
	
	private static Double getSingleRowProbability(EnumPredict e){
		readFile("singlerow");
		if(singleRowProbability.containsKey(e)){
			return singleRowProbability.get(e);
		}
		else{
			return Double.MIN_VALUE;
		}
	}
	
	private static Double getMultiRowProbability(EnumPredict e){
		readFile("multirow");

		if(multiRowProbability.containsKey(e)){
			return multiRowProbability.get(e);
		}
		else{
			return Double.MIN_VALUE;
		}
	}
	
	private static Double getSingleColumnProbability(EnumPredict e){
		readFile("singlecol");
		
		if(singleColumnProbability.containsKey(e)){
			return singleColumnProbability.get(e);
		}
		else{
			return Double.MIN_VALUE;
		}
	}
	
	private static Double getMultiColumnProbability(EnumPredict e){
		readFile("multicol");
		if(multiColumnProbability.containsKey(e)){
			return multiColumnProbability.get(e);
		}
		else{
			return Double.MIN_VALUE;
		}
	}
	
	public static void increaseSingleRowProbability(EnumPredict e){
		readFile("singlerow");
		if(singleRowProbability.containsKey(e)){
			Double d = singleRowProbability.get(e);
			if(d <= 0.0){
				d = 0.0;
			}
			d = Math.log(Math.log(d) + 1);
			singleRowProbability.put(e, d);
		}else{
			singleRowProbability.put(e, 1.0);
		}
		
		writeFile("singlerow");
	}
	
	public static void increaseMultiRowProbability(EnumPredict e){
		readFile("multirow");
		if(multiRowProbability.containsKey(e)){
			Double d = multiRowProbability.get(e);
			if(d <= 0.0){
				d = 0.0;
			}
			d = Math.log(Math.log(d) + 1);
			multiRowProbability.put(e, d);
		}else{
			multiRowProbability.put(e, 1.0);
		}
		writeFile("multirow");
	}
	
	public static void increaseSingleColumnProbability(EnumPredict e){
		readFile("singlecol");
		if(singleColumnProbability.containsKey(e)){
			Double d = singleColumnProbability.get(e);
			if(d <= 0.0){
				d = 0.0;
			}
			d = Math.log(Math.log(d) + 1);
			singleColumnProbability.put(e, d);
		}else{
			singleColumnProbability.put(e, 1.0);
		}
		
		writeFile("singlecol");
	}
	
	public static void increaseMultiColumnProbability(EnumPredict e){
		readFile("multicol");
		if(multiColumnProbability.containsKey(e)){
			Double d = multiColumnProbability.get(e);
			if(d <= 0.0){
				d = 0.0;
			}
			d = Math.log(Math.log(d) + 1);
			multiColumnProbability.put(e, d);
		}else{
			multiColumnProbability.put(e, 1.0);
		}
		
		writeFile("multicol");
	}

	private static void readFile(String type){
		try{
			File file = new File(type);
			FileInputStream f = new FileInputStream(file);
			ObjectInputStream s = new ObjectInputStream(f);
			switch(type){
			case "singlerow":
			    singleRowProbability = (HashMap<EnumPredict, Double>) s.readObject();
			    break;
			case "multirow":
			    multiRowProbability = (HashMap<EnumPredict, Double>) s.readObject();
			    break;
			case "singlecol":
			    singleColumnProbability = (HashMap<EnumPredict, Double>) s.readObject();
			    break;
			case "multicol":
			    multiColumnProbability = (HashMap<EnumPredict, Double>) s.readObject();
			    break;
			default:
			}
			s.close();
		
			
		}catch(Exception e){
			
		}
	}
	
	private static void writeFile(String type){
		try{
			File file = new File(type);
			FileOutputStream f = new FileOutputStream(file);
			ObjectOutputStream s = new ObjectOutputStream(f);
			switch(type){
			case "singlerow":
				s.writeObject(singleRowProbability);
			    break;
			case "multirow":
				s.writeObject(multiRowProbability);
			    break;
			case "singlecol":
				s.writeObject(singleColumnProbability);
			    break;
			case "multicol":
				s.writeObject(multiColumnProbability);
			    break;
			default:
			}
	        
	        s.close();
		}catch(Exception e){

		}finally{
		}
	}

}
