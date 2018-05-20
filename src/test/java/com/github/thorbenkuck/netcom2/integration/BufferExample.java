package com.github.thorbenkuck.netcom2.integration;

import com.github.thorbenkuck.netcom2.network.shared.DynamicBuffer;

import java.util.Arrays;

public class BufferExample {

	public static void main(String[] args) {
		DynamicBuffer buffer = new DynamicBuffer();

		byte b1 = 98;
		byte b2 = 99;
		byte b3 = 100;

		buffer.append(b1);
		buffer.append(b2);
		buffer.append(b3);

		System.out.println(buffer.size());
		System.out.println(Arrays.toString(buffer.array()));

		byte[] array = new byte[]{101, 102, 103};

		buffer.append(array);

		System.out.println(buffer.size());
		System.out.println(Arrays.toString(buffer.array()));
	}

}
