# JRXML Builder Library

Biblioteka `jrxml-builder-lib` to narzędzie w języku Java, które dynamicznie generuje raporty JasperReports na podstawie konfiguracji zdefiniowanej w obiektach Javy lub w plikach JSON. Umożliwia tworzenie złożonych raportów z grupowaniem, sumowaniem, stylizowaniem i podraportami bez potrzeby ręcznego pisania plików `.jrxml`.

## Główne Funkcje

- **Dynamiczne generowanie raportów**: Tworzenie szablonów `.jrxml` w locie na podstawie konfiguracji.
- **Konfiguracja przez JSON**: Definiowanie struktury raportu (kolumny, grupy, style) w pliku JSON.
- **Fluent API**: Możliwość programistycznego budowania raportów za pomocą czytelnego, łańcuchowego API (`ReportBuilder`).
- **Obsługa grupowania i agregacji**: Automatyczne obliczanie sum, średnich i innych wartości dla grup.
- **Elastyczne stylowanie**: Definiowanie stylów dla nagłówków, komórek i grup z gotowymi motywami.
- **Obsługa podraportów**: Możliwość osadzania jednego raportu w drugim.
- **Formatowanie warunkowe**: Podświetlanie komórek na podstawie wartości danych.
- **Obsługa różnych typów danych**: Automatyczne rozpoznawanie i konwersja typów (String, BigDecimal, Date, Boolean).

## Instalacja

Aby dodać bibliotekę do swojego projektu Maven, dodaj następującą zależność do pliku `pom.xml`:

```xml
<dependency>
    <groupId>pl.lib</groupId>
    <artifactId>jrxml-builder-lib</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

Upewnij się również, że w Twoim projekcie są dostępne zależności `JasperReports` oraz `Jackson`, które są kluczowe dla działania biblioteki:

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

- **`AutomatedReportService`**: Główna klasa usługowa oferująca wysokopoziomowe API do generowania raportów PDF z JSON.
- **`JsonReportGenerator`**: Silnik generujący raporty z automatyczną analizą struktury JSON i obsługą podraportów.
- **`ReportConfig`**: Obiekt (POJO) przechowujący pełną definicję raportu: tytuł, orientację strony, kolumny, grupy, informacje o firmie itp. Może być tworzony za pomocą wzorca budowniczego (Builder) lub deserializowany z pliku JSON.
- **`ReportBuilder`**: Wewnętrzny mechanizm, który na podstawie `ReportConfig` buduje obiekt `JasperDesign` krok po kroku, używając API JasperReports.
- **`ColumnDefinition` i `GroupDefinition`**: Obiekty konfiguracyjne dla kolumn i grup, określające ich właściwości (np. nazwa pola, tytuł, szerokość, styl, typ danych).
- **Modele (`Column`, `Group`, `Style` itp.)**: Wewnętrzne obiekty używane przez `ReportBuilder` do reprezentowania elementów raportu.

## Przykład użycia

Poniżej przykład użycia biblioteki w aplikacji Spring Boot do generowania raportu wizyt w klinice:

```java
// Serwis do generowania raportów
@Service
public class ClinicReportService {
    
    public byte[] generateVisitsPdf(ArrayNode visits) throws JRException {
        // 1) Opcje formatowania (zebra, zakładki PDF, reguły podświetlania)
        FormattingOptions fmt = new FormattingOptions();
        fmt.setZebraStripes(true);
        fmt.setGenerateBookmarks(true);
        fmt.setBookmarkField("patient.name");
        fmt.setHighlightRules(Arrays.asList(
            new HighlightRule("billing.amount", "GREATER_THAN", "5000", "#FFCCCC")
        ));

        // 2) Konfiguracja raportu
        ReportConfig config = new ReportConfig.Builder()
            .title("Raport wizyt")
            .addColumn(ColumnDefinition.builder("patient.name")
                .header("Pacjent")
                .width(160)
                .visible(true)
                .build())
            .addColumn(ColumnDefinition.builder("doctor.name")
                .header("Lekarz")
                .width(140)
                .visible(true)
                .build())
            .addColumn(ColumnDefinition.builder("visit.date")
                .header("Data wizyty")
                .width(120)
                .format("dd.MM.yyyy")
                .visible(true)
                .build())
            .addColumn(ColumnDefinition.builder("billing.amount")
                .header("Kwota")
                .width(100)
                .format("#,##0.00 zł")
                .reportCalculation(Calculation.SUM)     // suma w podsumowaniu raportu
                .groupCalculation(Calculation.SUM)      // suma w nagłówku grupy
                .visible(true)
                .build())
            .addGroup(GroupDefinition.builder("doctor.name")
                .label("Lekarz: ")
                .showHeader(true)
                .showFooter(true)
                .ascending(true)
                .build())
            .companyInfo(CompanyInfo.builder("Klinika Chirurgii Plastycznej XYZ")
                .address("ul. Zdrowa 1")
                .location("00-001", "Warszawa")
                .taxId("PL1234567890")
                .build())
            .addFormattingOption(fmt)
            .margins(Arrays.asList(20, 20, 20, 20))
            .withPageFooterEnabled(true)
            .withSummaryBandEnabled(true)
            .build();

        // Ustawienia dodatkowe
        config.setOrientation("LANDSCAPE");
        config.setFooterLeftText("Dokument wygenerowany automatycznie");
        config.setTheme("MODERN"); // DEFAULT, CLASSIC, MODERN, CORPORATE, MINIMAL

        // 3) Wygenerowanie raportu z JSON + konfiguracji i zapis do PDF
        AutomatedReportService service = new AutomatedReportService();
        JasperPrint print = service.generateReportFromArray(visits, config);
        return JasperExportManager.exportReportToPdf(print);
    }
}

// Kontroler HTTP
@RestController
@RequestMapping("/reports")
public class ReportController {
    
    private final ClinicReportService service;
    
    public ReportController(ClinicReportService service) {
        this.service = service;
    }
    
    @PostMapping(value = "/visits", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> visits(@RequestBody JsonNode body) throws JRException {
        if (!body.isArray()) {
            return ResponseEntity.badRequest().build();
        }
        byte[] pdf = service.generateVisitsPdf((ArrayNode) body);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"wizyty.pdf\"")
                .body(pdf);
    }
}
```

### Przykładowe dane wejściowe

```json
[
  {
    "patient": { "name": "Anna Kowalska" },
    "doctor": { "name": "dr Nowak" },
    "visit": { "date": "2025-05-10T00:00:00Z" },
    "billing": { "amount": 4200.00 }
  },
  {
    "patient": { "name": "Jan Nowak" },
    "doctor": { "name": "dr Nowak" },
    "visit": { "date": "2025-05-12T00:00:00Z" },
    "billing": { "amount": 2100.00 }
  },
  {
    "patient": { "name": "Ewa Wiśniewska" },
    "doctor": { "name": "dr Kowalski" },
    "visit": { "date": "2025-05-13T00:00:00Z" },
    "billing": { "amount": 5900.00 }
  }
]
```

## Konfiguracja przez JSON

Alternatywnie, można zdefiniować konfigurację w pliku JSON:

```json
{
  "title": "Raport sprzedaży",
  "orientation": "LANDSCAPE",
  "pageFooterEnabled": true,
  "summaryBandEnabled": true,
  "theme": "CORPORATE",
  "footerLeftText": "Wygenerowano automatycznie",
  "margins": [20, 20, 20, 20],
  "columns": [
    {
      "field": "product",
      "header": "Produkt",
      "width": 200,
      "visible": true
    },
    {
      "field": "category",
      "header": "Kategoria", 
      "width": 150,
      "visible": true
    },
    {
      "field": "price",
      "header": "Cena",
      "format": "#,##0.00 zł",
      "width": 120,
      "reportCalculation": "SUM",
      "groupCalculation": "SUM",
      "visible": true
    }
  ],
  "groups": [
    {
      "field": "category",
      "label": "Kategoria: ",
      "showHeader": true,
      "showFooter": true,
      "ascending": true
    }
  ],
  "companyInfo": {
    "name": "Moja Firma",
    "address": "ul. Przykładowa 1",
    "postalCode": "00-001",
    "city": "Warszawa",
    "taxId": "PL1234567890"
  },
  "formattingOptions": {
    "zebraStripes": true,
    "generateBookmarks": true,
    "bookmarkField": "product",
    "highlightRules": [
      {
        "field": "price",
        "operator": "GREATER_THAN",
        "value": "1000",
        "color": "#FFCCCC"
      }
    ]
  }
}
```

## Obsługiwane funkcje

### Typy danych
- **STRING** - tekst
- **INTEGER**, **LONG**, **DOUBLE**, **BIG_DECIMAL**, **FLOAT**, **SHORT** - liczby
- **DATE**, **TIME**, **TIMESTAMP** - daty i czas (obsługa ISO 8601)
- **BOOLEAN** - wartości logiczne
- **JR_DATA_SOURCE** - zagnieżdżone tablice (podraporty)

### Kalkulacje
- **SUM** - suma wartości
- **COUNT** - liczba rekordów
- **AVERAGE** - średnia arytmetyczna
- **LOWEST** / **HIGHEST** - wartość minimalna/maksymalna
- **STANDARD_DEVIATION** / **VARIANCE** - odchylenie standardowe/wariancja
- **DISTINCT_COUNT** - liczba unikalnych wartości

### Motywy wizualne
- **DEFAULT** - standardowy motyw
- **CLASSIC** - klasyczny motyw
- **MODERN** - nowoczesny motyw
- **CORPORATE** - motyw korporacyjny
- **MINIMAL** - minimalistyczny motyw

### Formatowanie warunkowe
Operatory porównania:
- **EQUALS** / **NOT_EQUALS** - równość/nierówność
- **CONTAINS** - zawiera tekst (tylko dla pól tekstowych)
- **GREATER_THAN** / **LESS_THAN** - większe/mniejsze od (tylko dla liczb)

## Funkcje zaawansowane

### Podraporty
Biblioteka automatycznie wykrywa zagnieżdżone tablice w JSON i tworzy dla nich podraporty:

```json
{
  "customer": "ACME Corp",
  "orders": [
    {"product": "Laptop", "quantity": 2},
    {"product": "Mouse", "quantity": 5}
  ]
}
```

### Grupowanie wielopoziomowe
Możliwość definiowania wielu poziomów grupowania z automatycznymi sumami:

```java
.addGroup(GroupDefinition.builder("region").label("Region: ").build())
.addGroup(GroupDefinition.builder("city").label("Miasto: ").build())
```

### Zakładki PDF
Automatyczne generowanie zakładek PDF na podstawie grup lub wskazanego pola:

```java
FormattingOptions options = new FormattingOptions();
options.setGenerateBookmarks(true);
options.setBookmarkField("customerName");
```

## Uwagi techniczne

- Nazwy pól w JSON z kropkami (np. `address.city`) są konwertowane na podkreślenia w JasperReports (`address_city`)
- Daty muszą być w formacie ISO 8601 dla automatycznego rozpoznania
- Szerokość kolumny `-1` oznacza automatyczne dopasowanie
- Biblioteka automatycznie sortuje dane według pól grupujących

## Współtworzenie

Jeśli chcesz współtworzyć projekt, postępuj zgodnie z poniższymi krokami:

1. Zrób `fork` repozytorium.
2. Stwórz nową gałąź (`git checkout -b feature/nazwa-funkcjonalnosci`).
3. Wprowadź zmiany i zrób `commit` (`git commit -m 'Dodaj nową funkcjonalność'`).
4. Wypchnij zmiany do gałęzi (`git push origin feature/nazwa-funkcjonalnosci`).
5. Otwórz `Pull Request`.

## Licencja

Ten projekt jest udostępniany na licencji MIT. Zobacz plik `LICENSE`, aby uzyskać więcej informacji.

## REST API Serwis (Osobna aplikacja)

**Uwaga**: Serwis REST API to osobna aplikacja Spring Boot, która używa biblioteki `jrxml-builder-lib` jako zależności. Nie jest częścią głównej biblioteki, ale stanowi gotowe rozwiązanie do generowania raportów przez HTTP.

### Architektura rozwiązania

```
┌─────────────────────────────────────┐
│         Aplikacja kliencka          │
│    (Frontend / Inna aplikacja)     │
└─────────────┬───────────────────────┘
              │ HTTP requests
              ▼
┌─────────────────────────────────────┐
│       REST API Serwis              │
│    (Osobna aplikacja Spring Boot)  │
│                                     │
│  - DynamicReportController          │
│  - ReportRequest DTO                │
│  - JacksonConfig                    │
└─────────────┬───────────────────────┘
              │ używa jako dependency
              ▼
┌─────────────────────────────────────┐
│      jrxml-builder-lib              │
│     (Biblioteka główna)             │
│                                     │
│  - AutomatedReportService           │
│  - JsonReportGenerator              │
│  - ReportConfig, Column, Group      │
└─────────────────────────────────────┘
```

### Serwis jako osobna aplikacja

Serwis REST API jest zaimplementowany jako niezależna aplikacja Spring Boot, która:

1. **Importuje bibliotekę** `jrxml-builder-lib` jako zależność Maven
2. **Udostępnia endpoint HTTP** do generowania raportów
3. **Obsługuje różne formaty** eksportu (PDF, XLSX, CSV)
4. **Automatycznie konwertuje** konfigurację JSON na obiekty biblioteki

### Struktura serwisu (osobna aplikacja)

```
your-report-service/                 # Osobny projekt
├── pom.xml                         # Zawiera dependency do jrxml-builder-lib
├── src/main/java/
│   └── pl/lib/reportservice/
│       ├── JrxmlServiceApplication.java    # @SpringBootApplication
│       ├── controller/
│       │   └── DynamicReportController.java # @RestController
│       ├── dto/
│       │   └── ReportRequest.java          # DTO dla żądań
│       └── config/
│           └── JacksonConfig.java          # Konfiguracja JSON
└── src/main/resources/
    └── application.properties              # Konfiguracja Spring Boot
```

### Zależności w pom.xml serwisu

```xml
<dependencies>
    <!-- Główna biblioteka raportów -->
    <dependency>
        <groupId>pl.lib</groupId>
        <artifactId>jrxml-builder-lib</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
    
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- JasperReports (może być już w bibliotece) -->
    <dependency>
        <groupId>net.sf.jasperreports</groupId>
        <artifactId>jasperreports</artifactId>
        <version>6.21.3</version>
    </dependency>
</dependencies>
```

### Przykłady użycia serwisu

#### 1. Generowanie PDF z curl

```bash
curl -X POST http://localhost:8080/api/generate-dynamic-report?format=pdf \
  -H "Content-Type: application/json" \
  -d '{
    "config": {
      "title": "Raport sprzedaży",
      "columns": [
        {"field": "product", "header": "Produkt", "visible": true},
        {"field": "price", "header": "Cena", "format": "#,##0.00 zł", "reportCalculation": "SUM", "visible": true}
      ]
    },
    "jsonData": "[{\"product\":\"Laptop\",\"price\":3999.99},{\"product\":\"Mysz\",\"price\":79.90}]"
  }' \
  --output raport.pdf
```

#### 2. Klient JavaScript/Frontend

```javascript
async function generateReport(config, data, format = 'pdf') {
  const response = await fetch('/api/generate-dynamic-report?format=' + format, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      config: config,
      jsonData: JSON.stringify(data)
    })
  });
  
  if (response.ok) {
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `raport.${format}`;
    a.click();
  }
}

// Przykład użycia
const config = {
  title: "Raporty wizyt pacjentów",
  orientation: "LANDSCAPE",
  columns: [
    {field: "patient.name", header: "Pacjent", visible: true},
    {field: "visit.date", header: "Data wizyty", format: "dd.MM.yyyy", visible: true},
    {field: "billing.amount", header: "Kwota", format: "#,##0.00 zł", reportCalculation: "SUM", visible: true}
  ],
  groups: [
    {field: "doctor.name", label: "Lekarz: ", showHeader: true, showFooter: true}
  ]
};

const data = [
  {patient: {name: "Jan Kowalski"}, doctor: {name: "dr Nowak"}, visit: {date: "2025-01-15T00:00:00Z"}, billing: {amount: 250.00}}
];

generateReport(config, data, 'pdf');
```

#### 3. Klient Java (Spring RestTemplate)

```java
@Service
public class ReportClient {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public byte[] generateReport(ReportConfig config, String jsonData, String format) {
        ReportRequest request = new ReportRequest();
        request.setConfig(config);
        request.setJsonData(jsonData);
        
        String url = "http://localhost:8080/api/generate-dynamic-report?format=" + format;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<ReportRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<byte[]> response = restTemplate.exchange(
            url, HttpMethod.POST, entity, byte[].class);
            
        return response.getBody();
    }
}
```

### Obsługiwane formaty eksportu

| Format | Opis | Content-Type |
|--------|------|--------------|
| `pdf` | Adobe PDF (domyślny) | application/pdf |
| `xlsx` | Microsoft Excel | application/vnd.openxmlformats-officedocument.spreadsheetml.sheet |
| `csv` | Comma Separated Values | text/csv |

### Konfiguracja serwisu

#### application.properties

```properties
# Port serwera
server.port=8080

# Konfiguracja Jackson
spring.jackson.deserialization.fail-on-unknown-properties=false
spring.jackson.deserialization.accept-single-value-as-array=true

# Logowanie
logging.level.pl.lib.reportservice=INFO
```

#### Uruchomienie serwisu

```bash
# Kompilacja
mvn clean package

# Uruchomienie
java -jar target/jrxml-service-1.0-SNAPSHOT.jar

# Lub przez Maven
mvn spring-boot:run
```

### Integracja z aplikacjami

#### Spring Boot + Angular/React

1. **Backend (Spring Boot)**:
```java
@CrossOrigin(origins = "http://localhost:4200")
@RestController
public class ReportIntegrationController {
    
    @Autowired
    private ReportClient reportClient;
    
    @PostMapping("/api/clinic/reports/visits")
    public ResponseEntity<byte[]> generateVisitsReport(@RequestBody VisitReportRequest request) {
        ReportConfig config = buildVisitsReportConfig();
        String jsonData = convertVisitsToJson(request.getVisits());
        
        byte[] pdf = reportClient.generateReport(config, jsonData, "pdf");
        
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header("Content-Disposition", "attachment; filename=wizyty.pdf")
            .body(pdf);
    }
}
```

2. **Frontend (Angular)**:
```typescript
@Injectable()
export class ReportService {
  
  generateVisitsReport(visits: Visit[]): Observable<Blob> {
    return this.http.post('/api/clinic/reports/visits', 
      { visits }, 
      { responseType: 'blob' }
    );
  }
  
  downloadReport(visits: Visit[]) {
    this.generateVisitsReport(visits).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'raport-wizyt.pdf';
      link.click();
    });
  }
}
```

### Monitorowanie i logowanie

Serwis automatycznie loguje:
- Szczegóły żądań i konfiguracji
- Proces konwersji danych
- Błędy generowania raportów
- Rozmiary wygenerowanych plików

```
2025-01-15 10:30:15 INFO  [DynamicReportController] - ====================== NOWE ŻĄDANIE GENEROWANIA RAPORTU ======================
2025-01-15 10:30:15 INFO  [DynamicReportController] - Format eksportu: PDF
2025-01-15 10:30:15 INFO  [DynamicReportController] - Pomyślnie wygenerowano obiekt JasperPrint. Rozpoczynanie eksportu...
2025-01-15 10:30:16 INFO  [DynamicReportController] - Eksport do formatu PDF zakończony sukcesem. Rozmiar pliku: 15420 bajtów.
```
