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
 
longer terms goals:
===
 - [ ] single-threaded network io shards with a fair dispatch interceptor per process - this worked for RelaxFactory to be as fast as the multi-threaded big boys without changing the simplest-possible NIO select FSM 
 - [ ] bonding nodes for n>1 record storage across a collection of shards
 - [ ] online shard reconfiguration - "noisey mode" for each permutation of key, value, and paired storage, shard should create a new packed index and from the slots it has and scatter-gather the slots it doesn't have.
 - [ ] add riak and mongo flavors.  emulate casandra with c in java, see where it leads.  
