package kouchdb;

import kouchdb.command.DbInfo;
import kouchdb.command.WsFrame;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.Executors;

/**
 * Created by jim on 4/14/14.
 */
public class Client {
    public static void main(String[] args) throws IOException {

        Executors.newSingleThreadExecutor().submit(() -> {
            Server.main(args);
        });
        URI ws_uri = Server.WS_URI;
        String s = ws_uri.toASCIIString().replaceFirst("ws://", "http://");
        try (InputStream inputStream = new URL(s).openStream(); ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream)) {

            WsFrame test = new WsFrame().setType(WsFrame.Type.DbInfo).setDbInfo(new DbInfo().setDb("test"));
         }

    }

}
