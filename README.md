KouchDb
======= 
    KouchDb aims to be the junction between kernel VFS, boost asio, java NIO[2], and couchdb for deployment of
    REST-based services.

some goals:
===
 - [ ] take a string, a key or an expression, and return bytes in a struct
 - [ ] create a pattern matcher for keys to specialize the handler.
 - [ ] implement byte-ranges for unspecialized byte returns

some couchdb nice-to-have goals:
===
 - [ ] couchdb flavor tables should implement http://docs.couchdb.org/en/latest/api/database/index.html 
 - [ ] pouchdb proto has been crafted for asyncronous channelized pouch API messages suitable for e.g. websocket

some pragmatic persistence:
===
 - [ ] implement pattern matchers that provide key-value request of (anything as simple as) camel beans, prautobeans, eventually proto
 - [ ] implement attachment pattern matchers that make all keys have a couch-like blob attachment fs subdir
 - [ ] implement view pattern matchers that parse expressions as tablescan filters

some things to simplify java k-v nosql development:
===
 - [ ] implement a camel code generator to build a server from xml spring beans dialect to serve up tables with the kitchen sink 
 - [ ] implement a baremetal 1xio hermetic implementation to benchmark against a given camel pipeline
 - [ ] implement prautobeans over channelized websockets in both above
 
