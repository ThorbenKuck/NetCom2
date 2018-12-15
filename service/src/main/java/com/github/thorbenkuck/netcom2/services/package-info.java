/**
 * This package provides a ServiceLocation method.
 * <p>
 * It allows you to find any NetCom2 ServerStart in a local area network. The Requirement for that is, that you
 * start a ServiceDiscoveryHub on the ServerStart side, which points to the ServerStart-Port.
 * <p>
 * And with that, you are golden. The methods used are asynchronous and require one WorkerTask each (aka one Thread).
 */
package com.github.thorbenkuck.netcom2.services;