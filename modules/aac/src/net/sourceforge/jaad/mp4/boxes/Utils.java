package net.sourceforge.jaad.mp4.boxes;

import java.util.Date;

public class Utils {

	private static final long DATE_OFFSET = 2082850791998l;

	public static Date getDate(long time) {
		return new Date(time*1000-DATE_OFFSET);
	}
}
