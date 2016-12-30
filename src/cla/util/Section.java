package cla.util;

import cla.ui.Timeline;

/**
 * This class defines when to turn an LED on, when to turn it off 
 * and whether it fades on, fades off or neither.
 * 
 * TODO Think of a better name for this class.
 * 
 * @author Duncan Cowan
 *
 */
public class Section implements Comparable<Section> {
	// When to turn the LED on and when to turn it off.
	private int start, end;
	// Should the LED fade on (fade: true, startOn: false),
	// fade off (fade: true, startOn: true), or
	// not fade at all (fade: false, startOn true).
	private boolean fade, startOn;
	// Represents fade and startOn as an integer.
	// 0 - don't fade
	// 1 - fade off
	// 2 - fade on
	private int mode = 0;
	
	/**
	 * Constructor.
	 * 
	 * Sets if the LED should start on, if it should fade and
	 * when it turns on and when it turns off.
	 * 
	 * Also sets the mode based on the startOn and fade values.
	 * 
	 * @param startOn should the LED start on.
	 * @param fade should the LED fade from one state to the other.
	 * @param start when to turn the LED on.
	 * @param end when to turn the LED off.
	 */
	public Section(boolean startOn, boolean fade, int start, int end) {
		this.start = start;
		this.end = end;
		this.fade = fade;
		this.startOn = startOn;
		
		// Set mode based on startOn and fade.
		if(startOn && fade) {
			mode = 1;
		} else if(startOn) {
			mode = 0;
		} else {
			mode = 2;
		}
	}
	
	/**
	 * Constructor.
	 * 
	 * Creates a section that doesn't fade on or off.
	 * 
	 * @param start when to turn the LED on.
	 * @param end when to turn the LED off.
	 */
	public Section(int start, int end) {
		this(true, false, start, end);
	}
	
	/**
	 * Cycles through the different modes.
	 */
	public void changeMode() {
		mode = (mode < 2) ? mode+1 : 0;
		this.fade = (mode > 0);
		this.startOn = (mode < 2);
	}
	
	/* Getters and setters. */
	public boolean isFade() {
		return fade;
	}

	public boolean isStartOn() {
		return startOn;
	}

	public int getMode() {
		return this.mode;
	}

	public int getStart() {
		return this.start;
	}
	
	public void setStart(int start) {
		this.start = (start < 0) ? 0 : start;
	}
	
	public int getEnd() {
		return this.end;
	}
	
	public void setEnd(int end) {
		this.end = (end > Timeline.NUM_OF_DIVISIONS) ? Timeline.NUM_OF_DIVISIONS : end;
	}
	
	/**
	 * Used to sort sections based on their start time.
	 */
	@Override
	public int compareTo(Section s) {
		return this.start - ((Section) s).getStart();
	}
}
