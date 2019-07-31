package com.github.thorbenkuck.netcom2.integration;

import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;

public class GenericTest {

	public static void main(String[] args) {
		CommunicationRegistration communicationRegistration = CommunicationRegistration.open();
		CommunicationRegistration communicationRegistration2 = CommunicationRegistration.open();
		communicationRegistration.updateBy(communicationRegistration2);
	}

	private static <T> Mapper<T> map(Class<T> type) {
		return new Mapper<>();
	}

	private static class Mapper<T> {
		public void to(Class<? super T> superType) {

		}
	}

	private static class SuperMessage {

	}

	private static class MessageA extends SuperMessage {

	}
}
