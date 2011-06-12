package test.org.discogs.ws;

import org.discogs.model.Artist;
import org.discogs.ws.Discogs;

public class DiscogsTester {

  /**
   * @param args
   */
  public static void main(
      String[] args) {
    Discogs discogs = new Discogs();
    discogs.loader.disableCaching();
    Artist a = discogs.getArtist("Richard H. Kirk");
    System.out.println(a);
    // this fails
    a = discogs.getArtist("Stéphane Pompougnac");
    System.out.println(a);
  }

}
