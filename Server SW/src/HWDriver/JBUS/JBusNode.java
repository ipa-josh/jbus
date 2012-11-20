package HWDriver.JBUS;

import java.util.Calendar;

public class JBusNode {
	private int hw_id_;
	private int max_analog_pins_ = 6;
	private int max_pins_ = 10;
	private boolean pins_[];
	private PinConfiguration conf_pins_[];
	private int polling_ms_ = 250;
	private long ts_polled_ = 0;
	
	private static class PinConfiguration {
		enum CONFIG {
			INPUT,
			INPUT_WITH_PULLUP,
			ANALOG,
			OUTPUT,
			PWM
		}
		
		public CONFIG conf_ = CONFIG.INPUT;
	}
	
	enum STATUS {
		NOT_INITIZALIZED,
		READY,
		NOT_FOUND
	}
	
	private STATUS status_ = STATUS.NOT_INITIZALIZED;
	private JBusInterface intf_;
	
	JBusNode(int hw_id, JBusInterface intf) {
		hw_id_ = hw_id;
		intf_ = intf;
		
		pins_ = new boolean[max_pins_];
		conf_pins_ = new PinConfiguration[max_pins_];
		
		intf_.connect(this);
	}
	
	public int getHwId() {
		return hw_id_;
	}
	
	public int getPollingMs() {
		return polling_ms_;
	}
	
	public void setPollingMs(int v) {
		polling_ms_ = v;
	}
	
	public int getPolled() {
		return ts_polled_;
	}
	
	public void polled(int v) {
		ts_polled_ = Calendar.getInstance().getTimeInMillis();
	}
	
	public int readAnalog(int pin) throws Exception {
		if(pin<0 || pin>max_analog_pins_)
			throw new Exception("pin no. out of range");
	}
	
	public boolean []readInput() {
		int input;
		boolean r[] = new boolean[max_pins_];
		for(int i=0; i<r.length; i++)
			r[i] = (input&(1<<i))>0;
		return r;
	}
	
	public boolean setOutput() {
		
	}
	
	public Vector<JBusInterface.Message> createPollingMsg() {
		Vector<JBusInterface.Message> r = new Vector<JBusInterface.Message>();
		return r;
	}
}
