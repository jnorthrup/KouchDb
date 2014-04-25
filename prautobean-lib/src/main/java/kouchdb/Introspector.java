package kouchdb;

import com.dyuproject.protostuff.parser.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Introspector {
    static Map<String, StripeLeveler> messages = new LinkedHashMap<>();
    private static Map<String, EnumGroup> enums = new LinkedHashMap<>();

    public static void main(String[] args) throws IOException {
        Path path = Paths.get(args.length < 0 ? args[0] : "/home/jim/work/KouchDb/kvdb/src/main/proto/commands.proto");

        try {
            Proto target = new Proto();
            ProtoUtil.loadFrom(Files.newBufferedReader(path), target);

            target.getMessages().forEach(Introspector::descend);
        } catch (Exception e) {
            e.printStackTrace();
        }
        enums.forEach(Introspector::printEnum);
        messages.forEach(Introspector::printMessage);
    }

    private static void printMessage(String k, StripeLeveler v) {
        String[] split = k.split("\\.");
        System.out.println("@ProtoOrigin(" + '"' + v.message.getFullName() + '"' + ")\ninterface " + split[split.length - 1] + "{");
        Map<String, List<Field>> f = v.stripes;
        AtomicInteger bits = new AtomicInteger(0);
        //package up the primitives first

        StripeLeveler.inFirst.forEach (o -> {List<Field> fields = f.get(o);
            if (null != fields)
                print(bits, o, fields);});

        StripeLeveler.inFirst.stream().filter(f::containsKey).forEachOrdered(o ->    print(bits, o, f.get(o)));



        System.out.println("}");
    }

    private static void print(AtomicInteger bits, String type, List<Field> fields) {
        fields.forEach(field -> {
            String capped = getCaps(field.getName());
            boolean repeated = field.isRepeated();
            boolean optional = field.isOptional();
 

            System.out.println((optional ? "\n\t@Optional(" + bits.incrementAndGet() + ")" : "") + "\n\t@ProtoNumber(" + field.getNumber() + ")\n\t" + (  type) + "\tget" + capped + "();\n");
        });
    }

    private static void printEnum(String k, EnumGroup v) {
        System.out.println("enum " + k + "{");
        StringJoiner stringJoiner = new StringJoiner(",");
        v.getValues().stream().map(EnumGroup.Value::getName).forEachOrdered(stringJoiner::add);
        System.out.println(stringJoiner.toString());
        System.out.println("};");
    }

    private static String getCaps(String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private static void descend(Message Message) {
        LinkedHashMap<String, Message> nestedMessageMap = Message.getNestedMessageMap();
        for (Map.Entry<String, Message> stringMessageEntry : nestedMessageMap.entrySet()) {
            descend(stringMessageEntry.getValue());
        }
        writeGen(Message);
    }

    static void writeGen(Message message) {
        StripeLeveler x = new StripeLeveler(message);
        messages.put(message.getFullName(), x);
        List<Field<?>> fields = message.getFields();
        for (Field<?> field : fields) {
            String javaType1 = field.getJavaType();
            x.addField(field, javaType1);
        }
        Collection<EnumGroup> nestedEnumGroups = message.getNestedEnumGroups();
        nestedEnumGroups.forEach(o -> {
            String name = o.getName();
            enums.put(name, o);
        });

    }

    static class StripeLeveler {

        static public List<String> inFirst =new ArrayList<>();
        static {
            Class[]x = {boolean.class, byte.class, short.class, int.class, float.class, long.class, double.class} ;
            for (Class aClass : x) {
                inFirst.add(aClass.getSimpleName());
            }
        }

        public Map<String, List<Field>> stripes = new LinkedHashMap<>();
        private Message message;

        public StripeLeveler(Message message) {

            this.message = message;
        }

        private void addField(Field<?> field, String javaType1) {
            Map<String, List<Field>> f = this.stripes;
            List<Field> fieldList = f.get(javaType1);
            if (fieldList == null) {
                fieldList = new ArrayList<>();
                this.stripes.put(javaType1, fieldList);
            }
            fieldList.add(field);
        }
    }
}
