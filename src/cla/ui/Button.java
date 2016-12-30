package cla.ui;

import java.awt.Color;
import java.awt.Graphics;

/**
 * This class defines the button custom UI component.
 * 
 * @author Duncan Cowan
 *
 */
public class Button extends Component {
	// Button text.
	private String text = "";
	
	/**
	 * Constructor.
	 * 
	 *  Calls the super method and sets the button's text.
	 */
	public Button(int id, Component parent, String text, int x, int y, int height, int width) {
		super(id, parent, x, y, height, width);
		this.text = text;
	}

	/**
	 * Draws the button.
	 */
	public void draw(Graphics g) {
		g.setColor(Color.white);
		drawCenteredString(g, this.text, this.getX()+this.getWidth()/2, this.getY()+this.getHeight()/2);
	}
	
	/**
	 * Draws a centered string onto the specified graphics object
	 * at the specified location.
	 * 
	 * @param g the graphics object to draw onto.
	 * @param s the string to center.
	 * @param x the x position.
	 * @param y the y position.
	 */
	private void drawCenteredString(Graphics g, String s, int x, int y) {
		g.drawString(s, x - (g.getFontMetrics().stringWidth(s)/2), y + (g.getFontMetrics().getHeight()/4)+1);
	}
	
	/**
	 * Sets the buttons text to the specified string.
	 * 
	 * @param text the string to set the button's text to.
	 */
	protected void setText(String text) {
		this.text = text;
	}
}
