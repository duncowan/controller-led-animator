package cla.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import cla.Screen;

/**
 * This class defines the preview custom UI panel.
 * 
 * It contains an image of a controller and
 * four circles that represent the programmable LEDs.
 * 
 * TODO Move the play button from the timeline panel to this panel.
 * 
 * @author Duncan Cowan
 *
 */
public class Preview extends Component {
	// Stores the original, raw image of the controller.
	private BufferedImage originalController;
	// Stores the resized image of the controller.
	private Image resizedController;
	// Stores the height and width of the resized image.
	private double imgW, imgH;
	private boolean showLedNumbers = false;

	/**
	 * Constructor.
	 * 
	 * Calls the super method, loads the controller image and resizes it so
	 * it fits in the preview panel.
	 */
	public Preview(int id, Component parent, int x, int y, int height, int width) {
		super(id, parent, x, y, height, width);

		try {
			originalController = ImageIO.read(Screen.class.getResource("/img/controllerMedNB.png"));
		} catch(IOException e1) {
			e1.printStackTrace();
		}
		imgH = height - 80;
		imgW = originalController.getWidth() * (imgH / (originalController.getHeight() * 1.0));

		resizedController = originalController.getScaledInstance((int) Math.round(imgW), (int) Math.round(imgH), Image.SCALE_SMOOTH);
	}

	/**
	 * Draws the resized controller image and the programmable LEDs.
	 */
	public void draw(Graphics g) {
		// Draw the resized image in the center of the preview panel.
		int imgX = (int) Math.round(x + (width / 2) - (imgW / 2.0)) - 20;
		int imgY = (int) Math.round(y + (height / 2) - (imgH / 2.0)) - 50;
		
		// Set LED's size relative to the height of the resized image.
		int ledSize = (int) Math.round(imgH * 0.1026);
		// Set the top LED's position relative to the height of the resized image.
		// (The other LED positions will be based on the top LED.)
		double topLedX = (imgX) + (imgW * 0.7485);
		double topLedY = (imgY) + (imgH * 0.0874);
		// LED position offsets form the top LED's position.
		double[] ledXOffset = {0, (imgH * 0.1084), 0, -(imgH * 0.1084)};
		double[] lesYOffset = {0, (imgH * 0.1084), (imgH * 0.1084) * 2, (imgH * 0.1084)};

		// Draw LEDs
		for(int i = 0; i < Timeline.NUM_OF_TIMELINES; i++) {
			g.setColor(Timeline.previewLedColors[i]);
			g.fillOval((int) Math.round(topLedX + ledXOffset[i]), (int) Math.round(topLedY + lesYOffset[i]), ledSize, ledSize);
			g.setColor(Color.WHITE);
			// Only enable antialiasing for the white outline of the LEDs.
			// This is done to make the LEDs look like they are a part of the controller image.
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.drawOval((int) Math.round(topLedX + ledXOffset[i]), (int) Math.round(topLedY + lesYOffset[i]), ledSize, ledSize);
			// Disable antialiasing after drawing the white outline of the LEDs to maximize performance.
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			if(this.showLedNumbers) 
				g.drawString("" + (i + 1), (int) Math.round(topLedX + ledXOffset[i]) + (ledSize / 2) - (g.getFontMetrics().stringWidth("" + (i + 1)) / 2), (int) Math.round(topLedY + lesYOffset[i]) - (g.getFontMetrics().getHeight() / 3));
		}
		
		// Draw the resized controller image.
		g.drawImage(resizedController, imgX, imgY, null);
	}

	/**
	 * Called when the main window is resized.
	 * 
	 * Resizes the controller image.
	 */
	public void updateSizeAndPosition(double xMod, double yMod) {
		super.updateSizeAndPosition(xMod, yMod);
		imgW *= yMod;
		imgH *= yMod;
		resizedController = originalController.getScaledInstance((int) Math.round(imgW), (int) Math.round(imgH), Image.SCALE_SMOOTH);
	}

	/**
	 * Toggles the visible state of the LED numbers (default is not visible).
	 */
	public void toggleLedNumbers() {
		this.showLedNumbers = !this.showLedNumbers;
		Screen.getInstance().repaint();
	}

	/**
	 * Gets the visible state of the LED numbers.
	 * 
	 * @return true if the LED numbers are visible, false otherwise.
	 */
	public boolean isLedNumbersShowen() {
		return this.showLedNumbers;
	}
}
