MicroThreading
==============

A system of 'light' threading, where objects implementing the interface can be added to a queue and run as and when
there are resources free to do so. Note there is no guarantee when the methods will be run, only that they will
be run at some point in time.


 * Conceptually, the idea is to allow a simpler version of threading that can split a load of multiple 
 * objects that require use of a thread over a single thread, to enable better load balancing.
