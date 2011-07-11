package org.discogs.dump;

import org.discogs.model.Release;
import org.discogs.ws.Discogs;
import org.w3c.dom.Element;

public class ReleaseProducer extends DiscogsDumpProducer<Release> {

  private Discogs client;

  public ReleaseProducer(Discogs client) {
    this();
    this.client = client;
  }

  public ReleaseProducer() {
    super("release");
  }

  @Override
  protected Release createObject(
      Element constructed) {
    return new Release(constructed,
      client);
  }

}
