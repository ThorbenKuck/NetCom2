package com.github.thorbenkuck.netcom2.services;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.pipe.Pipeline;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;
import com.github.thorbenkuck.netcom2.utility.threaded.NetComThreadPool;

import java.io.IOException;
import java.net.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

class NativeServiceDiscoveryHub implements ServiceDiscoveryHub {

	private final Value<Integer> port;
	private final Value<Integer> targetPort;
	private final Pipeline<DiscoveryRequest> discoveryRequestPipeline = Pipeline.unifiedCreation();
	private final Pipeline<Header> headerPipeline = Pipeline.unifiedCreation();
	private final DiscoveredService service = new DiscoveredService();
	private final Value<DatagramSocket> udp = Value.emptySynchronized();
	private final Logging logging = Logging.unified();
	private String name = "NO_NAME";
	private Supplier<Boolean> serverReachable = service.running::get;

	NativeServiceDiscoveryHub(int port, int targetPort) {
		this.port = Value.synchronize(port);
		this.targetPort = Value.synchronize(targetPort);
		headerPipeline.addFirst((Consumer<Header>) header -> header.addEntry("SERVER_NAME", name));
		headerPipeline.addFirst((Consumer<Header>) header -> header.addEntry("TARGET", Integer.toString(this.targetPort.get())));
		headerPipeline.addFirst(header -> {
			if (serverReachable.get()) {
				header.addEntry("STATUS", "200");
			} else {
				header.addEntry("STATUS", "401");
				header.addEntry("MESSAGE", "Server is not reachable");
			}
		});
		logging.instantiated(this);
	}

	private void checkOpen(DiscoveryRequest request) {
		if (request.getSocket().isClosed()) {
			terminate();
			request.invalidate();
		}
	}

	private void pingBack(DiscoveryRequest request) {
		if (!request.isValid()) {
			logging.warn(">>> Request cannot be handled. Invalid request.");
			return;
		}
		try {
			DatagramSocket socket = request.getSocket();
			DatagramPacket received = request.getPacket();

			String toSend = new String(received.getData()).trim();

			Header header;
			synchronized (headerPipeline) {
				header = headerPipeline.apply(new Header());
			}
			toSend += ";" + header.toString();

			DatagramPacket packet = new DatagramPacket(toSend.getBytes(), toSend.getBytes().length, received.getAddress(), received.getPort());
			socket.send(packet);
		} catch (IOException e) {
			logging.catching(e);
		}
	}

	@Override
	public void addHeaderEntry(Consumer<Header> headerConsumer) {
		synchronized (headerPipeline) {
			headerPipeline.addLast(headerConsumer);
		}
	}

	@Override
	public void listenBlocking() throws SocketException, InterruptedException {
		listen();
		Thread.currentThread().join();
	}

	@Override
	public void listen() throws SocketException {
		DatagramSocket datagramSocket;
		try {
			logging.info(">>> Listening for ServiceDiscoveryRequests on port " + port.get() + " with the target port being " + targetPort.get());
			datagramSocket = new DatagramSocket(port.get(), InetAddress.getByName("0.0.0.0"));
		} catch (UnknownHostException e) {
			throw new SocketException(">>> DatagramSocket could not be created");
		}
		datagramSocket.setBroadcast(true);
		udp.set(datagramSocket);

		synchronized (discoveryRequestPipeline) {
			discoveryRequestPipeline.addFirst(this::checkOpen);
			discoveryRequestPipeline.addLast(this::pingBack);
		}

		NetComThreadPool.submitCustomProcess(service);
	}

	@Override
	public void terminate() {
		service.stop();
	}

	@Override
	public int getPort() {
		return port.get();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String hubName) {
		NetCom2Utils.parameterNotNull(hubName);
		this.name = hubName;
	}

	@Override
	public void onDiscoverRequest(Consumer<DiscoveryRequest> requestConsumer) {
		synchronized (discoveryRequestPipeline) {
			discoveryRequestPipeline.addFirst(requestConsumer);
		}
	}

	@Override
	public void connect(ServerStart serverStart) {
		NetCom2Utils.parameterNotNull(serverStart);
		targetPort.set(serverStart.getPort());
		serverReachable = serverStart::running;
	}

	private final class DiscoveredService implements Runnable {

		private final Value<Boolean> running = Value.synchronize(true);

		@Override
		public void run() {
			DatagramSocket socket = udp.get();

			while (running.get()) {
				byte[] receiveBuf = new byte[15000];
				DatagramPacket packet = new DatagramPacket(receiveBuf, receiveBuf.length);
				try {
					logging.debug(">>> Awaiting communication request");
					socket.receive(packet);
					logging.debug(">>> Received new packet");
				} catch (IOException e) {
					logging.catching(e);
				}

				DiscoveryRequest request = new DiscoveryRequest(packet, socket);

				logging.debug(">>> Informing Pipeline");
				synchronized (discoveryRequestPipeline) {
					discoveryRequestPipeline.apply(request);
				}
			}
		}

		void stop() {
			running.set(false);
		}
	}
}
