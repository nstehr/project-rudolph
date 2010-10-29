
// This interface makes it easy to create alternate controller schemes
public interface LightController {
	void initialize(); // Set up connection if necessary

	void doEvent(LightEvent what); // Send a light event

	void close(); // Close connection gracefully
}
