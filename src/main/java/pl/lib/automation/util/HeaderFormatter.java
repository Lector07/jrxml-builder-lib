package pl.lib.automation.util;

public class HeaderFormatter {
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
}
