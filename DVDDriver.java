/*import java.util.*;

public class DVDDriver {
	public static void main(String[] args) {
		DVDCollection col = new DVDCollection();
		col.loadData("dvddata.txt");
		System.out.println(col.toString());
		col.addOrModifyDVD("test", "g", "-1");
		System.out.println(col.toString());
		Scanner scan = new Scanner(System.in);
		System.out.print("How many test dvds do I create: ");
		int num = 0;
		boolean cont = true;
		while(cont) {
			try {
				num = Integer.parseInt(scan.nextLine());
				cont = false;
			} catch(Exception e) {
				System.out.println("Invalid Int, Try again: ");
			}
		}
		String testRating = "G";
		for(int i = 0; i < num; i++) {
			switch(i % 5) {
				case 0:
					testRating = "G";
					break;
				case 1:
					testRating = "PG";
					break;
				case 2:
					testRating = "PG-13";
					break;
				case 3:
					testRating = "R";
					break;
				case 4:
					testRating = "NC-17";
					break;
			}
			col.addOrModifyDVD(("test" + i), testRating, "10");
		}
		System.out.println(col.toString());
		cont = true;
		num = 0;
		System.out.print("How many test dvds do I change: ");
		while(cont) {
			try {
				num = Integer.parseInt(scan.nextLine());
				cont = false;
			} catch(Exception e) {
				System.out.println("Invalid Int, Try again: ");
			}
		}
		for(int i = 0; i < num; i++) {
			col.addOrModifyDVD("test", "g", ("" + i));
			System.out.println(col.toString());
		}
		System.out.print("Please enter the number of tests to delete: ");
		cont = true;
		while(cont) {
			try {
				num = Integer.parseInt(scan.nextLine());
				cont = false;
			} catch(Exception e) {
				System.out.println("Invalid Int, Try again: ");
			}
		}
		for(int i = 0; i < num; i++) {
			col.removeDVD("test" + i);
			System.out.println(col.toString());
		}
		System.out.println("Attempting to delete a nonexistent dvd");
		col.removeDVD("no");
		System.out.println(col.toString());
		System.out.println("Total Running Time: " + col.getTotalRunningTime());
		System.out.println("G rated: " + col.getDVDsByRating("g"));
		System.out.println("PG rated: " + col.getDVDsByRating("pg"));
		System.out.println("PG-13 rated: " + col.getDVDsByRating("pg-13"));
		System.out.println("R rated: " + col.getDVDsByRating("R"));
		System.out.println("NC-17 rated: " + col.getDVDsByRating("Nc-17"));
		scan.close();
		col.save();
	}
}*/