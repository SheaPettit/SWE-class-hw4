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

	private void loadFile() {
		String fileName = "";
		while (true) {
			fileName = JOptionPane.showInputDialog(null, "Enter File Name", "Load File", JOptionPane.PLAIN_MESSAGE);
			if (fileName == null)
				System.exit(0);
			if (fileName.isBlank())
				continue;
			break;
		}
		dvdlist.loadData(fileName);
	}

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
					if(currentList.compareTo(dvdlist.toString()) != 0)
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

	private void copyFile(File from, File to) throws IOException {
		Files.copy(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

	private void deleteDvdImage(String title) {
		title = title.toUpperCase();
		File destinationImage = new File(dvdImages + "/" + title + ".png");
		destinationImage.delete();
	}

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

	private void createWindow() {
		JFrame frame = new JFrame("DVD Manager");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		createUI(frame);
		frame.setSize(600, 400);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private void createUI(final JFrame frame) {
		JPanel mainButtonPanel = new JPanel();
		JPanel displayPanel = new JPanel();
		JPanel ratingPanel = new JPanel();
		JPanel runtimePanel = new JPanel();
		JPanel button1Panel = new JPanel();
		JPanel button2Panel = new JPanel();
		JPanel button3Panel = new JPanel();
		JPanel imagePanel = new JPanel();
		LayoutManager layout = new FlowLayout();
		displayPanel.setLayout(layout);
		button1Panel.setLayout(layout);
		button2Panel.setLayout(layout);
		button3Panel.setLayout(layout);
		ratingPanel.setLayout(new BorderLayout());
		runtimePanel.setLayout(new BorderLayout());
		imagePanel.setLayout(new BorderLayout());
		mainButtonPanel.setLayout(new BorderLayout());

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

		JTextArea imageText = new JTextArea();
		imageText.setText("Image:");
		imageText.setEditable(false);
		imageText.setFocusable(false);
		JLabel dvdImage = new JLabel();

		JButton addButton = new JButton("Add DVD");
		JButton modifyButton = new JButton("Modify DVD");
		JButton removeButton = new JButton("Remove DVD");
		JButton ratingButton = new JButton("Get DVDs By Rating");
		JButton ratingResetButton = new JButton("Get all DVDs");
		ratingResetButton.setEnabled(false);
		JButton timeButton = new JButton("Get Total Running Time");
		JButton loadButton = new JButton("Combine With Another DVD Collection");
		JButton exitButton = new JButton("Exit and Save");
		JButton[] listButtons = { modifyButton, removeButton };

		List dvdList = new List();
		dvdInfoList = parseDVDLists(dvdList, listButtons, null, rating, runtime);
		ratingSort = null;
		try {
			initializeImages();
		} catch (IOException e) {
			e.printStackTrace();
		}

		dvdList.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					if (!modifyButton.isEnabled()) {
						for (JButton button : listButtons)
							button.setEnabled(true);
					}
					int index = (int) e.getItem();
					parseRatingRuntime(dvdInfoList, index, rating, runtime, dvdImage);
				}
			}
		});

		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] list = doAddDVD(dvdList, listButtons, rating, runtime, dvdImage);
				if (list != null)
					dvdInfoList = list;
			}
		});
		modifyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] list = doModifyDVD(dvdList, dvdInfoList, ratingText, runtimeText, listButtons, rating, runtime,
				    dvdImage);
				if (list != null)
					dvdInfoList = list;
			}
		});
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] list = doRemoveDVD(dvdList, dvdInfoList, ratingText, runtimeText, listButtons, rating, runtime,
				    dvdImage);
				if (list != null)
					dvdInfoList = list;
			}
		});
		ratingButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] list = doGetDVDsByRating(dvdList, ratingText, runtimeText, listButtons, rating, runtime,
				    ratingResetButton);
				if (list != null)
					dvdInfoList = list;
			}
		});
		ratingResetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] list = doResetDVDList(dvdList, ratingText, runtimeText, listButtons, rating, runtime,
				    ratingResetButton);
				if (list != null)
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
				String[] list = doResetDVDList(dvdList, ratingText, runtimeText, listButtons, rating, runtime,
				    ratingResetButton);
				if (list != null)
					dvdInfoList = list;
			}
		});
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSave();
				System.exit(0);
			}
		});

		ratingPanel.add(rating, BorderLayout.CENTER);
		ratingPanel.add(ratingText, BorderLayout.SOUTH);
		displayPanel.add(ratingPanel);

		runtimePanel.add(runtime, BorderLayout.CENTER);
		runtimePanel.add(runtimeText, BorderLayout.SOUTH);
		displayPanel.add(runtimePanel);

		imagePanel.add(imageText, BorderLayout.CENTER);
		imagePanel.add(dvdImage, BorderLayout.SOUTH);
		displayPanel.add(imagePanel);

		button1Panel.add(addButton);
		button1Panel.add(modifyButton);
		button1Panel.add(removeButton);
		button1Panel.add(timeButton);

		button2Panel.add(ratingButton);
		button2Panel.add(ratingResetButton);

		button3Panel.add(loadButton);
		button3Panel.add(exitButton);

		mainButtonPanel.add(button1Panel, BorderLayout.NORTH);
		mainButtonPanel.add(button2Panel, BorderLayout.CENTER);
		mainButtonPanel.add(button3Panel, BorderLayout.SOUTH);

		frame.getContentPane().add(displayPanel, BorderLayout.NORTH);
		frame.getContentPane().add(dvdList, BorderLayout.CENTER);
		frame.getContentPane().add(mainButtonPanel, BorderLayout.SOUTH);
	}

	private String[] parseDVDLists(List list, JButton[] listButtons, String dvdRating, JTextArea rating,
	    JTextArea runtime) {
		String str;
		for (JButton button : listButtons)
			button.setEnabled(false);
		rating.setText("Rating:\n\nNew Rating:");
		runtime.setText("Running Time:\n\nNew Running Time:");

		if (dvdRating == null)
			str = dvdlist.toString();
		else
			str = dvdlist.getDVDsByRating(dvdRating);
		if (str.isEmpty())
			return new String[0];
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
		infoList[numLines - 1] = str.substring(prevIndex, index);
		return infoList;
	}

	private void parseRatingRuntime(String[] list, int index, JTextArea rating, JTextArea runtime, JLabel image) {
		String parsestr = list[index];
		int prevIndex = 0;
		boolean notTitle = false;
		for (int i = 0; i < parsestr.length(); i++) {
			char character = parsestr.charAt(i);
			if (character == ',' && notTitle) {
				rating.setText("Rating:\n" + parsestr.substring(prevIndex, i) + "\nNew Rating:");
				prevIndex = i + 1;
				continue;
			}
			if (character == ',') {
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
		runtime.setText("Running Time:\n" + parsestr.substring(prevIndex, parsestr.length()) + "\nNew Running Time:");
	}

	private String[] doModifyDVD(List dvds, String[] currentList, JTextField ratingText, JTextField runtimeText,
	    JButton[] listButtons, JTextArea ratingLabel, JTextArea runtimeLabel, JLabel image) {
		int dvdIndex = dvds.getSelectedIndex();
		String[] currentInfo = parseAll(currentList, dvdIndex);

		String title = currentInfo[0];
		String rating = ratingText.getText();
		String time = runtimeText.getText();
		boolean ratingBlank = rating.isBlank();
		boolean timeBlank = time.isBlank();

		if (ratingBlank && timeBlank)
			return null;
		if (ratingBlank)
			rating = currentInfo[1];
		if (timeBlank)
			time = currentInfo[2];

		ratingText.setText("");
		runtimeText.setText("");
		image.setIcon(null);

		dvdlist.addOrModifyDVD(title, rating, time);

		dvds.removeAll();
		if (!ratingBlank) {
			try {
				newDvdImage(title, rating);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return parseDVDLists(dvds, listButtons, ratingSort, ratingLabel, runtimeLabel);
	}

	private String[] parseAll(String[] list, int index) {
		String parsestr = list[index];
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

	private String[] doAddDVD(List dvds, JButton[] listButtons, JTextArea ratingLabel, JTextArea runtimeLabel,
	    JLabel image) {

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
		String time = JOptionPane.showInputDialog(null, "Enter running time for " + title, "Running Time",
		    JOptionPane.PLAIN_MESSAGE);
		if (time == null) {
			return null;
		}

		// Add or modify the DVD (assuming the rating and time are valid
		dvdlist.addOrModifyDVD(title, rating, time);

		image.setIcon(null);
		try {
			newDvdImage(title, rating);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Display current collection to the console for debugging
		dvds.removeAll();
		return parseDVDLists(dvds, listButtons, ratingSort, ratingLabel, runtimeLabel);
	}

	private String[] doRemoveDVD(List dvds, String[] currentList, JTextField ratingText, JTextField runtimeText,
	    JButton[] listButtons, JTextArea ratingLabel, JTextArea runtimeLabel, JLabel image) {
		int dvdIndex = dvds.getSelectedIndex();
		String[] currentInfo = parseAll(currentList, dvdIndex);

		String title = currentInfo[0];
		ratingText.setText("");
		runtimeText.setText("");
		image.setIcon(null);

		// Remove the matching DVD if found
		dvdlist.removeDVD(title);

		// Display current collection to the console for debugging
		deleteDvdImage(title);
		dvds.removeAll();
		return parseDVDLists(dvds, listButtons, ratingSort, ratingLabel, runtimeLabel);
	}

	private String[] doGetDVDsByRating(List dvds, JTextField ratingText, JTextField runtimeText, JButton[] listButtons,
	    JTextArea ratingLabel, JTextArea runtimeLabel, JButton resetButton) {
		String rating = JOptionPane.showInputDialog(null, "Enter rating", "Rating", JOptionPane.PLAIN_MESSAGE);
		if (rating == null) {
			return null;
		}
		if (!resetButton.isEnabled())
			resetButton.setEnabled(true);
		ratingText.setText("");
		runtimeText.setText("");
		ratingSort = rating;
		dvds.removeAll();
		return parseDVDLists(dvds, listButtons, rating, ratingLabel, runtimeLabel);
	}

	private String[] doResetDVDList(List dvds, JTextField ratingText, JTextField runtimeText, JButton[] listButtons,
	    JTextArea ratingLabel, JTextArea runtimeLabel, JButton resetButton) {
		if (resetButton.isEnabled())
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