package nhb.common.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateTimeUtils {

	public static Date getTomorrowMidnight() {
		// today
		Calendar date = new GregorianCalendar();

		// reset hour, minutes, seconds and millis
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);

		// next day
		date.add(Calendar.DAY_OF_MONTH, 1);

		return date.getTime();
	}

	public static int getCurrentUnixTime() {
		return Double.valueOf(System.currentTimeMillis() / 1e3).intValue();
	}

}
