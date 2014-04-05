package kouchdb;

import javolution.util.FastMap;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;

import static java.lang.Math.abs;
import static java.lang.StrictMath.min;

public class Server{
    /**
     * byte-compare of suffixes
     *
     * @param terminator  the token used to terminate presumably unbounded growth of a list of buffers
     * @param currentBuff current ByteBuffer which does not necessarily require a list to perform suffix checks.
     * @param prev        a linked list which holds previous chunks
     * @return whether the suffix composes the tail bytes of current and prev buffers.
     */
    public static boolean suffixMatchChunks(byte[] terminator, ByteBuffer currentBuff,
                                            ByteBuffer... prev) {
        ByteBuffer tb = currentBuff;
        int prevMark = prev.length;
        int bl = terminator.length;
        int rskip = 0;
        int i = bl - 1;
        while (0 <= i) {
            rskip++;
            int comparisonOffset = tb.position() - rskip;
            if (0 > comparisonOffset) {
                prevMark--;
                if (0 <= prevMark) {
                    tb = prev[prevMark];
                    rskip = 0;
                    i++;
                } else {
                    return false;

                }
            } else if (terminator[i] != tb.get(comparisonOffset)) {
                return false;
            }
            i--;
        }
        return true;
    }


    public Server() {

        try (AsynchronousServerSocketChannel x = AsynchronousServerSocketChannel.open()) {
      x.accept(new FastMap(), new CompletionHandler<AsynchronousSocketChannel, FastMap>() {
          @Override
          public void completed(AsynchronousSocketChannel result, FastMap attachment) {
              result
                      .read(ByteBuffer.allocateDirect(8 << 10), attachment,
                              new CompletionHandler<Integer, FastMap>() {
                                  @Override
                                  public void completed(Integer result, FastMap attachment) {

                                  }

                                  @Override
                                  public void failed(Throwable exc, FastMap attachment) {

                                  }
                              });
          }

          @Override
          public void failed(Throwable exc, FastMap attachment) {

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
