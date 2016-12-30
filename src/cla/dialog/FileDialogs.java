package cla.dialog;

import java.awt.FileDialog;
import java.io.File;
import java.io.FilenameFilter;
import javax.swing.JOptionPane;
import cla.Screen;
import cla.util.FileManager;

/**
 * This class is used to display dialogs that relate to saving and loading files.
 * 
 * @author Duncan Cowan
 *
 */
public abstract class FileDialogs {
	
	/**
	 * Displays a dialog asking the user if they want to save the current file.
	 * 
	 * @return the option the user chose or 0 if the current file is already saved.
	 */
	public static int askToSaveDialog() {
		// If the current file isn't saved...
		if(!FileManager.isFileSaved()) {
			if(Screen.getInstance().isFullscreen())
				// Set the main window's state to windowed if it's fullscreen.
				Screen.getInstance().toggleFullscreen();
			
			// Show the dialog and store the result.
			int returnVal = JOptionPane.showConfirmDialog(Screen.getInstance().getFrame(), "Save changes to animation?", "Save Animation?", JOptionPane.YES_NO_OPTION);
			if(returnVal == JOptionPane.YES_OPTION) {
				if(FileManager.getOpenFile() == null)
					// If the user clicked yes and the current animation
					// has never been saved, show the save dialog.
					saveDialog();
				else
					// If the user clicked yes and the current animation
					// has been saved before, just update the file.
					FileManager.saveOpenFile();
			}
			// Return the option the user chose.
			return returnVal;
		}
		// Return 0 if the current file is saved.
		return 0;
	}
	
	/**
	 * Displays a dialog giving the user the ability to choose a file to open.
	 */
	public static void openDialog() {
		if(Screen.getInstance().isFullscreen())
			// Set the main window's state to windowed if it's fullscreen.
			Screen.getInstance().toggleFullscreen();
		
		// Create file dialog.
		FileDialog fd = new FileDialog(Screen.getInstance().getFrame(), "Open", FileDialog.LOAD);
		// Only accept Controller LED Animation (.cla) files.
		fd.setFile("*.cla");
		fd.setFilenameFilter(new FilenameFilter() {
		    @Override
		    public boolean accept(File dir, String name) {
		        return name.endsWith(".cla");
		    }
		});
		// Display the dialog above the main window.
		fd.setAlwaysOnTop(true);
		// Show the dialog.
		fd.setVisible(true);
		
		if (fd.getFile() != null) {
			// If the user chose a file, load it.
			FileManager.load(new File(fd.getDirectory()+fd.getFile()));
			System.out.println(fd.getDirectory()+fd.getFile());
		}
	}
	
	/**
	 * Displays a dialog giving the user the ability to select a directory
	 * and filename for the file to be saved as.
	 * 
	 * @return 1 if the user selected a directory/filename, 0 otherwise.
	 */
	public static int saveDialog() {
		if(Screen.getInstance().isFullscreen())
			// Set the main window's state to windowed if it's fullscreen.
			Screen.getInstance().toggleFullscreen();
		
		// Create file dialog.
		FileDialog fd = new FileDialog(Screen.getInstance().getFrame(), "Save", FileDialog.SAVE);
		// Display the dialog above the main window.
		fd.setAlwaysOnTop(true);
		// Show the dialog.
		fd.setVisible(true);
		
		if (fd.getFile() != null) {
			String path = fd.getDirectory()+fd.getFile();
			if(!path.endsWith(".cla"))
				// Ensure the file is saved as a .cla
				path += ".cla";
			
			// If the user choose a directory/filename, save the animation.
			FileManager.save(new File(path));
			System.out.println(path);
			
			// Return 1 if the user choose a directory/filename.
			return 1;
		} else {
			// Return 0 if the user didn't choose a directory/filename.
			return 0;
		}
	}
}
