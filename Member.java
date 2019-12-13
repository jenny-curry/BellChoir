package bells;

import javax.sound.sampled.SourceDataLine;

public class Member implements Runnable 
{
	Thread[] bells;
	BellNote current;
	boolean occupied;
	
	Conductor.Note[] notes = new Conductor.Note[2];
	int memberNum; //must be 1 or greater
	SourceDataLine line;
	public static final Object Lock = new Object();
	Member(int memberNum, Conductor.Note note0, Conductor.Note note1)
	{
		this.memberNum = memberNum;
		occupied = false;
		notes[0] = note0;
		notes[1] = note1;
		bells = new Thread[2];
		bells[0] = new Thread(this);
		bells[1] = new Thread(this);
		bells[0].start();
		bells[1].start();
	}
	
	public void run()
	{
		while(true)
		{
			synchronized (Lock)
			{
				while(!occupied)
				{
					try {
						Lock.wait();
						Lock.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			
				playNote(line,current);
				occupied = false;
			 	Lock.notifyAll();
	        } 
			
		}
	}
	
	public void play(SourceDataLine line, int noteNum, Conductor.NoteLength length)
	{
		this.line = line;
		int threadIndex = noteNum;
		current = new BellNote(notes[noteNum], length);
		System.out.println("Member Number " + memberNum + " is playing a "
		+ notes[noteNum] + " as a " + length);
		
	 	
	        
		
	}
	
	class BellNote 
	{
	    final Conductor.Note note;
	    final Conductor.NoteLength length;
	
	    BellNote(Conductor.Note note, Conductor.NoteLength length) 
	    {
	        this.note = note;
	        this.length = length;
	    }    
	}
	
	private void playNote(SourceDataLine line, BellNote bn) 
	{
        final int ms = Math.min(bn.length.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
        final int actualLength = Note.SAMPLE_RATE * ms / 1000;
    	line.write(bn.note.sample(), 0, actualLength);
    	line.write(Note.REST.sample(), 0, 50);
        
    }
	
	public void occupy()
	{
		synchronized (Lock){
            Lock.notifyAll();
            occupied = true;
        }
	}
	
	public void free()
	{
		synchronized (Lock){
            Lock.notifyAll();
            occupied = false;
        }
	}
	
	public boolean getOccupied()
	{
		return occupied;
	}

}
