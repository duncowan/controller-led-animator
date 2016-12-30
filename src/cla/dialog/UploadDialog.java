package cla.dialog;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import cla.Screen;
import cla.util.FileManager;
import jssc.SerialPortList;

/**
 * This class is used to search for controllers and select a controller to upload to.
 * 
 * @author Duncan Cowan
 *
 */
@SuppressWarnings("serial")
public class UploadDialog extends JDialog {
	// Get the OS name, used when searching for controllers.
	private final String osName = System.getProperty("os.name");
	// If the OS is a Mac, set the y offset to 20, for all other OSs set y offset to 0.
	private final int OSYOffset = (osName.equals("Mac OS X") || osName.equals("Darwin")) ? 20 : 0;
	// Set size of dialog.
	private final Dimension size = new Dimension(400, 150);
	
	// Stores the found controllers.
	private volatile String[] connectedControllers = new String[]{};
	// Stores the index of the controller to upload to.
	private int selectedController = 0;
	private volatile boolean waiting = true;

	/**
	 * Constructor.
	 * 
	 * Creates the upload dialog and starts searching for controllers.
	 */
	public UploadDialog() {
		super(Screen.getInstance().getFrame(), "Controller Selection", true);
		
		if(Screen.getInstance().isFullscreen())
			// Set the main window's state to windowed if it's fullscreen.
			Screen.getInstance().toggleFullscreen();
		
		// Set the dialog's location to the center of the main window.
		Rectangle mainWindowBounds = Screen.getInstance().getFrame().getBounds();
		this.setLocation(mainWindowBounds.x+(mainWindowBounds.width/2)-(size.width/2), 
						 mainWindowBounds.y+(mainWindowBounds.height/2)-(size.height/2));
		// Set the dialog's size and make it non-resizeable.
		this.setPreferredSize(size);
		this.setResizable(false);
		// Set custom content pane.
		this.setContentPane(new MainPanel());

		// Display the waiting UI while searching for controllers.
		waitingUI(this.getContentPane(), "Looking For Controllers");

		// Search for controllers in new thread.
		(new Thread() {
			public void run() {
				// Get available controllers.
				getControllers();
				// Once all available controllers have been found, 
				// stop waiting and remove the waiting UI components
				// from the content pane.
				waiting = false;
				getContentPane().removeAll();
				if(connectedControllers.length < 1) {
					// If no controllers were found, display the message UI
					// with the message "NO CONTROLLERS FOUND".
					messageUI(getContentPane(), "NO CONTROLLERS FOUND");
				} else {
					// If at least one controller was found, 
					// display the found controllers UI.
					foundControllersUI(getContentPane());
				}
				// Repaint the content pane to show the new UI.
				getContentPane().repaint();
			}
		}).start();
		
		this.pack();
		// Show the upload dialog.
		this.setVisible(true);
	}

	/**
	 * Searches for available controllers (COM ports) that are connected
	 *  to the computer based on the OS.
	 */
	private synchronized void getControllers() {
		Pattern regexp = null;
		if(osName.equals("Linux")) {
			regexp = Pattern.compile("rfcomm[0-9]{1,3}");
		} else if(osName.startsWith("Win")) {
			regexp = Pattern.compile("");
		} else if(osName.equals("SunOS")) {
			regexp = Pattern.compile("[0-9]*|[a-z]*");
		} else if(osName.equals("Mac OS X") || osName.equals("Darwin")) {
			regexp = Pattern.compile("tty\\..+-Dev[A-Z]");
		}
		// Set the array of connected controllers (COM port names).
		connectedControllers = SerialPortList.getPortNames(regexp);
	}

	/**
	 * Puts the waiting UI components into the specified container.
	 * 
	 * @param c the container to put the waiting UI components into.
	 * @param msg the waiting message.
	 */
	private void waitingUI(Container c, String msg) {
		// Waiting message.
		JLabel label = new JLabel(msg, SwingConstants.CENTER);
		label.setForeground(Color.WHITE);
		label.setBounds(0, 0, size.width, size.height-OSYOffset);
		c.add(label);

		//Animated dots.
		(new Thread() {
			public void run() {
				int count = 1;
				while(waiting) {
					String dots = "";
					for(int i = 0; i < count%4; i++)
						dots += ".";
					label.setText(msg+dots);
					count++;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {}
				}
			}
		}).start();
	}
	
	/**
	 * Puts the found controllers UI components into the specified container.
	 * 
	 * @param c the container to put the found controller UI components into.
	 */
	private synchronized void foundControllersUI(Container c) {
		// Controller (COM port) combo box label.
		JLabel label = new JLabel("Select a controller:", SwingConstants.CENTER);
		label.setForeground(Color.WHITE);
		label.setBounds(12, 12, size.width-24, 15);
		c.add(label);

		// Controller (COM port) combo box.
		JComboBox<String> ports = new JComboBox<String>();
		// Add all the found controllers to the combo box.
		for(int i = 0; i < connectedControllers.length; i++) {
			ports.addItem("Controller " + (i+1));
		}
		ports.setBounds((size.width/2)-150, (size.height/2)-20-(OSYOffset/2), 300, 20);
		ports.setBackground(Color.black);
		ports.setForeground(Color.white);
		ports.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedController = ports.getSelectedIndex();
			}
		});
		c.add(ports);

		// Upload button.
		JButton btnUpload = new JButton("Upload");
		btnUpload.setBounds(12, size.height-12-25-OSYOffset, size.width-24, 25);
		btnUpload.setBackground(Color.BLACK);
		btnUpload.setForeground(Color.white);
		btnUpload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println(connectedControllers[selectedController]);
				// When the upload button is clicked,
				// remove the found controllers UI components, 
				// start waiting and
				// display the waiting UI with the message "Uploading".
				c.removeAll();
				waiting = true;
				waitingUI(c, "Uploading");

				// Upload to selected controller in new thread.
				(new Thread() {
					public void run() {
						// Attempt to upload the current animation
						// to the selected controller.
						boolean success = FileManager.upload(connectedControllers[selectedController]);
						// After the upload has finished, stop waiting and
						// remove the waiting UI components.
						waiting = false;
						c.removeAll();
						if(success)
							// If the upload was successful, 
							// display the message UI with the message "UPLOAD COMPLETE".
							messageUI(c, "UPLOAD COMPLETE");
						else
							// If the upload was unsuccessful, 
							// display the message UI with the message "UPLOAD FAILED".
							messageUI(c, "UPLOAD FAILED");
						// Repaint the content pane to show the new UI.
						c.repaint();
					}
				}).start();
			}
		});
		c.add(btnUpload);
	}

	/**
	 * Puts the message UI components into the specified container.
	 * 
	 * @param c the container to put the message UI components into.
	 * @param msg the message to display.
	 */
	private synchronized void messageUI(Container c, String msg) {
		JLabel label = new JLabel(msg, SwingConstants.CENTER);
		label.setForeground(Color.WHITE);
		label.setBounds(0, 0, size.width, size.height-OSYOffset);
		c.add(label);
	}

	/**
	 * Custom content pane.
	 */
	private class MainPanel extends JPanel {
		public MainPanel() {
			// Set the background to black and use 
			// absolute positioning for the components.
			this.setBackground(Color.black);
			this.setLayout(null);
		}

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			// Add a white boarder around the edge of the content pane.
			g.setColor(Color.white);
			g.drawRect(0, -1, this.getWidth()-1, this.getHeight());
		}		
	}
}
