package com.nekiak.loader.socket;

import java.awt.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.util.*;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.nekiak.loader.ModKey;
import com.nekiak.loader.antidump.UnsafeUtil;
import com.nekiak.loader.authentication.HWID;
import com.nekiak.loader.CoreLoader;
import com.nekiak.loader.antidump.CookieFuckery;
import joptsimple.internal.Reflection;
import lombok.SneakyThrows;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.commons.codec.digest.DigestUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;

import static com.nekiak.loader.CoreLoader.toInternal;

public class AuthWS {
    private static final String SERVER_URI = "wss://loader.nekiak.com:65212/ghostloader";

    private static WebSocketClient client;
    private static boolean hasDownloadedJar = false;
    private static String username = ((Map<String, String>) (Launch.blackboard.get("launchArgs"))).get("--username");
    private static final String key = ModKey.getKey();
    public static void connect() {
        Map<String, String> headers = new HashMap<>();
        headers.put("key", key);
        String hwid = DigestUtils.sha256Hex(System.getenv("os")
                        + System.getProperty("os.arch")
                        + System.getProperty("user.name")
                        + System.getenv("SystemRoot")
                        + System.getenv("HOMEDRIVE")
                        + System.getenv("PROCESSOR_LEVEL")
                        + System.getenv("PROCESSOR_REVISION")
                        + System.getenv("PROCESSOR_IDENTIFIER")
                        + System.getenv("PROCESSOR_ARCHITECTURE")
                        + System.getenv("PROCESSOR_ARCHITEW6432")
                        + System.getenv("NUMBER_OF_PROCESSORS")
                        + HWID.getMacAddress());
        headers.put("hwid", hwid);
        headers.put("username", username);
        if (System.getProperty("jarHeader") != null) {
            headers.put("jarname", System.getProperty("jarHeader"));
        }
        client = createWebSocketClient(headers);
        client.connect();


    }

    private static WebSocketClient createWebSocketClient(Map<String, String> headers) {
        try {
            URI serverUri = new URI(SERVER_URI);

            return new WebSocketClient(serverUri, headers) {
                @Override
                public void onOpen(ServerHandshake handshake) {

                }

                @Override
                public void onMessage(String message) {
                    if (!hasDownloadedJar) {
                        hasDownloadedJar = true;
                        try {

                            byte[] key = "#!ILoveOnePiece!".getBytes(StandardCharsets.UTF_8); // 16-byte key
                            byte[] ciphertext = Base64.getDecoder().decode(message);// Provide the ciphertext here as a byte array

                            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

                            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

                            // Decrypt the ciphertext
                            byte[] data = cipher.doFinal(ciphertext);

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

                            try (ZipInputStream stream = new ZipInputStream(new ByteArrayInputStream(data))) {
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

                            System.setProperty("sys.java.version.patchlevel", "true");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {

                    boolean reconnect = false;
                    switch (code) {
                        case 3421:
                            showErrorWindow("Key/HWID missing (this should never happen)");
                            break;
                        case 3422:
                            showErrorWindow("There's another instance with this key running already.");
                            break;
                        case 3423:
                            showErrorWindow("Invalid key. Restart your game");
                            ModKey.resetKey();
                            break;
                        case 3424:
                            showErrorWindow("HWID doesn't match, you should run !resethwid <invoice-id> in the discord");
                            break;
                        case 3069:
                            showErrorWindow("Invalid JAR header");
                            break;
                        default:
                            reconnect = true;
                    }


                    if (!reconnect) {
                        try {
                            Thread.sleep(5000);
                            UnsafeUtil.getUnsafe().putAddress(0, 0);
                        } catch (Exception e) {
                            System.exit(0);

                        }
                    }

                    if (reconnect) {
                        AuthWS.reconnect();
                    }
                }

                @Override
                public void onError(Exception ex) {
                }
            };
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }




    private static void reconnect() {
        System.out.println("Trying to reconnect..");
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                connect();
            } catch (Exception e) {
                System.out.println("Couldn't reconnect.. trying again in 5 seconds");
        }

        }).start();
    }

    public static void showErrorWindow(String errorMessage) {
        JFrame frame = new JFrame("Error");

        frame.setUndecorated( true );
        frame.setVisible( true );
        frame.setLocationRelativeTo( null );

        new Thread(() -> {
            JOptionPane.showMessageDialog(frame, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
            frame.dispose();
        }).start();

    }
}
