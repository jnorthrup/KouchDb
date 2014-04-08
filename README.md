KouchDb
=============

    KouchDb aims to be the junction between btrfs kernel, boost asio, java NIO[2], and couchdb for deployment of rest-based services.

Features
========
 * a wire compatible couch entity
 * implementation of websocket sessions and async channels
 * protocol buffer mailbox internals
 * a dual language (twin) implementation
    *  a java NIO2 async implementation to leverage the particular strengths of java, and more narrowly defined GWT IO built-ins
    *  a c++14 boost/asio/std-http implementation to frame "big data" usecases that starve on erlang io.

