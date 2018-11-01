import org.apache.log4j.Logger;

import java.io.IOException;

public class Main {
    private static final String SRC_MAIN_RESOURCES = "./src/main/resources";
    private final static Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        final IConverter converter = new PropertiesConverter();
        try {
            converter.toProperties(String.join("/", SRC_MAIN_RESOURCES, "test.json"), String.join("/", SRC_MAIN_RESOURCES, "app.properties"));
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("Need 2 parameters!!!");
        }
    }
}
