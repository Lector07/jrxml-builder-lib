# JRXML Builder Library

Biblioteka `jrxml-builder-lib` to narzędzie w języku Java, które dynamicznie generuje raporty JasperReports na podstawie konfiguracji zdefiniowanej w obiektach Javy lub w plikach JSON. Umożliwia tworzenie złożonych raportów z grupowaniem, sumowaniem, stylizowaniem i podraportami bez potrzeby ręcznego pisania plików `.jrxml`.

## Główne Funkcje

- **Dynamiczne generowanie raportów**: Tworzenie szablonów `.jrxml` w locie na podstawie konfiguracji.
- **Konfiguracja przez JSON**: Definiowanie struktury raportu (kolumny, grupy, style) w pliku JSON.
- **Fluent API**: Możliwość programistycznego budowania raportów za pomocą czytelnego, łańcuchowego API (`ReportBuilder`).
- **Obsługa grupowania i agregacji**: Automatyczne obliczanie sum, średnich i innych wartości dla grup.
- **Elastyczne stylowanie**: Definiowanie stylów dla nagłówków, komórek i grup.
- **Obsługa podraportów**: Możliwość osadzania jednego raportu w drugim.

## Instalacja

Aby dodać bibliotekę do swojego projektu Maven, dodaj następującą zależność do pliku `pom.xml`:

```xml
<dependency>
    <groupId>pl.lib</groupId>
    <artifactId>jrxml-builder-lib</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

Upewnij się również, że w Twoim projekcie są dostępne zależności `JasperReports` oraz `Jackson`, które są kluczowe dla działania biblioteki.

```xml
<!-- JasperReports -->
<dependency>
    <groupId>net.sf.jasperreports</groupId>
    <artifactId>jasperreports</artifactId>
    <version>6.21.3</version>
</dependency>

<!-- Jackson (do obsługi JSON) -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.17.1</version>
</dependency>
```

## Architektura

Biblioteka składa się z kilku kluczowych komponentów:

- **`JsonReportGenerator`**: Główna klasa usługowa, która przyjmuje konfigurację (`ReportConfig`) oraz dane (np. `List<Map<String, Object>>`) i generuje gotowy raport `JasperPrint`.
- **`ReportConfig`**: Obiekt (POJO) przechowujący pełną definicję raportu: tytuł, orientację strony, kolumny, grupy, informacje o firmie itp. Może być tworzony za pomocą wzorca budowniczego (Builder) lub deserializowany z pliku JSON.
- **`ReportBuilder`**: Wewnętrzny mechanizm, który na podstawie `ReportConfig` buduje obiekt `JasperDesign` krok po kroku, używając API JasperReports.
- **`ColumnDefinition` i `GroupDefinition`**: Obiekty konfiguracyjne dla kolumn i grup, określające ich właściwości (np. nazwa pola, tytuł, szerokość, styl, typ danych).
- **Modele (`Column`, `Group`, `Style` itp.)**: Wewnętrzne obiekty używane przez `ReportBuilder` do reprezentowania elementów raportu.

## Jak używać

Głównym sposobem użycia biblioteki jest stworzenie konfiguracji raportu, a następnie przekazanie jej wraz z danymi do `JsonReportGenerator`.

### Krok 1: Przygotuj dane

Dane do raportu powinny być listą obiektów. Najbardziej elastycznym podejściem jest użycie `List<Map<String, Object>>`, gdzie każda mapa reprezentuje jeden wiersz.

```java
// Przykładowe dane
List<Map<String, Object>> data = new ArrayList<>();
Map<String, Object> row1 = new HashMap<>();
row1.put("miasto", "Warszawa");
row1.put("kategoria", "A");
row1.put("wartosc", 100.0);
data.add(row1);

Map<String, Object> row2 = new HashMap<>();
row2.put("miasto", "Kraków");
row2.put("kategoria", "A");
row2.put("wartosc", 150.0);
data.add(row2);
```

### Krok 2: Zdefiniuj konfigurację raportu (w JSON)

Stwórz plik JSON (np. `report-config.json`), który opisuje strukturę Twojego raportu.

```json
{
  "title": "Przykładowy Raport",
  "orientation": "PORTRAIT",
  "pageFooterEnabled": true,
  "columns": [
    {
      "fieldName": "miasto",
      "title": "Miasto",
      "type": "STRING",
      "width": 150
    },
    {
      "fieldName": "kategoria",
      "title": "Kategoria",
      "type": "STRING",
      "width": 100
    },
    {
      "fieldName": "wartosc",
      "title": "Wartość",
      "type": "DOUBLE",
      "width": 100,
      "pattern": "#,##0.00",
      "styleName": "NUMERIC_STYLE"
    }
  ],
  "groups": [
    {
      "fieldName": "kategoria",
      "headerExpression": "\"Kategoria: \" + $F{kategoria}",
      "showGroupHeader": true,
      "showGroupFooter": true,
      "styleName": "GROUP_HEADER_STYLE"
    }
  ]
}
```

### Krok 3: Wygeneruj raport

Użyj klasy `JsonReportGenerator`, aby wczytać konfigurację z pliku JSON, a następnie wygenerować raport.

```java
import pl.lib.automation.JsonReportGenerator;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ReportExample {

    public static void main(String[] args) {
        try {
            // Przygotowanie danych (jak w kroku 1)
            List<Map<String, Object>> data = prepareData();

            // Wczytanie konfiguracji z pliku JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonReportGenerator.ReportStructure reportStructure = mapper.readValue(
                new File("path/to/report-config.json"),
                JsonReportGenerator.ReportStructure.class
            );

            // Inicjalizacja generatora
            JsonReportGenerator generator = new JsonReportGenerator();

            // Generowanie raportu
            JasperPrint jasperPrint = generator.generateReport(reportStructure.getConfig(), data);

            // Wyświetlenie lub eksport raportu
            JasperViewer.viewReport(jasperPrint, false);
            // lub
            // JasperExportManager.exportReportToPdfFile(jasperPrint, "raport.pdf");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<Map<String, Object>> prepareData() {
        // ... implementacja przygotowania danych
        return new ArrayList<>();
    }
}
```

## Współtworzenie

Jeśli chcesz współtworzyć projekt, postępuj zgodnie z poniższymi krokami:

1.  Zrób `fork` repozytorium.
2.  Stwórz nową gałąź (`git checkout -b feature/nazwa-funkcjonalnosci`).
3.  Wprowadź zmiany i zrób `commit` (`git commit -m 'Dodaj nową funkcjonalność'`).
4.  Wypchnij zmiany do gałęzi (`git push origin feature/nazwa-funkcjonalnosci`).
5.  Otwórz `Pull Request`.

## Licencja

Ten projekt jest udostępniany na licencji MIT. Zobacz plik `LICENSE`, aby uzyskać więcej informacji.

