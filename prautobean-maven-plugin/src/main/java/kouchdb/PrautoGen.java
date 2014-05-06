package kouchdb;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.dyuproject.protostuff.parser.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "prautobean", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class PrautoGen
        extends AbstractMojo {
 
    static Map<String, StripeLeveler> messages = new LinkedHashMap<>();
    static Map<String, EnumGroup> enums = new LinkedHashMap<>();
    static String outdir;
    @Parameter(defaultValue = "${project.basedir}/src/main/proto")
    public File sourceDirectory;
    @Parameter(defaultValue = "${project.build.directory}/generated-sources")
    public File outputDirectory;

    static void printMessage(String k, StripeLeveler v) {
        String javaPackageName = v.message.getProto().getJavaPackageName();
        String[] split = k.split("\\.");
        String cname = split[split.length - 1];
        ArrayList<String> strings = new ArrayList<>();
        Collections.addAll(strings, javaPackageName.split("\\."));
        strings.add(cname+".java");
        Path path = Paths.get(outdir, (String[]) strings.toArray(new String[strings.size()]));
        //System.err.println("writing message: " + path);
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path,StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.WRITE);
             PrintWriter pw = new PrintWriter(bufferedWriter)
        ) {
            pw.println("package " + javaPackageName + ";\n\nimport kouchdb.ann.*;\n\n\n@ProtoOrigin(" + '"' + v.message.getFullName() + '"' + ")\n" +
                    "interface " + cname + "{");
            Map<String, List<Field>> f = v.stripes;
            AtomicInteger bits = new AtomicInteger(0);
            //package up the primitives first

            StripeLeveler.inFirst.forEach(o -> {

                List<Field> fields = f.get(o);
                if (null != fields)
                    print(bits, o, fields, ((PrintWriter) pw));
            });
            f.entrySet().stream().filter(o -> !StripeLeveler.inFirst.contains(o.getKey())).forEachOrdered(o -> print(bits, o.getKey(), o.getValue(), ((PrintWriter) pw)));

            pw.println("}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void print(AtomicInteger bits, String type, List<Field> fields, PrintWriter printWriter) {
        fields.forEach(field -> {
            String capped = getCaps(field.getName());
            boolean repeated = field.isRepeated();
            boolean optional = field.isOptional();
            printWriter.println((optional ? "\n\t@Optional(" + bits.incrementAndGet() + ")" : "") + "\n\t@ProtoNumber(" + field.getNumber() + ")\n\t" +
                    (repeated ? ("java.util.List<" + (type) + ">") : (type)) + "\tget" + capped + "();\n");
        });
    }

    static void printEnum(String k, EnumGroup v) {
        String javaPackageName = v.getProto().getJavaPackageName();
        String[] split = k.split("\\.");
        String cname = split[split.length - 1];
        ArrayList<String> strings = new ArrayList<>();
        Collections.addAll(strings, javaPackageName.split("\\."));
        strings.add(cname+".java");
        Path path = Paths.get(outdir, (String[]) strings.toArray(new String[strings.size()]));
        //System.err.println("writing enum: " + path);
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path); PrintWriter pw = new PrintWriter(bufferedWriter)) {

            pw.println("package " + javaPackageName + ";\n\nimport kouchdb.ann.*;\n\n\n@ProtoOrigin(" + '"' + v. getFullName() + '"' + ")\n" +
                    "enum " + k + "{");
            StringJoiner stringJoiner = new StringJoiner(",");
            v.getValues().stream().map(EnumGroup.Value::getName).forEachOrdered(stringJoiner::add);
            pw.println(stringJoiner.toString());
            pw.println("};");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String getCaps(String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    static void descend(Message Message) {

        //System.err.println("descend: " + Message.getFullName());
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
            String javaType1 = field.getJavaType().replace("ByteString","java.util.List<Byte>");
            x.addField(field, javaType1);
        }
        Collection<EnumGroup> nestedEnumGroups = message.getNestedEnumGroups();
        nestedEnumGroups.forEach(o -> {
            String name = o.getName();
            enums.put(name, o);
        });
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        try {
/*            String[] args = new String[]{sourceDirectory, outputDirectory};

            Path path = Paths.get(args.length > 0 ? args[0] : "src/main/proto/");*/
            outdir = outputDirectory.getAbsolutePath();
            //System.err.println("executing out: " + outdir + " in: " + sourceDirectory.getAbsolutePath());

            Files.walkFileTree(Paths.get(sourceDirectory.getCanonicalPath()), new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    boolean regularFile = attrs.isRegularFile();
                    if (regularFile) {
                        try {

                            Proto target = new Proto();
                            ProtoUtil.loadFrom(Files.newBufferedReader(file), target);
                            String javaPackageName = target.getJavaPackageName();

                            Path directories = Files.createDirectories(Paths.get(outdir, javaPackageName.split("\\.")));
                            //System.err.println("writing to " + directories.toUri());

                            target.getMessages().forEach(PrautoGen::descend);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        enums.forEach(PrautoGen::printEnum);
                        messages.forEach(PrautoGen::printMessage);
                    }
                    return FileVisitResult.CONTINUE;
                }


            });
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        Message message;

        public StripeLeveler(Message message) {
            this.message = message;
        }

        void addField(Field<?> field, String javaType1) {
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
