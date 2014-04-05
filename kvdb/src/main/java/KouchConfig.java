import com.google.gson.Gson;
import javolution.util.FastMap;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;


public class KouchConfig {
    static Map<String, ?> bootstrap = new FastMap<>();

    static {
        Path kouch_ini = Paths.get(getVar("KOUCH_INI_JSON", "kouch.ini.json"));
        try (FileInputStream in = new FileInputStream(kouch_ini.toFile()); InputStreamReader fileInputStream = new InputStreamReader(in)) {
            Gson gson = new Gson();
            bootstrap.putAll(gson.<Map>fromJson(fileInputStream, Map.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getVar(String kouch_var, String... defaultVal) {
        String javapropname =
                "kouch." + kouch_var.toLowerCase().replaceAll("^kouch_?", "").replace('_', '.');
        String kouchenv = System.getenv(kouch_var);
        String var = null == kouchenv ? System.getProperty(javapropname) : kouchenv;
        var = null == var ? bootstrap.get(javapropname).toString() : var;
        var = null == var && defaultVal.length > 0 ? defaultVal[0] : var;
        if (null != var) {
            System.setProperty(javapropname, var);
            System.err.println("// -D" + javapropname + "=" + "\"" + var + "\"");
            //noinspection RedundantStringConstructorCall
            return new String(var); //noleak
        }
        return null;
    }
}