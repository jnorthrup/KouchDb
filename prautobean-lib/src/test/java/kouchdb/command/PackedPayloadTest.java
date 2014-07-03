package kouchdb.command;

import kouchdb.io.PackedPayload;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import static java.lang.Integer.toBinaryString;


public class PackedPayloadTest {

    @Test
    public void packTest() {
        BitSet bitSet = new BitSet(6);
        bitSet.set(0, 6);
        System.err.println(toBinaryString(bitSet.toByteArray()[0]));
        ComplexPrautoBean complexPrautoBean = new ComplexPrautoBean() {
            @Override
            public boolean getAutoCompaction() {
                return true;
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
            public List<ComplexPrautoBean> getComplexObject() {
                List<ComplexPrautoBean> createOptionses = new ArrayList<>();
                createOptionses.add(new ComplexPrautoBean() {
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
                    public List<ComplexPrautoBean> getComplexObject() {
                        return null;
                    }

                    @Override
                    public List<String> getStringList() {
                        return Arrays.asList(new String[]{"c", "abcdefghijklmnopqrstuvwxyd"});
                    }

                    @Override
                    public List<TheEnum> getEnumList() {
                        return null;
                    }
                });
                return createOptionses;
            }

            @Override
            public List<String> getStringList() {
                return Arrays.asList("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "b");
            }

            @Override
            public List<TheEnum> getEnumList() {
                List<TheEnum> lists =
                        (Arrays.asList(TheEnum.y, TheEnum.y, TheEnum.z));

                return lists;
            }
        };
        PackedPayload<ComplexPrautoBean> createOptionsClassPackedPayload =PackedPayload.create(ComplexPrautoBean.class);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1 << 8);

        createOptionsClassPackedPayload.put(complexPrautoBean, byteBuffer);
        ByteBuffer outer = (ByteBuffer) byteBuffer.flip();

        System.err.println(StandardCharsets.UTF_8.decode(outer.duplicate()));

        ByteBuffer duplicate = outer.duplicate();
        PackedPayload.readSize(duplicate);
        byte b = duplicate.get();
        System.err.println(toBinaryString(b & 0xff));


        PackedPayload<ComplexPrautoBean> complexPrautoBeanPackedPayload = PackedPayload.create(ComplexPrautoBean.class);
        ComplexPrautoBean complexPrautoBean1 =
        complexPrautoBeanPackedPayload.get(ComplexPrautoBean.class, outer);

        List<TheEnum> enumThingy = complexPrautoBean1.getEnumList();
        TheEnum theEnum = enumThingy.get(2);
        org.junit.Assert.assertEquals( TheEnum.z ,theEnum);
        String cache = complexPrautoBean.getCache();
        String cache1 = complexPrautoBean1.getCache();
        org.junit.Assert.assertEquals(cache1, cache);
    }

}
