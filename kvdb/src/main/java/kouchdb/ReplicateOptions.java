package kouchdb;


import java.nio.channels.CompletionHandler;
import java.util.Set;


public interface ReplicateOptions <T,A>{
    /**
     * undocumented
     */
    Integer getBatch_size();

    /**
     * Reference a filter function from a design document to selectively get updates.
     */
    String getFilter();

    /**
     * Query params send to the filter function.
     */
    Set<String> getQuery_params();

    /**
     * Only replicate docs with these ids.
     */
    Set<String> getDoc_ids();

    /**
     * Initialize the replication on the server. The response is the CouchDB POST _replicate response and is different from the DB replication response. Also, Splittable get_onChange is not supported on server replications.
     */
    Boolean getServer();

    /**
     * Create target database if it does not exist. Only for server replications.
     */
    Boolean getCreateTarget();

    interface ReplicateCall<T,A> extends ReplicateOptions {
      /**
       * Function called when all changes have been processed.
       */
      CompletionHandler getComplete();

      /**
       * Function called on each change processed..
       */
      CompletionHandler<T,A> getOnChange();

      /**
       * If true starts subscribing to future changes in the source database and continue replicating them.
       */
      CompletionHandler<T,A> getContinuous();

    }

  }

