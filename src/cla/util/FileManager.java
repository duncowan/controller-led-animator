package cla.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import cla.Screen;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

/**
 * This class handles the opening, saving, and uploading of animations
 * as well as keeping track of the currently open animation and it's saved status.
 * 
 * @author Duncan Cowan
 *
 */
public abstract class FileManager {
	// The currently open file.
	private static File openFile;
	// The saved status of the current animation.
	private static boolean fileSaved = true;

	/**
	 * Loads the specified file.
	 * 
	 * @param f the file to load.
	 */
	public static void load(File f) {
		byte[] fileBytes = new byte[(int)f.length()];
		try {
			// Get the bytes from the file.
			FileInputStream fis = new FileInputStream(f);
			fis.read(fileBytes);
			// Close the file.
			fis.close();

			// Convert the bytes from the file to sections and
			// put those sections into the timeline.
			bytesToSections(fileBytes);
			// Set the open file.
			openFile = f;
			// Set file's saved status to true.
			fileSaved = true;
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Uploads the current animation to the specified controller.
	 * 
	 * @param sp the controller to upload the current animation to.
	 * @return true if the upload was successful, otherwise false.
	 */
	public static boolean upload(String sp) {
		try {
			// Open the serial port the controller is connected to.
			SerialPort serialPort = new SerialPort(sp);
			serialPort.openPort();
			// Give it time to open.
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// Set the serial port parameters and clear the input buffer.
			serialPort.setParams(9600, 8, 1, 0);
			serialPort.readBytes(serialPort.getInputBufferBytesCount());
			
			// Convert the sections that make up the animation to bytes.
			byte[] bytesToSend = sectionsToBytes();
			// Tell the controller to get ready for a new animation.
			serialPort.writeByte((byte)'R');
			// Send the length animation byte array.
			serialPort.writeByte((byte)bytesToSend.length);
			// Send the animation byte array.
			serialPort.writeBytes(bytesToSend);
			
			// Once the controller has all the animation bytes, 
			// it sends them back so we can check their validity.
			byte[] bytesToReceive = null;
			try {
				// Read the animation bytes sent back from the controller 
				// with a timeout of 10 seconds.
				bytesToReceive = serialPort.readBytes(bytesToSend.length, 10000);
			} catch (SerialPortTimeoutException e) {
				// If we don't get the full animation byte array within 10 seconds,
				// close the port and return false.
				System.out.println("TIMEOUT");
				serialPort.closePort();
				return false;
			}
			
			/* Debugging */
//			for(int i = 0; i < bytesToReceive.length; i++)
//				System.out.print(Integer.toHexString(Byte.toUnsignedInt(bytesToSend[i]))+":");
//			System.out.println();
//			for(int i = 0; i < bytesToReceive.length; i++)
//				System.out.print(Integer.toHexString(Byte.toUnsignedInt(bytesToReceive[i]))+":");
			
			// Check validity of bytes received by comparing them to the ones we sent.
			for(int i = 0; i < bytesToReceive.length; i++) {
				if(bytesToSend[i] != bytesToReceive[i]) {
					// If any byte is out of place, 
					// tell the controller not to use the new animation,
					// close the port and return false.
					serialPort.writeByte((byte)'0');
					serialPort.closePort();
					return false;
				}
			}
			
			// If the received bytes are the same as the sent bytes,
			// tell the controller to use the new animation,
			// close the port and return true.
			serialPort.writeByte((byte)'1');
			serialPort.closePort();
			return true;
		} catch (SerialPortException e) {
			// If anything goes wrong with the serial port
			// at any point during the upload process, return false.
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Saves the current animation to the specified file.
	 * 
	 * @param f the file to save the animation to.
	 */
	public static void save(File f) {
		try {
			if(!f.exists())
				// Create a new file if the specified file doesn't exist.
				f.createNewFile();

			// Get the specified file's output stream.
			FileOutputStream fos = new FileOutputStream(f.getAbsolutePath());
			// Convert the current animation to bytes
			// and write them to the file's output stream.
			fos.write(sectionsToBytes());
			// close the file.
			fos.close();
			// Set the open file.
			openFile = f;
			// Set file's saved status to true.
			fileSaved = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Save the currently open file.
	 */
	public static void saveOpenFile() {
		save(openFile);
	}

	/**
	 * Converts a byte array to sections, the adds those sections to the timeline.
	 * 
	 * @param bFile the byte array to convert to sections and add to the timeline.
	 */
	private static void bytesToSections(byte[] bFile) {
		ArrayList<ArrayList<Section>> timelines = new ArrayList<ArrayList<Section>>();
		// The first byte in the array stores the number of timelines.
		int numOfTimelines = (int)(bFile[0]);
		// Set the initial offset (beginning of section data) to the number of timelines + 2.
		// Skip number of timelines (byte[1])
		// Skip number of sections in each timeline (byte[number of timelines+1])
		// Skip animation end time (byte[number of timelines+1+1])
		// Beginning of section data (byte[number of timelines+2])
		int offSet = numOfTimelines+2;
		
		for(int tl = 0; tl < numOfTimelines; tl++) {
			// For every timeline, add a new timeline to the timelines array.
			timelines.add(new ArrayList<Section>());
			// Get the number of sections in this timeline.
			// The n+1 byte in the array stores the number of sections in the nth timeline.
			// This is multiplied by 2 because each section is 2 bytes long.
			int numOfSectionsInTimeline = bFile[tl+1]*2;
			for(int s = 0; s < numOfSectionsInTimeline; s+=2) {
				// Merge the MSB (first byte) and LSB (second byte) of the 
				// current section into an int (2 bytes long).
				int sectionBytes = (int)((bFile[s+offSet] << 8) | (bFile[(s+1)+offSet] & 0xFF));
				// Get startOn value (first bit in section).
				boolean startOn = (sectionBytes & 0x01) == 1;
				// Get fade value (second bit in section).
				boolean fade = ((sectionBytes >> 1) & 0x01) == 1;
				// Get start time (third bit to ninth bit, 7 bits long)
				int startTime = ((sectionBytes >> 2) & 0x7F);
				// Get end time (tenth bit to last bit, 7 bits long)
				int endTime = ((sectionBytes >> 9) & 0x7F);
				
				// Create new section object and add it to the current timeline.
				timelines.get(tl).add(new Section(startOn, fade, startTime, endTime));
			}
			// Set the offset to point to the next timeline's section data.
			// (i.e. add the number of sections in this timeline*2 to the current offset.)
			offSet += numOfSectionsInTimeline;
		}
		// Add the timelines to the timeline panel, 
		// and set the animation end time (the numOfTimelines+1 byte).
		Screen.getInstance().timeline.setTimelines(timelines, bFile[numOfTimelines+1]);
	}

	/**
	 * Converts the current animation into a byte array.
	 * 
	 * @return the animation byte array.
	 */
	private static byte[] sectionsToBytes() {
		// Get the current animation's sections.
		ArrayList<ArrayList<Section>> timelines = Screen.getInstance().timeline.getTimelines();
		
		// Set the number of timelines.
		byte numOfTimelines = (byte)(timelines.size() & 0xFF);
		// Store the number of sections in each timeline.
		byte[] numOfSectionsInTimeline = new byte[timelines.size()];
		// The animation end time.
		int endTime = 0;
		// Initilize the total size of the animation data to be the number of timelines
		// (1 byte) plus the number of sections in each timeline (numOfTimelines bytes)
		// plus the animation end time (1 byte).
		int totalSize = 2+numOfTimelines;
		for(ArrayList<Section> tl : timelines) {
			// Set the number of sections in the current timeline.
			numOfSectionsInTimeline[timelines.indexOf(tl)] = (byte)(tl.size() & 0xFF);
			// Increase the total size by the number of sections in this timeline * 2
			// (times 2 because each section is 2 bytes long).
			totalSize += tl.size()*2;
		}

		byte[] sections = new byte[totalSize];
		// Set the initial offset (beginning of section data) to the number of timelines + 2.
		// Skip number of timelines (byte[1])
		// Skip number of sections in each timeline (byte[number of timelines+1])
		// Skip animation end time (byte[number of timelines+1+1])
		// Beginning of section data (byte[number of timelines+2])
		int offSet = 2+numOfTimelines;
		for(int tl = 0; tl < timelines.size(); tl++) {
			for(int s = 0, sb = 0; s < timelines.get(tl).size(); s++, sb+=2) {
				// Get the s section from the tl timeline.
				Section currentSection = timelines.get(tl).get(s);
				// Stores the current section's data as 2 bytes (an int).
				int sectionBytes = 0;
				// Set the startOn bit.
				sectionBytes |= currentSection.isStartOn() ? 1 : 0;
				// Set the fade bit.
				sectionBytes |= (currentSection.isFade() ? 1 : 0) << 1;
				// Set the start time bits (7 bits)
				sectionBytes |= (currentSection.getStart() << 2);
				// Set the end time bits (7 bits).
				sectionBytes |= (currentSection.getEnd() << 9);
				
				// Add the current section's MSB to the byte array.
				sections[sb+offSet] = (byte)(sectionBytes >> 8);
				// Add the current section's LSB to the next slot in the byte array.
				sections[(sb+1)+offSet] = (byte)(sectionBytes & 0xFF);
				
				// If the current section's end time is more than the animation's end time,
				// set the animation's end time to the current section's end time.
				endTime = (currentSection.getEnd() > endTime) ? currentSection.getEnd() : endTime;
			}
			// Set the offset to point to the next timeline's section data.
			// (i.e. add the number of sections in this timeline*2 to the current offset.)
			offSet += numOfSectionsInTimeline[tl]*2;
		}
		
		// Set the first byte in the byte array to the number of timelines.
		sections[0] = numOfTimelines;
		// For every timeline n, 
		// set the n+1 byte in the byte array to the number of sections in timeline n.
		for(int i = 0; i < numOfSectionsInTimeline.length; i++)
			sections[i+1] = numOfSectionsInTimeline[i];
		// Set the number of timelines+1 byte in the byte array to the animation end time.
		sections[numOfTimelines+1] = (byte)(endTime & 0xFF);

		// Return the byte array that represents the current animation.
		return sections;
	}
	
	/**
	 * Used to set the saved state of the current file.
	 * 
	 * @param savedState true to set the file's saved state to 'saved', 
	 *                   false to set the file's saved state to 'not saved'.
	 */
	public static void isFileSaved(boolean savedState) {
		FileManager.fileSaved = savedState;
		
		if(FileManager.openFile != null)
			// If the current animation has been saved before
			// and the file's saved state is 'not saved', 
			// put an asterisk (*) in front of the file's name in the
			// main windoe's title bar.
			// If the saved state is 'saved', then just show the file name.
			Screen.getInstance().getFrame().setTitle(
					(fileSaved ? "" : "*") + FileManager.openFile.getName() + " - " + Screen.APP_NAME);
		else
			// If the current animation has never been saved before,
			// set the animation's filename in the main window's title bar
			// to "Untitled.cla" with an asterisk in front of it.
			Screen.getInstance().getFrame().setTitle("*Untitled.cla - " + Screen.APP_NAME);
	}
	
	/**
	 * Returns the current animation's saved state.
	 * 
	 * @return true if file's saved state is 'saved', 
	 *         false if the file's saved state is 'not saved'.
	 */
	public static boolean isFileSaved() {
		return FileManager.fileSaved;
	}
	
	/**
	 * Returns the currently open file.
	 * 
	 * @return the currently open file.
	 */
	public static File getOpenFile() {
		return FileManager.openFile;
	}
	
	/**
	 * Closes the urrently open file.
	 */
	public static void closeFile() {
		FileManager.openFile = null;
	}
}
