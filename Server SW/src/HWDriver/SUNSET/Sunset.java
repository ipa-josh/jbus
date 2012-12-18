package HWDriver.SUNSET;

import java.util.Calendar;

import org.jdom2.DataConversionException;
import org.jdom2.Element;

import rights.Group;
import rights.Right;
import rights.User;

import HWDriver.JBUS.JBusHWSerial;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import common.Attribute;
import common.HAObject;
import common.Output;
import common.attributes.Attr_Double;
import common.attributes.Attr_Error;

public class Sunset extends HAObject implements Runnable
{
	public Sunset(User usr, Group grp, Attribute parent) {
		super(usr, grp, parent);
	}

	private Location location_ = new Location("52", "13");
	private SunriseSunsetCalculator calculator_ = null;
	private Attribute sun_ = null;

	private void init(String x, String y) {
		location_ = new Location(x, y);
		calculator_ = new SunriseSunsetCalculator(location_, "Europe/Berlin");
	}

	@Override
	public boolean _readXML(Element el) {
		synchronized(this) {
			String x="52";
			String y="13";

			org.jdom2.Attribute t = el.getAttribute("x");
			if(t!=null) {
				x=t.getValue();
			}
			t = el.getAttribute("y");
			if(t!=null) {
				y=t.getValue();
			}

			init(x,y);
			
			sun_ = new Attr_Double(getUser(), getGroup(), this);
			sun_.setId("status");
			add( sun_ );
		}

		(new Thread(this)).start();
		return true;
	}

	private float get() {
		float v=_get();
		if(v>0)
			return 1-Math.abs( 2*(v-0.5f) );
		return -(1-Math.abs( 2*(v-0.5f) ));
	}
	
	private float _get() {
		Calendar today = Calendar.getInstance();
		Calendar officialSunrise = calculator_.getOfficialSunriseCalendarForDate(today);
		Calendar officialSunset = calculator_.getOfficialSunsetCalendarForDate(today);
		
		if( today.getTimeInMillis()>=officialSunrise.getTimeInMillis() &&
				today.compareTo(officialSunset)<=0)
			return (today.getTimeInMillis()-officialSunrise.getTimeInMillis())/
			(float)(officialSunset.getTimeInMillis()-officialSunrise.getTimeInMillis());
		else if( today.getTimeInMillis()<officialSunrise.getTimeInMillis() ) {
			Calendar yesterday = Calendar.getInstance();
			yesterday.add(Calendar.DAY_OF_YEAR, -1);
			officialSunset = calculator_.getOfficialSunsetCalendarForDate(yesterday);

			return (today.getTimeInMillis()-officialSunrise.getTimeInMillis())/
			(float)(officialSunset.getTimeInMillis()-officialSunrise.getTimeInMillis());
		}
		else {
			Calendar tomorrow = Calendar.getInstance();
			tomorrow.add(Calendar.DAY_OF_YEAR, 1);
			officialSunset = calculator_.getOfficialSunsetCalendarForDate(tomorrow);

			return (today.getTimeInMillis()-officialSunrise.getTimeInMillis())/
			(float)(officialSunset.getTimeInMillis()-officialSunrise.getTimeInMillis());
		}
		
	}

	@Override
	public void run() {

		while(true) {
			sun_.set(getUser(), new Double(get()));
			
			long ms = 100000;
			synchronized (this) {
				try {
					wait(ms);
				} catch (InterruptedException e) {
				}
			}
		}
				
	}
}