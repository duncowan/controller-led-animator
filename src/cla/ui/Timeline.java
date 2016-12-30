package cla.ui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.Collections;
import cla.Screen;
import cla.util.FileManager;
import cla.util.Section;

/**
 * This class defines the timeline custom UI panel.
 * 
 * It contains the timeline which is used to control when and how the
 * controller LEDs are turned off and on.
 * 
 * TODO Make the scrubber and tracks into their own classes (this class is way too big).
 * TODO Finish commenting this class.
 * 
 * @author Duncan Cowan
 *
 */
public class Timeline extends Component {
	// Set the number of timelines (tracks), 
	// how many time divisions they have and the colours of the preview LEDs.
	// TODO Put this information into a config file, should not hardcoded.
	public static final int NUM_OF_TIMELINES = 4;
	public static final int NUM_OF_DIVISIONS = 126;
	protected static final Color[] LED_COLORS = {Color.orange, Color.red, Color.green, Color.blue};
	/* TODO Make this part of the preview panel. */
	protected static Color[] previewLedColors = new Color[NUM_OF_TIMELINES];

	private ArrayList<ArrayList<Section>> timelines = new ArrayList<ArrayList<Section>>();
	private ArrayList<Section> selectedTimeline;
	private Section selectedSection;
	private Thread preview;

	private volatile boolean previewing = false;
	private volatile int curTime = 0;
	private volatile int endTime = 0;

	private int yPan = 0;
	private int yZoom = 0;
	private int xPan = 0;
	private int xZoom = 1;

	public Timeline(int id, Component parent, int x, int y, int height, int width) {
		super(id, parent, x, y, height, width);

		//Set preview leds to 'off' and add blank timelines
		for(int i = 0; i < NUM_OF_TIMELINES; i++) {
			previewLedColors[i] = Color.BLACK;
			timelines.add(new ArrayList<Section>());
		}

		//Add buttons
		this.add(new Button(0, this, "+", x, y+height+2, 12, 12));
		this.add(new Button(1, this, "-", x+14, y+height+2, 12, 12));
		this.add(new Button(2, this, "<", x+26, y+height+2, 12, 12));
		this.add(new Button(3, this, ">", x+38, y+height+2, 12, 12));

		this.add(new Button(4, this, "+", x+width+2, y, 12, 12));
		this.add(new Button(5, this, "-", x+width+2, y+14, 12, 12));
		this.add(new Button(6, this, "^", x+width+2, y+26, 12, 12));
		this.add(new Button(7, this, "v", x+width+2, y+38, 12, 12));

		this.add(new PlayButton(8, this, (Screen.WIDTH/2)-25, y-95, 50, 50));

		//Led animation timer
		preview = new Thread() {
			public void run() {
				long count = System.nanoTime() + 12600000;
				while(true) {
					if(previewing && (System.nanoTime()-count) >= 12500000) {
						count = System.nanoTime();
						if(curTime > endTime*10+1)
							curTime = 0;
						updatePreview(true);
						curTime++;
						Screen.getInstance().repaint();
						try {Thread.sleep(10);} catch (InterruptedException e) {}
					}
					
					if(!previewing)
						//Thread.yield();
						try {Thread.sleep(900000);} catch (InterruptedException e) {}
				}
			}
		};
		preview.start();
	}

	public void updateSizeAndPosition(double xMod, double yMod) {
		this.y *= yMod;
		this.height *= yMod;
		this.width = (Screen.WIDTH-(Screen.MARGIN*2))-40;

		for(Component c : getComponents()) {
			if(c.getId() < 4) {
				c.y = y+height+2;
				c.x = x+(14*c.getId());
			} else if(c.getId() > 3 && c.getId() < 8) {
				c.x = x+width+2;
				c.y = y+(14*(c.getId()-4));
			}

			if(c.getId() == 8) {
				c.x = (Screen.WIDTH/2)-45;
				c.y = y-95;
			}
		}
	}

	protected void buttonClicked(Button b) {
		if(b.getId() == 0) {
			xZoom++;
			if(xZoom > 13)
				xZoom = 15;
		} else if(b.getId() == 1 && xZoom > 1) {
			xZoom--;
			if(xZoom > 13)
				xZoom = 13;
			if(xPan > 8*15-(NUM_OF_DIVISIONS/xZoom)+6) {
				xPan = 8*15-(NUM_OF_DIVISIONS/xZoom)+6;
			}
		} else if(b.getId() == 2) {
			xPan-=8;
			if(xPan < 0) {
				xPan = 0;
			}
		} else if(b.getId() == 3) {
			xPan+=8;
			if(xPan > 8*15-(NUM_OF_DIVISIONS/xZoom)+6) {
				xPan = 8*15-(NUM_OF_DIVISIONS/xZoom)+6;
			}
		} 

		else if(b.getId() == 4) {
			yZoom++;
			if(yZoom > NUM_OF_TIMELINES-1)
				yZoom = NUM_OF_TIMELINES-1;
		} else if(b.getId() == 5) {
			yZoom--;
			if(yZoom < 0)
				yZoom = 0;
			if(yPan == yZoom+1)
				yPan--;
		} else if(b.getId() == 6) {
			yPan++;
			if(yPan > yZoom)
				yPan = yZoom;
		} else if(b.getId() == 7) {
			yPan--;
			if(yPan < 0)
				yPan = 0;
		} else if(b.getId() == 8) {
			if(!previewing) {
				for(ArrayList<Section> tl : timelines) {
					previewLedColors[timelines.indexOf(tl)] = Color.BLACK;
				}
				preview.interrupt();
			}
			previewing = !previewing;
			((PlayButton) b).toggle();
		}
	}

	public void draw(Graphics g) {
		double tlH = (height/(NUM_OF_TIMELINES-yZoom));
		double divW = (width/(NUM_OF_DIVISIONS/xZoom));

		//Draw timelines
		for(int i = 0; i < (NUM_OF_TIMELINES-yZoom); i++) {
			g.setColor(Color.WHITE);
			g.drawString("LED "+((i+yPan)+1), (int)Math.round(x-40), (int)Math.round((y+tlH*i)+(tlH/2)));
			g.setColor(new Color(30, 30, 30));
			g.drawLine((int)Math.round(x), (int)Math.round(y+tlH*i), (int)Math.round(x+width), (int)Math.round(y+tlH*i));
		}

		//Draw divisions
		for(int i = 0; i < NUM_OF_DIVISIONS/xZoom; i++) {
			if((i+xPan) % 8 == 0) {
				g.setColor(Color.white);
				g.drawString(""+((i+xPan)/8), (int)Math.round(x+divW*i), (int)Math.round(y-10));
			}
			g.setColor(new Color(255, 255, 255, ((i+xPan) % 8 == 0) ? 255 : 80));
			g.drawLine((int)Math.round(x+divW*i), (int)Math.round(y), (int)Math.round(x+divW*i), (int)Math.round(y+height));
		}

		//Draw sections
		g.clipRect(getX()+1, getY(), getWidth(), getHeight());
		for(ArrayList<Section> tl : timelines) {
			int sH = (int)Math.round(tlH);
			int sY = (int)Math.round(y+tlH*(timelines.indexOf(tl)-yPan));
			for(Section s : tl) {
				int sX = (int)Math.round(x+divW*(s.getStart()-xPan));
				int sW = (int)Math.round(divW*((s.getEnd()-s.getStart() <= 0) ? 0 : ((s.getEnd()-s.getStart()))));
				int[] xPoints = {sX, sX, sX+sW, sX+sW};
				int[] yPoints = {sY+sH, sY, sY, sY+sH};
				Color colour = LED_COLORS[timelines.indexOf(tl)];

				if(s.getMode() == 1) {
					yPoints[2] = sY+sH;
				} else if(s.getMode() == 2) {
					yPoints[1] = sY+sH;
				}

				if(timelines.indexOf(tl) >= yPan && timelines.indexOf(tl) < (NUM_OF_TIMELINES-yZoom)+yPan) {
					g.setColor(new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), 100));
					g.fillRect(sX, sY, sW, sH);
					g.setColor(colour);
					g.fillPolygon(xPoints, yPoints, 4);
					g.setColor(Color.BLACK);
					g.drawLine(sX, yPoints[1], sX+sW, yPoints[2]);
					//g.drawRect(sX, sY, sW, sH);
				}
			}
		}
		g.setClip(null);

		//Timeline outline
		g.setColor(Color.WHITE);
		g.drawRect(getX(), getY(), getWidth(), getHeight());

		//Draw scrubber
		g.clipRect(getX()+1, getY()-14, getWidth(), getHeight()+14);
		g.setColor(Color.BLACK);
		g.drawLine((int)(x+(divW/10)*(curTime-xPan*10))-1, getY()-14, (int)(x+(divW/10)*(curTime-xPan*10))-1, getY()+getHeight());
		g.drawLine((int)(x+(divW/10)*(curTime-xPan*10))+1, getY()-14, (int)(x+(divW/10)*(curTime-xPan*10))+1, getY()+getHeight());
		g.setColor(Color.RED);
		g.fillRect((int)(x+(divW/10)*(curTime-xPan*10))-5, getY()-14, 12, 12);
		g.drawLine((int)(x+(divW/10)*(curTime-xPan*10)), getY()-14, (int)(x+(divW/10)*(curTime-xPan*10)), getY()+getHeight());
		g.setClip(null);
		
		super.draw(g);
	}

	private synchronized void updatePreview(boolean updateXPan) {
		if(updateXPan) {
			xPan = (curTime == 0) ? 0 : xPan;
			if(curTime/10 >= xPan+(NUM_OF_DIVISIONS/xZoom)) {
				xPan = curTime/10;
				if(xPan > 8*15-(NUM_OF_DIVISIONS/xZoom)+6) {
					xPan = 8*15-(NUM_OF_DIVISIONS/xZoom)+6;
				}
			}
		}
		
		for(ArrayList<Section> tl : timelines) {
			for(int s = 0; s < tl.size(); s++) {
				Color ledColor = LED_COLORS[timelines.indexOf(tl)];
				int startOn = (tl.get(s).isStartOn() ? 1 : 0);
				int fade = (tl.get(s).isFade() ? 1 : 0);
				int startTime = tl.get(s).getStart()*10;
				int endTime = tl.get(s).getEnd()*10;

				if(curTime >= startTime && curTime <= endTime) {
					int brightness = (int)((255.0/(endTime-startTime))*(curTime-startTime));
					previewLedColors[timelines.indexOf(tl)] = new Color(ledColor.getRed(), ledColor.getGreen(), ledColor.getBlue() ,((startOn*255)+fade*(brightness-(brightness*2*startOn))));
				} 
				if(curTime == endTime+1) {
					if(endTime != tl.get((s+1)%(tl.size())).getStart()*10) {
						previewLedColors[timelines.indexOf(tl)] = Color.BLACK;
					}
				}
			}
		}
	}
	
	public ArrayList<ArrayList<Section>> getTimelines() {
		return this.timelines;
	}
	
	public void setTimelines(ArrayList<ArrayList<Section>> tl, int endTime) {
		this.timelines = tl;
		this.endTime = endTime;
		this.curTime = 0;
		this.xZoom = 1;
		if(previewing)
			buttonClicked((Button) getComponents().get(8));
		clearAndUpdatePreview();
		Screen.getInstance().repaint();
	}
	
	public void clearTimelines() {
		ArrayList<ArrayList<Section>> timelines = new ArrayList<ArrayList<Section>>();
		while(timelines.size() < NUM_OF_TIMELINES) timelines.add(new ArrayList<Section>());
		this.setTimelines(timelines, 0);
	}

	//	protected void clicked(MouseEvent e) {
	//		System.out.println(selectedSection.isFade());
	//	}
	
	public void togglePlay() {
		buttonClicked((Button) getComponents().get(8));
	}

	char mode;
	int oldPosition, oldStart, oldEnd;
	public void pressed(MouseEvent e) {
		selectedTimeline = timelines.get(yPan+(int)((e.getY()-y)/(height/(NUM_OF_TIMELINES-yZoom))));
		selectedSection = null;
		mode = 'n';
		double position = xPan+((e.getX()-x)/(width/(NUM_OF_DIVISIONS/xZoom)));

		if(!e.isShiftDown()) {
			for(Section s : selectedTimeline) {
				if(position >= s.getStart() && position <= s.getEnd()) {
					selectedSection = s;
					if(e.getButton() == 1) {
						if(e.isControlDown()) {
							selectedTimeline.remove(selectedSection);
						}

						double thirdOfWidth = (s.getEnd() - s.getStart())/3.0;
						if(position >= s.getStart() && position <= (s.getStart()+thirdOfWidth))
							mode = 'b';
						else if(position <= s.getEnd() && position >= (s.getEnd()-thirdOfWidth))
							mode = 'f';
						else {
							mode = 'm';
							oldPosition = (int)position;
							oldStart = selectedSection.getStart();
							oldEnd = selectedSection.getEnd();
						}
					} else if(e.getButton() == 3) {
						s.changeMode();
						FileManager.isFileSaved(false);
					}
					break;
				}
			}

			if(selectedSection == null) {
				selectedSection = new Section((int)position, (int)position);
				selectedTimeline.add(selectedSection);
				FileManager.isFileSaved(false);
				mode = 'f';
			}
		} else {
			curTime = (xPan*10)+(int)((e.getX()-x)/((width/(NUM_OF_DIVISIONS/xZoom))/10));
			mode = 's';
		}
		clearAndUpdatePreview();
	}

	protected void dragged(MouseEvent e) {
		int position = xPan+(int)((e.getX()-x)/(width/(NUM_OF_DIVISIONS/xZoom)));

		if(mode != 'n') {
			if(mode == 'f' && !collision(selectedSection.getStart(), position)) {
				selectedSection.setEnd(position);
			} else if(mode == 'b' && !collision(position, selectedSection.getEnd())) {
				selectedSection.setStart(position);
			} else if(mode == 'm' && !collision(oldStart+(position-oldPosition), oldEnd+(position-oldPosition))) {
				selectedSection.setStart(oldStart+(position-oldPosition));
				selectedSection.setEnd(oldEnd+(position-oldPosition));
			} else if(mode == 's') {
				curTime = (xPan*10)+(int)((e.getX()-x)/((width/(NUM_OF_DIVISIONS/xZoom))/10));
				if(previewing) {
					buttonClicked((Button) getComponents().get(8));
				}
			}
			
			if(mode != 's')
				FileManager.isFileSaved(false);
			clearAndUpdatePreview();
		}
	}

	private boolean collision(int start, int end) {
		for(Section s : selectedTimeline) {
			if(s != selectedSection) {
				for(int p = start; p <= end; p++) {
					if(p > s.getStart() && p < s.getEnd())
						return true;
				}
			}
		}
		return false;
	}

	protected void released(MouseEvent e) {
		if(selectedSection != null && this.selectedSection.getEnd() - this.selectedSection.getStart() < 1) {
			selectedTimeline.remove(this.selectedSection);
		}
		
		//Get new end time and sort
		endTime = 0;
		for(ArrayList<Section> tl : timelines) {
			Collections.sort(tl);
			if(!tl.isEmpty() && tl.get(tl.size()-1).getEnd() > endTime)
				endTime = tl.get(tl.size()-1).getEnd();
		}
		mode = 'n';
	}

	protected void scroll(MouseWheelEvent e) {
		int direction = e.getWheelRotation();
		if(direction > 0) {
			if(e.isShiftDown()) {
				if(e.isControlDown()) {
					buttonClicked((Button) getComponents().get(1));
				} else {
					buttonClicked((Button) getComponents().get(2));
				}
			} else {
				if(e.isControlDown()) {
					buttonClicked((Button) getComponents().get(5));
				} else {
					buttonClicked((Button) getComponents().get(6));
				}
			}
		} else if(direction < 0) {
			if(e.isShiftDown()) {
				if(e.isControlDown()) {
					buttonClicked((Button) getComponents().get(0));
				} else {
					buttonClicked((Button) getComponents().get(3));
				}
			} else {
				if(e.isControlDown()) {
					buttonClicked((Button) getComponents().get(4));
				} else {
					buttonClicked((Button) getComponents().get(7));
				}
			}
		}
	}
	
	private void clearAndUpdatePreview() {
		for(int i = 0; i < NUM_OF_TIMELINES; i++) {
			previewLedColors[i] = Color.BLACK;
		}
		updatePreview(false);
	}
}