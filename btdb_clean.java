import java.util.*;
import java.io.*;
public class btdb {
	//javac btdb_clean.java
	//java -Xmx32M btdb_clean Data.bt Data.values	
	//global variables initialization
	
	//Files to open
	public static String File_bt= "Data.bt"; // set as Data.bt by default --> contains keys,offsets, nodes
	public static String File_values = "Data.values"; // contains values of keys
	
	//variables for read and write + commands
	public static int m=5; //this is changeable depending on the degree of bt the user prefers
	public static final int length = 3*m-1; //fixed bytes for writing 
	public static final String CMD_INSERT = "insert", CMD_UPDATE= "update", CMD_SELECT = "select",CMD_DELETE = "delete",CMD_EXIT = "exit";
	
	// Data.values variables
	public static int value_recordCount = 0; // counter for # of keys
	public static final int value_recordBytes = 8; // recordCount when RAM written will only be limited to 8 bytes
	public static final int value_StringBytes = 258; //2 bytes length || 256 bytes string value
	
	//Data.bt variables
	public static int bt_recordCount = -1; // counter for # of nodes
	public static int bt_rootLocation = 0; // tracker for root location
	public static final int bt_recordBytes = 16;
	
	/** not sure what the next currentFocus, newfocus are for**/
	public static int keyArray_index = 0;
	public static int destArray_index = 0;
	public static Input read;
	public static Scanner sc = new Scanner(System.in);

	public static ArrayList<int[]> Records = new ArrayList<int[]>(); //record of all array representation of nodes
	public static ArrayList<String> Values = new ArrayList<String>(); // record of all values
	public static int[] keyArray; //array in focus
	public static int[] dest_Array; //destination array
	public static boolean parent = false;
	public static int popForwardReserve = 0;
	public static int popReverseReserve = 0;
	public static int moveOutReserve = 0;
	
	public static int promoteValue = -1;
	
	//generalized input Object
	public static class Input{
		String command;
		String value;
		int key;
		int offset;
		Input(String inp){
			String[] explode = inp.split(" ");
			this.command = explode[0];
			int length = explode.length;
			if(length>1) this.key=Integer.valueOf(explode[1]);
			if(this.command.equals("insert")) {
				if(length>2) this.value = String.join(" ", Arrays.copyOfRange(explode, 2, explode.length));	
				else this.value="";
				Values.add(this.value);
				offset = value_recordCount;
				value_recordCount++;
			}
			if(this.command.equals("update")) {
				if(length>2) this.value = String.join(" ", Arrays.copyOfRange(explode, 2, explode.length));	
				else this.value="";
			}
		}
	}
		
	
	public static void main(String[] args) throws IOException{
		File_bt = args[0];
		File_values = args[1];

		btdb_init();
		
		while(sc.hasNext()) {
			String inp = sc.nextLine();
			read = new Input(inp);
			int ref_index = searchNode(bt_rootLocation,2);
			promoteValue =-1;
			parent = false;
			switch(read.command) {
				case CMD_INSERT:
					if(exist(ref_index)) System.out.printf("< ERROR: %d already exists. \n", read.key);
					else{
						System.out.printf("< %d inserted.\n", read.key);
						insert(ref_index);
						write();	
					}
					break;
				case CMD_UPDATE:
					update(2);
					break;
				case CMD_SELECT:
					select(2);
					break;
				case CMD_DELETE:
					break;
				case CMD_EXIT:
					System.exit(0);
					break;
				default:
					System.out.println("ERROR: invalid command");
			}
			System.out.print(">");
		}
		
	}
	
	public static boolean exist(int index){
		if(keyArray[index]==read.key) return true;
		else return false;
	}
	
	public static void btdb_init() {
		createNew();
		keyArray = Records.get(bt_rootLocation);
		keyArray_index = bt_recordCount;
		System.out.print(">");
	}
	
	// method for adding a new bt array with all elements equal to -1; call this for splitting and promoting
	public static void createNew() {
		int[] newRecords = new int[length];
		Arrays.fill(newRecords, new Integer(-1));
		Records.add(newRecords);
		bt_recordCount +=1;
	}
	
	public static int searchNode(int focus, int index) {
		int temp_leftchild = keyArray[index-1];
		if(index==length) {
			if(keyArray[index-1]==-1) return index-3;
			else return searchNode(temp_leftchild, 2);
		}
		else {
			keyArray = Records.get(focus);
			int temp_rightchild = keyArray[index+2];
			temp_leftchild = keyArray[index-1];
			keyArray_index = focus;
			int temp_key = keyArray[index];
			if(temp_key==-1) {
				if(temp_leftchild==-1) {
					return index;
				}
				else {
					 return searchNode(temp_leftchild,2);
				}
			}
			else if (temp_key==read.key) return index;
			else if (temp_key < read.key) {
				return searchNode(focus, index+=3);
			}
			else {
				if (temp_leftchild==-1) return index;
				else return searchNode(temp_leftchild,2);
			}
		}
	}
	
	public static void insert(int index) {
		if(keyArray[index]==-1) {
			keyArray[index] = read.key;
			keyArray[index+1] = read.offset;
			keyArray[index+2] = -1;
		}
		else if(keyArray[index]==read.key) return;
		else {
			if(keyArray[length-3]!=-1) {
				split(index);
				return;
			}
			int[] bt = {-1, read.key, read.offset, -1};
			move_forward(index, bt, length);
		}
	}
	
	public static void move_forward(int index, int[] bt, int last) {	
		if(bt[1]==-1 || index>=last) return;
		else {
			 int[] temp = {keyArray[index-1], keyArray[index], keyArray[index+1], keyArray[index+2]};
			 keyArray[index]=bt[1];
			 keyArray[index+1]=bt[2];
			 keyArray[index+2]=bt[3];
			 move_forward(index+=3, temp, last);
		}
	}

	public static int[] popForward(int index, int[] bt, int mid) {
		if(index>mid) return bt;
		else {			
			 int[] temp = {keyArray[index-1], keyArray[index], keyArray[index+1], keyArray[index+2]};
			 keyArray[index]=bt[1];
			 keyArray[index+1]=bt[2];
			 //keyArray[index+2]=bt[3];
			 popForwardReserve = bt[3];
			 return popForward(index+=3, temp, mid);
		}
	}
	public static int[] popReverse(int index, int[] bt, int mid) {
		if(index==mid) return bt;
		else {
			 int[] temp = {keyArray[index-1], keyArray[index], keyArray[index+1], keyArray[index+2]};
			 popReverseReserve =bt[0]; //similar to pop forward, should not be overwritten
			
			 keyArray[index]=bt[1];
			 keyArray[index+1]=bt[2];
			 keyArray[index+2]=bt[3];
			 return popReverse(index-=3, temp, mid);
		}
	}
	

	public static int[] popPromote(int index, int[] bt) {
		int mid;
		if(m%2==0) mid = (m/2-1)*3-1;
		else  mid = (m/2)*3-1;
		if(index<=mid)  return popForward(index, bt, mid); 
		else if(index==mid+1) return bt;
		else {
			if(read.key<keyArray[index]) return popReverse(index-3, bt, mid);
			else return popReverse(index, bt, mid);
		}
	}
	
	public static void split(int index) {
		createNew();
		int[] bt = {keyArray[index-1], read.key, read.offset, keyArray[index+2]};
		int[] promote_array = popPromote(index, bt);	
		dest_Array = Records.get(bt_recordCount);
		destArray_index = bt_recordCount;
		read.key = promote_array[1]; //[0]
		read.offset = promote_array[2];//[1]
		int mid;
		int midpop;
		
		if(m%2==0) { 
			mid = (m/2-1)*3-1;
			move_out(mid+3, 2); //move_out(mid+2, 1);// //move_out(mid+3, 2);
			midpop = mid+2;
		}
		else  {
			mid = (m/2+1)*3-1;
			move_out(mid, 2);  //move_out(mid-1, 1); // //move_out(mid, 2);
			midpop = mid-1;			
		}	
		moveOutReserve = keyArray[midpop];
		
		if (parent == true){	
			if(index <= mid){					
				if(promoteValue !=  promote_array[1]){				
					keyArray[check_children(index, keyArray[index])] = destArray_index-1;	
					if(check_children(index, keyArray[index]) != midpop){
						keyArray[midpop] = popForwardReserve;
						dest_Array[1] = moveOutReserve;
					}
					else{
						dest_Array[1] = popForwardReserve;
					}
				}			
				else{			
					keyArray[check_children(index-3, keyArray[index-3])] = moveOutReserve; //destArray_index-1;	
					if(check_children(index-3, keyArray[index-3]) != midpop){
						keyArray[midpop] = moveOutReserve; //popForwardReserve;
						dest_Array[1] = destArray_index-1;	//moveOutReserve;
					}
					else{
						dest_Array[1] = destArray_index-1; //popForwardReserve;
					}
				}
				update_parents(destArray_index-1);
			}		
			else{		
				keyArray[midpop] = moveOutReserve;
				dest_Array[4] = destArray_index-1; //popForwardReserve;
				dest_Array[1] = promote_array[3];
			}
		}
		promoteValue = promote_array[1];
		
			
		//for parent of new child? ===current fix
		dest_Array[0] = keyArray[0];
		promote();
	}	
	public static int check_children(int index, int keyArrayindexvalue){
		
		int[] destArray = Records.get(destArray_index-1);	
		if (destArray[2] < keyArrayindexvalue){		
			return index-1;			
		}
		else{		
			return index+2;
		}
	}
	
	public static void root_insert(int index) {
		parent = false;
		if(keyArray[length-3]!=-1) {
			parent = true;
			if(index==length) split(index-3);
			else {
				if(keyArray[index]<read.key) root_insert(index+=3);
				else {
					split(index);
					//update_parents(destArray_index);
				}
			}
			update_parents(destArray_index);
			
		}
		else if(keyArray[index]==-1) {
			keyArray[index] = read.key;
			keyArray[index+1] = read.offset;
			keyArray[index+2] = destArray_index;
		}
		else {
			if(keyArray[index]<read.key) root_insert(index+=3);
			//if(keyArray[length-3]!=-1) {
			else {
				
				int[] bt = {-1, read.key, read.offset, destArray_index};
				move_forward(index, bt, length);
			}
		}
	}
	
	public static void promote() {
		if(keyArray[0]==-1) {
			createNew();
			bt_rootLocation = bt_recordCount;
			keyArray[0]= bt_recordCount;
			dest_Array[0]=bt_recordCount;
			keyArray = Records.get(bt_recordCount);
			insert(2);
			keyArray[1] = keyArray_index;
			keyArray[4] = destArray_index;
			keyArray_index = bt_recordCount;
		}
		else
		{
			keyArray_index = keyArray[0];
			keyArray=Records.get(keyArray[0]);
			root_insert(2);
			dest_Array[0]=keyArray_index;
		}
	}
	public static void update_parents(int ParentNum){
		//boolean up = false;
		int[] parentArray = Records.get(ParentNum);
		
		for (int i = 1; i < length; i = i+3){	
			if(parentArray[i] != -1){				
				int[] temp_array = Records.get(parentArray[i]); //get records under new parent	
				temp_array[0]= ParentNum;//Records.get(i)
			}
		}
	}
	
	public static void move_out(int index,int dest_index) {
		if(index==length) return;
		else {
			dest_Array[dest_index]=keyArray[index];
			keyArray[index] =-1;
			move_out(index+1, dest_index+1);
		}
	}
	
	public static void write() throws IOException {		
		//Write in Data.bt
		RandomAccessFile bt = new RandomAccessFile(File_bt, "rwd");
		bt.writeLong(bt_recordCount+1); //write/update num of records		
		bt.writeLong(bt_rootLocation); //ROOT		
		bt.seek(bt_recordBytes + bt_recordCount * length); 		
		
		
		for(int i = 0; i < length ; ++i){
			bt.writeInt(keyArray[i]); 			
		}
		
		bt.close();
		
		//Write in Data.values
		RandomAccessFile values = new RandomAccessFile(File_values, "rwd");		
		values.writeLong(value_recordCount+1); //write/update num of records
		//loop to update all records
		values.seek(value_recordBytes + value_recordCount * value_StringBytes); //look which "record" to updated/add new line
		values.writeShort((read.value).length()); 	//write length of value
		values.write((read.value).getBytes("UTF8")); 	//write value after converting to bytes
		values.close();
	}	
	
	public static void update(int index) {
		//check if key already exists (error if it does not)
		if(index==length || keyArray[index]>read.key) {
			System.out.printf("< ERROR: %d does not exist. \n", read.key);
		}
		//else if(keyArray[index]==read.key) {
		else if(keyArray[index]==read.key) {
			Values.set(keyArray[index+1], read.value);
			System.out.printf("< %d updated. \n", read.key);
		}
		else {
			update(index+=3);
		}
	}
	
	public static void select(int index){
		//using key, look for which record the value is in
		if(index == length) System.out.println("ERROR: "+ read.key + " does not exist.");
		else {
			if(keyArray[index]== read.key) System.out.println(read.key + " => "+Values.get(keyArray[index+1]));
			else select(index+=3);
		}
	}
}
