package org.delia.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateUtils {

	public static LocalDateTime convertToLocalDateTime(Date dt) {
		return dt.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDateTime();
	}

	public static LocalDate convertToLocalDate(Date dt) {
		return dt.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDate();
	}	
	public static Date convertToDate(LocalDateTime ldt) {
		return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());			
	}

//	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	public static String toJSONFormat(Date dt) {
		String formattedDate = dateFormat.format(dt);
		return formattedDate;
	}
	
	
	public static LocalDateTime convertToUTCLocalTime(Date dt) {
		return dt.toInstant()
				.atZone(ZoneId.of("UTC"))
				.toLocalDateTime();
	}

	public static LocalDate convertToUTCLocalDate(Date dt) {
		return dt.toInstant()
				.atZone(ZoneId.of("UTC"))
				.toLocalDate();
	}	
	
}
