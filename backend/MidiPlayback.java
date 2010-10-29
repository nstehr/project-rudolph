//Slightly modified version of:  
//Midi show control program in Java
// By Eric
// http://www.awesomelicious.com/

import java.io.*;
import java.util.*;
import javax.sound.midi.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.net.*;


public class MidiPlayback {
	protected static Sequencer sequencer;
	protected static Synthesizer synthesizer;
	public static boolean stillRunning;
	private LightController controller;

	public MidiPlayback(LightController controller){
	this.controller = controller;
	controller.initialize();
	}

	public void sequenceTrack(String filename) throws InvalidMidiDataException,
			IOException, MidiUnavailableException {
		
		stillRunning = true;
		Sequence sequence = MidiSystem.getSequence(new File(filename)); // Load
		// up
		// song
		sequencer = MidiSystem.getSequencer();

		sequencer.addMetaEventListener(new MetaEventListener() // Java midi quit
				// bug
				{
					public void meta(MetaMessage event) {
						if (event.getType() == 47) {
							sequencer.close();
							synthesizer.close();

							System.out.println("Finished.");
						    	stillRunning=false;
							controller.close();
							
						}
					}
				});
		sequencer.open();
		sequencer.setSequence(sequence);

		synthesizer = MidiSystem.getSynthesizer();
		synthesizer.open();
		sequencer.getTransmitter().setReceiver(
				new NoteEventReceiver(13, controller, 200)); // Set the midi
		// reciever to a
		// custom object
		// (NoteEventReciever)
		sequencer.start(); // Start playing
	}
}

// This class is a custom midi receiver to trap note events
class NoteEventReceiver implements Receiver {
	final static int offset = 60; // Offset from zero- 60 makes middle C = 0
	protected int chan; // Channel with light events
	protected int lag;

	protected LinkedBlockingQueue<LightEvent> queue; // Queue for light events
	// (Blocking queue is thread-safe)
	protected LightController controller; // The LightController to send the
	// events to

	protected Thread doEventsThread; // Thread for actually sending the events

	public NoteEventReceiver(int chan, LightController controller, int lag) // Constructor
	{
		// Initialize
		this.chan = chan;
		this.controller = controller;
		this.lag = lag;
		queue = new LinkedBlockingQueue<LightEvent>();

		// Start the queue monitor that sends the events
		doEventsThread = new MonitorQueueThread(this);
		doEventsThread.start();
	}

	// Called by the sequencer every time a note occurs
	public void send(MidiMessage message, long timeStamp) {
		if (message instanceof ShortMessage) {
			ShortMessage sm = (ShortMessage) message;
			// System.out.print(sm.getChannel());
			// System.out.print("  ");
			// System.out.print(chan);
			// System.out.print("  ");
			if (sm.getCommand() == ShortMessage.NOTE_ON) {
				// System.out.print("NOTE ON");
				new LagToQueue(this, lag, new LightEvent(sm.getChannel(), true))
						.start();
			}

			if (sm.getCommand() == ShortMessage.NOTE_OFF) {
				new LagToQueue(this, lag,
						new LightEvent(sm.getChannel(), false)).start();
			}

			// System.out.println("-------");
		}

	}

	public void close() // Deconstruct
	{
		doEventsThread.interrupt(); // Halt the thread
		while (doEventsThread.isAlive())
			; // Wait for haltation
	}

	// Getters
	public LinkedBlockingQueue getQueue() {
		return queue;
	}

	public LightController getController() {
		return controller;
	}
}

// Thread for monitoring the event queue and sending the events
// It is necessary to do this in a separate thread so the time taken to send an
// event doesn't interfere with the music being played
class MonitorQueueThread extends Thread {
	protected NoteEventReceiver caller; // Calling object, to get parameters
	// from
	protected LightController controller; // Controller to send events to

	public MonitorQueueThread(NoteEventReceiver caller) {
		super();
		this.caller = caller;
		this.controller = caller.getController();
	}

	public void run() {
		LinkedBlockingQueue queue;
		queue = caller.getQueue(); // Get queue
		for (;;) {
			try {
				controller.doEvent((LightEvent) queue.take()); // Wait for the
				// event and
				// send it
			} catch (InterruptedException e) {
				return;
			}
		}
	}
}


// This prints LightEvents it gets sent to the terminal- it doesn't actually
// control lights


// This class delays a bit before adding an event to the queue enabling exact
// synchronization between lights and music
class LagToQueue extends Thread {
	NoteEventReceiver caller;
	int lagTime;
	LightEvent add;

	public LagToQueue(NoteEventReceiver caller, int lagTime, LightEvent add) {
		super();
		this.caller = caller;
		this.lagTime = lagTime;
		this.add = add;
	}

	public void run() {
		try {
			Thread.sleep(lagTime); // lag
		} catch (InterruptedException e) {
		}
		caller.getQueue().offer(add); // add it
	}
}
