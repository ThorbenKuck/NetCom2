package com.github.thorbenkuck.netcom2.network.interfaces;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;

/**
 * This interface abstracts the implementing interface, to allow it to have a launch() method.
 *
 * This launch method is defining a {@link StartFailedException} to be thrown if anything goes wrong
 *
 * @version 1.0
 * @since 1.0
 */
public interface Launch {

	/**
	 * Launches the implementation.
	 * <p>
	 * This means, at this points, internal dependencies get calibrated and maybe created.
	 * In the case of the ClientStart/ServerStart, this means:
	 * <p>
	 * if the ClientStart launches and the ServerStart is not yet launched, the ClientStart launch will fail.
	 * <p>
	 * if the ServerStart launches and the specified port is already taken, the launch fails.
	 *
	 * @throws StartFailedException if any internal dependency could not be resolved
	 */
	void launch() throws StartFailedException;
}
