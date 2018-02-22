package com.github.thorbenkuck.netcom2.network.interfaces;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;

public interface Launch {

	/**
	 * Launches the implementation.
	 *
	 * This means, at this points, internal dependencies get calibrated and maybe created.
	 * In the case of the ClientStart/ServerStart, this means:
	 *
	 * if the ClientStart launches and the ServerStart is not yet launched, the ClientStart launch will fail.
	 *
	 * if the ServerStart launches and the specified port is already taken, the launch fails.
	 *
	 * @throws StartFailedException if any internal dependency could not be resolved
	 */
	void launch() throws StartFailedException;
}
