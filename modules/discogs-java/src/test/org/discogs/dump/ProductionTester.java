package test.org.discogs.dump;

import java.io.File;

import org.benow.xml.sax.ObjectProducer;
import org.benow.xml.sax.ObjectProducer.ProductionHandler;
import org.discogs.dump.LabelProducer;
import org.discogs.model.Label;

public class ProductionTester {

  /**
   * @param args
   */
  public static void main(
      String[] args) {
    try {
      File src=new File(args[0]);
      // create handler that will do something with the produced objects
      ProductionHandler<Label> handler = new ProductionHandler<Label>() {
        @Override
        public void onProduce(
            ObjectProducer<Label> prod,
            Label obj) {
          System.out.println("Produced " + prod.getCurrentObjectNumber() + ": " + obj.getName());
        };
      };
      // create a producer for the class
      LabelProducer prod = new LabelProducer();
      // produce objects from the source, and handle production with the handler
      prod.produce(src, handler);
      
      System.out.println("Produced all: " + prod.getCurrentObjectNumber());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }



}
