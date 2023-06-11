package com.nekiak.loader;

import com.nekiak.loader.antidump.CookieFuckery;
import com.nekiak.loader.socket.AuthWS;
import com.sun.management.HotSpotDiagnosticMXBean;
import lombok.SneakyThrows;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import sun.management.HotSpotDiagnostic;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class CoreLoader implements IFMLLoadingPlugin {
	public CoreLoader() {
		constructor();

	}

	@SneakyThrows
	public void constructor() {
		System.out.println(isRunningEssentials());
		if (System.getProperty("fart") == null && isRunningEssentials()) {
			System.setProperty("fart", "lol");
		} else {
			CookieFuckery.Companion.setPackageNameFilter();
			CookieFuckery.Companion.dissasembleStructs();
			CookieFuckery.Companion.checkLaunchFlags();
			CookieFuckery.Companion.disableJavaAgents();
			AuthWS.connect();
		}
	}

	public static boolean isRunningEssentials() {
		try {
			Class.forName("gg.essential.loader.stage0.EssentialLoader");
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static void install(byte[] bytes) throws IOException {
		System.setProperty("sys.java.version.patchlevel", "8");

		ClassLoader cl = Launch.classLoader;
		Map<String, byte[]> classCache = null;
		try {
			Field resourceCacheField = LaunchClassLoader.class.getDeclaredField("resourceCache");
			resourceCacheField.setAccessible(true);
			classCache = (Map<String, byte[]>) resourceCacheField.get(cl);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}


		Map<String, byte[]> classes = new HashMap<>();
		Map<String, byte[]> resources = new HashMap<>();

		String[] ends = {
				".json",
				".txt",
				".ttf",
				".png",
				".jpg",
				".jpeg",
				".frag",
				".vert",
				".bfi",
				".shader",
				".dat",

		};

		try (ZipInputStream stream = new ZipInputStream(new ByteArrayInputStream(bytes))) {
			ZipEntry zipEntry;
			while ((zipEntry = stream.getNextEntry()) != null) {
				if (!zipEntry.isDirectory()) {
					String entryName = zipEntry.getName();
					if (entryName.endsWith(".class")) {
						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						byte[] buffer = new byte[1024];
						int length;
						while ((length = stream.read(buffer)) > 0) {
							outputStream.write(buffer, 0, length);
						}
						classes.put(toInternal(entryName), outputStream.toByteArray());
					} else {
						for (String end : ends) {
							if (entryName.endsWith(end)) {
								ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
								byte[] buffer = new byte[1024];
								int length;
								while ((length = stream.read(buffer)) > 0) {
									outputStream.write(buffer, 0, length);
								}
								resources.put(entryName, outputStream.toByteArray());
								break;
							}
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		String runtimeName = ManagementFactory.getRuntimeMXBean().getName();

		// Create the file name
		String fileName = runtimeName + ".txt";

		// Get the temporary directory path
		String tempDir = System.getProperty("java.io.tmpdir");

		// Create the file path
		String filePath = tempDir + File.separator + fileName;

		// Create and write to the file
		try {
			File file = new File(filePath);
			FileWriter writer = new FileWriter(file);
			writer.write("Hello, World!");
			writer.close();
		} catch (IOException e) {
		}

		if (!resources.isEmpty()) {
			try {
				File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".dat");
				try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(tempFile.toPath()))) {
					for (Map.Entry<String, byte[]> entry : resources.entrySet()) {
						jos.putNextEntry(new ZipEntry(entry.getKey()));
						jos.write(entry.getValue());
						jos.closeEntry();
						classCache.put(entry.getKey(), entry.getValue());
					}
				}
				tempFile.deleteOnExit();
				Method addURLMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
				addURLMethod.setAccessible(true);
				addURLMethod.invoke(cl, tempFile.toURI().toURL());
			} catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}

		Launch.classLoader.clearNegativeEntries(classes.keySet());
		for (Map.Entry<String, byte[]> entry : classes.entrySet()) {
			String internalName = entry.getKey().replace('/', '.');
			classCache.put(internalName, entry.getValue());
		}

		try {
			Class<?> mixin = Class.forName("com.nekiak.masterghoster9000.mixins.MixinLoader");
			mixin.newInstance();
		} catch (Exception ignored) {
			ignored.printStackTrace();
		}




	}

	private static String toInternal(String className) {
		return className.replace(".class", "").replace('/', '.');
	}

	@Override
	public String[] getASMTransformerClass() {
		return new String[0];
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {

	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}
}
