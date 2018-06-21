package com.github.thorbenkuck.netcom2.network.interfaces;

/**
 * This interface defines what to do, if a new physical Client connected.
 * <p>
 * It defines multiple things, like:
 * <p>
 * <ul>
 * <li>handle a newly created client instance</li>
 * <li>create a new client instance for the physical Client.<br>This would be used, if you wanted to provide a custom
 * Client object</li>
 * </ul>
 * <p>
 * If you use this ClientConnectedHandler as a lambda, you will inevitably override the handle method.
 *
 * @version 1.0
 * @since 1.0
 * @deprecated This class will be removed with V.1.2. The new Design is decoupled from Sockets. All Methods have been removed
 */
@Deprecated
public interface ClientConnectedHandler {
}
