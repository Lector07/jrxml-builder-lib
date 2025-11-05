//package pl.lib.automation;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class JsonReportGeneratorTest {
//
//    private JsonReportGenerator generator;
//    private final ObjectMapper mapper = new ObjectMapper();
//
//    @BeforeEach
//    void setUp() {
//        generator = new JsonReportGenerator();
//    }
//
//    @Test
//    @DisplayName("Powinien poprawnie spłaszczyć prosty JSON z obiektami i wartościami")
//    void shouldFlattenSimpleJson() throws IOException {
//        String json = "{" +
//                "  \"wstep\": { \"autor\": \"Jan Kowalski\", \"data\": \"2025-11-03\" }," +
//                "  \"podpis\": \"Z poważaniem\"" +
//                "}";
//        JsonNode rootNode = mapper.readTree(json);
//
//        List<JsonReportGenerator.ReportElement> result = generator.flattenJsonForDebugging(rootNode);
//
//        assertThat(result).hasSize(4);
//
//        assertThat(result.get(0).type).isEqualTo("HEADER");
//        assertThat(result.get(0).level).isEqualTo(1);
//        assertThat(result.get(0).text).isEqualTo("wstep");
//
//        assertThat(result.get(1).type).isEqualTo("KEY_VALUE");
//        assertThat(result.get(1).level).isEqualTo(2);
//        assertThat(result.get(1).text).isEqualTo("autor");
//        assertThat(result.get(1).value).isEqualTo("Jan Kowalski");
//
//        assertThat(result.get(2).type).isEqualTo("KEY_VALUE");
//        assertThat(result.get(2).level).isEqualTo(2);
//        assertThat(result.get(2).text).isEqualTo("data");
//        assertThat(result.get(2).value).isEqualTo("2025-11-03");
//
//        assertThat(result.get(3).type).isEqualTo("KEY_VALUE");
//        assertThat(result.get(3).level).isEqualTo(1);
//        assertThat(result.get(3).text).isEqualTo("podpis");
//        assertThat(result.get(3).value).isEqualTo("Z poważaniem");
//    }
//
//    @Test
//    @DisplayName("Powinien poprawnie obsłużyć zagnieżdżone obiekty i tabele")
//    void shouldHandleNestedObjectsAndTables() throws IOException {
//        String json = "{" +
//                "  \"analiza\": {" +
//                "    \"podsumowanie\": { \"status\": \"Zakończono\" }," +
//                "    \"dane_szczegolowe\": [" +
//                "      { \"id\": 1, \"nazwa\": \"Produkt A\" }," +
//                "      { \"id\": 2, \"nazwa\": \"Produkt B\" }" +
//                "    ]" +
//                "  }" +
//                "}";
//        JsonNode rootNode = mapper.readTree(json);
//
//        List<JsonReportGenerator.ReportElement> result = generator.flattenJsonForDebugging(rootNode);
//
//        assertThat(result).hasSize(4);
//
//        assertThat(result.get(0).type).isEqualTo("HEADER");
//        assertThat(result.get(0).level).isEqualTo(1);
//        assertThat(result.get(0).text).isEqualTo("analiza");
//
//        assertThat(result.get(1).type).isEqualTo("HEADER");
//        assertThat(result.get(1).level).isEqualTo(2);
//        assertThat(result.get(1).text).isEqualTo("podsumowanie");
//
//        assertThat(result.get(2).type).isEqualTo("KEY_VALUE");
//        assertThat(result.get(2).level).isEqualTo(3);
//        assertThat(result.get(2).text).isEqualTo("status");
//        assertThat(result.get(2).value).isEqualTo("Zakończono");
//
//        assertThat(result.get(3).type).isEqualTo("TABLE");
//        assertThat(result.get(3).level).isEqualTo(2);
//        assertThat(result.get(3).text).isEqualTo("dane_szczegolowe");
//        assertThat(result.get(3).rawTableData).isNotNull();
//        assertThat(result.get(3).rawTableData.isArray()).isTrue();
//        assertThat(result.get(3).rawTableData.size()).isEqualTo(2);
//        assertThat(result.get(3).rawTableData.get(0).get("nazwa").asText()).isEqualTo("Produkt A");
//    }
//
//    @Test
//    @DisplayName("Powinien zwrócić pustą listę dla pustego JSON")
//    void shouldReturnEmptyListForEmptyJson() throws IOException {
//        JsonNode rootNode = mapper.readTree("{}");
//        List<JsonReportGenerator.ReportElement> result = generator.flattenJsonForDebugging(rootNode);
//        assertThat(result).isEmpty();
//    }
//
//    @Test
//    @DisplayName("Powinien zignorować pustą tablicę w JSON")
//    void shouldIgnoreEmptyArray() throws IOException {
//        JsonNode rootNode = mapper.readTree("{\"pusta_tabela\": [], \"inne_pole\": \"wartosc\"}");
//        List<JsonReportGenerator.ReportElement> result = generator.flattenJsonForDebugging(rootNode);
//
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).type).isEqualTo("KEY_VALUE");
//        assertThat(result.get(0).text).isEqualTo("inne_pole");
//        assertThat(result.get(0).value).isEqualTo("wartosc");
//    }
//
//    @Test
//    @DisplayName("Powinien obsłużyć tablicę z prostymi wartościami (nie obiektami)")
//    void shouldHandleArrayOfPrimitives() throws IOException {
//        JsonNode rootNode = mapper.readTree("{\"liczby\": [1, 2, 3], \"tekst\": \"test\"}");
//        List<JsonReportGenerator.ReportElement> result = generator.flattenJsonForDebugging(rootNode);
//
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).type).isEqualTo("KEY_VALUE");
//        assertThat(result.get(0).text).isEqualTo("tekst");
//    }
//
//    @Test
//    @DisplayName("Powinien poprawnie obsłużyć wartości null")
//    void shouldHandleNullValues() throws IOException {
//        JsonNode rootNode = mapper.readTree("{\"pole_null\": null, \"pole_tekstowe\": \"wartość\"}");
//        List<JsonReportGenerator.ReportElement> result = generator.flattenJsonForDebugging(rootNode);
//
//        assertThat(result).hasSize(2);
//        assertThat(result.get(0).type).isEqualTo("KEY_VALUE");
//        assertThat(result.get(0).text).isEqualTo("pole_null");
//        assertThat(result.get(0).value).isEqualTo("null");
//
//        assertThat(result.get(1).type).isEqualTo("KEY_VALUE");
//        assertThat(result.get(1).text).isEqualTo("pole_tekstowe");
//        assertThat(result.get(1).value).isEqualTo("wartość");
//    }
//
//    @Test
//    @DisplayName("Powinien obsłużyć głęboko zagnieżdżoną strukturę")
//    void shouldHandleDeeplyNestedStructure() throws IOException {
//        String json = "{" +
//                "  \"poziom1\": {" +
//                "    \"poziom2\": {" +
//                "      \"poziom3\": {" +
//                "        \"wartosc\": \"głęboko\"" +
//                "      }" +
//                "    }" +
//                "  }" +
//                "}";
//        JsonNode rootNode = mapper.readTree(json);
//
//        List<JsonReportGenerator.ReportElement> result = generator.flattenJsonForDebugging(rootNode);
//
//        assertThat(result).hasSize(4);
//        assertThat(result.get(0).type).isEqualTo("HEADER");
//        assertThat(result.get(0).level).isEqualTo(1);
//        assertThat(result.get(1).type).isEqualTo("HEADER");
//        assertThat(result.get(1).level).isEqualTo(2);
//        assertThat(result.get(2).type).isEqualTo("HEADER");
//        assertThat(result.get(2).level).isEqualTo(3);
//        assertThat(result.get(3).type).isEqualTo("KEY_VALUE");
//        assertThat(result.get(3).level).isEqualTo(4);
//        assertThat(result.get(3).value).isEqualTo("głęboko");
//    }
//
//    @Test
//    @DisplayName("Powinien obsłużyć wiele tabel na tym samym poziomie")
//    void shouldHandleMultipleTablesAtSameLevel() throws IOException {
//        String json = "{" +
//                "  \"zamowienia\": [" +
//                "    { \"id\": 1 }" +
//                "  ]," +
//                "  \"produkty\": [" +
//                "    { \"nazwa\": \"A\" }" +
//                "  ]" +
//                "}";
//        JsonNode rootNode = mapper.readTree(json);
//
//        List<JsonReportGenerator.ReportElement> result = generator.flattenJsonForDebugging(rootNode);
//
//        assertThat(result).hasSize(2);
//        assertThat(result.get(0).type).isEqualTo("TABLE");
//        assertThat(result.get(0).text).isEqualTo("zamowienia");
//        assertThat(result.get(1).type).isEqualTo("TABLE");
//        assertThat(result.get(1).text).isEqualTo("produkty");
//    }
//}
