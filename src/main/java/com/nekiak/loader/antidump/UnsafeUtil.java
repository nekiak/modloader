package com.nekiak.loader.antidump;

import sun.misc.Unsafe;
import java.lang.reflect.Field;

public class UnsafeUtil {
	private static Unsafe unsafe;

	static {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (Unsafe) field.get(null);
		} catch (Exception e) {
			throw new RuntimeException("Failed to access Unsafe", e);
		}
	}

	public static Unsafe getUnsafe() {
		return unsafe;
	}
}
