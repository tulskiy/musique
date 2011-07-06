package org.discogs.dump;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.benow.xml.sax.ObjectProducer;
import org.w3c.dom.Element;

abstract class DiscogsDumpProducer<E> extends ObjectProducer<E> {

  public DiscogsDumpProducer(String matchElementName) {
    super(matchElementName);
  }

  @Override
  protected abstract E createObject(
      Element constructed);

  @Override
  protected InputStream createStream(
      File src) throws IOException {
    return new BufferedInputStream(new GZIPInputStream(new FileInputStream(src)));
  }
}
