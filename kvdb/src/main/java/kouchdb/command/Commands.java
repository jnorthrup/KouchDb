package kouchdb.command;

import com.google.web.bindery.autobean.shared.Splittable;

import java.util.List;
import java.util.Set;

public interface Commands {
    /**
     * Create a database
     * <p>
     * new PouchDB([name], [options])
     * This method creates a database or opens an existing one. If you use a URL like http://domain.com/dbname then PouchDB will work as a client to an online CouchDB instance. Otherwise it will create a local database using whatever backend is present (i.e. IndexedDB, WebSQL, or LevelDB).
     * <p>
     * Options
     * options.name: You can omit the name argument and specify it via options instead. Note that the name is required.
     * options.auto_compaction: This turns on auto compaction (experimental). Defaults to false.
     * options.cache: Appends a random string to the end of all HTTP GET requests to avoid them being cached on IE. Set this to true to prevent this happening (can also be set per request). Defaults to false.
     * options.adapter: One of 'idb', 'leveldb', 'websql', or 'http'. If unspecified, PouchDB will infer this automatically, preferring IndexedDB to WebSQL in browsers that support both (i.e. Chrome, Opera and Android 4.4+).
     * Notes:
     * <p>
     * In IndexedDB and WebSQL, PouchDB will use _pouch_ to prefix the internal database names. Do not manually create databases with the same prefix.
     * When acting as a client on Node, any other options given will be passed to request.
     * When using the 'leveldb' adapter (the default on Node), any other options given will be passed to levelup. The storage layer of leveldb can be replaced by passing a level backend factory (such as MemDOWN) as options.db. The rest of the supported options are documented here.
     * Example Usage:
     * <p>
     * var db = new PouchDB('dbname');
     * // or
     * var db = new PouchDB('http://localhost:5984/dbname');
     * Create a WebSQL-only Pouch (e.g. when using the SQLite Plugin for Cordova/PhoneGap):
     * <p>
     * var db = new PouchDB('dbname', {adapter : 'websql'});
     * Create an in-memory Pouch (in Node):
     * <p>
     * var db = new PouchDB('dbname', {db : require('memdown')});
     */
    interface CreateDb {
    }

    /**
     * Delete a database
     * <p>
     * db.destroy([options], [callback])
     * Delete database.
     * <p>
     * Notes: With a remote CouchDB on Node, options are passed to request.
     * <p>
     * Example Usage:
     * <p>
     * db.destroy(function(err, info) { });
     * You can also delete a database using just the name:
     * <p>
     * PouchDB.destroy('dbname', function(err, info) { });
     */
    interface DeleteDb {
    }

    /**
     * Create / update a document
     * <p>
     * Using db.put()
     * db.put(doc, [_id], [_rev], [options], [callback])
     * Create a new document or update an existing document. If the document already exists, you must specify its revision _rev, otherwise a conflict will occur.
     * <p>
     * There are some restrictions on valid property names of the documents. These are explained here.
     * <p>
     * Example Usage:
     * <p>
     * Create a new doc with an _id:
     * <p>
     * db.put({
     * title: 'Heroes'
     * }, 'mydoc'), function(err, response) { });
     * Like all methods, you can also use a promise:
     * <p>
     * db.put({
     * title: 'Lady Stardust'
     * }, 'myOtherDoc').then(function(response) { });
     * Update an existing doc using _rev:
     * <p>
     * db.get('myOtherDoc', function(err, otherDoc) {
     * db.put({
     * title: "Let's Dance",
     * }, 'myOtherDoc', otherDoc._rev, function(err, response) { });
     * });
     * You can also include the _id and _rev directly in the document:
     * <p>
     * db.get('myOtherDoc').then(function(otherDoc) {
     * return db.put({
     * _id: 'myOtherDoc',
     * _rev: otherDoc._rev,
     * title: 'Be My Wife',
     * });
     * }, function(err, response) {
     * if (err) {
     * // on error
     * } else {
     * // on success
     * }
     * });
     * Example Response:
     * <p>
     * {
     * "ok": true,
     * "id": "mydoc",
     * "rev": "1-A6157A5EA545C99B00FF904EEF05FD9F"
     * }
     * Using db.post()
     * db.post(doc, [options], [callback])
     * Create a new document and let PouchDB generate an _id for it.
     * <p>
     * Example Usage:
     * <p>
     * db.post({
     * title: 'Ziggy Stardust'
     * }, function (err, response) { });
     * Example Response:
     * {
     * "ok" : true,
     * "id" : "8A2C3761-FFD5-4770-9B8C-38C33CED300A",
     * "rev" : "1-d3a8e0e5aa7c8fff0c376dac2d8a4007"
     * }
     * Put vs. post: The basic rule of thumb is: put new documents with an _id, post new documents without an _id.
     */
    interface PersistDoc {
        interface PutOptions {
        }
    }

    /**
     * Fetch a document
     * <p>
     * db.get(docid, [options], [callback])
     * Retrieves a document, specified by docid.
     * <p>
     * Options
     * All options default to false unless otherwise specified.
     * <p>
     * options.rev: Fetch specific revision of a document. Defaults to winning revision (see the CouchDB guide).
     * options.revs: Include revision history of the document.
     * options.revs_info: Include a list of revisions of the document, and their availability.
     * options.open_revs: Fetch all leaf revisions if open_revs="all" or fetch all leaf revisions specified in open_revs array. Leaves will be returned in the same order as specified in input array.
     * options.conflicts: If specified, conflicting leaf revisions will be attached in _conflicts array.
     * options.attachments: Include attachment data.
     * options.local_seq: Include sequence number of the revision in the database.
     * options.ajax: An object of options to be sent to the ajax requester. In Node they are sent ver batim to request with the exception of:
     * options.ajax.cache: Appends a random string to the end of all HTTP GET requests to avoid them being cached on IE. Set this to true to prevent this happening.
     * Example Usage:
     * <p>
     * db.get('mydoc', function(err, doc) { });
     * Example Response:
     * <p>
     * {
     * "title": "Rock and Roll Heart",
     * "_id": "mydoc",
     * "_rev": "1-A6157A5EA545C99B00FF904EEF05FD9F"
     * }
     */
    interface Fetchdoc {
        interface FetchOptions {
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
    }

    /**
     * Delete a document
     * <p>
     * db.remove(doc, [options], [callback])
     * Deletes the document. doc is required to be a document with at least an _id and a _rev property. Sending the full document will work as well.
     * <p>
     * Example Usage:
     * <p>
     * db.get('mydoc', function(err, doc) {
     * db.remove(doc, function(err, response) { });
     * });
     * With Promises:
     * db.get('mydoc').then(function(doc) {
     * return db.remove(doc);
     * }).catch(function(err){
     * //errors
     * });
     * Example Response:
     * <p>
     * {
     * "ok": true,
     * "id": "mydoc",
     * "rev": "2-9AF304BE281790604D1D8A4B0F4C9ADB"
     * }
     */
    interface DeleteDoc {
    }

    /**
     * Create a batch of documents
     * <p>
     * db.bulkDocs(docs, [options], [callback])
     * <p>
     * Modify, create or delete multiple documents. The docs argument is an object with property docs which is an array of documents. You can also specify a new_edits property on the docs object that when set to false allows you to post existing documents.
     * <p>
     * If you omit an _id parameter on a given document, the database will create a new document and assign the ID for you. To update a document, you must include both an _id parameter and a _rev parameter, which should match the ID and revision of the document on which to base your updates. Finally, to delete a document, include a _deleted parameter with the value true.
     * <p>
     * Example Usage:
     * <p>
     * db.bulkDocs({docs: [
     * {title : 'Lisa Says'},
     * {title : 'Space Oddity'}
     * ]}, function(err, response) { });
     * <p>
     * Example Response:
     * <p>
     * [
     * {
     * "ok": true,
     * "id": "06F1740A-8E8A-4645-A2E9-0D8A8C0C983A",
     * "rev": "1-84abc2a942007bee7cf55007cba56198"
     * },
     * {
     * "ok": true,
     * "id": "6244FB45-91DB-41E5-94FF-58C540E91844",
     * "rev": "1-7b80fc50b6af7a905f368670429a757e"
     * }
     * ]
     */
    interface BatchCreate {
    }

    /**
     * Fetch a batch of documents
     * <p>
     * db.allDocs([options], [callback])
     * Fetch multiple documents. Deleted documents are only included if options.keys is specified.
     * <p>
     * Options
     * All options default to false unless otherwise specified.
     * <p>
     * <p>
     * Notes: For pagination, options.limit and options.skip are also available, but the same performance concerns as in CouchDB apply. Use the startkey/endkey pattern instead.
     * <p>
     * Example Usage:
     * <p>
     * db.allDocs({include_docs: true}, function(err, response) { });
     * Example Response:
     * <p>
     * {
     * "total_rows": 1,
     * "rows": [{
     * "doc": {
     * "_id": "0B3358C1-BA4B-4186-8795-9024203EB7DD",
     * "_rev": "1-5782E71F1E4BF698FA3793D9D5A96393",
     * "title": "Sound and Vision"
     * },
     * "id": "0B3358C1-BA4B-4186-8795-9024203EB7DD",
     * "key": "0B3358C1-BA4B-4186-8795-9024203EB7DD",
     * "value": {
     * "rev": "1-5782E71F1E4BF698FA3793D9D5A96393"
     * }
     * }]
     * }
     */
    interface BatchFetch {
        interface AllDocOptions {
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
             * <p>
             * the rows are returned in the same order as the supplied "keys" array
             * the row for a deleted document will have the revision ID of the deletion, and an extra id "deleted":true in the "value" property
             * the row for a nonexistent document will just contain an "error" property with the value "not_found"
             */

            Set<String> getKeys();

            long getLimit();

            long getSkip();
        }
    }

    /**
     * Listen to database changes
     * <p>
     * db.changes(options)
     * A list of changes made to documents in the database, in the order they were made. It returns an object with one method cancel, which you call if you don't want to listen to new changes anymore. options.onChange will be be called for each change that is encountered.
     * <p>
     * Note the 'live' option was formally called 'continuous', you can still use 'continuous' if you can spell it.
     */
    interface Changes {
        interface ChangesOptions {
            /**
             * Include the associated document with each change.
             */
            Boolean isIncludeDocs();

            /**
             * Include conflicts.
             */
            Boolean isConflicts();

            /**
             * Include attachments.
             */
            Boolean isAttachments();

            /**
             * Reverse the order of the output documents.
             */
            boolean isDescending();

            /**
             * Reference a filter function from a design document to selectively get updates.
             */
            String getFilter();

            /**
             * Start the results from the change immediately after the given sequence number.
             */
            Long getSince();

            /**
             * enact subscription on connection.  should be multiplexed over websocket.
             */
            Boolean isLive();
        }


    }

    /**
     * Replicate a database
     * <p>
     * PouchDB.replicate(source, target, [options])
     * Replicate data from source to target. Both the source and target can be a string representing a CouchDB database url or the name a local PouchDB database. If options.live is true, then this will track future changes and also replicate them automatically.
     * <p>
     * If you want to sync data in both directions, you can call this twice, reversing the source and target arguments. Additionally, you can use PouchDB.sync().
     * <p>
     * Options
     * All options default to false unless otherwise specified.
     * <p>
     * options.filter: Reference a filter function from a design document to selectively get updates.
     * options.query_params: Query params sent to the filter function.
     * options.doc_ids: Only replicate docs with these ids.
     * options.complete: Function called when all changes have been processed.
     * options.onChange: Function called on each change processed.
     * options.live: If true, starts subscribing to future changes in the source database and continue replicating them.
     * options.since: Replicate changes after the given sequence number.
     * options.server: Initialize the replication on the server. The response is the CouchDB POST _replicate response and is different from the PouchDB replication response. Also, options.onChange is not supported on server replications.
     * options.create_target: Create target database if it does not exist. Only for server replications.
     * Example Usage:
     * <p>
     * PouchDB.replicate('mydb', 'http://localhost:5984/mydb', {
     * onChange: onChange,
     * complete: onComplete
     * });;
     * There are also shorthands for replication given existing PouchDB objects. These behave the same as PouchDB.replicate():
     * <p>
     * db.replicate.to(remoteDB, [options]);
     * // or
     * db.replicate.from(remoteDB, [options]);
     * Example Response:
     * <p>
     * {
     * 'ok': true,
     * 'docs_read': 2,
     * 'docs_written': 2,
     * 'start_time': "Sun Sep 23 2012 08:14:45 GMT-0500 (CDT)",
     * 'end_time': "Sun Sep 23 2012 08:14:45 GMT-0500 (CDT)",
     * 'status': 'complete',
     * 'errors': []
     * }
     * Note that the response for server replications (via options.server) is slightly different. See the CouchDB replication documentation for details.
     */
    interface Replication {
    }

    /**
     * Sync a database
     * <p>
     * var sync = PouchDB.sync(src, target, [options])
     * Sync data from src to target and target to src. This is a convience method for bidirectional data replication.
     * <p>
     * Options
     * Please refer to Replication for documention on options, as sync is just a convience method that entails bidirectional replication.
     * <p>
     * Example Usage:
     * <p>
     * PouchDB.sync('http://localhost:5984/mydb', {
     * onChange: onChange,
     * complete: onComplete
     * });;
     * There is also a shorthand for syncing given existing PouchDB objects. This behaves the same as PouchDB.sync():
     * <p>
     * db.sync(remoteDB, [options]);
     * For any further details, please further to Replication.
     */
    interface Sync {
        interface ReplicateOptions<T, A> {
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


        }
    }

    /**
     * Save an attachment
     * <p>
     * db.putAttachment(docId, attachmentId, rev, doc, type, [callback]);
     * Attaches a binary object to a document. Most of PouchDB's API deals with JSON, but if you're dealing with large binary data (such as PNGs), you may incur a performance or storage penalty if you simply include them as base64- or hex-encoded strings. In these cases, you can store the binary data as an attachment. For details, see the CouchDB documentation on attachments.
     * <p>
     * Example Usage:
     * <p>
     * var doc = new Blob(["It's a God awful small affair"]);
     * db.putAttachment('a', 'text', rev, doc, 'text/plain', function(err, res) {})
     * Example Response:
     * <p>
     * {
     * "ok": true,
     * "id": "otherdoc",
     * "rev": "2-068E73F5B44FEC987B51354DFC772891"
     * }
     * PouchDB also offers a createBlob function, which will work around browser inconsistencies:
     * <p>
     * var doc = PouchDB.utils.createBlob(["It's a God awful small affair"]);
     * Within Node, you must use a Buffer:
     * <p>
     * var doc = new Buffer("It's a God awful small affair");
     * For details, see the Mozilla docs on Blob or the Node docs on Buffer.
     * <p>
     * Save an inline attachment
     * You can also inline attachments inside the document. In this case, the attachment data must be supplied as a base64-encoded string:
     * <p>
     * {
     * '_id': 'otherdoc',
     * 'title': 'Legendary Hearts',
     * '_attachments': {
     * "text": {
     * "content_type": "text/plain",
     * "data": "TGVnZW5kYXJ5IGhlYXJ0cywgdGVhciB1cyBhbGwgYXBhcnQKT" +
     * "WFrZSBvdXIgZW1vdGlvbnMgYmxlZWQsIGNyeWluZyBvdXQgaW4gbmVlZA=="
     * }
     * }
     * }
     * See Inline Attachments on the CouchDB wiki for details.
     */
    interface SaveAttachment {
    }

    /**
     * Get an attachment
     * <p>
     * db.getAttachment(docId, attachmentId, [options], [callback])
     * Get attachment data.
     * <p>
     * Example Usage:
     * <p>
     * db.getAttachment('otherdoc', 'text', function(err, res) { });
     * In Node you get Buffers, and in the browser you get Blobs.
     * <p>
     * Inline attachments
     * You can specify attachments: true to most read operations. The attachment data will then be included inlined in the resulting list of docs.
     */
    interface GetAttachment {
    }

    /**
     * Delete an attachment
     * <p>
     * db.removeAttachment(docId, attachmentId, rev, [callback])
     * Delete an attachment from a doc.
     * <p>
     * Example Usage:
     * <p>
     * db.removeAttachment('otherdoc',
     * 'text',
     * '2-068E73F5B44FEC987B51354DFC772891',
     * function(err, res) { });
     * Example Response:
     * <p>
     * {
     * "ok": true,
     * "rev": "3-1F983211AB87EFCCC980974DFC27382F"
     * }
     */
    interface DeleteAttachment {
    }

    /**
     * Query the database
     * <p>
     * db.query(fun, [options], [callback])
     * Retrieve a view, which allows you to perform more complex queries on PouchDB. The CouchDB documentation for map reduce applies to PouchDB.
     * <p>
     * Options
     * All options default to false unless otherwise specified.
     * <p>
     * fun: Name of an existing view, the map function itself, or a full CouchDB-style mapreduce object: {map : ..., reduce: ...}.
     * options.reduce: Reduce function, or the string name of a built-in function: '_sum', '_count', or '_stats'. Defaults to false (no reduce).
     * Tip: if you're not using a built-in, you're probably doing it wrong.
     * options.include_docs: Include the document in each row in the doc field.
     * options.conflicts: Include conflicts in the _conflicts field of a doc.
     * options.attachments: Include attachment data.
     * options.startkey & options.endkey: Get documents with keys in a certain range (inclusive/inclusive).
     * options.descending: Reverse the order of the output documents.
     * options.key: Only return rows matching this string key.
     * options.keys: Array of string keys to fetch in a single shot.
     * Neither startkey nor endkey can be specified with this option.
     * The rows are returned in the same order as the supplied keys array.
     * The row for a deleted document will have the revision ID of the deletion, and an extra key "deleted":true in the value property.
     * The row for a nonexistent document will just contain an "error" property with the value "not_found".
     * For details, see the CouchDB query options documentation.
     * Example Usage:
     * <p>
     * function map(doc) {
     * if(doc.title) {
     * emit(doc.title, null);
     * }
     * }
     * <p>
     * db.query({map: map}, {reduce: false}, function(err, response) { });
     * Example Response:
     * <p>
     * {
     * "rows": [{
     * "id": "0B3358C1-BA4B-4186-8795-9024203EB7DD",
     * "key": "Cony Island Baby",
     * "value": null
     * }, {
     * "id": "otherdoc",
     * "key": "Legendary Hearts",
     * "value": null
     * }, {
     * "id": "828124B9-3973-4AF3-9DFD-A94CE4544005",
     * "key": "Lisa Says",
     * "value": null
     * }, {
     * "id": "mydoc",
     * "key": "Rock and Roll Heart",
     * "value": null
     * }]
     * }
     * If u pass a function to db.query and give it the emit function as the second argument, then you can use a closure. (Otherwise we have to use eval() to bind emit.)
     * <p>
     * // BAD! will throw error
     * var myId = 'foo';
     * db.query(function(doc) {
     * if (doc._id === myId) {
     * emit(doc);
     * }
     * }, function(err, results) {  ... });
     * <p>
     * // will be fine
     * var myId = 'foo';
     * db.query(function(doc, emit) {
     * if (doc._id === myId) {
     * emit(doc);
     * }
     * }, function(err, results) { ...});
     * You don't actuallly have to call them by those names, though:
     * <p>
     * var myId = 'foo';
     * db.query(function(thisIs, awesome) {
     * if (thisIs._id === myId) {
     * awesome(thisIs);
     * }
     * }, function(err, results) {... });
     * Notes:
     * <p>
     * Local databases do not currently support view caching; everything is a live view.
     * Linked documents (aka joins) are supported.
     * Complex keys are supported. Use them for fancy ordering (e.g. [firstName, lastName, isFemale]).
     * Closures are only supported by local databases. CouchDB still requires self-contained map/reduce functions.
     */
    interface QueryDb {
    }

    /**
     * Get database information
     * <p>
     * db.info(callback)
     * Get information about a database.
     * <p>
     * Example Usage:
     * <p>
     * db.info(function(err, info) { })
     * Example Response:
     * <p>
     * {
     * "db_name": "test",
     * "doc_count": 4,
     * "update_seq": 5
     * }
     */
    interface DbInfo {
    }

    /**
     * Compact the database
     * <p>
     * db.compact([options], [callback])
     * Runs compaction of the database. Fires callback when compaction is done. If you use the http adapter and have specified a callback, Pouch will ping the remote database in regular intervals unless the compaction is finished.
     * <p>
     * options.interval: Number of milliseconds Pouch waits before asking again if compaction is already done. Only for http adapter.
     */
    interface Compaction {
    }

    /**
     * Document revisions diff
     * <p>
     * db.revsDiff(diff, [callback])
     * Given a set of document/revision IDs, returns the subset of those that do not correspond to revisions stored in the database. Primarily used in replication.
     * <p>
     * Example Usage:
     * <p>
     * db.revsDiff({
     * myDoc1: [
     * "1-b2e54331db828310f3c772d6e042ac9c",
     * "2-3a24009a9525bde9e4bfa8a99046b00d"
     * ]
     * }, function (err, diffs) { });
     * Example Response:
     * <p>
     * {
     * "myDoc1": {
     * "missing": ["2-3a24009a9525bde9e4bfa8a99046b00d"]
     * }
     * }
     */
    interface RevisionDiff {
    }

    /**
     * Events
     * <p>
     * PouchDB is an event emiter and will emit a 'created' event when a database is created. A 'destroy' event is emited when a database is destroyed.
     * <p>
     * PouchDB.on('created', function (dbName) {
     * // called whenver a db is created.
     * });
     * PouchDB.on('destroyed', function (dbName) {
     * // called whenver a db is destroyed.
     * });
     */
    interface Events {
    }

    /**
     * Plugins
     * <p>
     * Writing a plugin is easy! The API is:
     * <p>
     * PouchDB.plugin({
     * methodName: myFunction
     * }
     * });
     * This will add the function as a method of all databases with the given method name. It will always be called in context, so that this always refers to the database object.
     * <p>
     * Example Usage:
     * <p>
     * PouchDB.plugin({
     * sayMyName : function () {
     * this.info().then(function (info)   {
     * console.log('My name is ' + info.db_name);
     * }).catch(function (err) { });
     * }
     * });
     * new PouchDB('foobar').sayMyName(); // prints "My name is foobar"
     */
    interface Plugins {
    }
}
