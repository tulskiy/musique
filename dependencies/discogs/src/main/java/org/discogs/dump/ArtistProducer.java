package org.discogs.dump;

import org.discogs.model.Artist;
import org.discogs.ws.Discogs;
import org.w3c.dom.Element;

public class ArtistProducer extends DiscogsDumpProducer<Artist> {

  private Discogs client;

  public ArtistProducer(Discogs client) {
    this();
    this.client = client;
  }

  public ArtistProducer() {
    super("artist");
  }

  @Override
  protected Artist createObject(
      Element constructed) {
    return new Artist(constructed,
      client);
  }

}
