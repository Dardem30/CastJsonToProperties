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
        //todo make stream collect
        jsonObject.toMap().forEach((key, value) -> {
            if (value instanceof ArrayList) {
                ArrayList list =
                        (ArrayList) value;
                IntStream.range(0, list.size())
                        .forEach(index -> properties.put(key + "." + (index + 1), list.get(index)));
            } else if (value instanceof Map) {
                jsonObjectEntry(key, properties, (Map<String, Object>) value);
            } else {
                //  properties.put(key, String.valueOf(value));
            }
        });
        Stream<Map.Entry<String, Object>> stream = jsonObject.toMap().entrySet().stream();
       /////// collect
//        List<Map.Entry<String, Object>> list = stream.filter(entry -> !(entry.getValue() instanceof Map) && !(entry.getValue() instanceof ArrayList))
//                .collect(Collectors.toList());
//
//        for (Map.Entry entry : list)
//            properties.put(entry.getKey(), String.valueOf(entry.getValue()));
        stream.filter(entry -> !(entry.getValue() instanceof Map) && !(entry.getValue() instanceof ArrayList))
                .forEach(entry -> properties.put(entry.getKey(), String.valueOf(entry.getValue())));
        stream.filter(entry -> entry instanceof ArrayList)
                .forEach(entry -> {
                    ArrayList list =
                            (ArrayList) entry.getValue();
                    IntStream.range(0, list.size())
                            .forEach(index -> properties.put(entry.getKey() + "." + (index + 1), list.get(index)));
                });
        stream.filter(entry -> entry instanceof Map)
                .forEach(entry -> jsonObjectEntry(entry.getKey(), properties, (Map<String, Object>) entry.getValue()));

        FileWriter writer = new FileWriter(propertiesPath);
        properties.store(writer, null);
        writer.close();
    }

    private void jsonObjectEntry(final Object key, final Properties properties, final Map<String, Object> map) {
        //todo stream ??? collect
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