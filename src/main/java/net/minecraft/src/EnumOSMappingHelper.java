package net.minecraft.src;

public class EnumOSMappingHelper {
	public static final int[] field_1585_a = new int[EnumOS2.values().length];

	static {
		try {
			field_1585_a[EnumOS2.windows.ordinal()] = 1;
		} catch (NoSuchFieldError var4) {
		}

		try {
			field_1585_a[EnumOS2.macos.ordinal()] = 2;
		} catch (NoSuchFieldError var3) {
		}

		try {
			field_1585_a[EnumOS2.solaris.ordinal()] = 3;
		} catch (NoSuchFieldError var2) {
		}

		try {
			field_1585_a[EnumOS2.linux.ordinal()] = 4;
		} catch (NoSuchFieldError var1) {
		}

	}
}
