package kouchdb;

import com.google.web.bindery.autobean.shared.Splittable;

import java.util.List;
import java.util.Map;

public interface ViewResults {

    Map<String, String> getErr();

    Results getResults();

    interface Response {
      boolean getOk();

      String getId();

      String getRev();

      String getError();
    }

    /**
     * {
     * "total_rows": 1,
     * "rows": [
     * { "doc": { "_id": "0B3358C1-BA4B-4186-8795-9024203EB7DD", "_rev": "1-5782E71F1E4BF698FA3793D9D5A96393", "blog_post": "my blog post" }, "id": "0B3358C1-BA4B-4186-8795-9024203EB7DD", "id": "0B3358C1-BA4B-4186-8795-9024203EB7DD", "value": { "rev": "1-5782E71F1E4BF698FA3793D9D5A96393" } }
     * ] }
     */

    interface Results {
      double getTotalRows();

      List<Record> getRows();

      interface Record {
        String getId();

        Long getSeq();

        String getKey();

        List<Splittable> getChanges();

        Splittable getValue();

        Splittable getDoc();
      }
    }
  }
