package kouchdb;

import com.dyuproject.protostuff.parser.*;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class Introspector {
    public static final Pattern COMPILE = Pattern.compile("[/]{2}.*$");
    static Map<String, x> messages = new LinkedHashMap<>();
    private static Map<String, EnumGroup> enums = new LinkedHashMap<>();

    public static void main(String[] args) throws IOException {
        Path path = Paths.get("/home/jim/work/KouchDb/kvdb/src/main/proto/commands.proto");

        try {
            Proto target = new Proto();
            ProtoUtil.loadFrom(Files.newBufferedReader(path), target);

            Collection<Message> messages = target.getMessages();

//            messages.forEach(o -> err.println(o.getFullName()));
            messages.forEach(Introspector::descend);
        } catch (Exception e) {
            e.printStackTrace();
        }
        enums.forEach(Introspector::printEnum);
        messages.forEach(Introspector::printMessage);
    }

    private static void printMessage(String k, x v) {
        String[] split = k.split("\\.");

        System.out.println("interface " + split[split.length - 1] + "{");
        Map<String, List<Field>> f = v.f;
        AtomicInteger bits = new AtomicInteger(0);
        f.forEach((type, fields) -> {
            fields.forEach(field -> {
                String capped = getCaps(field.getName());
                boolean repeated = field.isRepeated();
                System.out.println((field.isOptional() ? "\n\t@Optional(" + bits.incrementAndGet() +
                        ")" : "") + "\n\t@ProtoNumber(" + field.getNumber() + ")\n\t" + type + "\tget" + capped + "();\n");
            });
        });
        System.out.println("}");
    }

    private static void printEnum(String k, EnumGroup v) {
        System.out.println("enum " + k + "{");
       System.out.println(  Joiner.on(",").join(Collections2.transform(v.getValues(), EnumGroup.Value::getName)));
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

    static void writeGen(Message Message) {
        x x = new x();
        messages.put(Message.getFullName(), x);
        List<Field<?>> fields = Message.getFields();
        for (Field<?> field : fields) {
            String javaType1 = field.getJavaType();
            x.addField(field, javaType1);
        }
        Collection<EnumGroup> nestedEnumGroups = Message.getNestedEnumGroups();
        nestedEnumGroups.forEach(o -> {
            String name = o.getName();

            enums.put(name, o);
        });
    }

    static class x {
        @Override
        public String toString() {
            return "x{" +
                    "f=" + f +
                    '}';
        }

        public Map<String, List<Field>> f = new LinkedHashMap<String, List<Field>>();

        private void addField(Field<?> field, String javaType1) {
            Map<String, List<Field>> f = this.f;
            List<Field> fieldList = f.get(javaType1);
            if (fieldList == null) {
                fieldList = new ArrayList<>();
                this.f.put(javaType1, fieldList);
            }
            fieldList.add(field);
        }
    }
}


/**
 * kouchdb.command.WsFrame.CreateDb.CreateOptions/x{f={String=[
 * {type:String,name:name,number:1,modifier:OPTIONAL,packable:false,defaultValue:null},
 * {type:String,name:cache,number:3,modifier:OPTIONAL,packable:false,defaultValue:null},
 * {type:String,name:adapter,number:4,modifier:OPTIONAL,packable:false,defaultValue:null}],
 * boolean=[{type:Bool,name:auto_compaction,number:2,modifier:OPTIONAL,packable:true,defaultValue:null}]}}
 * kouchdb.command.WsFrame.CreateDb/x{f={String=[{type:String,name:name,number:1,modifier:OPTIONAL,packable:false,defaultValue:null}], CreateOptions=[{type:MessageField,name:options,number:2,modifier:OPTIONAL,packable:false,defaultValue:null}]}}
 */


interface CreateOptions {
    String getName();

    String getCache();

    String getAdapter();
}

/**
 * kouchdb.command.WsFrame.CreateDb/x{f={String=[{type:String,name:name,number:1,modifier:OPTIONAL,packable:false,defaultValue:null}], CreateOptions=[{type:MessageField,name:options,number:2,modifier:OPTIONAL,packable:false,defaultValue:null}]}}
 */
interface CreateDb {

    String getName();

    CreateOptions getOptions();
}