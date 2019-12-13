package bells;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Conductor 
{
    private final AudioFormat af;
    Member[] members;
    Scanner readLine;
    Scanner readNote;
    int numMembers = 7;
    int SeqNum;
    int[][] Member_Bell_Length = new int[50][3];
    ArrayList<Note> Notes = new ArrayList<Note>(
    		Arrays.asList(Note.REST,
		    	    Note.A4,
		    	    Note.A4S,
		    	    Note.B4,
		    	    Note.C4,
		    	    Note.C4S,
		    	    Note.D4,
		    	    Note.D4S,
		    	    Note.E4,
		    	    Note.F4,
		    	    Note.F4S,
		    	    Note.G4,
		    	    Note.G4S,
		    	    Note.A5));
    ArrayList<NoteLength> NoteLengths = new ArrayList<NoteLength>(
    		Arrays.asList(NoteLength.WHOLE,
    				NoteLength.HALF,
    				NoteLength.QUARTER,
    				NoteLength.WHOLE));
    
    

	Conductor(AudioFormat af) throws FileNotFoundException, LineUnavailableException
	{
		this.af = af;
		readSong("MaryHadALittleLamb");
		members = new Member[numMembers];
		
		for (int currentNum = 0; currentNum < numMembers; currentNum++)
		{
			System.out.println("Creating Member:" + currentNum + " with notes " 
					+ Notes.get(currentNum*2) + " and " + Notes.get(currentNum*2+1)); 
			Member member = new Member(currentNum,Notes.get(currentNum*2), Notes.get(currentNum*2+1));
			members[currentNum] = member;
		}
		System.out.println("\n");
		
	}

	public static void main(String[] args) throws FileNotFoundException, LineUnavailableException 
	{
		
		final AudioFormat af =
	            new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
	        Conductor c = new Conductor(af);
			c.playSong();
	}
	
	void playSong() throws LineUnavailableException {
        try (final SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
            line.open();
            line.start();
            boolean free = true;
            //Make members play
            for(int seq = 0; seq <= SeqNum; seq++)
    		{
            	members[Member_Bell_Length[seq][0]].play(line,Member_Bell_Length[seq][1], NoteLengths.get(Member_Bell_Length[seq][2]));
            	members[Member_Bell_Length[seq][0]].occupy();
    			while(free)
            	{
            		free = !members[Member_Bell_Length[seq][0]].getOccupied();
            	}
    		}
            
            line.drain();
        }
    }

	
	enum Note {
	    // REST Must be the first 'Note'
	    REST,
	    A4,
	    A4S,
	    B4,
	    C4,
	    C4S,
	    D4,
	    D4S,
	    E4,
	    F4,
	    F4S,
	    G4,
	    G4S,
	    A5;

	    public static final int SAMPLE_RATE = 48 * 1024; // ~48KHz
	    public static final int MEASURE_LENGTH_SEC = 1;

	    // Circumference of a circle divided by # of samples
	    private static final double step_alpha = (2.0 * Math.PI) / SAMPLE_RATE;

	    private final double FREQUENCY_A_HZ = 440.0;
	    private final double MAX_VOLUME = 127.0;

	    private final byte[] sinSample = new byte[MEASURE_LENGTH_SEC * SAMPLE_RATE];

	    private Note() {
	        int n = this.ordinal();
	        if (n > 0) {
	            // Calculate the frequency!
	            final double halfStepUpFromA = n - 1;
	            final double exp = halfStepUpFromA / 12.0;
	            final double freq = FREQUENCY_A_HZ * Math.pow(2.0, exp);

	            // Create sinusoidal data sample for the desired frequency
	            final double sinStep = freq * step_alpha;
	            for (int i = 0; i < sinSample.length; i++) {
	                sinSample[i] = (byte)(Math.sin(i * sinStep) * MAX_VOLUME);
	            }
	        }
	    }
	    public byte[] sample() {
	        return sinSample;
	    }
	}
	
	enum NoteLength {
	    WHOLE(1.0f),
	    HALF(0.5f),
	    QUARTER(0.25f),
	    EIGHTH(0.125f);

	    private final int timeMs;

	    private NoteLength(float length) {
	        timeMs = (int)(length * Note.MEASURE_LENGTH_SEC * 1000);
	    }

	    public int timeMs() {
	        return timeMs;
	    }
	}
	public void readSong(String fileName) throws FileNotFoundException
	{
		SeqNum = 0;
		readLine = new Scanner (new File(fileName));
		readLine.useDelimiter(";");
		Note note;
		String songName = readLine.next();
		System.out.println("Reading in the song " + songName + "\n");
		readLine.nextLine();
		while (readLine.hasNext())
		{
			
			String noteString = readLine.next();
	        switch (noteString) 
	        {
	            case "Rest":  note = Note.REST;
	                     break;
	            case "A4":  note = Note.A4;
	                     break;
	            case "A4S":  note = Note.A4S;
	                     break;
	            case "B4":  note = Note.B4;
	                     break;
	            case "C4":  note = Note.C4;
	                     break;
	            case "C4S":  note = Note.C4S;
	                     break;
	            case "D4":  note = Note.D4;
	                     break;
	            case "D4S":  note = Note.D4S;
	                     break;
	            case "E4":  note = Note.E4;
	                     break;
	            case "F4": note = Note.F4;
	                     break;
	            case "F4S": note = Note.F4S;
	                     break;
	            case "G4": note = Note.G4;
	                     break;
	            case "G4S": note = Note.G4S;
                		break;
	            case "A5": note = Note.A5;
                		break;
	            default: note = Note.REST;
	                     break;
                 }
	               
                Member_Bell_Length[SeqNum][0] = Notes.indexOf(note)/2;
     	        Member_Bell_Length[SeqNum][1] = Notes.indexOf(note)%2;
     	        
     			String lengthString = readLine.next();
     			int lengthNum;
     			
     			switch (lengthString) {
                 case "WHOLE":  lengthNum = 0;
                          break;
                 case "HALF":  lengthNum = 1;
                          break;
                 case "QUARTER":  lengthNum = 2;
                          break;
                 case "EIGTH":  lengthNum = 3;
                          break;
                 default: lengthNum = 0;
                 		 break;
     			}
     		    Member_Bell_Length[SeqNum][2] = lengthNum;
                 		 
     			System.out.println("Reading in Bellnote Number" + SeqNum + " is " + note + " which has an index of " +
     					Notes.indexOf(note) + " and so player number " + Member_Bell_Length[SeqNum][0] + 
     					" will play it from hand number " + Member_Bell_Length[SeqNum][1] + " for a " + 
     					lengthString + " note."); 
     			SeqNum++;
     			readLine.nextLine();
	        }
		System.out.println("\n");
		readLine.close();
	}
}
