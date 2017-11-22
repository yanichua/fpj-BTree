import java.util.*; 
import java.io.*;
public class btdb{	
//change 1: value record, instead of 256 yung bung record, gawin 2 bytes for the record information and 256 yung length of strings for value of input
	public static final int order = 5;
	public static final int length = (3 * order) - 1;
	public static final String CMD_INSERT = "insert", CMD_UPDATE= "update", CMD_SELECT = "select",CMD_DELETE = "delete",CMD_EXIT = "exit";
	
	//Data.values RELATED VARIABLES
	public static int value_recordCount;	
	public static int value_recordBytes = 8; //bytes alloted for record count
	public static int value_stringBytes = 258; //2 bytes length || 256 bytes string value
	
	//Data.bt RELATED VARIABLES
	public static int bt_recordCount;
	String[] keys = new String[length]; //Keys
	ArrayList<String[]> Records = new ArrayList<String[]>(); //Records of keys
	ArrayList<String> Values = new ArrayList<String>(); //Records of Values
		
	public static void main(String[] args)throws IOException {
		//error handling for if file exist

		//====================================================
		System.out.println(args[0]);
		System.out.println(args[1]);	
		
		value_recordCount = 0;
		bt_recordCount = 0;	
		
		Scanner sc = new Scanner(System.in);
		System.out.print(">");
		while (sc.hasNext())
		{						
			String input = sc.nextLine();		
			String[] dictionary = input.split(" ");			
			switch(dictionary[0])
			{
				case CMD_INSERT:
					insert(dictionary[1], dictionary[2]);
					break;
				case CMD_UPDATE:
					update(dictionary[1], dictionary[2]);
					break;
				case CMD_SELECT:
					select(dictionary[1]);
					break;
				case CMD_DELETE:;
					delete(dictionary[1], dictionary[2]);
					break;
				case CMD_EXIT:
					break;
			}			
			System.out.print(">");
		}
	}
	
	public static void insert(String key, String value)throws IOException{				
		//check if key already exists (error if it does)
		value_write(value);		
		value_recordCount+=1;
		
		System.out.printf("< %s inserted.", key);
	}
	
	public static void update(String key, String value) {
		//check if key already exists (error if it does not)
		System.out.printf("< %s updated.", key);
	}
	
	public static void select(String key){
		//check if key already exists (error if it does not)
		//using key, look for which record the value is in
		System.out.printf("< %s => %s.", key, "");
	}
	
	public static void delete(String key, String value) {
	}
	
	public static void value_write(String value) throws IOException{
		RandomAccessFile file = new RandomAccessFile("Data.values.txt", "rwd");		
		file.writeLong(value_recordCount+1); //write/update num of records		
	
		file.seek(value_recordBytes + value_recordCount * value_stringBytes); //look which "record" to updated/add new line
		file.writeShort(value.length()); 	//write length of value
		file.write(value.getBytes("UTF8")); 	//write value after converting to bytes
		file.close();
	}
}
//check if values exist method
//valuesrecords(string strFile) throws IOException {
//	File file = new File(strFile);
//	if(!file.exists()){
//			
//	}
//		}