package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationModelRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RemoteAccessCommunicationModelResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Semaphore;

class RemoteAccessBlockRegistration {

	private final Map<UUID, Semaphore> semaphoreMap = new HashMap<>();
	private final Map<UUID, RemoteAccessCommunicationModelResponse> responseMap = new HashMap<>();
	private final Logging logging = Logging.unified();

	private Semaphore getAndCreateSemaphore(UUID uuid) {
		Semaphore semaphore;
		synchronized (semaphoreMap) {
			semaphore = semaphoreMap.get(uuid);
		}

		if(semaphore == null) {
			semaphore = new Semaphore(1);
		}

		synchronized (semaphoreMap) {
			semaphoreMap.put(uuid, semaphore);
		}

		semaphore = getSemaphore(uuid);

		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			logging.catching(e);
		}

		return semaphore;
	}

	private Semaphore getSemaphore(UUID uuid) {
		Semaphore semaphore;

		synchronized (semaphoreMap) {
			semaphore = semaphoreMap.get(uuid);
		}

		return semaphore;
	}

	public void clearSemaphore(UUID uuid) {
		synchronized (semaphoreMap) {
			semaphoreMap.remove(uuid);
		}
	}

	public void clearResult(UUID uuid) {
		synchronized (responseMap) {
			responseMap.remove(uuid);
		}
	}

	public Semaphore await(RemoteAccessCommunicationModelRequest request) {
		Semaphore semaphore = getAndCreateSemaphore(request.getUuid());
		responseMap.remove(request.getUuid());

		return semaphore;
	}

	public void release(RemoteAccessCommunicationModelResponse response) {
		synchronized (responseMap) {
			responseMap.put(response.getUuid(), response);
		}
		Semaphore semaphore = getSemaphore(response.getUuid());

		if(semaphore == null) {
			return;
		}

		semaphore.release();
	}

	public RemoteAccessCommunicationModelResponse getResponse(UUID uuid) {
		synchronized (responseMap) {
			return responseMap.get(uuid);
		}
	}
}
