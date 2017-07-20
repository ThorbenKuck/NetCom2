package test;

import com.github.thorbenkuck.netcom2.network.shared.heartbeat.HeartBeat;
import com.github.thorbenkuck.netcom2.network.shared.heartbeat.HeartBeatFactory;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class HeartBeatTest {

	private int i = 1;
	private HeartBeatFactory heartBeatFactory = HeartBeatFactory.get();

	@Test
	public void testHeartBeatChain() throws Exception {
		HeartBeat<String> heartBeat = heartBeatFactory.produce();

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

		i = 1;
		System.out.println("\n----\n");


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
	}

	private void proceed(Object o) {
		System.out.println(i + ": " + o);
		i++;
	}

}
