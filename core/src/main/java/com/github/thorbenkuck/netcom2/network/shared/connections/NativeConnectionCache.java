package com.github.thorbenkuck.netcom2.network.shared.connections;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.logging.Logging;

final class NativeConnectionCache implements ConnectionCache {

	private static final byte[] EMPTY_BYTES = new byte[0];
	private final Value<byte[]> bytesValue = Value.synchronize(EMPTY_BYTES);
	private final Logging logging = Logging.unified();

	NativeConnectionCache() {
		logging.instantiated(this);
	}

	private byte[] concatenate(final byte[] a, final byte[] b) {
		int aLen = a.length;
		int bLen = b.length;

		byte[] result = new byte[aLen + bLen];

		System.arraycopy(a, 0, result, 0, aLen);
		System.arraycopy(b, 0, result, aLen, bLen);

		return result;
	}

	private synchronized void appendToValue(final byte[] bytes) {
		final byte[] currentByte = bytesValue.get();
		final byte[] newValue = concatenate(currentByte, bytes);

		bytesValue.set(newValue);
	}

	private synchronized byte[] getAndClearFromValue() {
		final byte[] result = bytesValue.get();
		bytesValue.set(EMPTY_BYTES);
		return result;
	}

	@Override
	public final void append(final byte[] bytes) {
		appendToValue(bytes);
	}

	@Override
	public final byte[] take() {
		return getAndClearFromValue();
	}
}
