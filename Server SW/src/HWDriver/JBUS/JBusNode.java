package HWDriver.JBUS;

import java.util.Calendar;
import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import rights.Group;
import rights.User;

import common.Attribute;
import common.Callback;
import common.HAObject;
import common.Output;
import common.Path;
import common.attributes.Attr_Boolean;
import common.attributes.Attr_Double;
import common.attributes.Attr_String;

public class JBusNode extends HAObject implements Callback {
	private int hw_id_;
	private int sw_version_=-1;
	private int max_analog_pins_ = 6;
	private int max_pins_ = 10;
	private PinConfiguration conf_pins_[];
	private int polling_ms_ = 250;
	private long ts_polled_ = Calendar.getInstance().getTimeInMillis();
	private long ts_last_received_ = 0;

	private static class PinConfiguration {
		static final byte INPUT 				= 0;
		static final byte ANALOG 				= 4;
		static final byte INPUT_WITH_PULLUP 	= 1;
		static final byte OUTPUT_LOW 			= 2;
		static final byte OUTPUT_HIGH			= 6;
		static final byte PWM00				= 3;
		static final byte PWM25				= 3+4;
		static final byte PWM75				= 3+8;
		static final byte PWM99				= 3+12;

		public byte conf_ = INPUT;
	}

	enum STATUS {
		NOT_INITIZALIZED,
		READY,
		NOT_FOUND
	}

	static final byte GET_INPUT 	= 0;
	static final byte SET_OUTPUT 	= 1;
	static final byte CONFIG 		= 2;

	private STATUS status_ = STATUS.NOT_INITIZALIZED;
	private JBusInterface intf_;

	private int input_ = 0;
	private int output_= 0;
	private int analog_[] = new int[8];
	private int pwm_[] = new int[8];

	JBusNode(User usr, Group grp, Attribute parent, int hw_id, JBusInterface intf) {
		super(usr, grp, parent);
		Attribute config = new Attr_String(getUser(), getGroup(), this);
		config.setId("config");
		add( config );

		hw_id_ = hw_id;
		intf_ = intf;

		conf_pins_ = new PinConfiguration[0];

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

	public long getPolled() {
		return ts_polled_;
	}

	public void polled() {
		ts_polled_ = Calendar.getInstance().getTimeInMillis();
	}

	public int readAnalog(int pin) throws Exception {
		if(pin<0 || pin>max_analog_pins_)
			throw new Exception("pin no. out of range");
		return analog_[pin];
	}

	public JBusInterface.Message ping() {
		return new JBusInterface.Message(hw_id_);
	}

	public boolean parseMessage(JBusInterface.Message msg) throws Exception {		
		if(msg.getId()!=hw_id_)
			throw new Exception("hardware id does not match");

		ts_last_received_ = Calendar.getInstance().getTimeInMillis();

		if(msg.length()==6)
			return true; //ping

		switch(msg.read(6,2)) {
		case GET_INPUT:
			if(msg.length()==18) {
				//digital inputs
				int old = input_;
				input_ = msg.read(8, 10);
				onInput(old);
			}
			else if(msg.length()==18+3) {
				//analg inputs
				int ch = msg.read(8, 3);
				int val= msg.read(11, 10);
				int old = analog_[ch];
				analog_[ch] = val;
				onAnalog(ch, old);
			}
			else
				throw new Exception("malformed message INPUT");
			break;

		case SET_OUTPUT:
			//no response, perhaps other master on bus?
			throw new Exception("unexpected message OUTPUT, second master on bus?");
			//break;

		case CONFIG:
			if(msg.length()>16 && msg.length()%8==0) {
				list_.clear();
				sw_version_ = msg.read(msg.length()-8, 8);
				conf_pins_ = new PinConfiguration[ (msg.length()-8)/4 ];
				for(int i=8; i<msg.length()-8; i+=4) {
					conf_pins_[i] = new PinConfiguration();
					conf_pins_[i].conf_ = (byte) msg.read(i, 4);

					Attribute attr = null;
					switch(conf_pins_[i].conf_) {
					case PinConfiguration.ANALOG:
					case PinConfiguration.PWM00:
					case PinConfiguration.PWM25:
					case PinConfiguration.PWM75:
					case PinConfiguration.PWM99:
						attr = new Attr_Double(getUser(),getGroup(),this);
						break;
					case PinConfiguration.INPUT:
					case PinConfiguration.INPUT_WITH_PULLUP:
					case PinConfiguration.OUTPUT_HIGH:
					case PinConfiguration.OUTPUT_LOW:
						attr = new Attr_Boolean(getUser(),getGroup(),this);
						break;
					default:
						throw new Exception("unknown config: "+conf_pins_[i].conf_);
					}
					if(attr!=null) {
						attr.setId(""+i);
						attr.getNotifier().addCallback(this);
						list_.add(attr);
					}
				}
			}
			else
				throw new Exception("malformed message CONFIG");
			
			//generate config string
			JSONObject obj=new JSONObject();
			obj.put("id",new Integer(hw_id_));
			obj.put("sw",new Integer(hw_id_));
			for(int i=0; i<conf_pins_.length; i++) {
				obj.put(""+i,conf_pins_[i].conf_);
			}

			Path p = new Path();
			p.parseString("config");
			Attribute attr = (Attribute) get(getUser(), p);
			attr.set(getUser(), obj.toString());

			break;

		default:
			throw new Exception("malformed message of unknown command");
		}

		ts_last_received_ = Calendar.getInstance().getTimeInMillis();

		return false;
	}

	private void onAnalog(int ch, int old) {
		if(analog_[ch]==old) return;

		Path p = new Path();
		p.parseString(""+ch);
		Attribute obj = (Attribute) get(getUser(), p);
		double v = analog_[ch]/255.;
		if(obj!=null)
			obj.set(getUser(), v);
	}

	private void onInput(int old) {
		int ch = (old^input_);

		for(int i=0; i<max_pins_; i++) {
			//skip double values
			if( i<conf_pins_.length && (
					conf_pins_[i].conf_ == PinConfiguration.ANALOG ||
					conf_pins_[i].conf_ == PinConfiguration.PWM00 ||
					conf_pins_[i].conf_ == PinConfiguration.PWM25 ||
					conf_pins_[i].conf_ == PinConfiguration.PWM75 ||
					conf_pins_[i].conf_ == PinConfiguration.PWM99
					) )
				continue;
			
			if( (ch&(1<<i))!=0 ) {
				Path p = new Path();
				p.parseString(""+i);
				Attribute obj = (Attribute) get(getUser(), p);
				boolean v = (input_&(1<<i))!=0;
				if(obj!=null)
					obj.set(getUser(), v);
			}
		}

	}

	public boolean []readInput() {
		boolean r[] = new boolean[max_pins_];
		for(int i=0; i<r.length; i++)
			r[i] = (input_&(1<<i))>0;
			return r;
	}

	public boolean setOutput(int maskAND, int maskOR) throws Exception {
		int old = output_;
		output_ &= maskAND;
		output_ |= maskOR;
		if(old!=output_)
			intf_.addMessage(genSetOutput(output_));
		return false;
	}

	public boolean setPWM(int ch, int val) throws Exception {
		if(ch>pwm_.length) return false;
		int old = pwm_[ch];
		pwm_[ch] = val;
		if(old!=pwm_[ch])
			intf_.addMessage(genSetPWM(ch, pwm_[ch]));
		return false;
	}

	public Vector<JBusInterface.Message> createPollingMsg() throws Exception {
		Vector<JBusInterface.Message> r = new Vector<JBusInterface.Message>();

		if(status_ == STATUS.NOT_INITIZALIZED) {
			r.add( genReadConfig() );
			r.add( genReadInput() );
			r.add( genSetOutput(output_) );
		}
		else {
			int n=0;
			for(PinConfiguration pc : conf_pins_)  {
				switch(pc.conf_) {
				case PinConfiguration.ANALOG:
					r.add( genReadAnalg(n) );
					break;
				}
				++n;
			}
		}

		return r;
	}

	private JBusInterface.Message genReadConfig() throws Exception {
		JBusInterface.Message msg = new JBusInterface.Message(hw_id_);
		msg.add(CONFIG, 2);
		return msg;
	}

	private JBusInterface.Message genReadInput() throws Exception {
		JBusInterface.Message msg = new JBusInterface.Message(hw_id_);
		msg.add(GET_INPUT, 2);
		return msg;
	}

	private JBusInterface.Message genReadAnalg(int ch) throws Exception {
		JBusInterface.Message msg = new JBusInterface.Message(hw_id_);
		msg.add(GET_INPUT, 2);
		msg.add((byte) ch, 3);
		return msg;
	}

	private JBusInterface.Message genSetOutput(int val) throws Exception {
		JBusInterface.Message msg = new JBusInterface.Message(hw_id_);
		msg.add(SET_OUTPUT, 2);
		msg.add((byte) (val&0xff), 8);
		msg.add((byte) ((val>>8)&0x03), 2);
		return msg;
	}

	private JBusInterface.Message genSetPWM(int ch, int val) throws Exception {
		JBusInterface.Message msg = new JBusInterface.Message(hw_id_);
		msg.add(SET_OUTPUT, 2);
		msg.add((byte) ch, 3);
		msg.add((byte) (val&0xff), 8);
		return msg;
	}

	@Override
	public boolean onAttributeChanged(Attribute attr) {
		if( attr.getId().equals("config")) {
			Object obj=((Attr_String)attr).get(getUser());
			if(obj==null || !String.class.equals(obj.getClass()))
				return false;
			return onConfig( (String)obj );
		}
		
		int pin = Integer.parseInt(attr.getId());

		if(pin>conf_pins_.length) {
			return false;
		}

		Object obj;
		switch(conf_pins_[pin].conf_) {
		case PinConfiguration.PWM00:
		case PinConfiguration.PWM25:
		case PinConfiguration.PWM75:
		case PinConfiguration.PWM99:

			obj=((Attr_Double)attr).get(getUser());
			if(obj==null || !Double.class.equals(obj.getClass()))
				return false;
			double val = (Double) obj;
			int ival = (int) (val*255);

			if(ival<0) ival=0;
			if(ival>255) ival=255;

			try {
				setPWM(pin, ival);
			} catch (Exception e) {
				Output.error(e);
			}
			
			return true;

		case PinConfiguration.OUTPUT_HIGH:
		case PinConfiguration.OUTPUT_LOW:
			obj=((Attr_Boolean)attr).get(getUser());
			if(obj==null || !Boolean.class.equals(obj.getClass()))
				return false;
			boolean set = (Boolean) obj;

			try {
				if( set )
					setOutput(~0, (1<<pin) );
				else
					setOutput( ~(1<<pin), 0 );
			} catch (Exception e) {
				Output.error(e);
			}

			break;

		default:
			break;
		}

		return false;	//return false, and expect response from device
	}

	@Override
	public boolean required() {
		return true;
	}

	private boolean onConfig(String s) {
		Object obj=JSONValue.parse(s);
		JSONObject jo=(JSONObject)obj;
		
		jo.get("id");

		return false;
	}
}
