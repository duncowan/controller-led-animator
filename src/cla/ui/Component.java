package cla.ui;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

/**
 * This is the base class for all custom UI components.
 * 
 * @author Duncan Cowan
 *
 */
public abstract class Component {
	// Store size and position of component.
	protected double x, y, height, width;
	// Store id of component
	// TODO Investigate if this is really necessary.
	protected int id;
	// Component visibility.
	private boolean visible = true;
	// This components parent component.
	// (i.e. the component that this component is in)
	private Component parent;
	// List of components in this component.
	private ArrayList<Component> components = new ArrayList<Component>();
	
	/**
	 * Constructor.
	 * 
	 * @param id the id of this component.
	 * @param parent the component that this component is in.
	 * @param x the x position of this component relative to its parent.
	 * @param y the y position of this component relative to its parent.
	 * @param height the height of this component.
	 * @param width the width of this component.
	 */
	public Component(int id, Component parent, int x, int y, int height, int width) {
		this.id = id;
		this.parent = parent;
		this.x = x;
		this.y = y;
		this.height = height;
		this.width = width;
	}
	
	/**
	 * Draws all components in this component (if they are visible).
	 */
	public void draw(Graphics g) {
		for(Component c : components) {
			if(c.visible)
				c.draw(g);
		}
	}
	
	/**
	 * Adds the specified component to this component.
	 * 
	 * @param c the component to add to this component.
	 */
	public void add(Component c) {
		this.components.add(c);
	}
	
	/**
	 * Returns this component's parent.
	 * 
	 * @return this component's parent.
	 */
	protected Component getParent() {
		return this.parent;
	}
	
	/**
	 * Returns the list of components in this component.
	 * 
	 * @return the list of components in this component.
	 */
	public ArrayList<Component> getComponents() {
		return components;
	}
	
	/**
	 * Updates the size and position of this component based on xMod and yMod.
	 * 
	 * This is called whenever the main window is resized.
	 * 
	 * TODO Completely rework how components are resized and repositioned.
	 * 
	 * @param xMod the ratio between the main window's old width and new width.
	 * @param yMod the ratio between the main window's old height and new height.
	 */
	public void updateSizeAndPosition(double xMod, double yMod) {
		this.y *= yMod;
		this.height *= yMod;
		this.x *= xMod;
		this.width *= xMod;
		for(Component c : this.components) {
			c.updateSizeAndPosition(xMod, yMod);
		}
	}
	
	/**
	 * Checks if this component contains the point at x and y.
	 * 
	 * @param x point's x position.
	 * @param y point's y position.
	 * @return true if this component contains point, false otherwise.
	 */
	public boolean contains(int x, int y) {
		return (x >= this.x && x <= this.x+this.width && y >= this.y && y <= this.y+this.height);
	}
	
	/**
	 * Called if a button in this component was clicked.
	 * 
	 * TODO Investigate if this is too specific to be in this class.
	 * 
	 * @param b the clicked button.
	 */
	protected void buttonClicked(Button b) {}
	
	/**
	 * Called if a mouse button was clicked over this component.
	 * 
	 * @param e mouse event information.
	 */
	protected void clicked(MouseEvent e) {}
	
	/**
	 * Called if a mouse button was pressed over this component.
	 * 
	 * @param e mouse event information.
	 */
	protected void pressed(MouseEvent e) {}
	
	/**
	 * Called if a mouse button was relaesed over this component.
	 * 
	 * @param e mouse event information.
	 */
	protected void released(MouseEvent e) {}

	/**
	 * Called if the mouse is dragged over this component.
	 * 
	 * @param e mouse event information.
	 */
	protected void dragged(MouseEvent e) {}
	
	/**
	 * Called if the mouse wheel is scrolled over this component.
	 * 
	 * @param e mouse event information.
	 */
	protected void scroll(MouseWheelEvent e) {}
	
	/**
	 * Called whenever the user clicks a mouse button.
	 * 
	 * Triggers when the mouse button goes from being pressed to being released.
	 * 
	 * @param e mouse event information.
	 */
	public void mouseClicked(MouseEvent e) {
		for(Component c : components)
			c.mouseClicked(e);
		if(this.contains(e.getX(), e.getY()) && this.visible)
			this.clicked(e);
	}
	
	/**
	 * Called whenever the user presses a mouse button.
	 * 
	 * Triggers when the mouse button goes from being not pressed to being pressed.
	 * 
	 * @param e mouse event information.
	 */
	public void mousePressed(MouseEvent e) {
		for(Component c : components) {
			c.mousePressed(e);
			if(c.contains(e.getX(), e.getY()) && c instanceof Button && c.visible)
				this.buttonClicked((Button)c);
		}
		if(this.contains(e.getX(), e.getY()) && this.visible)
			this.pressed(e);
	}
	
	/**
	 * Called whenever the user releases a mouse button.
	 * 
	 * Triggers when the mouse button goes from being pressed to being not pressed.
	 * 
	 * @param e mouse event information.
	 */
	public void mouseReleased(MouseEvent e) {
		if(this.visible)
			this.released(e);
		for(Component c : components)
			c.mouseReleased(e);
	}
	
	/**
	 * Called whenever the user drags the mouse.
	 * 
	 * Triggers when the mouse is moved.
	 * 
	 * @param e mouse event information.
	 */
	public void mouseDragged(MouseEvent e) {
		if(this.contains(e.getX(), e.getY()) && this.visible)
			this.dragged(e);
		for(Component c : components)
			c.mouseDragged(e);
	}
	
	/**
	 * Called whenever the user scrolls the mouse wheel.
	 * 
	 * Triggers when the mouse wheel is moved.
	 * 
	 * @param e mouse event information.
	 */
	public void mouseScroll(MouseWheelEvent e) {
		if(this.contains(e.getX(), e.getY()) && this.visible)
			this.scroll(e);
		for(Component c : components)
			c.mouseScroll(e);
	}

	/* Getters and setters */
	public int getId() {
		return this.id;
	}
	
	public int getX() {
		return (int)Math.round(x);
	}

	public void setX(double x) {
		this.x = x;
	}

	public int getY() {
		return (int)Math.round(y);
	}

	public void setY(double y) {
		this.y = y;
	}

	public int getHeight() {
		return (int)Math.round(height);
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public int getWidth() {
		return (int)Math.round(width);
	}

	public void setWidth(double width) {
		this.width = width;
	}
	
	public boolean isVisible() {
		return this.visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
}
