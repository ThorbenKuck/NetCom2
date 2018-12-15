package com.github.thorbenkuck.netcom2.services;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.pipe.Pipeline;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.utility.threaded.NetComThreadPool;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

final class NativeServiceDiscoverer implements ServiceDiscoverer {

	private static final String REQUEST_MESSAGE = "NET_COM_SERVICE_DISCOVER_REQUEST";
	private final ServiceFinderProcess process = new ServiceFinderProcess();
	private final Pipeline<ServiceHubLocation> receivedPipeline = Pipeline.unifiedCreation();
	private final Value<DatagramSocket> outputValue = Value.emptySynchronized();
	private final Logging logging = Logging.unified();
	private final Map<String, BiFunction<String, DiscoveryProcessingRequest, Boolean>> headerMapping = new HashMap<>();
	private final int port;

	NativeServiceDiscoverer(int port) {
		this.port = port;
		synchronized (headerMapping) {
			headerMapping.put("STATUS", (string, request) -> {
				if (!"200".equals(string)) {
					logging.info(">>> Message: " + string + " " + request.header().get("MESSAGE"));
					return false;
				}
				return true;
			});
			headerMapping.put("TARGET", (string, request) -> {
				try {
					logging.debug("Updating target port");
					request.setPort(Integer.parseInt(string));
					return true;
				} catch (NumberFormatException e) {
					logging.catching(e);
					return false;
				}
			});
			headerMapping.put("SERVER_NAME", (string, request) -> {
				logging.debug("Setting ServerName");
				request.setHubName(string);
				return true;
			});
		}
		logging.instantiated(this);
	}

	private BiFunction<String, DiscoveryProcessingRequest, Boolean> wrapHeaderConsumer(BiConsumer<String, DiscoveryProcessingRequest> headerProcessor) {
		return new ConsumerWrapper(headerProcessor);
	}

	@Override
	public void addHeaderMapping(String headerType, BiConsumer<String, DiscoveryProcessingRequest> headerProcessor) {
		synchronized (headerMapping) {
			headerMapping.put(headerType, wrapHeaderConsumer(headerProcessor));
		}
	}

	@Override
	public void addHeaderMapping(String headerType, BiFunction<String, DiscoveryProcessingRequest, Boolean> headerProcessor) {
		synchronized (headerMapping) {
			headerMapping.put(headerType, headerProcessor);
		}
	}

	@Override
	public void onDiscover(Consumer<ServiceHubLocation> locationConsumer) {
		synchronized (receivedPipeline) {
			receivedPipeline.add(locationConsumer);
		}
	}

	@Override
	public void findServiceHubs() throws SocketException {
		DatagramSocket output = new DatagramSocket();
		output.setBroadcast(true);
		outputValue.set(output);

		NetComThreadPool.submitCustomProcess(process);

		byte[] sendData = REQUEST_MESSAGE.getBytes();

		try {
			logging.debug(">>> Searching for ServiceDiscoveryHubs with open port " + port);
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), port);
			output.send(sendPacket);
			logging.info(">>> Request packet sent to: 255.255.255.255 (DEFAULT)");
		} catch (Exception e) {
			logging.catching(e);
		}

		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

		logging.debug(">>> Broadcasting message over all network interfaces");
		while (interfaces.hasMoreElements()) {
			NetworkInterface networkInterface = interfaces.nextElement();
			if (networkInterface.isLoopback() || !networkInterface.isUp()) {
				logging.trace(">>> Ignoring Loopback interface");
				continue;
			}
			for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
				InetAddress broadcast = interfaceAddress.getBroadcast();
				if (broadcast == null) {
					continue;
				}

				try {
					logging.debug(">>> Sending request message ..");
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 8888);
					output.send(sendPacket);
				} catch (Exception e) {
					logging.catching(e);
				}
				logging.info(">>> Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
			}
		}

	}

	@Override
	public void close() {
		process.stop();
	}

	private final class ConsumerWrapper implements BiFunction<String, DiscoveryProcessingRequest, Boolean> {

		private final BiConsumer<String, DiscoveryProcessingRequest> consumer;

		private ConsumerWrapper(BiConsumer<String, DiscoveryProcessingRequest> consumer) {
			this.consumer = consumer;
		}

		@Override
		public Boolean apply(String s, DiscoveryProcessingRequest discoveryProcessingRequest) {
			try {
				consumer.accept(s, discoveryProcessingRequest);
				return true;
			} catch (Throwable t) {
				logging.catching(t);
				return false;
			}
		}
	}

	private final class ServiceFinderProcess implements Runnable {

		private final Value<Boolean> running = Value.synchronize(true);

		private ServiceHubLocation parseHeadEntries(Header header, int port, InetAddress address) {
			DiscoveryProcessingRequest request = new DiscoveryProcessingRequest(header);
			request.setPort(port);
			request.setAddress(address);

			for (Header.Entry entry : header) {
				BiFunction<String, DiscoveryProcessingRequest, Boolean> converter;
				synchronized (headerMapping) {
					converter = headerMapping.get(entry.key());
				}

				if (converter == null) {
					logging.warn(">>> Unhandled Header-Entry: " + entry);
					continue;
				}

				try {
					boolean result = converter.apply(entry.value(), request);
					if (!result) {
						logging.warn(">>> Header Entry marked as faulty: " + entry + ". This implies no Location found.");
						return null;
					}
				} catch (Throwable t) {
					logging.error(">>> Function throw unexpected Throwable! Not tolerable, removing mapping for said header!", t);
					synchronized (headerMapping) {
						headerMapping.remove(entry.key());
					}
					logging.warn(">>> After unexpected Throwable, returning null because of faulty header.");
					return null;
				}
			}

			return request.toServiceHubLocation();
		}

		private ServiceHubLocation parseMessage(String message, int port, InetAddress address) {
			String[] messageParts = message.split(";");
			if (messageParts.length == 2) {
				String originalRequest = messageParts[0];
				String rawResponseHeader = messageParts[1];
				if (!originalRequest.equals(REQUEST_MESSAGE)) {
					logging.warn(">>> Received faulty message ping: " + message);
					return null;
				}

				Header header = new Header(rawResponseHeader);
				logging.debug(">>> " + header);

				return parseHeadEntries(header, port, address);
			}


			logging.warn(">>> Unknown response structure");

			return null;
		}

		private ServiceHubLocation awaitPingBack(DatagramPacket receivePacket) throws IOException {
			logging.debug(">>> Awaiting broadcast response");
			outputValue.get().receive(receivePacket);

			logging.info(">>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress());
			String message = new String(receivePacket.getData()).trim();

			logging.debug(">>> Received message " + message);
			return parseMessage(message, receivePacket.getPort(), receivePacket.getAddress());
		}

		@Override
		public void run() {
			logging.debug(">>> Starting to listen for broadcast responses ..");
			while (running.get()) {
				byte[] buffer = new byte[15000];

				DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
				try {
					ServiceHubLocation serviceHubLocation = awaitPingBack(receivePacket);
					if (serviceHubLocation != null) {
						logging.debug(">>> Informing about ServiceHubLocation");
						synchronized (receivedPipeline) {
							receivedPipeline.apply(serviceHubLocation);
						}
					} else {
						logging.warn(">>> Unknown broadcast response. Ignoring ..");
					}
				} catch (IOException e) {
					logging.catching(e);
				}
			}
			logging.debug(">>> Finished listening for broadcast responses ..");
		}

		public void stop() {
			running.set(false);
			outputValue.get().close();
		}
	}
}
