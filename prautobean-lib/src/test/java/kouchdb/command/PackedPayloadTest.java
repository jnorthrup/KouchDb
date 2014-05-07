package kouchdb.command;

import kouchdb.io.PackedPayload;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Integer.toBinaryString;


public class PackedPayloadTest {

    @Test public
    void packTest() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(256 << 10);
        CreateOptions createOptions = new CreateOptions() {
            @Override
            public boolean getAutoCompaction() {
                return false;
            }

            @Override
            public String getName() {
                return "theName";
            }

            @Override
            public String getCache() {
                return "someString";
            }

            @Override
            public String getAdapter() {
                return "someOtherString";
            }

            @Override
            public List<CreateOptions> getChallenge() {
                ArrayList<CreateOptions> createOptionses = new ArrayList<>();
                createOptionses.add(new CreateOptions() {
                    @Override
                    public boolean getAutoCompaction() {
                        return false;
                    }

                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public String getCache() {
                        return null;
                    }

                    @Override
                    public String getAdapter() {
                        return null;
                    }

                    @Override
                    public List<CreateOptions> getChallenge() {
                        return null;
                    }

                    @Override
                    public List<String> getChallenge2() {
                        return Arrays.asList(new String[]{"c", "d"});
                    }
                });
                return createOptionses;
            }
            @Override
            public List<String> getChallenge2() {
                return Arrays.asList("a", "b");
            }
        };
        PackedPayload<CreateOptions> createOptionsClassPackedPayload = new PackedPayload<>(CreateOptions.class);
        createOptionsClassPackedPayload.put(createOptions, byteBuffer);
        System.err.println(StandardCharsets.UTF_8.decode((ByteBuffer) byteBuffer.duplicate().flip()));

        byte b = byteBuffer.get(5);
        System.err.println(""+toBinaryString(b & 0xff));

        assert 0b11111100 == (b & 0xff); //bitmap

        CreateOptions createOptions1 = createOptionsClassPackedPayload.get(CreateOptions.class, (ByteBuffer) byteBuffer.flip());
        assert createOptions1.getCache().equals(createOptions.getCache());
    }

}
