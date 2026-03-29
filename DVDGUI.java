import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import java.util.Scanner;
import java.awt.List;

public class DVDGUI implements DVDUserInterface {

	private DVDCollection dvdlist;
	private String ratingSort;
	private String[] dvdInfoList;
	private String dvdImages;
	private File[] defaultImages;

	public DVDGUI(DVDCollection dl) {
		dvdlist = dl;
		defaultImages = new File[5];
	}

	public void processCommands() {
		loadFile();
		createWindow();
	}

	// Gets the dvd data file name until the it is canceled or a non-blank string,
	// then loads it
	private void loadFile() {
		String fileName = "";
		while (true) {
			fileName = JOptionPane.showInputDialog(null, "Enter File Name", "Load File", JOptionPane.PLAIN_MESSAGE);
			if (fileName == null)
				// exits if canceled
				System.exit(0);
			if (fileName.isBlank())
				continue;
			break;
		}
		dvdlist.loadData(fileName);
	}

	// Same blank validation checking as loadFile, then adds all valid dvds in the
	// file to the collection
	// Will generate images for the new dvds
	// Overwrites dvds already in collection
	private void loadNew() {
		String fileName = "";
		while (true) {
			fileName = JOptionPane.showInputDialog(null,
			    "Enter File Name\n*WARNING* This will overwrite any dvds already in the collection", "Load File",
			    JOptionPane.PLAIN_MESSAGE);
			if (fileName == null)
				return;
			if (fileName.isBlank())
				continue;
			break;
		}
		File file = new File(fileName);
		Scanner scan = null;
		try {
			if (!file.createNewFile()) {
				scan = new Scanner(file);
				String line, title, rating, runningTime;
				int index = 0;
				int prevIndex = 0;
				while (scan.hasNextLine()) {
					line = scan.nextLine();
					index = nextBreak(line, index);
					title = line.substring(prevIndex, index);
					prevIndex = ++index;
					index = nextBreak(line, index);
					rating = line.substring(prevIndex, index++);
					runningTime = line.substring(index, line.length());
					String currentList = dvdlist.toString();
					dvdlist.addOrModifyDVD(title, rating, runningTime);
					if (currentList.compareTo(dvdlist.toString()) != 0)
						newDvdImage(title, rating);
					index = 0;
					prevIndex = 0;
				}
			}
		} catch (Exception e) {
		} finally {
			if (scan != null)
				scan.close();
		}
	}

	// Helper method to copy files
	private void copyFile(File from, File to) throws IOException {
		Files.copy(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

	// Deletes the dvd's associated image
	private void deleteDvdImage(String title) {
		title = title.toUpperCase();
		File destinationImage = new File(dvdImages + "/" + title + ".png");
		destinationImage.delete();
	}

	// Creates a new image for a dvd
	private void newDvdImage(String title, String rating) throws IOException {
		title = title.toUpperCase();
		rating = rating.toUpperCase();
		int ratingIndex = -1;
		switch (rating) {
		case "G":
			ratingIndex = 0;
			break;
		case "PG":
			ratingIndex = 1;
			break;
		case "PG-13":
			ratingIndex = 2;
			break;
		case "NC-17":
			ratingIndex = 3;
			break;
		case "R":
			ratingIndex = 4;
			break;
		}
		if (ratingIndex < 0) {
			System.err.println("Invalid Rating. Should not happen");
			return;
		}
		File destinationImage = new File(dvdImages + "/" + title + ".png");
		destinationImage.createNewFile();
		copyFile(defaultImages[ratingIndex], destinationImage);
	}

	// Initializes the images for dvds in the collection
	private void initializeImages() throws IOException {
		File dir = new File(".");
		String path = dir.getAbsolutePath();
		dvdImages = (path.substring(0, path.length() - 1) + "DVDImages");
		File imageDir = new File(dvdImages);
		String[] ratings = { "G", "PG", "PG-13", "NC-17", "R" };
		for (int i = 0; i < ratings.length; i++) {
			defaultImages[i] = new File(ratings[i] + ".png");
		}
		imageDir.mkdir();
		int index = 0;
		int prevIndex = 0;
		String title, rating;
		for (String dvd : dvdInfoList) {
			index = nextBreak(dvd, index);
			title = dvd.substring(prevIndex, index);
			prevIndex = ++index;
			index = nextBreak(dvd, index);
			rating = dvd.substring(prevIndex, index++);
			int ratingIndex = -1;
			switch (rating) {
			case "G":
				ratingIndex = 0;
				break;
			case "PG":
				ratingIndex = 1;
				break;
			case "PG-13":
				ratingIndex = 2;
				break;
			case "NC-17":
				ratingIndex = 3;
				break;
			case "R":
				ratingIndex = 4;
				break;
			}
			File dvdImage = new File(dvdImages + "/" + title + ".png");
			if (ratingIndex >= 0) {
				dvdImage.createNewFile();
				copyFile(defaultImages[ratingIndex], dvdImage);
			}
			index = 0;
			prevIndex = 0;
		}
	}

	private int nextBreak(String str, int index) {
		// This helper method returns the index in the string the next time
		// a ',' will occur, starting at 'index'
		char character;
		while (index < str.length()) {
			character = str.charAt(index);
			if (character == ',') {
				break;
			}
			index++;
		}
		return index;
	}

	// Creates the frame
	private void createWindow() {
		JFrame frame = new JFrame("DVD Manager");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		createUI(frame);
		frame.setSize(600, 400);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	// Main UI method
	private void createUI(final JFrame frame) {
		// Panels needed
		JPanel mainButtonPanel = new JPanel();
		JPanel displayPanel = new JPanel();
		JPanel ratingPanel = new JPanel();
		JPanel runtimePanel = new JPanel();
		JPanel button1Panel = new JPanel();
		JPanel button2Panel = new JPanel();
		JPanel button3Panel = new JPanel();
		JPanel imagePanel = new JPanel();

		// Layouts in the panels
		LayoutManager layout = new FlowLayout();
		displayPanel.setLayout(layout);
		button1Panel.setLayout(layout);
		button2Panel.setLayout(layout);
		button3Panel.setLayout(layout);
		ratingPanel.setLayout(new BorderLayout());
		runtimePanel.setLayout(new BorderLayout());
		imagePanel.setLayout(new BorderLayout());
		mainButtonPanel.setLayout(new BorderLayout());

		// Rating text area
		JTextArea rating = new JTextArea();
		rating.setText("Rating:\n\nNew Rating:");
		rating.setEditable(false);
		rating.setFocusable(false);
		JTextField ratingText = new JTextField();
		ratingText.setSize(rating.getSize());

		// Runtime text area
		JTextArea runtime = new JTextArea();
		runtime.setText("Running Time:\n\nNew Running Time:");
		runtime.setEditable(false);
		runtime.setFocusable(false);
		JTextField runtimeText = new JTextField();
		runtimeText.setSize(runtime.getSize());

		// Image area
		JTextArea imageText = new JTextArea();
		imageText.setText("Image:");
		imageText.setEditable(false);
		imageText.setFocusable(false);
		JLabel dvdImage = new JLabel();

		// Button initialization
		JButton addButton = new JButton("Add DVD");
		JButton modifyButton = new JButton("Modify DVD");
		JButton removeButton = new JButton("Remove DVD");
		JButton ratingButton = new JButton("Get DVDs By Rating");
		JButton ratingResetButton = new JButton("Get all DVDs");
		ratingResetButton.setEnabled(false);
		JButton timeButton = new JButton("Get Total Running Time");
		JButton loadButton = new JButton("Combine With Another DVD Collection");
		JButton exitButton = new JButton("Exit and Save");
		// Buttons to enable/disable based on if something is selected in the list
		JButton[] listButtons = { modifyButton, removeButton };

		// List initialization based on the collection's dvds
		List dvdList = new List();
		// Storing the list and current rating to sort the list for display
		dvdInfoList = parseDVDLists(dvdList, listButtons, null, rating, runtime);
		ratingSort = null;
		// initialize the images
		try {
			initializeImages();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Logic for selected dvds from the list
		dvdList.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					// enables buttons
					if (!modifyButton.isEnabled()) {
						for (JButton button : listButtons)
							button.setEnabled(true);
					}
					// Updates display information based on dvd selected
					int index = (int) e.getItem();
					parseRatingRuntime(index, rating, runtime, dvdImage);
				}
			}
		});

		// Logic for add button
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] list = doAddDVD(dvdList, listButtons, rating, runtime, dvdImage);
				// Updates the info list if updated
				if (list != null)
					dvdInfoList = list;
			}
		});

		// Logic for modify button
		modifyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] list = doModifyDVD(dvdList, ratingText, runtimeText, listButtons, rating, runtime, dvdImage);
				// Updates the info list if updated
				if (list != null)
					dvdInfoList = list;
			}
		});

		// Logic for remove button
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] list = doRemoveDVD(dvdList, ratingText, runtimeText, listButtons, rating, runtime, dvdImage);
				// Updates the info list if updated
				if (list != null)
					dvdInfoList = list;
			}
		});

		// Logic for the sort by rating button
		ratingButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] list = doGetDVDsByRating(dvdList, ratingText, runtimeText, listButtons, rating, runtime,
				    ratingResetButton, dvdImage);
				// Updates the info list if updated
				if (list != null)
					dvdInfoList = list;
			}
		});

		// Logic for the reset rating sort button
		ratingResetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] list = doResetDVDList(dvdList, ratingText, runtimeText, listButtons, rating, runtime,
				    ratingResetButton, dvdImage);
				// Updates the info list if updated
				if (list != null)
					dvdInfoList = list;
			}
		});

		// Logic for getting total runtime
		timeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doGetTotalRunningTime();
			}
		});

		// Logic for loading a new file
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadNew();
				String[] list = doResetDVDList(dvdList, ratingText, runtimeText, listButtons, rating, runtime,
				    ratingResetButton, dvdImage);
				// Updates the info list if updated
				if (list != null)
					dvdInfoList = list;
			}
		});

		// Logic for saving and exiting
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSave();
			}
		});

		// Initializes rating panel
		ratingPanel.add(rating, BorderLayout.CENTER);
		ratingPanel.add(ratingText, BorderLayout.SOUTH);

		// Initializes runtime panel
		runtimePanel.add(runtime, BorderLayout.CENTER);
		runtimePanel.add(runtimeText, BorderLayout.SOUTH);

		// Initializes image panel
		imagePanel.add(imageText, BorderLayout.CENTER);
		imagePanel.add(dvdImage, BorderLayout.SOUTH);

		// Initializes display panel
		displayPanel.add(ratingPanel);
		displayPanel.add(runtimePanel);
		displayPanel.add(imagePanel);

		// Initializes button1 panel
		button1Panel.add(addButton);
		button1Panel.add(modifyButton);
		button1Panel.add(removeButton);
		button1Panel.add(timeButton);

		// Initializes button2 panel
		button2Panel.add(ratingButton);
		button2Panel.add(ratingResetButton);

		// Initializes button3 panel
		button3Panel.add(loadButton);
		button3Panel.add(exitButton);

		// Initializes main button panel
		mainButtonPanel.add(button1Panel, BorderLayout.NORTH);
		mainButtonPanel.add(button2Panel, BorderLayout.CENTER);
		mainButtonPanel.add(button3Panel, BorderLayout.SOUTH);

		// Adds all panels to frame
		frame.getContentPane().add(displayPanel, BorderLayout.NORTH);
		frame.getContentPane().add(dvdList, BorderLayout.CENTER);
		frame.getContentPane().add(mainButtonPanel, BorderLayout.SOUTH);
	}

	// Parses the dvd list
	private String[] parseDVDLists(List list, JButton[] listButtons, String dvdRating, JTextArea rating,
	    JTextArea runtime) {
		String str;
		// Reset button and text areas to deselected list view
		for (JButton button : listButtons)
			button.setEnabled(false);
		rating.setText("Rating:\n\nNew Rating:");
		runtime.setText("Running Time:\n\nNew Running Time:");

		// Gets the current list to parse
		if (dvdRating == null)
			str = dvdlist.toString();
		else
			str = dvdlist.getDVDsByRating(dvdRating);
		if (str.isEmpty())
			// If empty, return empty list
			return new String[0];
		// Gets the number of lines to initialize an array equal in size
		int numLines = 1;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '\n')
				numLines++;
		}
		String[] infoList = new String[numLines];
		int currentItem = 0;
		int prevIndex = 0;
		int index = 0;
		boolean newLine = true;
		// Parse through the list to build each line.
		for (; index < str.length(); index++) {
			char current = str.charAt(index);
			if (newLine && current == ',') {
				list.add(str.substring(prevIndex, index));
				newLine = false;
			}
			if (current == '\n') {
				infoList[currentItem++] = str.substring(prevIndex, index);
				prevIndex = index + 1;
				newLine = true;
			}
		}
		// last line
		infoList[numLines - 1] = str.substring(prevIndex, index);
		return infoList;
	}

	// parses through the current item list to update the text areas and image
	private void parseRatingRuntime(int index, JTextArea rating, JTextArea runtime, JLabel image) {
		String parsestr = dvdInfoList[index];
		int prevIndex = 0;
		boolean notTitle = false;
		for (int i = 0; i < parsestr.length(); i++) {
			char character = parsestr.charAt(i);
			if (character == ',' && notTitle) {
				// Rating text update
				rating.setText("Rating:\n" + parsestr.substring(prevIndex, i) + "\nNew Rating:");
				prevIndex = i + 1;
				continue;
			}
			if (character == ',') {
				// Image update
				try {
					image.setIcon(
					    new ImageIcon(ImageIO.read(new File(dvdImages + "/" + parsestr.substring(prevIndex, i) + ".png"))));
				} catch (IOException e) {
					e.printStackTrace();
				}
				prevIndex = i + 1;
				notTitle = true;
			}
		}
		// runtime text update
		runtime.setText("Running Time:\n" + parsestr.substring(prevIndex, parsestr.length()) + "\nNew Running Time:");
	}

	// Modify's the dvd collection based on information in text fields
	private String[] doModifyDVD(List dvds, JTextField ratingText, JTextField runtimeText, JButton[] listButtons,
	    JTextArea ratingLabel, JTextArea runtimeLabel, JLabel image) {
		int dvdIndex = dvds.getSelectedIndex();
		String[] currentInfo = parseAll(dvdIndex);

		// getting title and modify data
		String title = currentInfo[0];
		String rating = ratingText.getText();
		String time = runtimeText.getText();

		// checks for empty text fields
		boolean ratingBlank = rating.isBlank();
		boolean timeBlank = time.isBlank();

		if (ratingBlank && timeBlank)
			// does nothing if the fields are empty
			return null;
		if (ratingBlank)
			// uses current rating if rating field is blank
			rating = currentInfo[1];
		if (timeBlank)
			// uses current runtime if runtime field is blank
			time = currentInfo[2];

		// resets image and text fields
		ratingText.setText("");
		runtimeText.setText("");
		image.setIcon(null);

		dvdlist.addOrModifyDVD(title, rating, time);

		// resets list
		dvds.removeAll();
		// updates image
		if (!ratingBlank) {
			try {
				newDvdImage(title, rating);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// updates list
		return parseDVDLists(dvds, listButtons, ratingSort, ratingLabel, runtimeLabel);
	}

	// Parses a dvd tostring to return a string list of title, rating, runtime
	private String[] parseAll(int index) {
		String parsestr = dvdInfoList[index];
		String[] dvdInfo = new String[3];
		int prevIndex = 0;
		int i = 0;
		int num = 0;
		for (; i < parsestr.length(); i++) {
			char character = parsestr.charAt(i);
			if (character == ',') {
				dvdInfo[num++] = parsestr.substring(prevIndex, i);
				prevIndex = i + 1;
			}
		}
		dvdInfo[num++] = parsestr.substring(prevIndex, i);
		return dvdInfo;
	}

	// Adds a new dvd based on input dialogs, or modifies if already in collection
	private String[] doAddDVD(List dvds, JButton[] listButtons, JTextArea ratingLabel, JTextArea runtimeLabel,
	    JLabel image) {

		// Request the title
		String title = JOptionPane.showInputDialog(null, "Enter title", "Title", JOptionPane.PLAIN_MESSAGE);
		if (title == null) {
			return null; // dialog was cancelled
		}

		// Request the rating
		String rating = JOptionPane.showInputDialog(null, "Enter rating for " + title, "Rating", JOptionPane.PLAIN_MESSAGE);
		if (rating == null) {
			return null; // dialog was cancelled
		}

		// Request the running time
		String time = JOptionPane.showInputDialog(null, "Enter running time for " + title, "Running Time",
		    JOptionPane.PLAIN_MESSAGE);
		if (time == null) {
			return null; // dialog was cancelled
		}

		dvdlist.addOrModifyDVD(title, rating, time);

		// reset image
		image.setIcon(null);
		// set new image
		try {
			newDvdImage(title, rating);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// reset list
		dvds.removeAll();
		// update list
		return parseDVDLists(dvds, listButtons, ratingSort, ratingLabel, runtimeLabel);
	}

	// removes dvd based on selected item in list
	private String[] doRemoveDVD(List dvds, JTextField ratingText, JTextField runtimeText, JButton[] listButtons,
	    JTextArea ratingLabel, JTextArea runtimeLabel, JLabel image) {
		// gets the current dvd's info
		int dvdIndex = dvds.getSelectedIndex();
		String[] currentInfo = parseAll(dvdIndex);

		// gets the title
		String title = currentInfo[0];
		// resets text fields and images
		ratingText.setText("");
		runtimeText.setText("");
		image.setIcon(null);

		dvdlist.removeDVD(title);

		// deletes the dvd's image file
		deleteDvdImage(title);
		// resets list
		dvds.removeAll();
		// updates list
		return parseDVDLists(dvds, listButtons, ratingSort, ratingLabel, runtimeLabel);
	}

	// sorts the dvd list by rating
	private String[] doGetDVDsByRating(List dvds, JTextField ratingText, JTextField runtimeText, JButton[] listButtons,
	    JTextArea ratingLabel, JTextArea runtimeLabel, JButton resetButton, JLabel image) {
		// gets rating
		String rating = JOptionPane.showInputDialog(null, "Enter rating", "Rating", JOptionPane.PLAIN_MESSAGE);
		if (rating == null) {
			return null; // does nothing if dialog is canceled
		}
		if (!resetButton.isEnabled())
			// enables the button to display all dvds if currently disabled
			resetButton.setEnabled(true);
		// Resets text fields and image
		ratingText.setText("");
		runtimeText.setText("");
		image.setIcon(null);
		// Sets the new rating to sort by
		ratingSort = rating;
		// resets list
		dvds.removeAll();
		// updates list
		return parseDVDLists(dvds, listButtons, rating, ratingLabel, runtimeLabel);
	}

	// resets the sorting of the dvd list
	private String[] doResetDVDList(List dvds, JTextField ratingText, JTextField runtimeText, JButton[] listButtons,
	    JTextArea ratingLabel, JTextArea runtimeLabel, JButton resetButton, JLabel image) {
		// disables the button to display all dvds
		if (resetButton.isEnabled())
			resetButton.setEnabled(false);
		// resets text fields and image
		ratingText.setText("");
		runtimeText.setText("");
		image.setIcon(null);
		// Sets the new rating to sort by to null (no sort)
		ratingSort = null;
		// resets list
		dvds.removeAll();
		// updates list
		return parseDVDLists(dvds, listButtons, null, ratingLabel, runtimeLabel);
	}

	// Opens a dialog displaying the total runtime
	private void doGetTotalRunningTime() {
		JOptionPane.showMessageDialog(null, dvdlist.getTotalRunningTime(), "Total Running Time", JOptionPane.PLAIN_MESSAGE);
	}

	// Simply saves
	private void doSave() {
		dvdlist.save();
		System.exit(0);
	}

}