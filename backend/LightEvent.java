// Simple class for storing a single light event
public class LightEvent {
	protected int number; // Light #
	protected boolean on; // true=on, false=off

	public LightEvent(int number, boolean on) {
		this.number = number;
		this.on = on;
	}

	public int getNumber() {
		return number;
	}

	public boolean getOn() {
		return on;
	}

	public String toString() {
		return "Light " + number + (on ? " on" : " off");
	}
}
