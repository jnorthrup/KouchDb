KouchDb
======= 
    KouchDb aims to be the junction between kernel VFS, boost asio, java NIO[2], and couchdb for deployment of
    REST-based services.

    Driven by type-safe language and source-to-source deployment experiences in-the-trenches to deliver a thin and
    robust platform for browser usecases with plain and obviously scale-up options.

Features
========
 * a wire compatible couch entity
 * implementation of websocket sessions and async channels
    * protocol buffer mailbox internals
 * PrautoBeans (Done, see https://github.com/jnorthrup/prautobeans)
   * autobean compatible interfaces from src/main/proto
 * a dual language (twin) implementation
    *  a java NIO2 async implementation to leverage the particular strengths of java, and more narrowly defined GWT IO built-ins
    *  a c++14 boost/asio/std-http implementation to frame "big data" usecases that starve on erlang io.
    *  one big happy build system and shared data structs. (protobuffs, javolution structs, yaml, etc.)
 * hierachy-as-db, REST as access
    * dirs-as-db's.
    * docs as json files
    * attachments as binary files


Roadmap
========
 * deferring storage strategies to the underlying filesystem
    * any log-structured fs can provide Append-Only, time-series and rigorous versioning
    * BTRFS could enable COW, Compression, snapshotting, n-copies, defrag, subvol-per-table, etc.
    * read-only-compressed volumes as db's.  why not?  tar, zip, squish, etc.
    * git-as-backing-store has to count for some interesting experiments.
 * micro and macroscopic indexing
    * for footprint savings defer btrees and checksumming to VFS if these are user-space accessable
    * plug in stxxl and hat-tries for in-memory indexes where load warrants
    * inverted indexes and database agnostic lucene configurations as simple as shell scripts or maven additions.
 *  btrfs send/receive deltas add an interesting dimension to existing master-master and cluster wire options
 *  out-of-band fnotify and on inodes-as-docs
 *  gwt-centric "_design" analogs
    * upload/edit/sync an attachment hierarchy for gwtc compiled apps
    * make js-usable clientbundles for spriting and data: urls
 *  type-safe tables
    * while having a simplest-possible json directory full of records is easy and sounds like a fantastic default, having a specific schema from Prautobeans can enable mmap as a performance option on feeding mapreduce views or storing them and serving them.
    * likewise attribute level REST/Websocket methods could be enabled by Prautobean in addition to json document level methods.
