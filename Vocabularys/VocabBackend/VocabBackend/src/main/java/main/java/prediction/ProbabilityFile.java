package main.java.prediction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class ProbabilityFile {
	static Map<EnumPredict, Double> singleRowProbability = new HashMap<EnumPredict, Double>();
	static Map<EnumPredict, Double> multiRowProbability = new HashMap<EnumPredict, Double>();
	static Map<EnumPredict, Double> singleColumnProbability = new HashMap<EnumPredict, Double>();
	static Map<EnumPredict, Double> multiColumnProbability = new HashMap<EnumPredict, Double>();
	
	private static Connection getConnection() {
		Connection connection = null;
		try{
			Class.forName("com.mysql.jdbc.Driver");
			
			connection = DriverManager.getConnection(
			   "jdbc:mysql://localhost:3306/probability","root", "root");
			
			if (connection == null) {
		            System.out.println("Failed to make connection");
		    }
		}catch(Exception e){
			
		}
		
		return connection;
	}
	
	private static void closeConnection(Connection connection) {
		try{
			connection.close();
		}catch(Exception e){
			
		}
		
	}
	
	public static void increaseProbability(EnumPredict ep){
		Connection conn = getConnection();
		
		try{
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select value from probability where key_id = '" + ep +"'");
			
			if (!rs.next()) {
				stmt.execute("insert into probability values('" + ep + "', '" + 1.0 + "')");
            }
			else{
				String value = rs.getString("value");
            	Integer iValue = Integer.parseInt(value);
            	iValue += 1;
            	value = iValue.toString();
            	stmt.execute("update probability set value = '" + value + "' where key_id = '" + ep + "'");
			}
			conn.close();
		}catch(Exception e){
		}
		
		
		
		//d = Math.log(Math.log(d) + 1);
	}
	
	
	
	public static Integer getProbability(EnumPredict ep){
		Connection conn = getConnection();
		
		Integer iValue = null;
		
		try{
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select * from probability where key_id = '" + ep +"'");
			if (!rs.next()) {
				stmt.execute("insert into probability values('" + ep + "', '" + 1 + "')");
				iValue = 1;
			}else{
				String value = rs.getString("value");
            	iValue = Integer.parseInt(value);
			}
			
			conn.close();
		}catch(Exception e){
		}
		
		return iValue;
	}
	
	/*
	private static void readFile(int type){
		try{
			File file = new File("readFile");
			FileInputStream f = new FileInputStream(file);
			ObjectInputStream s = new ObjectInputStream(f);
			switch(type){
			case 1:
			    singleRowProbability = (HashMap<EnumPredict, Double>) s.readObject();
			    break;
			case 2:
			    multiRowProbability = (HashMap<EnumPredict, Double>) s.readObject();
			    break;
			case 3:
			    singleColumnProbability = (HashMap<EnumPredict, Double>) s.readObject();
			    break;
			case 4:
			    multiColumnProbability = (HashMap<EnumPredict, Double>) s.readObject();
			    break;
			default:
			}
			s.close();
		
			
		}catch(Exception e){
			
		}
	}
	
	private static void writeFile(int type){
		try{
			File file = new File("writeFile");
			FileOutputStream f = new FileOutputStream(file);
			ObjectOutputStream s = new ObjectOutputStream(f);
			switch(type){
			case 1:
				s.writeObject(singleRowProbability);
			    break;
			case 2:
				s.writeObject(multiRowProbability);
			    break;
			case 3:
				s.writeObject(singleColumnProbability);
			    break;
			case 4:
				s.writeObject(multiColumnProbability);
			    break;
			default:
			}
	        
	        s.close();
		}catch(Exception e){

		}finally{
		}
	}
	*/

}
