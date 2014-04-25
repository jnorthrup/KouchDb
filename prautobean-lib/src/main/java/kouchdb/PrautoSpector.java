package kouchdb;

import com.dyuproject.protostuff.parser.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PrautoSpector {
    static Map<String, StripeLeveler> messages = new LinkedHashMap<>();
    private static Map<String, EnumGroup> enums = new LinkedHashMap<>();
    private static Path outdir;

    public static void main(String... args) throws IOException {

        Path path = Paths.get(args.length > 0 ? args[0] : "src/main/proto/");

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                 boolean regularFile = attrs.isRegularFile();
                if (regularFile) {
                    try {

                        Proto target = new Proto();
                        ProtoUtil.loadFrom(Files.newBufferedReader(file), target);
                        String javaPackageName = target.getJavaPackageName();
                        outdir = Paths.get((args.length > 1 ? args[1] : "target/generated-sources")+javaPackageName.replace('.','/'));

                        Path directories = Files.createDirectories(outdir);
                        System.out.println("writing to "+ directories.toUri());

                        target.getMessages().forEach(PrautoSpector::descend);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    enums.forEach(PrautoSpector::printEnum);
                    messages.forEach(PrautoSpector::printMessage);
                }
                return FileVisitResult.CONTINUE;
            }


        });


    }

    private static void printMessage(String k, StripeLeveler v) {

        String javaPackageName = v.message.getProto().getJavaPackageName();
        String[] split = k.split("\\.");
        String cname = split[split.length - 1];
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(outdir.toString(), cname+".java"));PrintWriter pw=new PrintWriter(bufferedWriter)) {
            pw.println("package " + javaPackageName +
                    ";\n\n@ProtoOrigin(" + '"' + v.message.getFullName() + '"' + ")\ninterface " + cname + "{");
            Map<String, List<Field>> f = v.stripes;
            AtomicInteger bits = new AtomicInteger(0);
            //package up the primitives first

            StripeLeveler.inFirst.forEach(o -> {

                List<Field> fields = f.get(o);
                if (null != fields)
                    print(bits, o, fields, ((PrintWriter) pw));
            });
            f.entrySet().stream().filter(o -> !StripeLeveler.inFirst.contains(o.getKey())).forEachOrdered(o -> print(bits,o.getKey(),o.getValue(), ((PrintWriter) pw)));

            pw.println("}");  } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void print(AtomicInteger bits, String type, List<Field> fields, PrintWriter printWriter) {

        fields.forEach(field -> {
            String capped = getCaps(field.getName());
            boolean repeated = field.isRepeated();
            boolean optional = field.isOptional();
            if (!repeated)
                printWriter.println((optional ? "\n\t@Optional(" + bits.incrementAndGet() + ")" : "") + "\n\t@ProtoNumber(" + field.getNumber() + ")\n\t" + (type) + "\tget" + capped + "();\n");
            else
                printWriter.println((optional ? "\n\t@Optional(" + bits.incrementAndGet() + ")" : "") + "\n\t@ProtoNumber(" + field.getNumber() + ")\n\tList<" + (type) + ">\tget" + capped + "();\n");

        });
    }

    private static void printEnum(String k, EnumGroup v) {

        String javaPackageName = v.getProto().getJavaPackageName();
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(outdir.toString(), k+".java")); PrintWriter pw= new PrintWriter(bufferedWriter)) {
            pw.println("package " + javaPackageName +
                    ";\nenum " + k + "{");
            StringJoiner stringJoiner = new StringJoiner(",");
            v.getValues().stream().map(EnumGroup.Value::getName).forEachOrdered(stringJoiner::add);
            pw.println(stringJoiner.toString());
            pw.println("};");
        } catch (IOException e) {
            e.printStackTrace();
        }

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

        static public List<String> inFirst = new ArrayList<>();

        static {
            Class[] x = {boolean.class, byte.class, short.class, int.class, float.class, long.class, double.class};
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
