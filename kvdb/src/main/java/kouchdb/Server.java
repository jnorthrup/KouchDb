package kouchdb;

 import kouchdb.util.Rfc822HeaderState;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
 import static java.lang.StrictMath.min;

/**
 * ws-only http server
 *   The handshake from the client looks as follows:

 GET /chat HTTP/1.1
 Host: server.example.com
 Upgrade: websocket
 Connection: Upgrade
 Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==
 Origin: http://example.com
 Sec-WebSocket-Protocol: chat, superchat
 Sec-WebSocket-Version: 13

 The handshake from the server looks as follows:

 HTTP/1.1 101 Switching Protocols
 Upgrade: websocket
 Connection: Upgrade
 Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=
 Sec-WebSocket-Protocol: chat

 */
public class Server{
    final static boolean $DBG= "true".equals(Config.get("KOUCH_DEBUG", "false"));

    public Server() {

        try (AsynchronousServerSocketChannel x = AsynchronousServerSocketChannel.open()) {
          x.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
          public boolean hasHeaders;
          private Rfc822HeaderState rfc822HeaderState;
          ByteBuffer cursor= ByteBuffer.allocateDirect(1 << 10) ;
          @Override
          public void completed(AsynchronousSocketChannel socketChannel, Void attachment) {

              socketChannel.read(cursor , attachment, new CompletionHandler<Integer, Void>() {
                          @Override
                          public void completed(Integer result, Void attachment) {
                              rfc822HeaderState = new Rfc822HeaderState();
                              Rfc822HeaderState.HttpRequest httpRequest = rfc822HeaderState.$req();
                              hasHeaders = httpRequest.apply((ByteBuffer) cursor.mark().rewind());
                              if(!cursor.hasRemaining())cursor=ByteBuffer.allocateDirect(cursor.limit()<<2).put((ByteBuffer) cursor.rewind());
                              if (!hasHeaders) socketChannel.read(cursor, null, this);
                              else
                              this.parseRequest();
                          }

                          /**
                           *
                           assumes:
                           cursor is an unfinished ByteBuffer
                           exists with all the state needed from surrounding enclosures.
                           */
                          void parseRequest() {

                          }


                          @Override
                          public void failed(Throwable exc, Void attachment) {

          //no longer need $DBG!
              exc.printStackTrace();
                          }
                      }
              );
          }
          @Override
          public void failed(Throwable exc, Void attachment) {
          //no longer need $DBG!
              exc.printStackTrace();

          }
      });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    public static String wheresWaldo(int... depth) {
      int d = depth.length > 0 ? depth[0] : 2;
      Throwable throwable = new Throwable();
      Throwable throwable1 = throwable.fillInStackTrace();
      StackTraceElement[] stackTrace = throwable1.getStackTrace();
      String ret = "";
      for (int i = 2, end = min(stackTrace.length - 1, d); i <= end; i++) {
        StackTraceElement stackTraceElement = stackTrace[i];
        ret +=
            "\tat " + stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName()
                + "(" + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber()
                + ")\n";

      }
      return ret;
    }
}
