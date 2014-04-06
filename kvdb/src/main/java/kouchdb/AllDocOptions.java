package kouchdb;

import java.util.Set;

public interface AllDocOptions {
  /**
   * Include the document in each row in the doc field
   */

  boolean isInclude_docs();

  /**
   * Include conflicts in the _conflicts field of a doc
   */

  boolean isConflicts();

  /**
   * Include attachment data
   */

  boolean isAttachments();

  /**
   * Get documents with keys in a certain range descending: Reverse the order of the output table
   */

  String getStartkey();

  /**
   * Get documents with keys in a certain range descending: Reverse the order of the output table
   */

  String getEndkey();

  /**
   * array of keys you want to get
   * neither startkey nor endkey can be specified with this option
   * <p/>
   * the rows are returned in the same order as the supplied "keys" array
   * the row for a deleted document will have the revision ID of the deletion, and an extra id "deleted":true in the "value" property
   * the row for a nonexistent document will just contain an "error" property with the value "not_found"
   */

  Set<String> getKeys();

}
