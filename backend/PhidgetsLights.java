import com.phidgets.*;
import com.phidgets.event.*;

public class PhidgetsLights implements LightController {
	InterfaceKitPhidget ik;
	public void initialize() {
		try {
			
			ik = new InterfaceKitPhidget();

			ik.addAttachListener(new AttachListener() {

				public void attached(AttachEvent ae) {

					System.out.println("attachment of " + ae);

				}

			});
			ik.openAny();
			System.out.println("waiting for InterfaceKit attachment...");
			ik.waitForAttachment();
			System.out.println(ik.getDeviceName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Phidgets lights initialized.");
	}

	public void doEvent(LightEvent what) {

		int channel = what.getNumber() % 4;
		try {
			if(what.getOn() && !ik.getOutputState(channel))
				ik.setOutputState(channel, what.getOn());
			else if(!what.getOn() && ik.getOutputState(channel))
				ik.setOutputState(channel, what.getOn());
		} catch (PhidgetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void close() {
		try{		
			ik.setOutputState(0, false);
			ik.setOutputState(1, false);
			ik.setOutputState(2,false);
			ik.setOutputState(3, false);
			ik.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		MidiPlayback.stillRunning = false;
		System.out.println("Phidgets lights closed.");
	}
}
