import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.List;

/**
 * This class is an implementation of DVDUserInterface that uses JOptionPane to
 * display the menu of command choices.
 */

public class DVDGUI implements DVDUserInterface {

	private DVDCollection dvdlist;

	public DVDGUI(DVDCollection dl) {
		dvdlist = dl;
	}

	public void processCommands() {
		loadFile();
		createWindow();
	}

	private void loadFile() {
		String file = "";
		while(true) {
			file = JOptionPane.showInputDialog("Enter File Name");
			if(file == null)
				System.exit(0);
			if(file.isBlank())
				continue;
			break;
		}
		dvdlist.loadData(file);
	}

	private void createWindow() {
		JFrame frame = new JFrame("DVD Manager");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		createUI(frame);
		frame.setSize(560, 200);
		frame.setLocationRelativeTo(null); // Center on screen
		frame.setVisible(true); // make visible
	}

	private void createUI(final JFrame frame) {
		JPanel displayPanel = new JPanel();
		JPanel buttonPanel = new JPanel();
		LayoutManager layout = new FlowLayout();
		displayPanel.setLayout(layout);
		buttonPanel.setLayout(layout);
		
		List dvdList = new List();
		displayPanel.putClientProperty("ratingRuntimeList", getDVDLists(dvdList));
		JTextArea rating = new JTextArea();
		rating.setEditable(false);
		rating.setFocusable(false);
		JTextArea runtime = new JTextArea();
		runtime.setEditable(false);
		runtime.setFocusable(false);

		dvdList.addItemListener(new ItemListener() {
			public void itemStateChanged (ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					int index = (int) e.getItem();
					parseRatingRuntime((String []) displayPanel.getClientProperty("ratingRuntimeList"), index, rating, runtime);
				}
			}
		});
		
		displayPanel.add(dvdList);
		displayPanel.add(rating);
		displayPanel.add(runtime);

		frame.getContentPane().add(displayPanel, BorderLayout.NORTH);
		
		JButton addButton = new JButton("Add DVD");
		JButton modifyButton = new JButton("Modify DVD");
		JButton removeButton = new JButton("Remove DVD");
		JButton ratingButton = new JButton("Get DVDs By Rating");
		JButton timeButton = new JButton("Get Total Running Time");
		JButton exitButton = new JButton("Exit and Save");

		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] list = doAddDVD(dvdList);
				if(list != null)
					displayPanel.putClientProperty("ratingRuntimeList", list);
			}
		});
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doRemoveDVD();
			}
		});
		ratingButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doGetDVDsByRating();
			}
		});
		timeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doGetTotalRunningTime();
			}
		});
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSave();
				System.exit(0);
			}
		});
		

		frame.getRootPane().setDefaultButton(addButton);
		buttonPanel.add(addButton);
		buttonPanel.add(removeButton);
		buttonPanel.add(ratingButton);
		buttonPanel.add(timeButton);
		buttonPanel.add(exitButton);

		frame.getContentPane().add(buttonPanel, BorderLayout.CENTER);
	}

	private String[] getDVDLists(List list) {
		String str = dvdlist.toString();
		if(str.isEmpty())
			return new String[0];
		int numLines = 1;
		for(int i = 0; i < str.length(); i++) {
			if(str.charAt(i) == '\n')
				numLines++;
		}
		String[] ratingRuntimeList = new String[numLines];
		int currentItem = 0;
		int prevIndex = 0;
		int index = 0;
		boolean newLine = true;
		for(; index < str.length(); index++) {
			char current = str.charAt(index);
			if(newLine && current == ',') {
				list.add(str.substring(prevIndex, index));
				prevIndex = index + 1;
				newLine = false;
			}
			if(current == '\n') {
				ratingRuntimeList[currentItem++] = str.substring(prevIndex, index);
				prevIndex = index + 1;
				newLine = true;
			}
		}
		ratingRuntimeList[numLines-1] = str.substring(prevIndex, index);
		return ratingRuntimeList;
	}

	private void parseRatingRuntime(String[] list, int index, JTextArea rating, JTextArea runtime) {
		String parsestr = list[index];
		int prevIndex = 0;
		for(int i = 0; i < parsestr.length(); i++) {
			char character = parsestr.charAt(i);
			if(character == ',') {
				rating.setText("Rating\n" + parsestr.substring(0, i));
				prevIndex = i + 1;
			}
		}
		runtime.setText("Running Time\n" + parsestr.substring(prevIndex, parsestr.length()));
	}

	private void doModifyDVD() {
		return;
	}
	
	private String[] doAddDVD(List dvds) {

		// Request the title
		String title = JOptionPane.showInputDialog("Enter title");
		if (title == null) {
			return null; // dialog was cancelled
		}
		title = title.toUpperCase();

		// Request the rating
		String rating = JOptionPane.showInputDialog("Enter rating for " + title);
		if (rating == null) {
			return null; // dialog was cancelled
		}
		rating = rating.toUpperCase();

		// Request the running time
		String time = JOptionPane.showInputDialog("Enter running time for " + title);
		if (time == null) {
			return null;
		}

		// Add or modify the DVD (assuming the rating and time are valid
		dvdlist.addOrModifyDVD(title, rating, time);

		// Display current collection to the console for debugging
		System.out.println("Adding/Modifying: " + title + "," + rating + "," + time);
		System.out.println(dvdlist);
		dvds.removeAll();
		return getDVDLists(dvds);
	}

	private void doRemoveDVD() {

		// Request the title
		String title = JOptionPane.showInputDialog("Enter title");
		if (title == null) {
			return; // dialog was cancelled
		}
		title = title.toUpperCase();

		// Remove the matching DVD if found
		dvdlist.removeDVD(title);

		// Display current collection to the console for debugging
		System.out.println("Removing: " + title);
		System.out.println(dvdlist);

	}

	private void doGetDVDsByRating() {

		// Request the rating
		String rating = JOptionPane.showInputDialog("Enter rating");
		if (rating == null) {
			return; // dialog was cancelled
		}
		rating = rating.toUpperCase();

		String results = dvdlist.getDVDsByRating(rating);
		System.out.println("DVDs with rating " + rating);
		System.out.println(results);

	}

	private void doGetTotalRunningTime() {

		int total = dvdlist.getTotalRunningTime();
		System.out.println("Total Running Time of DVDs: ");
		System.out.println(total);

	}

	private void doSave() {

		dvdlist.save();

	}

}
