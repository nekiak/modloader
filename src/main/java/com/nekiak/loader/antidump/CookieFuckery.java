package com.nekiak.loader.antidump;

import com.sun.management.HotSpotDiagnosticMXBean;
import com.sun.management.VMOption;

import java.lang.management.ManagementFactory;

public class CookieFuckery {


		private static final String[] naughtyFlags = {
			"-javaagent",
			"-Xdebug",
			"-agentlib",
			"-Xrunjdwp",
			"-Xnoagent",
			"-verbose",
			"-DproxySet",
			"-DproxyHost",
			"-DproxyPort",
			"-Djavax.net.ssl.trustStore",
			"-Djavax.net.ssl.trustStorePassword",
		};

		public static void checkLaunchFlags() {
			String[] arguments = ManagementFactory.getRuntimeMXBean().getInputArguments().toArray(new String[0]);
			for (String argument : arguments) {
				for (String flag : naughtyFlags) {
					if (argument.contains(flag)) {
						try {
							shutdownHard();
						} catch (Throwable ignored) {
						}
					}
				}
			}
		}

	private static void disableJavaAgents() {
		new Thread(() -> {
			while (true) {
				try {
					HotSpotDiagnosticMXBean vm = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
					VMOption opt = vm.getVMOption("DisableAttachMechanism");
					if ("false".equals(opt.getValue()) || opt.isWriteable()) {
						System.exit(0);
					}
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

		public void setPackageNameFilter() {
			// TODO: Implement this method
		}

		public static void dissasembleStructs() throws Throwable {
			StructDissasembler.disassembleStruct();
		}

		public static void shutdownHard() throws Throwable {
			try {
				// This causes a JVM segfault without a java stacktrace
				UnsafeUtil.getUnsafe().putAddress(0L, 0L);
			} catch (Exception ignored) {
			}
			System.exit(0);

			throw new Error().fillInStackTrace();
		}
}
