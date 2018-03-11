package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Semaphore;

/**
 * This Class contains information about requests to the Server.
 * <p>
 * Important is, that any request send, has an unique {@link UUID}. So any invocation handler will lead to the same result
 * <p>
 * If you have an {@link com.github.thorbenkuck.netcom2.annotations.remoteObjects.SingletonRemoteObject}, the UUID will be
 * the same, so that the any call will wait, no matter what.
 */
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
	 * To add a new Semaphore, if non exists, use  {@link #getAndCreateSemaphore(UUID)}
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

	@APILevel
	void clearSemaphore(UUID uuid) {
		synchronized (semaphoreMap) {
			semaphoreMap.remove(uuid);
		}
	}

	@APILevel
	void clearResult(UUID uuid) {
		synchronized (responseMap) {
			responseMap.remove(uuid);
		}
	}

	@APILevel
	Semaphore await(RemoteAccessCommunicationRequest request) {
		Semaphore semaphore = getAndCreateSemaphore(request.getUuid());
		responseMap.remove(request.getUuid());

		return semaphore;
	}

	@APILevel
	void release(RemoteAccessCommunicationResponse response) {
		synchronized (responseMap) {
			responseMap.put(response.getUuid(), response);
		}
		Semaphore semaphore = getSemaphore(response.getUuid());

		if (semaphore == null) {
			return;
		}

		semaphore.release();
	}

	@APILevel
	RemoteAccessCommunicationResponse getResponse(UUID uuid) {
		synchronized (responseMap) {
			return responseMap.get(uuid);
		}
	}

	@APILevel
	int countSemaphores() {
		synchronized (semaphoreMap) {
			return semaphoreMap.size();
		}
	}

	@APILevel
	int countResponses() {
		synchronized (responseMap) {
			return responseMap.size();
		}
	}
}
