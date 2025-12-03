package pl.lib.automation.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeaderFormatter {

    private Map<Integer, Integer> levelCounters = new HashMap<>();

    public static String formatHeaderName(String jsonFieldName) {
        if(jsonFieldName == null || jsonFieldName.isEmpty()){
            return jsonFieldName;
        }

        String name = jsonFieldName;

        if(name.contains("_")){
            String[] parts = name.split("_", -1);
            name = parts[parts.length - 1];
            if(name.isEmpty()) {
                return "";
            }
        }

        name = name.replaceAll("([a-z])([A-Z])", "$1 $2");
        name = name.replace("_", " ");
        name = name.trim().replaceAll("\\s+", " ");

        String[] words = name.split("\\s+");
        StringBuilder formatted = new StringBuilder();
        for(String word : words){
            if(!word.isEmpty()){
                formatted.append(Character.toUpperCase(word.charAt(0)));
                if(word.length() > 1) {
                    formatted.append(word.substring(1).toLowerCase());
                }
                formatted.append(" ");
            }
        }

        return formatted.toString().trim();
    }

    public String formatNumberedHeader(String text, List<Integer> numberPath) {
        if (numberPath == null || numberPath.isEmpty()) {
            return text;
        }

        StringBuilder numbering = new StringBuilder();
        for (int i = 0; i < numberPath.size(); i++) {
            numbering.append(numberPath.get(i));
            if (i < numberPath.size() - 1) {
                numbering.append(".");
            }
        }
        numbering.append(". ");

        return numbering.toString() + text;
    }

    public String getNextNumber(int level) {
        resetDeeperLevels(level);

        int currentCounter = levelCounters.getOrDefault(level, 0);
        currentCounter++;
        levelCounters.put(level, currentCounter);

        List<Integer> path = new ArrayList<>();
        for (int i = 1; i <= level; i++) {
            path.add(levelCounters.getOrDefault(i, 1));
        }

        return buildNumberString(path);
    }

    public void resetCounters() {
        levelCounters.clear();
    }

    private void resetDeeperLevels(int level) {
        List<Integer> keysToRemove = new ArrayList<>();
        for (Integer key : levelCounters.keySet()) {
            if (key > level) {
                keysToRemove.add(key);
            }
        }
        for (Integer key : keysToRemove) {
            levelCounters.remove(key);
        }
    }

    private String buildNumberString(List<Integer> path) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            result.append(path.get(i));
            if (i < path.size() - 1) {
                result.append(".");
            }
        }
        return result.toString();
    }

    public List<Integer> getCurrentNumberPath(int level) {
        List<Integer> path = new ArrayList<>();
        for (int i = 1; i <= level; i++) {
            path.add(levelCounters.getOrDefault(i, 0));
        }
        return path;
    }
}
