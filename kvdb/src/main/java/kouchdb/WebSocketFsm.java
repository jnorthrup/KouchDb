package kouchdb;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Date;

/**
 * Created by jim on 4/11/14.
 */
class WebSocketFsm implements CompletionHandler<Integer, Object> {

    public final AsynchronousSocketChannel socketChannel;
    ByteBuffer response, cursor;
    CompletionHandler<Integer, Object> startNewFrameHandler = new CommandCreation(this);
    private CompletionHandler<Integer, Object> resumePayloadHandler = new FrameResume();

    public WebSocketFsm(ByteBuffer response, ByteBuffer cursor, AsynchronousSocketChannel socketChannel) {
        this.response = response;
        this.cursor = cursor;
        this.socketChannel = socketChannel;

        try {
            System.err.println("+++ " + new Date() + " session online " + socketChannel.getRemoteAddress() + ' ');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void completed(Integer result, Object attachment) {
          {
            while (response.hasRemaining()) try {
                socketChannel.write(response).get();
            } catch (Throwable  e) {
                e.printStackTrace();
            }
            socketChannel.read(cursor, this, startNewFrameHandler);
        }
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        exc.printStackTrace();
    }

    private static class FrameResume implements CompletionHandler<Integer, Object> {
        @Override
        public void completed(Integer result, Object attachment) {
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
        }
    }


}
