package com.nhb.common.utils;

import java.nio.ByteBuffer;
import java.util.UUID;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;

public class UuidUtils {

	private static final NoArgGenerator timeBasedGenerator = Generators.timeBasedGenerator();

	public static UUID randomUuid() {
		return UUID.randomUUID();
	}

	public static UUID timebasedUuid() {
		return timeBasedGenerator.generate();
	}

	public static byte[] timebasedUuidAsBytes() {
		return uuidToBytes(timebasedUuid());
	}

	public static byte[] randomUuidAsBytes() {
		return uuidToBytes(randomUuid());
	}

	public static String timebasedUuidAsString() {
		return timebasedUuid().toString();
	}

	public static String randomUuidAsString() {
		return randomUuid().toString();
	}

	public static byte[] uuidToBytes(UUID uuid) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return bb.array();
	}

	public static byte[] uuidToBytes(String uuidString) {
		return uuidToBytes(UUID.fromString(uuidString));
	}

	public static UUID bytesToUUID(byte[] bytes) {
		if (bytes.length != 16) {
			throw new IllegalArgumentException();
		}
		int i = 0;
		long msl = 0;
		for (; i < 8; i++) {
			msl = (msl << 8) | (bytes[i] & 0xFF);
		}
		long lsl = 0;
		for (; i < 16; i++) {
			lsl = (lsl << 8) | (bytes[i] & 0xFF);
		}
		return new UUID(msl, lsl);
	}

	public static String bytesToUuidString(byte[] bytes) {
		return bytesToUUID(bytes).toString();
	}
}
