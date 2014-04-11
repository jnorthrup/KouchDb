package kouchdb;

/**
 * config options via env, -Dkocuh.var.name, and json ini file.
 */
public interface  Config {

      enum fetchConfig {
        env {
            @Override
            <T> T value(String key, String systemKey) {
                return (T) System.getenv(key);
            }
        },
        system {
            @Override
            <T> T value(String key, String systemKey) {
                return (T) System.getProperty(systemKey);
            }
        },

/*        properties {
                            Path kouch_ini = Paths.get(get("KOUCH_INI", "kouch.ini"));

           ResourceBundle p=   ResourceBundle.getBundle( kouch_ini.toString() );
            @Override
            <T> T value(String key, String systemKey) {
                T object = (T) p.getObject(key);
                return object==null? (T) p.getObject(systemKey) :object;
            }
        },
        json_ini {
            Map<String, ?> bootstrap = new FastMap<>();

            {
                Path kouch_ini = Paths.get(get("KOUCH_INI_JSON", "kouch.ini.json"));

                try (FileInputStream in = new FileInputStream(kouch_ini.toFile()); InputStreamReader fileInputStream = new InputStreamReader(in)) {
                    Gson gson = new Gson();
                    bootstrap.putAll(gson.<Map>fromJson(fileInputStream, Map.class));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            <T> T value(String key, String systemKey) {
                Object o = bootstrap.get(key);
                return (T) (o ==null? bootstrap.get(systemKey):o);
            }
        },*/;

        abstract <T> Object value(String key, String systemKey);


    }

      static String getSystemKey(String kouch_var) {
        return "kouch." + kouch_var.toLowerCase().replaceAll("^kouch_?", "").replace('_', '.');
    }

   public    static String get(String kouch_var, String... defaultVal) {

        Object var = null;
        String systemKey = getSystemKey(kouch_var);

        for (fetchConfig fetchConfig : Config.fetchConfig.values()) {

                var = fetchConfig.value(kouch_var, systemKey);
                if (null != var) break;

        }
        if (null == var) if (defaultVal.length > 0) var = defaultVal[0];
        if (null != var) {
            String value = String.valueOf(var);
            System.setProperty(systemKey, value);
            System.err.println("// -D" + systemKey + "=" + "\"" + value + "\"");
            //noinspection RedundantStringConstructorCall
            return new String(value); //noleak
        }
        return null;
    }

}


