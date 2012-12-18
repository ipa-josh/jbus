package HWDriver.SUNSET;

import java.util.Calendar;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

class Sunset
{
	private Location location_ = new Location("39.9522222", "-75.1641667");
	private SunriseSunsetCalculator calculator_ = null;

	private void init() {
		calculator_ = new SunriseSunsetCalculator(location_, "America/New_York");
	}
	
	private float get() {
		Calendar officialSunrise = calculator_.getOfficialSunriseCalendarForDate(Calendar.getInstance());
		Calendar officialSunset = calculator_.getOfficialSunsetCalendarForDate(Calendar.getInstance());
	}
}