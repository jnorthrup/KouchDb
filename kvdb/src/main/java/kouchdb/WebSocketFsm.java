package kouchdb;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jim on 4/11/14.
 */
class WebSocketFsm implements CompletionHandler<Integer, Object> {

    private final AsynchronousSocketChannel socketChannel;
    ByteBuffer response, cursor;
    CompletionHandler<Integer, Object> startNewFrameHandler = new FrameCreation();
    private CompletionHandler<Integer, Object> resumePayloadHandler = new FrameResume();

    public WebSocketFsm(ByteBuffer response, ByteBuffer cursor, AsynchronousSocketChannel socketChannel) {
        this.response = response;
        this.cursor = cursor;
        this.socketChannel = socketChannel;
        this.cursor = cursor;

        try {
            System.err.println("+++ " +
                    new Date()+"session online "+socketChannel.getRemoteAddress()+' ' );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void completed(Integer result, Object attachment) {
        if (response.hasRemaining()) socketChannel.write(response);
        else
            socketChannel.read(cursor, null, startNewFrameHandler);
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

    private class FrameCreation implements CompletionHandler<Integer, Object> {
        @Override
        public void completed(Integer result, Object attachment) {
            List<WebSocketFrame> segmented = new ArrayList<WebSocketFrame>();
            WebSocketFrame webSocketFrame = new WebSocketFrame();
            boolean apply = webSocketFrame.apply(cursor);

            if (apply) {
                if (webSocketFrame.payloadLength > cursor.remaining()) {
                    ByteBuffer byteBuffer = ByteBuffer.allocateDirect((int) (webSocketFrame.payloadLength - cursor.remaining()));
                    socketChannel.read(byteBuffer, attachment, new CompletionHandler<Integer, Object>() {
                        @Override
                        public void completed(Integer result, Object attachment) {
                            if (!byteBuffer.hasRemaining()) {
                                ByteBuffer slice = null;
                                if (webSocketFrame.isMasked) {
                                    slice = cursor.slice();
                                    WebSocketFrame.applyMask(webSocketFrame.maskingKey, slice);
                                    WebSocketFrame.applyMask(webSocketFrame.maskingKey, byteBuffer);
                                }
                                command(FrameCreation.this, webSocketFrame, slice, byteBuffer);
                            }
                        }

                        @Override
                        public void failed(Throwable exc, Object attachment) {

                        }
                    });
                }
            }

        }

        @Override
        public void failed(Throwable exc, Object attachment) {

        }
    }

    private void command(FrameCreation frameCreation, WebSocketFrame webSocketFrame, ByteBuffer ...byteBuffer) {
        System.err.println("");
    }
}
