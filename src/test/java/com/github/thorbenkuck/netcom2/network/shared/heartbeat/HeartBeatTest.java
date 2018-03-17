package com.github.thorbenkuck.netcom2.network.shared.heartbeat;

import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class HeartBeatTest {

	private final AtomicInteger counts = new AtomicInteger(0);
	private int i = 1;
	private HeartBeatFactory heartBeatFactory = HeartBeatFactory.get();

	private void proceed(Object o) {
		i++;
		counts.incrementAndGet();
	}

	@Test
	public void testHeartBeatChain() throws Exception {
		HeartBeat<String> heartBeat = heartBeatFactory.produce();
		counts.set(0);

		heartBeat.configure()
				.tickRate()
				.times(3)
				.in(1, TimeUnit.SECONDS)
				.and()
				.run()
				.setAction(this::proceed)
				.and()
				.run()
				.until(o -> i > 10)
				.and()
				.run()
				.onlyIf(o -> {
					if (i % 3 != 0) {
						i++;
					}
					return i % 3 == 0;
				})
				.then()
				.run("Test");

		assertEquals(3, counts.get());
	}

	@Test
	public void testHeartBeatChain1() throws Exception {
		HeartBeat<String> heartBeat = heartBeatFactory.produce();
		counts.set(0);

		heartBeat.configure()
				.run()
				.onlyIf(o -> {
					if (i == 10) {
						i++;
					}
					return i != 10;
				})
				.and()
				.run()
				.until(object -> i == 13)
				.and()
				.tickRate()
				.times(10)
				.in(1, TimeUnit.SECONDS)
				.then()
				.run("Das ist ein Test", this::proceed);

		assertEquals(11, counts.get());
	}

}
