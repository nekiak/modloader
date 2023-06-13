package com.nekiak.loader;

import com.nekiak.loader.antidump.CookieFuckery;
import com.nekiak.loader.socket.AuthWS;
import com.sun.management.HotSpotDiagnosticMXBean;
import lombok.SneakyThrows;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.FMLModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

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
		try {
			Class.forName("cc.polyfrost.oneconfig.loader.stage0.LaunchWrapperTweaker");
		} catch (Exception e) {
			AuthWS.showErrorWindow("Master GhosterBuster 9000 requires you to install OneConfig in order to run. You don't have to install oneconfig-bootstrap.jar if you use other mods that already use OneConfig. Please check our discord for more info, and sorry for the inconvenience. Happy macroing! :)");
			return;
		}
		if (System.getProperty("fart") == null && isRunningEssentials()) {
			System.out.println("You're running essentials, if this ISN'T running DM one of the devs!!");
			System.setProperty("fart", "lol");
		} else {
			CookieFuckery.checkLaunchFlags();
			CookieFuckery.dissasembleStructs();
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


	public static String toInternal(String className) {
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
