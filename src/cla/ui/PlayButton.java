package cla.ui;

import java.awt.Color;
import java.awt.Graphics;

/**
 * This class defines the play button custom UI component.
 * 
 * @author Duncan Cowan
 *
 */
public class PlayButton extends Button {
	// State of play button.
	private boolean clicked = false;
	
	/**
	 * Constructor.
	 * 
	 * Calls the super method with the text argument set to a empty string.
	 */
	public PlayButton(int id, Component parent, int x, int y, int height, int width) {
		super(id, parent, "", x, y, height, width);
	}
	
	/*
	 * Draws the play button.
	 */
	public void draw(Graphics g) {
		int[] xPoints = {getX(), getX(), getX()+getWidth(), getX()+getWidth()};
		int[] yPoints = {getY()+getHeight(), getY(), getY(), getY()+getHeight()};
		// If the play button is not clicked, show the play symbol,
		// otherwise show the stop symbol.
		if(!clicked)
			yPoints[2] = yPoints[3] = getY()+(getHeight()/2);
		g.setColor(Color.WHITE);
		g.drawPolygon(xPoints, yPoints, 4);
	}
	
	/*
	 * Toggle the state of the play button.
	 */
	protected void toggle() {
		clicked = !clicked;
	}
}
