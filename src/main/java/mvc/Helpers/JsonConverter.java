package mvc.Helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonConverter {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Convert List<T> to JSON String
    public static <T> String serialize(ArrayList<T> list) throws JsonProcessingException {
        return objectMapper.writeValueAsString(list);
    }

    // Convert a single object of type T to JSON String
    public static <T> String serialize(T item) throws JsonProcessingException {
        return objectMapper.writeValueAsString(item);
    }

    // Convert JSON String to List<T>
    public static <T> List<T> deserialize(String json, Class<T> type) throws JsonProcessingException {
        // Check if the JSON is a list or a single object
        if (json.trim().startsWith("[")) {
            // Deserialize as a list
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, type));
        } else {
            // Deserialize as a single object and return a list with the single object
            T singleObject = objectMapper.readValue(json, type);
            return Collections.singletonList(singleObject);
        }

    }
}