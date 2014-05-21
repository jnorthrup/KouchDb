/*
package kouchdb;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;

 */
/**
 * Created by jim on 4/12/14.
 */
/*

 public class CommandCreation implements CompletionHandler<Integer, Object> {
 private WebSocketFsm fsm;

 public CommandCreation(WebSocketFsm fsm) {
 this.fsm = fsm;
 }

 @Override
 public void completed(Integer ignored, Object attachment) {
 while (fsm.cursor.hasRemaining()) {
 if (-1 == ignored) try {
 fsm.socketChannel.close();
 return;
 } catch (IOException e) {
 }
 WebSocketFrame webSocketFrame = new WebSocketFrameBuilder().createWebSocketFrame();//todo: keep this cheap
 boolean apply = webSocketFrame.apply((ByteBuffer) fsm.cursor.flip());//todo: keep this cheap
 if (apply) {
 if (webSocketFrame.payloadLength > fsm.cursor.remaining()) {
 ByteBuffer byteBuffer = ByteBuffer.allocateDirect((int) (webSocketFrame.payloadLength - fsm.cursor.remaining()));
 CompletionHandler<Integer, Object> handler = new CompletionHandler<Integer, Object>() {//relinquish control
 @Override
 public void completed(Integer ignored, Object attachment) {
 if (!byteBuffer.hasRemaining()) {//spin till full.  then goto top
 ByteBuffer slice = null;
 if (webSocketFrame.isMasked) {
 slice = ByteBuffer.allocateDirect((int) webSocketFrame.payloadLength).put(fsm.cursor);
 WebSocketFrame.applyMask(webSocketFrame.maskingKey, slice);
 WebSocketFrame.applyMask(webSocketFrame.maskingKey, byteBuffer);
 }

 command(webSocketFrame, slice, byteBuffer);

 CommandCreation.this.completed(ignored, attachment);//resume control of draining the cursor to the outer outer class loop
 }
 }

 @Override
 public void failed(Throwable exc, Object attachment) {
 }
 };
 fsm.socketChannel.read(byteBuffer, attachment, handler);
 return;//relinquish control
 } else
 //(webSocketFrame.payloadLength<=cursor.remaining())
 command(webSocketFrame, (ByteBuffer) ByteBuffer.allocateDirect((int) webSocketFrame.payloadLength).put(fsm.cursor).rewind());
 } else
 bail();//deficient cursor.  handle elswhere.
 return;
 }
 }

 private void bail() {
 fsm.socketChannel.read(fsm.cursor, null, fsm.startNewFrameHandler);
 }

 @Override
 public void failed(Throwable exc, Object attachment) {

 }

 */
/**
 * receive a series of command fragments and assemble.  each fragment may have more than one buffer.
 *  @param webSocketFrame
 * @param byteBuffer
 */
/*

 void  command(WebSocketFrame webSocketFrame, ByteBuffer... byteBuffer) {
 fsm.cursor.compact();
 System.err.println("");

 //do something.... crack a payload... then
 ByteBuffer r = null;
 switch (webSocketFrame.opcode) {
 case continuation:
 break;
 case text:
 //echo

 ByteBuffer slice = byteBuffer[0].slice();
 if (webSocketFrame.isMasked) {
 WebSocketFrame.applyMask(webSocketFrame.maskingKey, slice);
 slice.flip();
 }
 String decode = StandardCharsets.UTF_8.decode(slice).toString();
 System.err.println(decode);
 WebSocketFrame rframe = new WebSocketFrameBuilder().setPayloadLength(slice.limit()).createWebSocketFrame();

 rframe.apply((ByteBuffer) fsm.response.clear());
 fsm.response.put((ByteBuffer) slice.rewind());
 break;
 case binary:
 break;
 case reservedDataFrame3:
 break;
 case reservedDataFrame4:
 break;
 case reservedDataFrame5:
 break;
 case reservedDataFrame6:
 break;
 case reservedDataFrame7:
 break;
 case close:
 break;
 case ping:
 case pong:
 break;
 }


 }
 }
 */
