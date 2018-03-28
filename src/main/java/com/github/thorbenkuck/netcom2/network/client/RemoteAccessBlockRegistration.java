package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.annotations.Tested;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationResponse;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Semaphore;

/**
 * This Class contains information about requests to the Server.
 * <p>
 * Important is, that any request sent, has a unique {@link UUID}. So any invocation handler will lead to the same result
 * <p>
 * If you have a {@link com.github.thorbenkuck.netcom2.annotations.rmi.SingletonRemoteObject}, the UUID will be
 * the same, so that the any call will wait, no matter what.
 *
 * @version 1.0
 * @since 1.0
 */
@Synchronized
@Tested(responsibleTest = "com.github.thorbenkuck.netcom2.network.client.RemoteAccessBlockRegistrationTest")
public class RemoteAccessBlockRegistration {

	private final Map<UUID, Semaphore> semaphoreMap = new HashMap<>();
	private final Map<UUID, RemoteAccessCommunicationResponse> responseMap = new HashMap<>();
	private final Logging logging = Logging.unified();

	/**
	 * This Method will fetch an existing Semaphore out of the internal Mapping, identified by the UUID
	 * <p>
	 * If the Semaphore does not exist, it will create on.
	 *
	 * @param uuid the Identifier of the Semaphore
	 * @return the identified Semaphore
	 */
	private Semaphore getAndCreateSemaphore(UUID uuid) {
		Semaphore semaphore;
		synchronized (semaphoreMap) {
			semaphoreMap.computeIfAbsent(uuid, key -> new Semaphore(1));
		}

		semaphore = getSemaphore(uuid);

		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			logging.catching(e);
		}

		return semaphore;
	}

	/**
	 * Returns the saved instance, identified by UUID.
	 * <p>
	 * To add a new Semaphore, if none exists, use  {@link #getAndCreateSemaphore(UUID)}
	 *
	 * @param uuid the identifier
	 * @return the saved Semaphore
	 * @see #getAndCreateSemaphore(UUID)
	 */
	private Semaphore getSemaphore(UUID uuid) {
		Semaphore semaphore;

		synchronized (semaphoreMap) {
			semaphore = semaphoreMap.get(uuid);
		}

		return semaphore;
	}

	/**
	 * Removes a set Semaphore for the provided UUID.
	 *
	 * @param uuid the UUID, which identifies the Semaphore
	 */
	@APILevel
	void clearSemaphore(UUID uuid) {
		NetCom2Utils.parameterNotNull(uuid);
		synchronized (semaphoreMap) {
			semaphoreMap.remove(uuid);
		}
	}

	/**
	 * Removes the set Results for the provided UUID.
	 *
	 * @param uuid the UUID, which identifies the results.
	 */
	@APILevel
	void clearResult(UUID uuid) {
		NetCom2Utils.parameterNotNull(uuid);
		synchronized (responseMap) {
			responseMap.remove(uuid);
		}
	}

	/**
	 * Returns a Semaphore, which is linked to the UUID, provided by the <code>request</code>
	 * <p>
	 * The returned Semaphore will be already acquired, therefore calling {@link Semaphore#acquire()}, will instantly block.
	 * Once {@link #release(RemoteAccessCommunicationResponse)} is called, this Semaphore will be released and the waiting
	 * instance will get access to the Semaphore.
	 *
	 * @param request the Request, that will be send over the network
	 * @return an acquired Semaphore, that will block until the response is received.
	 */
	@APILevel
	Semaphore await(RemoteAccessCommunicationRequest request) {
		NetCom2Utils.parameterNotNull(request);
		Semaphore semaphore = getAndCreateSemaphore(request.getUuid());
		responseMap.remove(request.getUuid());

		return semaphore;
	}

	/**
	 * Releases the Semaphore, identified with the UUID within the response.
	 *
	 * @param response the Response, received from the ServerStart.
	 */
	@APILevel
	void release(RemoteAccessCommunicationResponse response) {
		NetCom2Utils.parameterNotNull(response);
		synchronized (responseMap) {
			responseMap.put(response.getUuid(), response);
		}
		Semaphore semaphore = getSemaphore(response.getUuid());

		if (semaphore == null) {
			return;
		}

		semaphore.release();
	}

	/**
	 * Returns a set {@link RemoteAccessCommunicationResponse}.
	 * <p>
	 * This might be null, since it depends on some other instance previously setting this response.
	 *
	 * @param uuid the Identifier
	 * @return the set response.
	 */
	@APILevel
	RemoteAccessCommunicationResponse getResponse(UUID uuid) {
		NetCom2Utils.parameterNotNull(uuid);
		synchronized (responseMap) {
			return responseMap.get(uuid);
		}
	}

	/**
	 * returns how many semaphores are held within this BlockRegistration
	 *
	 * @return the number of all semaphores
	 */
	@APILevel
	int countSemaphores() {
		synchronized (semaphoreMap) {
			return semaphoreMap.size();
		}
	}

	/**
	 * returns how many responses are held within this BlockRegistration
	 *
	 * @return the number of all responses
	 */
	@APILevel
	int countResponses() {
		synchronized (responseMap) {
			return responseMap.size();
		}
	}
}
