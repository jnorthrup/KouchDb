package kouchdb;

import com.google.web.bindery.autobean.shared.Splittable;

import java.util.List;

public interface FetchOptions {
    /**
     * Fetch specific revision of a document. Defaults to winning revision (see couchdb guide.
     */
    String getRev();

    /**
     * Include revision history of the document
     */
    Boolean getRevs();

    /**
     * Include a list of revisions of the document, and their availability.
     */
    List<String> getRevs_info();

    /**
     * Fetch all leaf revisions if openrevs="all" or fetch all leaf revisions specified in openrevs array. Leaves will be returned in the same order as specified in input array
     */
    Splittable getOpen_revs();

    /**
     * If specified conflicting leaf revisions will be attached in _conflicts array
     */
    Boolean getConflicts();

    /**
     * Include attachment data
     */
    Boolean getAttachments();

    /**
     * Include sequence number of the revision in the database
     */
    Boolean getLocal_seq();

    /**
     * An object of options to be sent to the ajax requester. In Node they are sent ver batim to request with the exception of:
     * options.ajax.cache: Appends a random string to the end of all HTTP GET requests to avoid them being cached on IE. Set this to true to prevent this happening.
     */
    Splittable getAjax();
  }
