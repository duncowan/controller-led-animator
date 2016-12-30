package cla;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.*;
import cla.dialog.FileDialogs;
import cla.ui.Component;
import cla.ui.Preview;
import cla.ui.Timeline;

/**
 * This is the main class (i.e where everything starts).
 * 
 * It handles the creation of the main window, the drawing of the custom UI and
 * the passing of mouse events to the custom UI.
 * 
 * @author Duncan Cowan
 *
 */
@SuppressWarnings("serial")
public class Screen extends JPanel {
	public static final String APP_NAME = "Controller LED Animator";
	private static final ImageIcon ICON = new ImageIcon(Screen.class.getResource("/img/icon.png"));
	
	private static Screen instance;
	private static JFrame frame;
	
	// Set initial height and width of window.
	public static int WIDTH = 800;
	public static int HEIGHT = 600;
	// Set the padding between the edge of the window and the components.
	public static final int MARGIN = 20;
	
	// Set the screen to use when making the window fullscreen.
	private final GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	private boolean fullscreen = false;
	
	/* 
	 * 	List of top-level custom UI components.
	 *  
	 *  These components are directly drawn and passed mouse events by the JPanel.
	 *  
	 *  TODO Create a custom UI component that acts as a bridge between
	 *       Java UI components (e.g. JPanel) and custom UI components
	 *       so only one custom UI component is directly interacting
	 *       with a Java UI component.
	 */
	private ArrayList<Component> components = new ArrayList<Component>();
	public final Preview preview;
	public final Timeline timeline;
	
	/**
	 * Constructor.
	 * 
	 * Sets up the JPanel and adds the top-level custom UI component(s).
	 * 
	 * Note: This should only ever be called once (singleton).
	 */
	private Screen() {
		this.setBackground(Color.black);
		this.setFont(new Font("sans-serif", Font.PLAIN, 12));
		/*
		 * Whenever the window is resized, recalculate the size and position of
		 * the the custom UI components.
		 */
	    this.addComponentListener(new ComponentListener() {
	        public void componentResized(ComponentEvent e) {
	        	// Get ratio of the new height compared to the new height.
	        	double yMod = e.getComponent().getHeight() / (HEIGHT*1.0);
	        	// Get ratio of the new width compared to the new width.
	        	double xMod = e.getComponent().getWidth() / (WIDTH*1.0);
	        	
	        	// Set the new height and width.
	        	HEIGHT = e.getComponent().getHeight();
	        	WIDTH = e.getComponent().getWidth();
	        	
	        	// Notify the custom UI components.
	        	for(Component c : components)
	        		c.updateSizeAndPosition(xMod, yMod);
	        	
	        	// Re-draw the custom UI components.
	        	repaint();
	        }

	        /* Unused methods. */
			public void componentMoved(ComponentEvent e) {}
			public void componentShown(ComponentEvent e) {}
			public void componentHidden(ComponentEvent e) {}
	    });
	    
	    // Handle events from mouse clicks, movement and scrolling.
	    MouseHandler mh = new MouseHandler();
	    this.addMouseListener(mh);
	    this.addMouseMotionListener(mh);
	    this.addMouseWheelListener(mh);
	    
	    // Create the preview and timeline custom UI components.
	    int timeLineHeight = HEIGHT/4;
	    preview = new Preview(0, null, MARGIN, MARGIN, (timeLineHeight*3)-(MARGIN*2), WIDTH-(MARGIN*2));
	    timeline = new Timeline(1, null, MARGIN+40, (HEIGHT-MARGIN)-timeLineHeight, timeLineHeight, WIDTH-(MARGIN*2)-40);
	    
	    // ...And add them to the list of top-level custom UI components.
	    components.add(preview);
	    components.add(timeline);
	}
	
	/**
	 * Draws the custom UI components to the JPanel.
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		for(Component c : components) {
			if(c.isVisible())
				c.draw(g);
		}
	}
	
	/**
	 * Toggles between fullscreen and windowed mode.
	 */
	public void toggleFullscreen() {
		fullscreen = !fullscreen;
		
		if(fullscreen) {
			frame.setVisible(false);
			frame.dispose();
			frame.setUndecorated(true);
			graphicsDevice.setFullScreenWindow(frame);
			frame.setVisible(true);
		} else {
			frame.setVisible(false);
			frame.dispose();
			frame.setUndecorated(false);
			graphicsDevice.setFullScreenWindow(null);
			frame.setSize(WIDTH, HEIGHT);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		}
	}
	
	/**
	 * Returns whether the frame is in fullscreen mode or windowed mode.
	 * 
	 * @return true if in fullscreen mode, false if in windowed mode.
	 */
	public boolean isFullscreen() {
		return this.fullscreen;
	}
	
	/**
	 * Returns the main window.
	 * 
	 * @return the main window (JFrame).
	 */
	public JFrame getFrame() {
		return Screen.frame;
	}
	
	/**
	 * Returns the Screen instance. If there is no screen instance, create one.
	 * 
	 * @return the instance of Screen.
	 */
	public static synchronized Screen getInstance() {
		if(Screen.instance == null)
			Screen.instance = new Screen();
		return Screen.instance;
	}

	/**
	 * The entry point of the program.
	 * 
	 * @param args The arguments that were set when the program was executed.
	 */
	public static void main(String[] args) {
		// Make the Java UI look the same across all platforms.
		try {UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());} catch (Exception e) {e.printStackTrace();}
		
		// Set up the main window.
		frame = new JFrame(APP_NAME);
		frame.setIconImage(ICON.getImage());
		// Ask the user if they want to save before closing the program.
	    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	    frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if(FileDialogs.askToSaveDialog() != JOptionPane.CLOSED_OPTION)
					System.exit(0);
			}
	    });
	    
	    // Set the minimum/initial size for the main window.
	    frame.setMinimumSize(new Dimension(WIDTH, HEIGHT));
	    
	    /*
	     * Add the Screen (JPanel) instance to the main window.
	     * This should be the one and only time a new Screen instance is created.
	     */
	    frame.getContentPane().add(Screen.getInstance());
	    // Add the menu bar to the main window.
	    frame.setJMenuBar(new MenuBar());
	    
	    // Resize the main window to fit the preferred size of the Screen (JPanel).
	    frame.pack();
	    // Position the main window to the center of the display.
	    frame.setLocationRelativeTo(null);
	    // Show the main window.
	    frame.setVisible(true);
	}
	
	/**
	 * This inner class is used to send mouse events 
	 * to the top-level custom UI component(s).
	 * 
	 * Note: Mouse events also cause the custom UI components to be re-drawn.
	 */
	private class MouseHandler extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			for(Component c : components)
        		c.mouseClicked(e);
			repaint();
		}
	
		public void mouseDragged(MouseEvent e) {
			for(Component c : components)
        		c.mouseDragged(e);
			repaint();
		}

		public void mousePressed(MouseEvent e) {
        	for(Component c : components)
        		c.mousePressed(e);
        	repaint();
        }

        public void mouseReleased(MouseEvent e) {
        	for(Component c : components)
        		c.mouseReleased(e);
        	repaint();
        }

		public void mouseWheelMoved(MouseWheelEvent e) {
			for(Component c : components) {
				c.mouseScroll(e);
			}
			repaint();
		}
	}
}
