import java.io.*;
import java.util.Scanner;

public class DVDCollection {

	// Data fields
	
	/** The current number of DVDs in the array */
	private int numdvds;
	
	/** The array to contain the DVDs */
	private DVD[] dvdArray;
	
	/** The name of the data file that contains dvd data */
	private String sourceName;
	
	/** Boolean flag to indicate whether the DVD collection was
	    modified since it was last saved. */
	private boolean modified;
	
	/**
	 *  Constructs an empty directory as an array
	 *  with an initial capacity of 7. When we try to
	 *  insert into a full array, we will double the size of
	 *  the array first.
	 */
	public DVDCollection() {
		numdvds = 0;
		dvdArray = new DVD[7];
		modified = false;
	}
	
	public String toString() {
		// Return a string containing all the DVDs in the
		// order they are stored in the array along with
		// the values for numdvds and the length of the array.
		// See homework instructions for proper format.
		String returnString = "";
		for (int i = 0; i < numdvds; i++){
			if(!returnString.isBlank())
				returnString += "\n";
			returnString += dvdArray[i];
		}
		return returnString;
	}

	public void addOrModifyDVD(String title, String rating, String runningTime) {
		// NOTE: Be careful. Running time is a string here
		// since the user might enter non-digits when prompted.
		// If the array is full and a new DVD needs to be added,
		// double the size of the array first.
		title = title.toUpperCase();
		rating = rating.toUpperCase();
		int runTimeInt;

		//Validity checking
		switch(rating) {
			case "G":
			case "PG":
			case "PG-13":
			case "R":
			case "NC-17":
				break;
			default:
				System.out.println("Invalid Rating");
				return;
		}
		
		try {
			runTimeInt = Integer.parseInt(runningTime);
		} catch (Exception e) {
			System.out.println("Invalid Running Time");
			return;
		}
		if(runTimeInt <= 0) {
			System.out.println("Please enter a non-negative Running Time");
			return;
		}

		//New DVD
		DVD dvd = new DVD(title, rating, runTimeInt);

		//Finding where in the array the dvd should be inserted
		int arrayIndex = 0;
		while(arrayIndex < numdvds) {
			if(dvdArray[arrayIndex].getTitle().compareTo(title) >= 0) {
				break;
			}
			arrayIndex++;
		}

		//If the dvd is in the array, modify it and return
		if(arrayIndex < numdvds) {
			if(dvdArray[arrayIndex] != null &&
					dvdArray[arrayIndex].getTitle().compareTo(title) == 0) {
				dvdArray[arrayIndex] = dvd;
				modified = true;
				return;
			}
		}

		//Making space if necessary
		if(numdvds >= dvdArray.length) {
			DVD[] newDvdArray = new DVD[dvdArray.length * 2];
			for(int i = 0; i < dvdArray.length; i++) {
				newDvdArray[i] = dvdArray[i];
			}
			dvdArray = newDvdArray;
		}

		//add new dvd
		for(int i = numdvds; i > arrayIndex; i--) {
			dvdArray[i] = dvdArray[i-1];
		}
		dvdArray[arrayIndex] = dvd;
		numdvds++;
		modified = true;
	}
	
	public void removeDVD(String title) {
		title = title.toUpperCase();
		int indexInArray = 0;
		//Finding the title in the array
		while(indexInArray < numdvds) {
			if(dvdArray[indexInArray].getTitle().compareTo(title) == 0) {
				break;
			}
			indexInArray++;
		}
		//Return if not in array
		if(indexInArray >= numdvds) {
			return;
		}
		//Shift the array one forward at the index
		for(int i = indexInArray; i < (numdvds - 1); i++) {
			dvdArray[i] = dvdArray[i+1];
		}
		dvdArray[numdvds - 1] = null;
		numdvds--;
		modified = true;
	}
	
	public String getDVDsByRating(String rating) {
		String returnString = "";
		rating = rating.toUpperCase();
		for(int i = 0; i < numdvds; i++) {
			if(dvdArray[i].getRating().compareTo(rating) == 0) {
				if(!returnString.isBlank()) {
					returnString += "\n";
				}
				returnString += (dvdArray[i]);
			}
		}
		return returnString;	
	}

	public int getTotalRunningTime() {
		int totalRunTime = 0;
		for(int i = 0; i < numdvds; i++) {
			totalRunTime += dvdArray[i].getRunningTime();
		}
		return totalRunTime;	
	}

	
	public void loadData(String filename) {
		sourceName = filename;
		File file = new File(filename);
		Scanner scan = null;
		try {
			if(!file.createNewFile()){ //if it doesn't create a new file, scan the file
				scan = new Scanner(file);
				String line, title, rating, runningTime;
				int index = 0;
				int prevIndex = 0;
				while(scan.hasNextLine()) {
					line = scan.nextLine();
					//get title
					index = nextBreak(line, index);
					title = line.substring(prevIndex, index);
					//get rating
					prevIndex = ++index;
					index = nextBreak(line, index);
					rating = line.substring(prevIndex, index++);
					//get runningtime
					runningTime = line.substring(index, line.length());
					//check for corruption, and add dvd to array
					modified = false;
					addOrModifyDVD(title, rating, runningTime);
					if(modified == false) break;
					//prepare for next line
					index = 0;
					prevIndex = 0;
				}
			}
		} catch(Exception e) {
		} finally {
			if(scan != null) scan.close();
			modified = false;
		}
	}
	
	public void save() {
		if (!modified) return; //does nothing if unmodified
		File file = new File(sourceName);
		file.delete(); //this assures the file will be overwritten instead of causing some corruption
		try {
			file.createNewFile();
			FileWriter writer = new FileWriter(sourceName);
			writer.write(this.toString());
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			modified = false;
		}
	}

	private int nextBreak(String str, int index) {
		//This helper method returns the index in the string the next time
		// a ',' will occur, starting at 'index'
		char character;
			while(index < str.length()) {
				character = str.charAt(index);
				if(character == ',') {
					break;
				}
				index++;
			}
		return index;
	}
}