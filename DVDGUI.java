import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.List;
import java.awt.Dimension;

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
		frame.setSize(500, 200);
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
		JButton exitButton = new JButton("Exit and Save");
		JButton[] listButtons = {modifyButton, removeButton};

		List dvdList = new List();
		displayPanel.putClientProperty("dvdInfoList", parseDVDLists(dvdList, listButtons, null, rating, runtime));
		displayPanel.putClientProperty("ratingSort", null);

		dvdList.addItemListener(new ItemListener() {
			public void itemStateChanged (ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					if(!modifyButton.isEnabled()) {
						for(JButton button : listButtons)
							button.setEnabled(true);
					}
					int index = (int) e.getItem();
					parseRatingRuntime((String []) displayPanel.getClientProperty("dvdInfoList"), index, rating, runtime);
				}
			}
		});

		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] list = doAddDVD(displayPanel, dvdList, listButtons, rating, runtime);
				if(list != null)
					displayPanel.putClientProperty("dvdInfoList", list);
			}
		});
		modifyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] list = doModifyDVD(displayPanel, dvdList, (String[]) displayPanel.getClientProperty("dvdInfoList"), ratingText, runtimeText, listButtons, rating, runtime);
				if(list != null)
					displayPanel.putClientProperty("dvdInfoList", list);
			}
		});
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] list = doRemoveDVD(displayPanel, dvdList, (String[]) displayPanel.getClientProperty("dvdInfoList"), ratingText, runtimeText, listButtons, rating, runtime);
				if(list != null)
					displayPanel.putClientProperty("dvdInfoList", list);
			}
		});
		ratingButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] list = doGetDVDsByRating(displayPanel, dvdList, ratingText, runtimeText, listButtons, rating, runtime, ratingResetButton);
				if(list != null)
					displayPanel.putClientProperty("dvdInfoList", list);
			}
		});
		ratingResetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] list = doResetDVDList(displayPanel, dvdList, ratingText, runtimeText, listButtons, rating, runtime, ratingResetButton);
				if(list != null)
					displayPanel.putClientProperty("dvdInfoList", list);
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

		displayPanel.add(dvdList);

		ratingPanel.add(rating, BorderLayout.CENTER);
		ratingPanel.add(ratingText, BorderLayout.SOUTH);
		displayPanel.add(ratingPanel);

		runtimePanel.add(runtime, BorderLayout.CENTER);
		runtimePanel.add(runtimeText, BorderLayout.SOUTH);
		displayPanel.add(runtimePanel);

		buttonPanel.add(addButton);
		buttonPanel.add(modifyButton);
		buttonPanel.add(ratingResetButton);
		buttonPanel.add(removeButton);
		buttonPanel.add(ratingButton);
		buttonPanel.add(timeButton);
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

	private String[] doModifyDVD(JPanel panel, List dvds, String[] currentList, JTextField ratingText, JTextField runtimeText, JButton[] listButtons, JTextArea ratingLabel, JTextArea runtimeLabel) {
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
		return parseDVDLists(dvds, listButtons, (String) panel.getClientProperty("ratingSort"), ratingLabel, runtimeLabel);
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
	
	private String[] doAddDVD(JPanel panel, List dvds, JButton[] listButtons, JTextArea ratingLabel, JTextArea runtimeLabel) {

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
		dvds.removeAll();
		return parseDVDLists(dvds, listButtons, (String) panel.getClientProperty("ratingSort"), ratingLabel, runtimeLabel);
	}

	private String[] doRemoveDVD(JPanel panel, List dvds, String[] currentList, JTextField ratingText, JTextField runtimeText, JButton[] listButtons, JTextArea ratingLabel, JTextArea runtimeLabel) {
		int dvdIndex = dvds.getSelectedIndex();
		String[] currentInfo = parseAll(currentList, dvdIndex);

		String title = currentInfo[0];
		ratingText.setText("");
		runtimeText.setText("");

		// Remove the matching DVD if found
		dvdlist.removeDVD(title);

		// Display current collection to the console for debugging
		
		dvds.removeAll();
		return parseDVDLists(dvds, listButtons, (String) panel.getClientProperty("ratingSort"), ratingLabel, runtimeLabel);
	}

	private String[] doGetDVDsByRating(JPanel panel, List dvds, JTextField ratingText, JTextField runtimeText, JButton[] listButtons, JTextArea ratingLabel, JTextArea runtimeLabel, JButton resetButton) {
		String rating = JOptionPane.showInputDialog("Enter rating");
		if (rating == null) {
			return null;
		}
		if(!resetButton.isEnabled())
			resetButton.setEnabled(true);
		ratingText.setText("");
		runtimeText.setText("");
		panel.putClientProperty("ratingSort", rating);
		dvds.removeAll();
		return parseDVDLists(dvds, listButtons, rating, ratingLabel, runtimeLabel);
	}

	private String[] doResetDVDList(JPanel panel, List dvds, JTextField ratingText, JTextField runtimeText, JButton[] listButtons, JTextArea ratingLabel, JTextArea runtimeLabel, JButton resetButton) {
		if(resetButton.isEnabled())
			resetButton.setEnabled(false);
		ratingText.setText("");
		runtimeText.setText("");
		panel.putClientProperty("ratingSort", null);
		dvds.removeAll();
		return parseDVDLists(dvds, listButtons, null, ratingLabel, runtimeLabel);
		
	}

	private void doGetTotalRunningTime() {

		int total = dvdlist.getTotalRunningTime();
	}

	private void doSave() {

		dvdlist.save();

	}

}
