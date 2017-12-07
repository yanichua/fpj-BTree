	import java.util.*; 
	import java.io.*;
	public class btdb2{	
	//java -Xmx32M btdb2 Data.bt Data.values
		//GLOBAL VARIABLES INITIALIZATION
		//Files to open
		public static String File_bt = "Data.bt"; 			// set as Data.bt by default --> contains keys,offsets, nodes
		public static String File_values = "Data.values";	// contains values of keys
		//Variables for read and write + commands
		public static final int m = 4;						//this is changeable depending on the degree of bt the user prefers
		public static final int length = (3 * m) - 1;		//fixed bytes for writing 
		public static final String CMD_INSERT = "insert", CMD_UPDATE= "update", CMD_SELECT = "select",CMD_DELETE = "delete",CMD_EXIT = "exit";
		//Data.values variables
		public static int value_recordCount = 0;			// counter for # of keys
		public static final int value_recordBytes = 8; 		// recordCount when RAM written will only be limited to 8 bytes
		public static final int value_stringBytes = 258; 	// 2 bytes length || 256 bytes string value
		//Data.bt variables
		public static int bt_recordCount = 0;				// counter for # of nodes
		public static int bt_rootLocation = 0;				// tracker for root location
		public static final int bt_recordBytes = 16;		// bytes for record count and root location
		public static int currentFocus = 0;
		public static int newFocus = -1;
		//Internal Memory
		public static Input read;	
		public static int[] keyArray; 						//array in focus
		public static ArrayList<int[]> Records = new ArrayList<int[]>(); //	record of all array representation of nodes
		public static ArrayList<String> Values = new ArrayList<String>();// record of all values
		
		public static int[] parent_array = new int[length];
		//public static int[] temp;	
		
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
			
			File_bt = args[0];
			File_values = args[1];	
			createNew();	
			keyArray= Records.get(bt_rootLocation);		
			Scanner sc = new Scanner(System.in);
			System.out.print(">");
			
			while (sc.hasNext())
			{				
				//Input			
				read = new Input(sc.nextLine());			
				searchNode(bt_rootLocation, 2); 
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
				Records.set(currentFocus, keyArray);	
				System.out.print(">");
			}
		}
		
		public static void createNew() {
			int[] newRecords = new int[length];
			Arrays.fill(newRecords, new Integer(-1));
			Records.add(newRecords);
			bt_recordCount +=1;
		}
		
		public static void searchNode(int focus, int index) throws IOException {
			currentFocus = focus;	
			newFocus = -1;				
			keyArray = Records.get(focus);
			
			if(keyArray[index] == -1) return; 		//means key node is empty
			if(keyArray[index]==read.key) return; 	//for select/Update/Already exist
			else { 									//if not empty and not equal to key	
				if(keyArray[length-3] != -1){ 		//if selected array is full				
					if (keyArray[length-1] != -1){	//check if last child is -1
						find_child(focus, index);
					}
					else
					{
						//SPLIT	
						int promote = findPromote(read.key, keyArray);		
						split(read.key, promote, 0, keyArray, newFocus);			
						//latest array is the root
						if (read.key == promote){			
							keyArray = Records.get(bt_rootLocation);
							currentFocus = bt_rootLocation;
							return;
						}				
						else{
							searchNode(bt_rootLocation, 2);
						}
					}			
				}			
				find_child(focus, index);
			}
		}
		public static void find_child(int focus, int index)throws IOException {
			int firstID = -1;
			if(read.key > keyArray[index]){
				firstID = keyArray[index+2];				
				if(firstID==-1) return;
				else{
					if(index == length - 3){
						focus = firstID;
						searchNode(focus, 2);
					}
					else{
						if (keyArray[index+3]== -1 || read.key < keyArray[index+3]){
							focus = firstID;
							searchNode(focus, 2);
						}
						else{	
							index+=3;
							searchNode(focus, index);
						}
					}
				}
			}
			else{
				firstID = keyArray[index-1];
				if(firstID==-1) return; //if has no child
				else{ //going into the child = firstID		
					focus = firstID;
					searchNode(focus, 2);
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

		public static void split(int key, int promote, int promote_value, int[] temp, int newFocus2){			
			//SPLIT CURRENT ARRAY TO 2	
			int index = 2;	
			createNew();
			keyArray = Records.get(bt_recordCount-1);	//newly created child
			System.out.println("PROMOTE = " + promote);
			//update current array and new array to execute split
			for(int i = 2; i < length; i = i+3){ //loop in temp	
				if (temp[i] == promote){
					promote_value = temp[i+1];
					temp[i] = -1;
					temp[i+1] = -1;				
				}
				else if(temp[i] > promote){
					keyArray[index-1] = temp[i-1];
					keyArray[index] = temp[i];
					keyArray[index+1] = temp[i+1];
					keyArray[index+2] = temp[i+2];
					
					temp[i-1] = -1;
					temp[i] = -1;
					temp[i+1] = -1;
					temp[i+2] = -1;
					index+=3;
				}
			}	
			System.out.println("======new child=========");
			for(int x : keyArray){
				System.out.printf("%d ", x );
			}	
			System.out.println();	
				
				
			newFocus2 = bt_recordCount-1;  //focus of new child	
			newFocus = newFocus2;
			parent_array = Records.get(bt_rootLocation);	
			
			//FIRST NEW PARENT_ARRAY		
			if(temp[0] == -1 && keyArray[0] == -1){ //if no parent and root, create new root
				createNew(); //create new parent/root
				bt_rootLocation = bt_recordCount-1; //new root location
				
				//Assign parent node to children
				keyArray[0] = bt_rootLocation; 	
				temp[0] = bt_rootLocation;
			
				//update records
				Records.set(currentFocus, temp);			
				Records.set(newFocus2, keyArray);
				
				parent_array = Records.get(bt_rootLocation);
				//Assign children to parent
				parent_array[1] = currentFocus;
				parent_array[1+3] = newFocus2;
				Records.set(bt_rootLocation, parent_array);
			}	
			
			System.out.println("======new parent=========");
			for(int x : parent_array){
				System.out.printf("%d ", x );
			}	
			System.out.println();	
			
			Records.set(newFocus2, keyArray);			
			int idupdate = bt_rootLocation;		
			
			if(temp[0] != -1){ //already with existing parent	
				
				parent_array = Records.get(temp[0]);
				if(parent_array[length-1] != -1){ //full parent
					System.out.println("======new root promotion=========");
					//SECOND NEW PARENT ARRAY										
					currentFocus = bt_rootLocation;
					
					split(promote, findPromote(promote, parent_array), promote_value, keyArray ,newFocus2);
					//split(promote, findPromote(key, parent_array), promote_value, keyArray ,newFocus2);
					
					System.out.println("======after root promotion=========");
					for(int x : keyArray){
						System.out.printf("%d ", x );
					}					
					System.out.println();
					
					System.out.println("======parent update=========");
					update_parents(keyArray, newFocus);				
					
					parent_array = Records.get(bt_rootLocation);			
				
					System.out.println("======parent update others=========");
					for(int x = 1; x < length -1; x = x+3){					
						if (keyArray[x+3] == -1 && keyArray[x] != -1){	
							update_parents(Records.get(keyArray[x]), keyArray[x]);
						}
					}
					
					int inx = 1;	
					for (int i = 2; i < length-1; i = i + 3){	
						int firstID = parent_array[inx];
						if(parent_array[i]==-1){
							System.out.println("===============0===============");
							if (keyArray[0] == -1){
								keyArray[0] = currentFocus;
								}
							parent_array = Records.get(temp[0]);//firstID);						
							idupdate = temp[0] ;//firstID;
							break;
						}
						else{ //going into the child = firstID
							System.out.println("===============1===============");
							if (promote < parent_array[i]){		
								keyArray[0] = currentFocus;	
								parent_array = Records.get(firstID);
								idupdate = firstID;
								break;
							}
						}
						inx = inx + 3;
						if(inx == length-1){
							System.out.println("===============2===============");
							firstID = parent_array[inx];
							keyArray[0] = currentFocus;	
							parent_array = Records.get(firstID);
							idupdate = firstID;
							break;
						}
					}
				}
				else{ //if parent not full, put parent # in 1st offset
					System.out.println("===============3===============");
					keyArray[0] = temp[0];	 //keyArray[0] = bt_rootLocation;		
					parent_array = Records.get(temp[0]);	
					idupdate = temp[0];
				}	
			}	
			else{ //if parent not full, put parent # in 1st offset
					System.out.println("===============4===============");
					keyArray[0] = temp[0];	 //keyArray[0] = bt_rootLocation;		
					parent_array = Records.get(temp[0]);	
					idupdate = temp[0];
			}			
			//add promoted nodes if not same as key
			if(key != promote){
				//insert to root node		
				promote_to_root(promote, promote_value,parent_array,newFocus2);		
				Records.set(idupdate, parent_array);	
				
				System.out.println("======Promote if key != promote=========");
				for(int recordnum : parent_array){
					System.out.printf("%d ", recordnum);
				}	
				System.out.println("===========");	
			}		
			
		}	

		public static void update_parents(int[] A, int ParentNum){
			boolean up = false;
			System.out.println("update with new parents");	
			for (int i = 1; i < length-1; i = i+3){	
				if(A[i] != -1){
					
					int[] temp_array = Records.get(A[i]); //get records under new parent	
					temp_array[0]= ParentNum;//Records.get(i)
					
					for(int x : temp_array){
						System.out.printf("%d ", x);
					}
					System.out.println();
					
					Records.set(A[i], temp_array);	
					up = true;	
				}
			}
			System.out.println("update with new parents p2");	
			if (up == true){
			int[] temp_array = Records.get(ParentNum-1); //get records under new parent	
			//if(temp_array[2] > promote ){ //&& != newFocus){
			temp_array[0]= ParentNum;//Records.get(i)
			Records.set(ParentNum-1, temp_array);
			for(int x : temp_array){
				System.out.printf("%d ", x);
			}
			System.out.println();
			}
				
		}
		
		
		public static int findPromote(int key, int[] currentArray){
			//if index is equal to mid of m, that is to be promoted
			System.out.println("======CURRENT ARRAY FOR PROMOTION=========");
				for(int X : currentArray){
					System.out.printf("%d ", X);
				}	
				System.out.println();	
			System.out.println("Key = " + key);
			
			System.out.println("m/2 = " + (m/2));
			int index = 0;		
			for(int i = 2; i < length; i = i+3){
				int keyTemp = currentArray[i];	
				System.out.println("index = " + index);
				System.out.println("KeyTemp = " + keyTemp);
				if (key < keyTemp){	
					System.out.println("key < keyTemp");
					if (index == m/2){
						System.out.println("return key");
						return key;
					}
					index++;
				}
				if (key > keyTemp && index == m/2){
					System.out.println("return currentArray[i] = " + currentArray[i]);
					
					return keyTemp;				
				}
				index++;
			}
			return -1;
		}
		
		public static void promote_to_root(int key, int offset_value, int[] parent_array, int newFocus){
			//keyArray = keyArray[i]		
			for(int recordnum : parent_array){
					System.out.printf("%d ", recordnum);
				}
		
			System.out.println();		
			
			for(int i = 2; i < length; i = i+3){
				int keyTemp = parent_array[i];
				if(keyTemp == -1){ 							//if empty space
					parent_array[i] = key; 						//insert key
					parent_array[i+1] = offset_value; 			//insert offset of value
					parent_array[i+2] = newFocus;		//new child offset
					break;
				}
				else{
					if (key < keyTemp){						
						for(int j =  length - 6; j >= i; j = j-3){
							if (parent_array[j] != -1){	
								//parent_array[j+2] = parent_array[j-1];	
								parent_array[j+3] = parent_array[j];		//insert key
								parent_array[j+3+1] = parent_array[j+1];	//insert offset of value
								parent_array[j+3+2] = parent_array[j+2];	//child offset
							}
						}
						parent_array[i] = key; 						//insert key
						parent_array[i+1] = offset_value; 		//insert offset of value
						parent_array[i+2] = newFocus;	//new child offset
						break;
					}					
				}
			}		
		}
		
		public static void insert(int key, String value)throws IOException{	
			for(int i = 2; i < length; i = i+3){
				int keyTemp = keyArray[i];
				if(keyTemp == -1){ 							//if empty space
					keyArray[i] = key; 						//insert key
					keyArray[i+1] = value_recordCount; 		//insert offset of value
					keyArray[i+2] = newFocus;
					break;
				}
				else{
					if (key < keyTemp){						
						for(int j =  length - 6; j >= i; j = j-3){
							if (keyArray[j] != -1){							
								keyArray[j+3] = keyArray[j];		//insert key
								keyArray[j+3+1] = keyArray[j+1];	//insert offset of value
								keyArray[j+3+2] = keyArray[j+2];
							}
						}
						keyArray[i] = key; 						//insert key
						keyArray[i+1] = value_recordCount; 		//insert offset of value
						keyArray[i+2] = newFocus;
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
