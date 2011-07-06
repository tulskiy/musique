package test.org.discogs.dump;

import java.io.File;

import org.benow.xml.sax.ObjectProducer;
import org.benow.xml.sax.ObjectProducer.ProductionHandler;
import org.discogs.dump.ArtistProducer;
import org.discogs.dump.LabelProducer;
import org.discogs.dump.ReleaseProducer;
import org.discogs.model.Artist;
import org.discogs.model.Label;
import org.discogs.model.Release;

public class DumpTester {

  /**
   * @param args
   */
  public static void main(
      String[] args) {
    if (args.length == 0) {
      System.err.println("Usage: java " + DumpTester.class.getName() + " <dumpfile.xml[.gz]>");
      System.err.println("Creates object from the given dumpfile");
      System.err.println("Recent dumps can be found at: http://discogs.com/data");
      System.exit(-1);
    }

    try {
      String fn = args[0];
      File inFile = new File(fn);
      if (fn.contains("_artists.xml")) {
        ArtistProducer prod = new ArtistProducer();
        prod.setPreCalculate(false);
        prod.produce(inFile, new ProductionHandler<Artist>() {
          public void onProduce(
              ObjectProducer<Artist> producer,
              Artist obj) {
            System.out.println("Artist " + producer.getCurrentObjectNumber() + ": " + obj.getName());
          };
        });
      } else if (fn.contains("_releases.xml")) {
        ReleaseProducer prod = new ReleaseProducer();
        prod.setPreCalculate(false);
        prod.produce(inFile, new ProductionHandler<Release>() {
          public void onProduce(
              ObjectProducer<Release> producer,
              Release obj) {
            System.out.println("Release " + producer.getCurrentObjectNumber() + ": " + obj.getTitle());
            /*
            System.out.println(obj);
            if (producer.getCurrentObjectNumber() == 10)
              System.exit(0);
              */
          };
        });
      } else if (fn.contains("_labels.xml")) {
        LabelProducer prod = new LabelProducer();
        prod.setPreCalculate(false);
        prod.produce(inFile, new ProductionHandler<Label>() {
          public void onProduce(
              ObjectProducer<Label> producer,
              Label produced) {
            System.out.println("Label " + producer.getCurrentObjectNumber() + ": " + produced.getName());
          };
        });
      }

      System.exit(0);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

}
