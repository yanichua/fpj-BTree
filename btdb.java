import java.util.*; 
import java.io.*;
public class btdb{	
//change 1: value record, instead of 256 yung bung record, gawin 2 bytes for the record information and 256 yung length of strings for value of input = 258 bytes
//change 2: from m = 5, now m = 7
//java -Xmx32M btdb Data.bt Data.values
	//File names
	public static String File_bt = "Data.bt";
	public static String File_values = "Data.values";	
	//Final Variables
	public static final int m = 5;
	public static final int length = (3 * m) - 1;
	public static final String CMD_INSERT = "insert", CMD_UPDATE= "update", CMD_SELECT = "select",CMD_DELETE = "delete",CMD_EXIT = "exit";
	//Data.values RELATED VARIABLES
	public static int value_recordCount = 0;	
	public static final int value_recordBytes = 8; //bytes alloted for record count
	public static final int value_stringBytes = 258; //2 bytes length || 256 bytes string value
	//Data.bt RELATED VARIABLES
	public static int currentroot = 0;
	public static int bt_recordCount = 0;
	public static int bt_rootLocation = 0;
	public static final int bt_recordBytes = 16;
	//Internal Memory	
	public static int[] keyArray; //Keys
	public static ArrayList<int[]> Records = new ArrayList<int[]>(); //Records of keys
	public static ArrayList<String> Values = new ArrayList<String>(); //Records of Values
		
	//generalized input object
	static class Input{
		String command;
		String value="";
		int key;	
		Input(String inp){
			String[] explode = inp.split(" ");
			this.command = explode[0];
			if(explode.length>1) this.key = Integer.valueOf(Integer.parseInt(explode[1]));
			if(explode.length>2) {
				for (int i = 2; i < explode.length;++i){
					if(i>2) this.value+=" ";
					this.value += explode[i];
				}	
			}
		}
	}
	
	public static void main(String[] args)throws IOException {
		//error handling for if file exist
		//====================================================		
		//Initialize
		File_bt = args[0];
		File_values = args[1];	
		createNew();	
		keyArray= Records.get(bt_rootLocation);		
		Scanner sc = new Scanner(System.in);
		System.out.print(">");
		
		while (sc.hasNext())
		{				
			//Input
			Input read = new Input(sc.nextLine());
			//selects correct array to execute command, always starts at root	
			searchNode(read, bt_rootLocation, 2); 
			System.out.println("current root = " + currentroot);
			//Commands
			switch(read.command)
			{	
				case CMD_INSERT:
					if(exist(read.key)){
						System.out.printf("< ERROR: %d already exists.\n", read.key);
						break;
					}
					insert(read.key, read.value);
					break;
				case CMD_UPDATE:
					if(!exist(read.key)){
						System.out.printf("< ERROR: %d does not exists.\n", read.key);
						break;
					}
					update(read.key, read.value);					
					break;
				case CMD_SELECT:
					if(!exist(read.key)){
						System.out.printf("< ERROR: %d does not exists.\n", read.key);
						break;
					}
					select(read);
					break;
				case CMD_DELETE:
					if(!exist(read.key)){
						System.out.printf("< ERROR: %d does not exists.\n", read.key);
						break;
					}
					delete(read.key, read.value);
					break;
				case CMD_EXIT:
					System.exit(0);
					break;
				default:
					System.out.println("ERROR: invalid command");
					break;				
			}	
			System.out.println("update currentroot = " + currentroot);
			Records.set(currentroot, keyArray);	
			System.out.print(">");
		}
	}
	
	public static void createNew() {
		int[] newRecords = new int[length];
		Arrays.fill(newRecords, new Integer(-1));
		Records.add(newRecords);
		bt_recordCount +=1;
	}
	
	public static void searchNode(Input read, int focus, int index) throws IOException {
		/* By the end of this method, correct array/record for execution of command is selected
		Focus - the record num of bt
		Read - inputs
		Index is index of array in bt ~ keys, IDs, etc */
		
		//focus starts with root
		currentroot = focus;		
		keyArray = Records.get(focus);
		//if(index==length-3) { //if full, split
			if(keyArray[length-3] != -1){
			int promote = findPromote(read.key);		
			System.out.println("promote = " + promote);
			split(promote);			
			searchNode(read, bt_rootLocation, 2);
			}
		//}
			
		// if vacant
		if(keyArray[index] == -1) return;//if(index == -1) return; //means key node is empty,
		if(keyArray[index]==read.key) return; //for select/Update/Already exist
		else { //if not empty and not equal to any
			//if(index==length-3) { //if full, split
			//	if(keyArray[index] != -1){
			//	int promote = findPromote(read.key);		
			//	System.out.println(promote);
			//	split(promote);			
			//	searchNode(read, bt_rootLocation, 2);
			//	}
			//}
			if(read.key>keyArray[index]){
				index+=3;
				searchNode(read, focus, index);
			}
			else{
				int firstID = keyArray[index-1];
				if(firstID==-1) return; //if has no child
				else{ //going into the child = firstID		
					focus = firstID;
					searchNode(read, focus, 2);
				}
			}
		}
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

	public static void split(int promote){			
		//Split 2 children
		int[] temp = keyArray; //current focus array		
		int index = 2;
		
		//create new child
		createNew();
		keyArray = Records.get(bt_recordCount-1);	//newly creaate	
		for(int i = 2; i < length; i = i+3){ //loop in temp	
			if (temp[i] == promote){
				temp[i] = -1;
				temp[i+1] = -1;
				index+=3;
			}
			else if(temp[i] > promote){
				keyArray[index] = temp[i];
				keyArray[index+1] = temp[i+1];
				temp[i] = -1;
				temp[i+1] = -1;
				index+=3;
			}
		}	
		int tempfocus = bt_recordCount-1; //focus of new child		
		System.out.println("currentroot = " + tempfocus);
		System.out.println("tempfocus = " + tempfocus);
		//if no parent, create new root
		if(temp[0] == -1 && keyArray[0] == -1){			
			createNew(); //create parent/root
			bt_rootLocation = bt_recordCount-1;
			System.out.println("root location = " + bt_rootLocation);
			
			temp[0] = bt_rootLocation;
			keyArray[0] = bt_rootLocation;
			Records.set(currentroot, temp);			
			Records.set(tempfocus, keyArray);
			
			keyArray = Records.get(bt_rootLocation);
			keyArray[1] = currentroot;
			keyArray[1+3] = tempfocus;	
		}	
		//update new child in record
		//Records.set(currentroot, temp);
		//Records.set(tempfocus, keyArray);
				
	}
	
	public static int findPromote(int key){
		//if index is equal to mid of m, that is to be promoted
		int index = 0;		
		for(int i = 2; i < length; i = i+3){
			int keyTemp = keyArray[i];	
			if (key < keyTemp){	
				if (index == m/2){
					return key;
				}
				index++;
			}
			if (index == m/2){
				return keyArray[i];				
			}
			index++;
		}
		return -1;
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
				if (key < keyTemp){						
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
		Values.add(value);  //add value to value array	
		write(value);
		value_recordCount += 1;			
		System.out.printf("< %d inserted.\n", key);		
	}
			
	public static void update(int key, String value) {
		//check if key already exists (error if it does not)
		
		System.out.printf("< %d updated.\n", key);
	}
	
	public static void select(Input read) throws IOException{
		//using key, look for which record the value is in
		for(int i=2; i< length; i+=3) {
			int temp = keyArray[i];
			if(temp == read.key) {
				System.out.println(Values.get(keyArray[i+1])); 
				return;
			}
		}
		System.out.println("ERROR: "+ read.key + " does not exist.");
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
		}
			
		for(int[] recordnum : Records){
			for(int x : recordnum){
				System.out.printf("%d ", x);
			}
			System.out.println();
		}
		System.out.println();
		bt.close();
		
		//Write in Data.values
		RandomAccessFile values = new RandomAccessFile(File_values, "rwd");		
		values.writeLong(value_recordCount+1); //write/update num of records
		//loop to update all records
		values.seek(value_recordBytes + value_recordCount * value_stringBytes); //look which "record" to updated/add new line
		values.writeShort(value.length()); 	//write length of value
		values.write(value.getBytes("UTF8")); 	//write value after converting to bytes
		values.close();	
	}	 
	
}
//check if values exist method
//valuesrecords(string strFile) throws IOException {
//	File file = new File(strFile);
//	if(!file.exists()){
//			
//	}
//		}
