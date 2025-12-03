package pl.lib.automation.util;

import com.fasterxml.jackson.databind.JsonNode;
import pl.lib.model.DataType;

public class JsonDataTypeGuesser {


    public static DataType guessType(JsonNode arrayData, String fieldName) {
        if (!arrayData.isArray() || arrayData.isEmpty()) {
            return DataType.STRING;
        }

        for (JsonNode row : arrayData) {
            if (row.has(fieldName)) {
                JsonNode field = row.get(fieldName);
                if (field.isNumber()) {
                    return DataType.DOUBLE;
                }
                if (field.isBoolean()) {
                    return DataType.BOOLEAN;
                }
            }
        }
        return DataType.STRING;
    }
}
