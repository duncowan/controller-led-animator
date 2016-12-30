package cla;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URI;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import cla.dialog.FileDialogs;
import cla.dialog.UploadDialog;
import cla.util.FileManager;

/**
 * This class defines the custom menu bar used by the main window.
 * 
 * It creates each menu and its items as well as handling what happens
 * when a menu item is clicked.
 * 
 * @author Duncan Cowan
 * 
 */
@SuppressWarnings("serial")
public class MenuBar extends JMenuBar {
	private JMenuItem toggleLedNumbersMenuItem;
	private JMenuItem toggleFullscreenMenuItem;
	
	/**
	 * Constructor.
	 * 
	 * Creates each menu and its items then, adds each menu to the menu bar.
	 */
	public MenuBar() {
		/*
		 *  Attempts to load the example files which should be in a folder
		 *  called 'examples' in the same directory as the .jar executable.
		 */
		File[] examples = null;
		try {
			String examplesPath = MenuBar.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			examplesPath = examplesPath.substring(0, examplesPath.lastIndexOf(File.separator))+"/examples/";
			examples = (new File(examplesPath)).listFiles();
			System.out.println(examplesPath);
		} catch (Exception e) {
			examples = null;
			e.printStackTrace();
		}
		
		// Remove the boarder from the menu bar.
		this.setBorderPainted(false);
		
		// Menu item listener, handles menu item clicks.
		MenuItemListener menuItemListener = new MenuItemListener();

		// File menu.
		JMenu fileMenu = new JMenu("File");
		fileMenu.setFont(new Font("sans-serif", Font.PLAIN, 15));
		fileMenu.setForeground(Color.WHITE);

		// New menu item.
		JMenuItem newMenuItem = new JMenuItem("New");
		newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		newMenuItem.setActionCommand("New");
		newMenuItem.addActionListener(menuItemListener);
		fileMenu.add(newMenuItem);
		// Open menu item.
		JMenuItem openMenuItem = new JMenuItem("Open...");
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		openMenuItem.setActionCommand("Open");
		openMenuItem.addActionListener(menuItemListener);
		fileMenu.add(openMenuItem);
		// If the examples were loaded, make the Examples menu item.
		if(examples != null) {
			JMenu examplesMenu = new JMenu("Examples");
			for(File e : examples) {
				JMenuItem exampleMenuItem = new JMenuItem(e.getName());
				exampleMenuItem.setActionCommand("example~"+e.getAbsolutePath());
				exampleMenuItem.addActionListener(menuItemListener);
				examplesMenu.add(exampleMenuItem);
			}
			fileMenu.add(examplesMenu);
		}
		// Save menu item.
		JMenuItem saveMenuItem = new JMenuItem("Save");
		saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		saveMenuItem.setActionCommand("Save");
		saveMenuItem.addActionListener(menuItemListener);
		fileMenu.add(saveMenuItem);
		// Save As menu item.
		JMenuItem saveAsMenuItem = new JMenuItem("Save As...");
		saveAsMenuItem.setActionCommand("SaveAs");
		saveAsMenuItem.addActionListener(menuItemListener);
		fileMenu.add(saveAsMenuItem);
		fileMenu.addSeparator();
		// Exit menu item.
		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.setActionCommand("Exit");
		exitMenuItem.addActionListener(menuItemListener);
		fileMenu.add(exitMenuItem);
		
		this.add(fileMenu);
		
		// View menu.
		JMenu viewMenu = new JMenu("View");
		viewMenu.setFont(new Font("sans-serif", Font.PLAIN, 15));
		viewMenu.setForeground(Color.WHITE);

		// Toggle LED Numbers menu item.
		toggleLedNumbersMenuItem = new JMenuItem("Show LED Numbers");
		toggleLedNumbersMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		toggleLedNumbersMenuItem.setActionCommand("ToggleLedNumbers");
		toggleLedNumbersMenuItem.addActionListener(menuItemListener);
		viewMenu.add(toggleLedNumbersMenuItem);
		// Toggle Fullscreen menu item.
		toggleFullscreenMenuItem = new JMenuItem("Fullscreen");
		toggleFullscreenMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		toggleFullscreenMenuItem.setActionCommand("ToggleFullscreen");
		toggleFullscreenMenuItem.addActionListener(menuItemListener);
		viewMenu.add(toggleFullscreenMenuItem);

		this.add(viewMenu);

		// Tools menu.
		JMenu toolsMenu = new JMenu("Tools");
		toolsMenu.setFont(new Font("sans-serif", Font.PLAIN, 15));
		toolsMenu.setForeground(Color.WHITE);

		// Play menu item
		JMenuItem playMenuItem = new JMenuItem("Play");
		playMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
		playMenuItem.setActionCommand("Play");
		playMenuItem.addActionListener(menuItemListener);
		toolsMenu.add(playMenuItem);
		toolsMenu.addSeparator();
		// Upload menu item
		JMenuItem uploadMenuItem = new JMenuItem("Upload");
		uploadMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		uploadMenuItem.setActionCommand("Upload");
		uploadMenuItem.addActionListener(menuItemListener);
		toolsMenu.add(uploadMenuItem);

		this.add(toolsMenu);

		// Help menu.
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setFont(new Font("sans-serif", Font.PLAIN, 15));
		helpMenu.setForeground(Color.WHITE);

		// Fasebook message Duncan menu item
		JMenuItem fbMsgMenuItem = new JMenuItem("Facebook message Duncan...");
		fbMsgMenuItem.setActionCommand("fbMsg");
		fbMsgMenuItem.addActionListener(menuItemListener);
		helpMenu.add(fbMsgMenuItem);

		this.add(helpMenu);
	}

	/**
	 * Handles menu item clicks.
	 */
	private class MenuItemListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.out.println(e.getActionCommand());
			
			// If an example file was clicked, load it.
			if(e.getActionCommand().contains("example")) {
				FileManager.load(new File(e.getActionCommand().split("~")[1]));
				FileManager.closeFile();
				return;
			}
			
			switch (e.getActionCommand()) {
				// If the new menu item was clicked...
				case "New":
					// Ask to save the current file.
					FileDialogs.askToSaveDialog();
					// Close the current file.
					FileManager.closeFile();
					// Set the new file's save status to not saved.
					FileManager.isFileSaved(false);
					// Clear the timeline of the closed files data.
					Screen.getInstance().timeline.clearTimelines();
					break;
				// If the open menu item was clicked...
				case "Open":
					// Ask to save the current file.
					if(FileDialogs.askToSaveDialog() != JOptionPane.CLOSED_OPTION) {
						// If the user didn't close the ask to save dialog, 
						// display the open file dialog.
						FileDialogs.openDialog();
						/* Update the toggle fullscreen menu item's text to
						 * reflect the current state of the main window.
						 * This has to be done because displaying any
						 * file dialog causes the main window to go into windowed mode.
						 */
						toggleFullscreenMenuItem.setText(Screen.getInstance().isFullscreen() ? "Windowed" : "Fullscreen");
					}
					break;
				// If the save menu item was clicked...
				case "Save":
					if(FileManager.getOpenFile() == null) {
						// Display the save dialog if their isn't a open file
						// (i.e the current changes have never been saved).
						FileDialogs.saveDialog();
						/* Update the toggle fullscreen menu item's text to
						 * reflect the current state of the main window.
						 * This has to be done because displaying any
						 * file dialog causes the main window to go into windowed mode.
						 */
						toggleFullscreenMenuItem.setText(Screen.getInstance().isFullscreen() ? "Windowed" : "Fullscreen");
					} else
						// If there is a file open (i.e. the current 
						// changes are to a existing file), then just save it (no dialog).
						FileManager.saveOpenFile();
					break;
				// If the save as menu item was clicked...
				case "SaveAs":
					// Display the save dialog.
					FileDialogs.saveDialog();
					/* Update the toggle fullscreen menu item's text to
					 * reflect the current state of the main window.
					 * This has to be done because displaying any
					 * file dialog causes the main window to go into windowed mode.
					 */
					toggleFullscreenMenuItem.setText(Screen.getInstance().isFullscreen() ? "Windowed" : "Fullscreen");
					break;
				// If the exit menu item was clicked...
				case "Exit":
					// Ask to save the current file.
					if(FileDialogs.askToSaveDialog() != JOptionPane.CLOSED_OPTION)
						// If the user didn't close the ask to save dialog, 
						// close the program.
						System.exit(0);
					break;
				// If the toggle led numbers menu item was clicked...
				case "ToggleLedNumbers":
					// Toggle the led numbers in the preview UI.
					Screen.getInstance().preview.toggleLedNumbers();
					/* Update the toggle led numbers menu item's text to
					 * reflect the current state of the led numbers in the preview UI.
					 */
					toggleLedNumbersMenuItem.setText((Screen.getInstance().preview.isLedNumbersShowen() ? "Hide " : "Show ") + "LED Numbers");
					break;
				// If the toggle fullscreen menu item was clicked...
				case "ToggleFullscreen":
					// Toggle the main window's windowed state.
					Screen.getInstance().toggleFullscreen();
					/* Update the toggle fullscreen menu item's text to
					 * reflect the current state of the main window.
					 */
					toggleFullscreenMenuItem.setText(Screen.getInstance().isFullscreen() ? "Windowed" : "Fullscreen");
					break;
				// If the play menu item was clicked...
				case "Play":
					// Toggle the current animation's play status.
					Screen.getInstance().timeline.togglePlay();
					break;
				// If the upload menu item was clicked...
				case "Upload":
					int val = -1;
					if(FileManager.getOpenFile() == null)
						// Display the save dialog if the current changes
						// have never been saved.
						val = FileDialogs.saveDialog();
					if(val != 0)
						// Display the upload dialog if the changes
						// were successfully saved.
						new UploadDialog();
					/* Update the toggle fullscreen menu item's text to
					 * reflect the current state of the main window.
					 * This has to be done because displaying any
					 * file dialog causes the main window to go into windowed mode.
					 */
					toggleFullscreenMenuItem.setText(Screen.getInstance().isFullscreen() ? "Windowed" : "Fullscreen");
					break;
				// If the facebook message duncan menu item was clicked...
				case "fbMsg":
		            try {
		            	// Try to open facebook in the default web browser.
		            	Desktop.getDesktop().browse(new URI("http://www.facebook.com"));
		            } catch (Exception e1) {
		            	// If the default web browser can't be opened,
		            	// just tell the used to facebook message duncan.
		            	JOptionPane.showMessageDialog(Screen.getInstance().getFrame(), "For some reason your web browser can't be opened.\nTo fix this: \n    1. Open a web browser yourself \n    2. Go to facebook.com \n    3. Message Duncan your Controller LED Animator related problems");
		            }
		            
		            if(Screen.getInstance().isFullscreen()) {
		            	// If the the main window is fullscreen, make it windowed.
		            	Screen.getInstance().toggleFullscreen();
		            	// ...And change the toggle fullscreen menu item's
		            	// text to windowed.
						toggleFullscreenMenuItem.setText("Fullscreen");
		            }
					break;
				// If some other action happend...
				default:
					// Print it to the console.
					System.out.println("Unknown action: " + e.getActionCommand());
					break;
			}
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		// Make the background black and the text white.
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.WHITE);
	}
}