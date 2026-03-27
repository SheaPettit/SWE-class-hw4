import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Scanner;
import java.awt.List;
import java.awt.Dimension;

public class DVDGUI implements DVDUserInterface {

	private DVDCollection dvdlist;
	private String ratingSort;
	private String[] dvdInfoList;

	public DVDGUI(DVDCollection dl) {
		dvdlist = dl;
	}

	public void processCommands() {
		loadFile();
		createWindow();
	}

	private void loadFile() {
		String fileName = "";
		while(true) {
			fileName = JOptionPane.showInputDialog(null, "Enter File Name", "Load File", JOptionPane.PLAIN_MESSAGE);
			if(fileName == null)
				System.exit(0);
			if(fileName.isBlank())
				continue;
			break;
		}
		dvdlist.loadData(fileName);
	}

	private void loadNew() {
		String fileName = "";
		while(true) {
			fileName = JOptionPane.showInputDialog(null, "Enter File Name\n*WARNING* This will overwrite any dvds already in the collection", "Load File", JOptionPane.PLAIN_MESSAGE);
			if(fileName == null)
				return;
			if(fileName.isBlank())
				continue;
			break;
		}
		File file = new File(fileName);
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
					dvdlist.addOrModifyDVD(title, rating, runningTime);
					//prepare for next line
					index = 0;
					prevIndex = 0;
				}
			}
		} catch(Exception e) {
		} finally {
			if(scan != null) scan.close();
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

	private void createWindow() {
		JFrame frame = new JFrame("DVD Manager");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		createUI(frame);
		frame.setSize(500, 220);
		frame.setLocationRelativeTo(null); // Center on screen
		frame.setVisible(true); // make visible
	}

	private void createUI(final JFrame frame) {
		JPanel displayPanel = new JPanel();
		JPanel ratingPanel = new JPanel();
		JPanel runtimePanel = new JPanel();
		JPanel buttonPanel = new JPanel();
		LayoutManager layout = new FlowLayout();
		displayPanel.setLayout(layout);
		buttonPanel.setLayout(layout);
		ratingPanel.setLayout(new BorderLayout());
		runtimePanel.setLayout(new BorderLayout());
		

		JTextArea rating = new JTextArea();
		rating.setText("Rating:\n\nNew Rating:");
		rating.setEditable(false);
		rating.setFocusable(false);
		JTextField ratingText = new JTextField();
		ratingText.setSize(rating.getSize());

		JTextArea runtime = new JTextArea();
		runtime.setText("Running Time:\n\nNew Running Time:");
		runtime.setEditable(false);
		runtime.setFocusable(false);
		JTextField runtimeText = new JTextField();
		runtimeText.setSize(runtime.getSize());
		
		JButton addButton = new JButton("Add DVD");
		JButton modifyButton = new JButton("Modify DVD");
		JButton removeButton = new JButton("Remove DVD");
		JButton ratingButton = new JButton("Get DVDs By Rating");
		JButton ratingResetButton = new JButton("Get all DVDs");
		ratingResetButton.setEnabled(false);
		JButton timeButton = new JButton("Get Total Running Time");
		JButton loadButton = new JButton("Combine With Another DVD Collection");
		JButton exitButton = new JButton("Exit and Save");
		JButton[] listButtons = {modifyButton, removeButton};

		List dvdList = new List();
		dvdInfoList = parseDVDLists(dvdList, listButtons, null, rating, runtime);
		ratingSort = null;

		dvdList.addItemListener(new ItemListener() {
			public void itemStateChanged (ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					if(!modifyButton.isEnabled()) {
						for(JButton button : listButtons)
							button.setEnabled(true);
					}
					int index = (int) e.getItem();
					parseRatingRuntime(dvdInfoList, index, rating, runtime);
				}
			}
		});

		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] list = doAddDVD(dvdList, listButtons, rating, runtime);
				if(list != null)
					dvdInfoList = list;
			}
		});
		modifyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] list = doModifyDVD(dvdList, dvdInfoList, ratingText, runtimeText, listButtons, rating, runtime);
				if(list != null)
					dvdInfoList = list;
			}
		});
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] list = doRemoveDVD(dvdList, dvdInfoList, ratingText, runtimeText, listButtons, rating, runtime);
				if(list != null)
					dvdInfoList = list;
			}
		});
		ratingButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] list = doGetDVDsByRating(dvdList, ratingText, runtimeText, listButtons, rating, runtime, ratingResetButton);
				if(list != null)
					dvdInfoList = list;
			}
		});
		ratingResetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] list = doResetDVDList(dvdList, ratingText, runtimeText, listButtons, rating, runtime, ratingResetButton);
				if(list != null)
					dvdInfoList = list;
			}
		});
		timeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doGetTotalRunningTime();
			}
		});
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadNew();
				String[] list = doResetDVDList(dvdList, ratingText, runtimeText, listButtons, rating, runtime, ratingResetButton);
				if(list != null)
					dvdInfoList = list;
			}
		});
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSave();
				System.exit(0);
			}
		});

		displayPanel.add(dvdList);

		ratingPanel.add(rating, BorderLayout.CENTER);
		ratingPanel.add(ratingText, BorderLayout.SOUTH);
		displayPanel.add(ratingPanel);

		runtimePanel.add(runtime, BorderLayout.CENTER);
		runtimePanel.add(runtimeText, BorderLayout.SOUTH);
		displayPanel.add(runtimePanel);

		buttonPanel.add(addButton);
		buttonPanel.add(modifyButton);
		buttonPanel.add(removeButton);
		buttonPanel.add(ratingButton);
		buttonPanel.add(ratingResetButton);
		buttonPanel.add(timeButton);
		buttonPanel.add(loadButton);
		buttonPanel.add(exitButton);

		frame.getContentPane().add(displayPanel, BorderLayout.NORTH);
		frame.getContentPane().add(buttonPanel, BorderLayout.CENTER);
	}

	private String[] parseDVDLists(List list, JButton[] listButtons, String dvdRating, JTextArea rating, JTextArea runtime) {
		String str;
		for(JButton button : listButtons)
			button.setEnabled(false);
		rating.setText("Rating:\n\nNew Rating:");
		runtime.setText("Running Time:\n\nNew Running Time:");

		if(dvdRating == null)
			str = dvdlist.toString();
		else
			str = dvdlist.getDVDsByRating(dvdRating);
		if(str.isEmpty())
			return new String[0];
		int numLines = 1;
		for(int i = 0; i < str.length(); i++) {
			if(str.charAt(i) == '\n')
				numLines++;
		}
		String[] infoList = new String[numLines];
		int currentItem = 0;
		int prevIndex = 0;
		int index = 0;
		boolean newLine = true;
		for(; index < str.length(); index++) {
			char current = str.charAt(index);
			if(newLine && current == ',') {
				list.add(str.substring(prevIndex, index));
				newLine = false;
			}
			if(current == '\n') {
				infoList[currentItem++] = str.substring(prevIndex, index);
				prevIndex = index + 1;
				newLine = true;
			}
		}
		infoList[numLines-1] = str.substring(prevIndex, index);
		return infoList;
	}

	private void parseRatingRuntime(String[] list, int index, JTextArea rating, JTextArea runtime) {
		String parsestr = list[index];
		int prevIndex = 0;
		boolean notTitle = false;
		for(int i = 0; i < parsestr.length(); i++) {
			char character = parsestr.charAt(i);
			if(character == ',' && notTitle) {
				rating.setText("Rating:\n" + parsestr.substring(prevIndex, i) + "\nNew Rating:");
				prevIndex = i + 1;
				continue;
			}
			if(character == ',') {
				prevIndex = i + 1;
				notTitle = true;
			}
		}
		runtime.setText("Running Time:\n" + parsestr.substring(prevIndex, parsestr.length()) + "\nNew Running Time:");
	}

	private String[] doModifyDVD(List dvds, String[] currentList, JTextField ratingText, JTextField runtimeText, JButton[] listButtons, JTextArea ratingLabel, JTextArea runtimeLabel) {
		int dvdIndex = dvds.getSelectedIndex();
		String[] currentInfo = parseAll(currentList, dvdIndex);

		String title = currentInfo[0];
		String rating = ratingText.getText();
		String time = runtimeText.getText();
		boolean ratingBlank = rating.isBlank();
		boolean timeBlank = time.isBlank();

		if(ratingBlank && timeBlank)
			return null;
		if(ratingBlank)
			rating = currentInfo[1];
		if(timeBlank)
			time = currentInfo[2];

		ratingText.setText("");
		runtimeText.setText("");

		dvdlist.addOrModifyDVD(title, rating, time);

		// Display current collection to the console for debugging
		dvds.removeAll();
		return parseDVDLists(dvds, listButtons, ratingSort, ratingLabel, runtimeLabel);
	}
	private String[] parseAll(String[] list, int index) {
		String parsestr = list[index];
		String[] dvdInfo = new String[3];
		int prevIndex = 0;
		int i = 0;
		int num = 0;
		for(; i < parsestr.length(); i++) {
			char character = parsestr.charAt(i);
			if(character == ',') {
				dvdInfo[num++] = parsestr.substring(prevIndex, i);
				prevIndex = i + 1;
			}
		}
		dvdInfo[num++] = parsestr.substring(prevIndex, i);
		return dvdInfo;
	}
	
	private String[] doAddDVD(List dvds, JButton[] listButtons, JTextArea ratingLabel, JTextArea runtimeLabel) {

		// Request the title
		String title = JOptionPane.showInputDialog(null, "Enter title", "Title", JOptionPane.PLAIN_MESSAGE);
		if (title == null) {
			return null; // dialog was cancelled
		}
		title = title.toUpperCase();

		// Request the rating
		String rating = JOptionPane.showInputDialog(null, "Enter rating for " + title, "Rating", JOptionPane.PLAIN_MESSAGE);
		if (rating == null) {
			return null; // dialog was cancelled
		}
		rating = rating.toUpperCase();

		// Request the running time
		String time = JOptionPane.showInputDialog(null, "Enter running time for " + title, "Running Time", JOptionPane.PLAIN_MESSAGE);
		if (time == null) {
			return null;
		}

		// Add or modify the DVD (assuming the rating and time are valid
		dvdlist.addOrModifyDVD(title, rating, time);

		// Display current collection to the console for debugging
		dvds.removeAll();
		return parseDVDLists(dvds, listButtons, ratingSort, ratingLabel, runtimeLabel);
	}

	private String[] doRemoveDVD(List dvds, String[] currentList, JTextField ratingText, JTextField runtimeText, JButton[] listButtons, JTextArea ratingLabel, JTextArea runtimeLabel) {
		int dvdIndex = dvds.getSelectedIndex();
		String[] currentInfo = parseAll(currentList, dvdIndex);

		String title = currentInfo[0];
		ratingText.setText("");
		runtimeText.setText("");

		// Remove the matching DVD if found
		dvdlist.removeDVD(title);

		// Display current collection to the console for debugging
		
		dvds.removeAll();
		return parseDVDLists(dvds, listButtons, ratingSort, ratingLabel, runtimeLabel);
	}

	private String[] doGetDVDsByRating(List dvds, JTextField ratingText, JTextField runtimeText, JButton[] listButtons, JTextArea ratingLabel, JTextArea runtimeLabel, JButton resetButton) {
		String rating = JOptionPane.showInputDialog(null, "Enter rating", "Rating", JOptionPane.PLAIN_MESSAGE);
		if (rating == null) {
			return null;
		}
		if(!resetButton.isEnabled())
			resetButton.setEnabled(true);
		ratingText.setText("");
		runtimeText.setText("");
		ratingSort = rating;
		dvds.removeAll();
		return parseDVDLists(dvds, listButtons, rating, ratingLabel, runtimeLabel);
	}

	private String[] doResetDVDList(List dvds, JTextField ratingText, JTextField runtimeText, JButton[] listButtons, JTextArea ratingLabel, JTextArea runtimeLabel, JButton resetButton) {
		if(resetButton.isEnabled())
			resetButton.setEnabled(false);
		ratingText.setText("");
		runtimeText.setText("");
		ratingSort = null;
		dvds.removeAll();
		return parseDVDLists(dvds, listButtons, null, ratingLabel, runtimeLabel);
	}

	private void doGetTotalRunningTime() {
		JOptionPane.showMessageDialog(null, dvdlist.getTotalRunningTime(), "Total Running Time", JOptionPane.PLAIN_MESSAGE);
	}

	private void doSave() {

		dvdlist.save();

	}

}
