package com.nekiak.loader.socket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.nekiak.loader.ModKey;
import com.nekiak.loader.authentication.HWID;
import com.nekiak.loader.CoreLoader;
import com.nekiak.loader.antidump.CookieFuckery;
import joptsimple.internal.Reflection;
import lombok.SneakyThrows;
import net.minecraft.launchwrapper.Launch;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.swing.*;

public class AuthWS {
    private static final String SERVER_URI = "ws://localhost:65212/ghostloader";

    private static WebSocketClient client;
    private static boolean hasDownloadedJar = false;
    private static String username = ((Map<String, String>) (Launch.blackboard.get("launchArgs"))).get("--username");
    private static final String key = ModKey.getKey();
    public static void connect() {
        Map<String, String> headers = new HashMap<>();
        headers.put("key", key);
        headers.put("hwid", HWID.getSystemInfo());
        headers.put("username", username);
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
                }

                @Override
                public void onMessage(ByteBuffer message) {
                    if (!hasDownloadedJar) {
                        hasDownloadedJar = true;
                        try {
                            CoreLoader.install(message.array());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Logger.getLogger("Loader").log(Level.SEVERE, String.valueOf(code));
                    Logger.getLogger("Loader").log(Level.SEVERE, String.valueOf(reason));
                    Logger.getLogger("Loader").log(Level.SEVERE, String.valueOf(remote));

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
                        default:
                            reconnect = true;
                    }


                    if (!reconnect) {
                        killMyself();
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

    @SneakyThrows
    private static void killMyself() {
        try {
            CookieFuckery.Companion.shutdownHard();
        } catch (Exception e) {
            System.exit(0);

        }
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
        new Thread(() -> JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE)).start();

    }
}
