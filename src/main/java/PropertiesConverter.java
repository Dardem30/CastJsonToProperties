import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PropertiesConverter implements IConverter {
    private boolean flag = false;
    private String keys = "";

    @Override
    public void toProperties(final String jsonPath, final String propertiesPath) throws IOException {
        final String text = IConverter.readFile(jsonPath);
        final Properties properties = new Properties();
        properties.load(new FileInputStream(propertiesPath));
        final JSONObject jsonObject = new JSONObject(text);
//        jsonObject.toMap().forEach((key, value) -> {
//            if (value instanceof ArrayList) {
//                ArrayList list =
//                        (ArrayList) value;
//                IntStream.range(0, list.size())
//                        .forEach(index -> properties.put(key + "." + (index + 1), list.get(index)));
//            } else if (value instanceof Map) {
//                jsonObjectEntry(key, properties, (Map<String, Object>) value);
//            } else {
//                //  properties.put(key, String.valueOf(value));
//            }
//        });
        //todo make stream collect
        Stream<Map.Entry<String, Object>> stream = jsonObject.toMap().entrySet().stream();
        properties.putAll(stream
                .peek(entry -> {
                    if (entry.getValue() instanceof Map)
                        jsonObjectEntry(entry.getKey(), properties, (Map<String, Object>) entry.getValue());
                })
                .filter(entry -> !(entry.getValue() instanceof Map))
                .peek(entry -> {
                    if (entry.getValue() instanceof ArrayList) {
                        ArrayList list =
                                (ArrayList) entry.getValue();
                        IntStream.range(0, list.size())
                                .forEach(index -> properties.put(entry.getKey() + "." + (index + 1), list.get(index)));
                    }
                })
                .filter(entry -> !(entry.getValue() instanceof ArrayList))
                .collect(Collectors.toMap(val -> val.getKey(), val -> String.valueOf(val.getValue()))));

        FileWriter writer = new FileWriter(propertiesPath);
        properties.store(writer, null);
        writer.close();
    }

    private void jsonObjectEntry(final Object key, final Properties properties, final Map<String, Object> map) {
        //todo stream ??? collect ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        map.forEach((enKey, enValue) -> {
            if (enValue instanceof HashMap) {

                keys = keys.equals("") ? key + "." + enKey : keys + "." + enKey;

                final Map<String, Object> recMap = (Map<String, Object>) enValue;
                if (recMap.entrySet().stream().anyMatch(recEntry -> recEntry.getValue() instanceof Map)) {
                    flag = true;
                }

                if (flag) {
                    jsonObjectEntry(keys, properties, recMap);
                    flag = false;
                } else {
                    String fakeKey = keys;
                    keys = keys.split("\\.")[0];
                    jsonObjectEntry(fakeKey, properties, recMap);
                }
            } else if (enValue instanceof ArrayList) {
                ArrayList list =
                        (ArrayList) enValue;
                IntStream.range(0, list.size())
                        .forEach(index -> properties.put(key + "." + enKey + "." + (index + 1), list.get(index)));
            } else {
                properties.put(key + "." + enKey, String.valueOf(enValue));
            }
        });
    }
}