package org.discogs.dump;

import org.discogs.model.Label;
import org.discogs.ws.Discogs;
import org.w3c.dom.Element;

public class LabelProducer extends DiscogsDumpProducer<Label> {

  private Discogs client;

  public LabelProducer(Discogs client) {
    this();
    this.client = client;
  }

  public LabelProducer() {
    super("label");
  }

  @Override
  protected Label createObject(
      Element constructed) {
    return new Label(constructed,
      client);
  }

}
