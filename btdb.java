import java.util.*; 
import java.io.*;
public class btdb{	
//change 1: value record, instead of 256 yung bung record, gawin 2 bytes for the record information and 256 yung length of strings for value of input = 258 bytes
//change 2: from m = 5, now m = 7
	//File names
	public static String File_bt = "Data.bt";
	public static String File_values = "Data.values";	
	
	public static final int m = 5;
	public static final int length = (3 * m) - 1;
	public static final String CMD_INSERT = "insert", CMD_UPDATE= "update", CMD_SELECT = "select",CMD_DELETE = "delete",CMD_EXIT = "exit";
	
	//Data.values RELATED VARIABLES
	public static int value_recordCount = 0;	
	public static final int value_recordBytes = 8; //bytes alloted for record count
	public static final int value_stringBytes = 258; //2 bytes length || 256 bytes string value
	
	//Data.bt RELATED VARIABLES
	public static int bt_recordCount = 0;
	public static int bt_rootLocation = 0;
	public static final int bt_recordBytes = 16;
		
	public static int[] keyArray; //Keys
	public static ArrayList<int[]> Records = new ArrayList<int[]>(); //Records of keys
	public static ArrayList<String> Values = new ArrayList<String>(); //Records of Values
		
	public static void main(String[] args)throws IOException {
		//error handling for if file exist

		//====================================================
		File_bt = args[0];
		File_values = args[1];		
		
		//Records[bt_recordCount] = create_array(); INSERT CREATE NEW RECORD HERE
		createNew();
		keyArray= Records.get(0);
		Scanner sc = new Scanner(System.in);
		System.out.print(">");
		while (sc.hasNext())
		{		
			String input = sc.nextLine();				
			String[] dictionary = input.split(" ");	
			String Command = dictionary[0];	
			int keyInt = Integer.valueOf(dictionary[1]);
			String valueString = dictionary[2];
			
			switch(Command)
			{
				case CMD_INSERT:
					if(exist(keyInt)){
						System.out.printf("< ERROR: %d already exists.\n", keyInt);
						break;
					}
					insert(keyInt, valueString);
					break;
				case CMD_UPDATE:
					if(!exist(keyInt)){
						System.out.printf("< ERROR: %d does not exists.\n", keyInt);
						break;
					}
					update(keyInt, valueString);					
					break;
				case CMD_SELECT:
					if(!exist(keyInt)){
						System.out.printf("< ERROR: %d does not exists.\n", keyInt);
						break;
					}
					select(keyInt);
					break;
				case CMD_DELETE:;
					delete(keyInt, valueString);
					break;
				case CMD_EXIT:
					break;
			}			
			System.out.print(">");
		}
	}
		
	public static void insert(int key, String value)throws IOException{				
		for(int i = 2; i < length; i = i+3){
			int keyTemp = keyArray[i];
			if(keyTemp == -1){ 							//if empty space
				keyArray[i] = key; 						//insert key
				keyArray[i+1] = value_recordCount; 		//insert offset of value
				break;
			}
			else{
				if (keyTemp > key){
					if (keyArray[length - 3] != -1){ 	
						System.out.printf("< %s !!\n", "FULL");
						
					}
					else{//SHIFTING VALUES						
						for(int j =  length - 6; j >= i; j = j-3){
							if (keyArray[j] != -1){							
								keyArray[j+3] = keyArray[j];		//insert key
								keyArray[j+3+1] = keyArray[j+1];	//insert offset of value												
							}
						}
						keyArray[i] = key; 						//insert key
						keyArray[i+1] = value_recordCount; 		//insert offset of value
						break;
					}					
				}
			}
		}
		Values.add(value);  //add value to value array	
		write(value);
		value_recordCount += 1;			
		System.out.printf("< %d inserted.\n", key);
	}
	
	public static void createNew() {
		int[] newRecords = new int[length];
		Arrays.fill(newRecords, new Integer(-1));
		Records.add(newRecords);
	}
	
	public static void split(){
		
	}
	
	public static void update(int key, String value) {
		//check if key already exists (error if it does not)
		
		System.out.printf("< %d updated.\n", key);
	}
	
	public static void select(int key){
		//check if key already exists (error if it does not)
		//using key, look for which record the value is in
		System.out.printf("< %d => %s.\n", key, "");
	}
	
	public static void delete(int key, String value) {
	}
	
	public static void write(String value) throws IOException{		
		//Write in Data.bt
		RandomAccessFile bt = new RandomAccessFile(File_bt, "rwd");
		bt.writeLong(bt_recordCount+1); //write/update num of records		
		bt.writeLong(bt_rootLocation); //ROOT		
		bt.seek(bt_recordBytes + bt_recordCount * length); 		
		for(int i = 0; i < length ; ++i){
			bt.writeInt(keyArray[i]); 
			System.out.printf("%d ", keyArray[i]);
		}
		System.out.println();
		bt.close();
		
		//Write in Data.values
		RandomAccessFile values = new RandomAccessFile(File_values, "rwd");		
		values.writeLong(value_recordCount+1); //write/update num of records	
		values.seek(value_recordBytes + value_recordCount * value_stringBytes); //look which "record" to updated/add new line
		values.writeShort(value.length()); 	//write length of value
		values.write(value.getBytes("UTF8")); 	//write value after converting to bytes
		values.close();	
	}	

	public static boolean exist(int key){
		for(int i = 2; i < length; i = i+3){
			int keyTemp = keyArray[i];			
			if(keyTemp == key){
				return true;			
			}
		}
		return false;
	}

	
}
//check if values exist method
//valuesrecords(string strFile) throws IOException {
//	File file = new File(strFile);
//	if(!file.exists()){
//			
//	}
//		}
