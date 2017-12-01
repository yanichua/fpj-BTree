import java.util.*; 
import java.io.*;
public class btdb{	
//change 1: value record, instead of 256 yung bung record, gawin 2 bytes for the record information and 256 yung length of strings for value of input = 258 bytes
//change 2: from m = 5, now m = 7
//java -Xmx32M btdb Data.bt Data.values
	//File names
	public static String File_bt = "Data.bt";
	public static String File_values = "Data.values";	
	
	public static final int m = 7;
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
	
	public static int root;
	
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
		File_bt = args[0];
		File_values = args[1];		
		
		//Initialize
		createNew();
		root=0;
		keyArray= Records.get(0);
		Scanner sc = new Scanner(System.in);
		System.out.print(">");
		
		while (sc.hasNext())
		{	
			//Input
			Input read = new Input(sc.nextLine());

			//Commands
			switch(read.command)
			{
				case CMD_INSERT:
					/**if(exist(read.key)){
						System.out.printf("< ERROR: %d already exists.\n", read.key);
						break;
					}**/
					insert2(read);
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
					select(read.key);
					break;
				case CMD_DELETE:;
					delete(read.key, read.value);
					break;
				case CMD_EXIT:
					System.exit(0);
					break;
				default:
					System.out.println("ERROR: invalid command");
					break;
			}			
			System.out.print(">");
		}
	}
	
	public static void createNew() {
		int[] newRecords = new int[length];
		Arrays.fill(newRecords, new Integer(-1));
		Records.add(newRecords);
	}
	
	public static int searchNode (Input read, int focus, int index) throws IOException {
		//focus starts with root
		keyArray = Records.get(focus);
		// if vacant
		if(index==-1) return focus;
		if(keyArray[index]==read.key) return focus;
		else {
			if(index==length-3) {
				if(focus+1==Records.size()) return focus;
				else return searchNode(read, focus++, 2);
			}
			if(read.key>keyArray[index]) {
				index+=3;
				return searchNode(read, focus, index);
			}
			else {
				int firstID = keyArray[index-1];
				if(firstID==-1) return focus;
				else {
					for(int i=0; i<Records.size(); i++) {
						if(Records.get(i)[2]==firstID) return searchNode(read, i, 2);
					}
					return focus;
				}
			}
		}
	}
	public static void insert2(Input read) throws IOException{
		int f = searchNode(read, root,2);
		keyArray = Records.get(searchNode(read, root, 2));
		System.out.println(f);
		insert(read);
	}
		
	public static void insert(Input read)throws IOException{				
		if (keyArray[length - 3] != -1){ 	
			System.out.printf("< %s !!\n", "FULL");
			//split(key);
		}		
		for(int i = 2; i < length; i = i+3){
			int keyTemp = keyArray[i];
			if(keyTemp == -1){ 							//if empty space
				keyArray[i] = read.key; 						//insert key
				keyArray[i+1] = value_recordCount; 		//insert offset of value
				break;
			}
			else{
				if (keyTemp > read.key){						
					for(int j =  length - 6; j >= i; j = j-3){
						if (keyArray[j] != -1){							
							keyArray[j+3] = keyArray[j];		//insert key
							keyArray[j+3+1] = keyArray[j+1];	//insert offset of value												
						}
					}
					keyArray[i] = read.key; 						//insert key
					keyArray[i+1] = value_recordCount; 		//insert offset of value
					break;
				}					
			}
		}
		Values.add(read.value);  //add value to value array	
		write(read.value);
		value_recordCount += 1;			
		System.out.printf("< %d inserted.\n", read.key);
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
		for(int i=2; i< length; i+=3) {
			int temp = keyArray[i];
			if(temp == key) {
				System.out.println(Values.get(keyArray[i+1])); 
				return;
			}
		}
		System.out.println("ERROR: "+ key + " does not exist.");
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
