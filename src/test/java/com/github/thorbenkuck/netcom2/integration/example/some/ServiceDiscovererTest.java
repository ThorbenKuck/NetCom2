package com.github.thorbenkuck.netcom2.integration.example.some;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.services.ServiceDiscoverer;

import java.net.SocketException;
import java.util.concurrent.TimeUnit;

public class ServiceDiscovererTest {

	public static void main(String[] args) {
		ServiceDiscoverer discoverer = ServiceDiscoverer.open(8888);

		discoverer.addHeaderMapping("TIME_RUNNING", (s, discoveryProcessingRequest) -> {
			String value = discoveryProcessingRequest.header().get("TIME_RUNNING");
			long timeRunning = Long.valueOf(value);
			return TimeUnit.MILLISECONDS.toSeconds(timeRunning) >= 5;
		});

		discoverer.onDiscover(serviceHubLocation -> {
			System.out.println("Found a ServiceHub!");
			ClientStart clientStart = serviceHubLocation.toClientStart();
			try {
				clientStart.launch();
			} catch (StartFailedException e) {
				e.printStackTrace();
			}

			discoverer.close();
		});

		try {
			discoverer.findServiceHubs();
		} catch (SocketException e) {
			e.printStackTrace();
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
