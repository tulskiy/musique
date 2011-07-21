package org.benow.xml.sax;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public abstract class ObjectProducer<E> extends DOMProducer {

  public interface ProductionHandler<E> {
    public void onProduce(
        ObjectProducer<E> producer,
        E produced);
  }

  private ProductionHandler<E> handler;
  private boolean preCalculate = true;
  private final List<E> produced = new ArrayList<E>();
  private E lastProduced;

  public ObjectProducer(String matchElementName) {
    super(matchElementName);
  }

  @Override
  protected void onConstruct(
      Element constructed) {
    E obj = createObject(constructed);
    if (handler != null)
      handler.onProduce(this, obj);
    else {
      this.produced.add(obj);
      this.lastProduced = obj;
    }
    obj = null;
    constructed = null;
  }

  public E getLastProduced() {
    if (handler != null)
      throw new IllegalStateException("not valid if a handler has been assigned");
    return lastProduced;
  }

  public List<E> getProduced() {
    if (handler != null)
      throw new IllegalStateException("not valid if a handler has been assigned");
    return produced;
  }

  public void setPreCalculate(
      boolean precalculate) {
    this.preCalculate = precalculate;
  }

  public void produce(
      File src,
      ProductionHandler<E> handler) throws ParserConfigurationException, SAXException, IOException {
    this.handler = handler;
    if (preCalculate)
      preCalculate(createStream(src));
    parse(createStream(src));
  }

  protected InputStream createStream(
      File src) throws IOException {
    return new FileInputStream(src);
  }

  protected abstract E createObject(
      Element constructed);

}
