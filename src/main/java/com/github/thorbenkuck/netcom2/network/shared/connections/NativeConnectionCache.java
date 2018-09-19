package com.github.thorbenkuck.netcom2.network.shared.connections;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.logging.Logging;

class NativeConnectionCache implements ConnectionCache {

	private static final byte[] EMPTY_BYTES = new byte[0];
	private final Value<byte[]> bytesValue = Value.synchronize(EMPTY_BYTES);
	private final Logging logging = Logging.unified();

	NativeConnectionCache() {
		logging.instantiated(this);
	}

	private byte[] concatenate(byte[] a, byte[] b) {
		int aLen = a.length;
		int bLen = b.length;

		byte[] result = new byte[aLen + bLen];

		System.arraycopy(a, 0, result, 0, aLen);
		System.arraycopy(b, 0, result, aLen, bLen);

		return result;
	}

	private synchronized void appendToValue(byte[] bytes) {
		byte[] currentByte = bytesValue.get();
		byte[] newValue = concatenate(currentByte, bytes);

		bytesValue.set(newValue);
	}

	private synchronized byte[] getAndClearFromValue() {
		byte[] result = bytesValue.get();
		bytesValue.set(EMPTY_BYTES);
		return result;
	}

	@Override
	public void append(byte[] bytes) {
		appendToValue(bytes);
	}

	@Override
	public byte[] take() {
		return getAndClearFromValue();
	}
}
