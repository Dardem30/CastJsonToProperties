import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public interface IConverter {
    static String readFile(String filename) throws IOException {
        String result = "";
        try (final BufferedReader bufferedReader = new BufferedReader(new FileReader(filename))) {
            String line = bufferedReader.readLine();
            while (line != null) {
                result += line;
                line = bufferedReader.readLine();
            }
        }
        return result;
    };

    void toProperties(String jsonPath, String propertiesPath) throws IOException;
}
