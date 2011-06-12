package org.discogs.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.benow.java.rest.XMLAccessor;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class Track extends XMLAccessor {

  private static final SimpleDateFormat DURATION_FORMAT = new SimpleDateFormat("mm:ss");
  private static final SimpleDateFormat DURATION_FORMAT2 = new SimpleDateFormat("hh:mm:ss");

  public Track(Element item) {
    super(item);
    // this.inputEncoding = "ISO-8859-1";
    // this.outputEncoding = "UTF-8";
  }

  public String getTitle() {
    return getStringByPath("title");
  }

  public int getPosition() {
    return getIntByPath("position");
  }

  public String getPositionRaw() {
    return getStringByPath("position");
  }

  public long getDurationInMillis() {
    String durStr = getDuration();
    if (durStr == null)
      return 0;
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(0);
    String[] split = durStr.split(":");
    if (split.length == 1)
      cal.set(Calendar.SECOND, Integer.parseInt(split[0]));
    else if (split.length == 2) {
      cal.set(Calendar.MINUTE, Integer.parseInt(split[0]));
      cal.set(Calendar.SECOND, Integer.parseInt(split[1]));
    } else if (split.length == 3) {
      cal.set(Calendar.HOUR, Integer.parseInt(split[0]));
      cal.set(Calendar.MINUTE, Integer.parseInt(split[1]));
      cal.set(Calendar.SECOND, Integer.parseInt(split[2]));
    }
    return cal.getTimeInMillis();
  }

  public String getDuration() {
    return getStringByPath("duration");
  }

  public List<ReleaseArtist> getArtists() {
    List<ReleaseArtist> results = new ArrayList<ReleaseArtist>();
    Element aE = (Element) getNodeByPath("artists");
    if (aE != null) {
      NodeList cn = aE.getElementsByTagName("artist");
      for (int i = 0; i < cn.getLength(); i++)
        results.add(new ReleaseArtist((Element) cn.item(i),
          null));
    }
    return results;
  }

}
