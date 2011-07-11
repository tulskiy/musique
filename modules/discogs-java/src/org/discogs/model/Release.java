package org.discogs.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.discogs.ws.Discogs;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Release extends DiscogsObject {

  private static final SimpleDateFormat RELEASED_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  private static final SimpleDateFormat RELEASED_FORMAT_MONTH = new SimpleDateFormat("yyyy-MM");
  private static final SimpleDateFormat RELEASED_FORMAT_YEAR = new SimpleDateFormat("yyyy");

  public Release(Element releaseElem, Discogs client) {
    super(releaseElem, client);
  }

  public String getId() {
    return getStringByPath("@id");
  }

  public String getStatus() {
    return getStringByPath("@status");
  }

  public List<ReleaseArtist> getArtists() {
    List<ReleaseArtist> results = new ArrayList<ReleaseArtist>();
    Element aE = (Element) getNodeByPath("artists");
    if (aE != null) {
      NodeList cn = aE.getElementsByTagName("artist");
      for (int i = 0; i < cn.getLength(); i++)
        results.add(new ReleaseArtist((Element) cn.item(i),
          client));
    }
    return results;
  }

  public String getTitle() {
    return getStringByPath("title");
  }

  public List<LabelRelease> getLabelReleases() {
    List<LabelRelease> results = new ArrayList<LabelRelease>();
    NodeList cn = element.getElementsByTagName("label");
    for (int i = 0; i < cn.getLength(); i++)
      results.add(new LabelRelease((Element) cn.item(i),
        client));
    return results;
  }

  public List<ReleaseArtist> getExtraArtists() {
    List<ReleaseArtist> results = new ArrayList<ReleaseArtist>();
    Element aE = (Element) getNodeByPath("extraartists");
    if (aE != null) {
      NodeList cn = aE.getElementsByTagName("artist");
      for (int i = 0; i < cn.getLength(); i++)
        results.add(new ReleaseArtist((Element) cn.item(i),
          client));
    }
    return results;
  }

  public List<Format> getFormats() {
    List<Format> results = new ArrayList<Format>();
    Element aE = (Element) getNodeByPath("formats");
    if (aE != null) {
      NodeList cn = aE.getElementsByTagName("format");
      for (int i = 0; i < cn.getLength(); i++)
        results.add(new Format((Element) cn.item(i)));
    }
    return results;
  }

  public List<String> getGenres() {
    List<String> genres = new ArrayList<String>();
    Element gE = (Element) getNodeByPath("genres");
    if (gE != null) {
      NodeList ges = gE.getElementsByTagName("genre");
      for (int i = 0; i < ges.getLength(); i++) {
        Element curr = (Element) ges.item(i);
        genres.add(curr.getFirstChild().getNodeValue());
      }
    }
    return genres;
  }

  public List<String> getStyles() {
    List<String> genres = new ArrayList<String>();
    Element gE = (Element) getNodeByPath("styles");
    if (gE != null) {
      NodeList ges = gE.getElementsByTagName("style");
      for (int i = 0; i < ges.getLength(); i++) {
        Element curr = (Element) ges.item(i);
        genres.add(curr.getFirstChild().getNodeValue());
      }
    }
    return genres;
  }

  public String getCountry() {
    return getStringByPath("country");
  }

  public Date getReleaseDate() {
    String dateStr = getReleaseDateRaw();
    if (dateStr == null)
      return null;
    try {
      return RELEASED_FORMAT.parse(dateStr);
    } catch (ParseException e) {
      try {
        return RELEASED_FORMAT_MONTH.parse(dateStr);
      } catch (ParseException ee) {
        try {
          return RELEASED_FORMAT_YEAR.parse(dateStr);
        } catch (ParseException eee) {
          System.err.println("Error parsing date from: " + dateStr + ".  Add parse format");
          eee.printStackTrace();
          return null;
        }
      }
    }
  }

  public String getReleaseDateRaw() {
    return getStringByPath("released");
  }

  public String getNotes() {
    return getStringByPath("notes");
  }

  public String getMasterId() {
    return getStringByPath("master_id");
  }

  public List<Track> getTracks() {
    List<Track> tracks = new ArrayList<Track>();
    Element gE = (Element) getNodeByPath("tracklist");
    if (gE != null) {
      NodeList ges = gE.getElementsByTagName("track");
      for (int i = 0; i < ges.getLength(); i++) {
        Element curr = (Element) ges.item(i);
        tracks.add(new Track(curr));
      }
    }
    return tracks;
  }

}
