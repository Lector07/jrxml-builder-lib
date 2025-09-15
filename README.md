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

#### Laravel + Vue.js

1. **Backend (Laravel)**:

**ReportController.php**:
```php
<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Http\Response;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;

class ReportController extends Controller
{
    private $reportServiceUrl;

    public function __construct()
    {
        $this->reportServiceUrl = config('services.jasper_report.url', 'http://localhost:8080');
    }

    /**
     * Generuje raport wizyt pacjentów
     */
    public function generateVisitsReport(Request $request)
    {
        $request->validate([
            'visits' => 'required|array',
            'visits.*.patient.name' => 'required|string',
            'visits.*.doctor.name' => 'required|string',
            'visits.*.visit.date' => 'required|date',
            'visits.*.billing.amount' => 'required|numeric',
            'format' => 'sometimes|in:pdf,xlsx,csv'
        ]);

        $format = $request->input('format', 'pdf');
        
        // Konfiguracja raportu
        $config = [
            'title' => 'Raport wizyt pacjentów',
            'orientation' => 'LANDSCAPE',
            'pageFooterEnabled' => true,
            'summaryBandEnabled' => true,
            'theme' => 'MODERN',
            'footerLeftText' => 'Wygenerowano przez system kliniki',
            'columns' => [
                [
                    'field' => 'patient.name',
                    'header' => 'Pacjent',
                    'width' => 160,
                    'visible' => true
                ],
                [
                    'field' => 'doctor.name',
                    'header' => 'Lekarz',
                    'width' => 140,
                    'visible' => true
                ],
                [
                    'field' => 'visit.date',
                    'header' => 'Data wizyty',
                    'width' => 120,
                    'format' => 'dd.MM.yyyy',
                    'visible' => true
                ],
                [
                    'field' => 'billing.amount',
                    'header' => 'Kwota (PLN)',
                    'width' => 100,
                    'format' => '#,##0.00 zł',
                    'reportCalculation' => 'SUM',
                    'groupCalculation' => 'SUM',
                    'visible' => true
                ]
            ],
            'groups' => [
                [
                    'field' => 'doctor.name',
                    'label' => 'Lekarz: ',
                    'showHeader' => true,
                    'showFooter' => true,
                    'ascending' => true
                ]
            ],
            'companyInfo' => [
                'name' => config('app.clinic_name', 'Klinika Medyczna'),
                'address' => config('app.clinic_address', 'ul. Zdrowia 1'),
                'postalCode' => config('app.clinic_postal', '00-001'),
                'city' => config('app.clinic_city', 'Warszawa'),
                'taxId' => config('app.clinic_tax_id', 'PL1234567890')
            ],
            'formattingOptions' => [
                'zebraStripes' => true,
                'generateBookmarks' => true,
                'bookmarkField' => 'patient.name',
                'highlightRules' => [
                    [
                        'field' => 'billing.amount',
                        'operator' => 'GREATER_THAN',
                        'value' => '500',
                        'color' => '#FFE5E5'
                    ]
                ]
            ]
        ];

        try {
            // Wywołanie serwisu JasperReports
            $response = Http::timeout(30)
                ->withHeaders(['Content-Type' => 'application/json'])
                ->post("{$this->reportServiceUrl}/api/generate-dynamic-report", [
                    'config' => $config,
                    'jsonData' => json_encode($request->input('visits'))
                ], [
                    'format' => $format
                ]);

            if ($response->successful()) {
                $contentType = $this->getContentType($format);
                $filename = $this->generateFileName('raport-wizyt', $format);
                
                return response($response->body())
                    ->header('Content-Type', $contentType)
                    ->header('Content-Disposition', "attachment; filename=\"{$filename}\"");
            }

            Log::error('Report service error', [
                'status' => $response->status(),
                'body' => $response->body()
            ]);

            return response()->json([
                'error' => 'Błąd generowania raportu'
            ], 500);

        } catch (\Exception $e) {
            Log::error('Report generation failed', [
                'message' => $e->getMessage(),
                'trace' => $e->getTraceAsString()
            ]);

            return response()->json([
                'error' => 'Serwis raportów niedostępny'
            ], 503);
        }
    }

    /**
     * Generuje raport sprzedaży produktów
     */
    public function generateSalesReport(Request $request)
    {
        $request->validate([
            'sales' => 'required|array',
            'dateFrom' => 'required|date',
            'dateTo' => 'required|date',
            'format' => 'sometimes|in:pdf,xlsx,csv'
        ]);

        $format = $request->input('format', 'pdf');
        
        $config = [
            'title' => "Raport sprzedaży {$request->dateFrom} - {$request->dateTo}",
            'orientation' => 'PORTRAIT',
            'columns' => [
                [
                    'field' => 'product.name',
                    'header' => 'Produkt',
                    'width' => 200,
                    'visible' => true
                ],
                [
                    'field' => 'category',
                    'header' => 'Kategoria',
                    'width' => 120,
                    'visible' => true
                ],
                [
                    'field' => 'quantity',
                    'header' => 'Ilość',
                    'width' => 80,
                    'reportCalculation' => 'SUM',
                    'visible' => true
                ],
                [
                    'field' => 'unitPrice',
                    'header' => 'Cena jedn.',
                    'width' => 100,
                    'format' => '#,##0.00 zł',
                    'visible' => true
                ],
                [
                    'field' => 'totalValue',
                    'header' => 'Wartość',
                    'width' => 120,
                    'format' => '#,##0.00 zł',
                    'reportCalculation' => 'SUM',
                    'groupCalculation' => 'SUM',
                    'visible' => true
                ]
            ],
            'groups' => [
                [
                    'field' => 'category',
                    'label' => 'Kategoria: ',
                    'showHeader' => true,
                    'showFooter' => true
                ]
            ]
        ];

        // Analogiczne wywołanie serwisu jak w generateVisitsReport
        // ...
    }

    private function getContentType(string $format): string
    {
        return match($format) {
            'pdf' => 'application/pdf',
            'xlsx' => 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
            'csv' => 'text/csv',
            default => 'application/pdf'
        };
    }

    private function generateFileName(string $base, string $format): string
    {
        $date = now()->format('Y-m-d_H-i');
        return "{$base}_{$date}.{$format}";
    }
}
```

**config/services.php**:
```php
<?php

return [
    // ...existing services...
    
    'jasper_report' => [
        'url' => env('JASPER_REPORT_SERVICE_URL', 'http://localhost:8080'),
        'timeout' => env('JASPER_REPORT_TIMEOUT', 30),
    ],
];
```

**routes/api.php**:
```php
<?php

use App\Http\Controllers\ReportController;

Route::middleware(['auth:sanctum'])->group(function () {
    Route::post('/reports/visits', [ReportController::class, 'generateVisitsReport']);
    Route::post('/reports/sales', [ReportController::class, 'generateSalesReport']);
});
```

2. **Frontend (Vue.js 3 + Composition API)**:

**services/ReportService.js**:
```javascript
import axios from 'axios';

export class ReportService {
    constructor() {
        this.apiClient = axios.create({
            baseURL: '/api',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });

        // Dodaj token autoryzacji
        this.apiClient.interceptors.request.use(config => {
            const token = localStorage.getItem('auth_token');
            if (token) {
                config.headers.Authorization = `Bearer ${token}`;
            }
            return config;
        });
    }

    async generateVisitsReport(visits, format = 'pdf') {
        try {
            const response = await this.apiClient.post('/reports/visits', {
                visits,
                format
            }, {
                responseType: 'blob'
            });

            return {
                success: true,
                data: response.data,
                filename: this.extractFilename(response.headers['content-disposition'])
            };
        } catch (error) {
            return {
                success: false,
                error: error.response?.data?.error || 'Błąd generowania raportu'
            };
        }
    }

    async generateSalesReport(salesData, dateFrom, dateTo, format = 'pdf') {
        try {
            const response = await this.apiClient.post('/reports/sales', {
                sales: salesData,
                dateFrom,
                dateTo,
                format
            }, {
                responseType: 'blob'
            });

            return {
                success: true,
                data: response.data,
                filename: this.extractFilename(response.headers['content-disposition'])
            };
        } catch (error) {
            return {
                success: false,
                error: error.response?.data?.error || 'Błąd generowania raportu'
            };
        }
    }

    downloadBlob(blob, filename) {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
    }

    extractFilename(contentDisposition) {
        if (!contentDisposition) return 'raport.pdf';
        const match = contentDisposition.match(/filename="(.+)"/);
        return match ? match[1] : 'raport.pdf';
    }
}

export default new ReportService();
```

**components/VisitsReportGenerator.vue**:
```vue
<template>
  <div class="visits-report-generator">
    <div class="card">
      <div class="card-header">
        <h3>Generator raportów wizyt</h3>
      </div>
      
      <div class="card-body">
        <!-- Filtry -->
        <div class="row mb-3">
          <div class="col-md-4">
            <label class="form-label">Data od:</label>
            <input 
              type="date" 
              v-model="filters.dateFrom" 
              class="form-control"
            />
          </div>
          <div class="col-md-4">
            <label class="form-label">Data do:</label>
            <input 
              type="date" 
              v-model="filters.dateTo" 
              class="form-control"
            />
          </div>
          <div class="col-md-4">
            <label class="form-label">Lekarz:</label>
            <select v-model="filters.doctorId" class="form-control">
              <option value="">Wszyscy lekarze</option>
              <option 
                v-for="doctor in doctors" 
                :key="doctor.id" 
                :value="doctor.id"
              >
                {{ doctor.name }}
              </option>
            </select>
          </div>
        </div>

        <!-- Format eksportu -->
        <div class="row mb-3">
          <div class="col-md-6">
            <label class="form-label">Format eksportu:</label>
            <div class="btn-group d-block">
              <button 
                v-for="format in formats" 
                :key="format.value"
                @click="selectedFormat = format.value"
                :class="['btn', selectedFormat === format.value ? 'btn-primary' : 'btn-outline-primary']"
              >
                <i :class="format.icon"></i> {{ format.label }}
              </button>
            </div>
          </div>
        </div>

        <!-- Akcje -->
        <div class="d-flex gap-2">
          <button 
            @click="loadVisits"
            :disabled="loading"
            class="btn btn-secondary"
          >
            <i class="fas fa-search"></i> Wczytaj wizyty
          </button>
          
          <button 
            @click="generateReport"
            :disabled="loading || !visits.length"
            class="btn btn-success"
          >
            <i class="fas fa-file-download"></i>
            <span v-if="loading">Generowanie...</span>
            <span v-else>Generuj raport ({{ visits.length }} wizyt)</span>
          </button>
        </div>

        <!-- Podgląd danych -->
        <div v-if="visits.length" class="mt-4">
          <h5>Podgląd wizyt ({{ visits.length }} rekordów)</h5>
          <div class="table-responsive">
            <table class="table table-sm">
              <thead>
                <tr>
                  <th>Pacjent</th>
                  <th>Lekarz</th>
                  <th>Data</th>
                  <th>Kwota</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="visit in visits.slice(0, 10)" :key="visit.id">
                  <td>{{ visit.patient.name }}</td>
                  <td>{{ visit.doctor.name }}</td>
                  <td>{{ formatDate(visit.visit.date) }}</td>
                  <td>{{ formatCurrency(visit.billing.amount) }}</td>
                </tr>
              </tbody>
            </table>
            <p v-if="visits.length > 10" class="text-muted">
              ... i {{ visits.length - 10 }} więcej rekordów
            </p>
          </div>
        </div>

        <!-- Komunikaty -->
        <div v-if="message" :class="['alert', messageType === 'error' ? 'alert-danger' : 'alert-success']">
          {{ message }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue';
import reportService from '@/services/ReportService';
import visitsApi from '@/api/visits';
import doctorsApi from '@/api/doctors';

export default {
  name: 'VisitsReportGenerator',
  
  setup() {
    const loading = ref(false);
    const visits = ref([]);
    const doctors = ref([]);
    const selectedFormat = ref('pdf');
    const message = ref('');
    const messageType = ref('success');
    
    const filters = reactive({
      dateFrom: '',
      dateTo: '',
      doctorId: ''
    });

    const formats = [
      { value: 'pdf', label: 'PDF', icon: 'fas fa-file-pdf' },
      { value: 'xlsx', label: 'Excel', icon: 'fas fa-file-excel' },
      { value: 'csv', label: 'CSV', icon: 'fas fa-file-csv' }
    ];

    const loadVisits = async () => {
      loading.value = true;
      try {
        const response = await visitsApi.getVisits(filters);
        visits.value = response.data.map(visit => ({
          patient: { name: visit.patient_name },
          doctor: { name: visit.doctor_name },
          visit: { date: visit.visit_date },
          billing: { amount: parseFloat(visit.billing_amount) }
        }));
        
        showMessage(`Wczytano ${visits.value.length} wizyt`, 'success');
      } catch (error) {
        showMessage('Błąd wczytywania wizyt', 'error');
      } finally {
        loading.value = false;
      }
    };

    const generateReport = async () => {
      if (!visits.value.length) {
        showMessage('Brak danych do wygenerowania raportu', 'error');
        return;
      }

      loading.value = true;
      try {
        const result = await reportService.generateVisitsReport(
          visits.value, 
          selectedFormat.value
        );
        
        if (result.success) {
          reportService.downloadBlob(result.data, result.filename);
          showMessage('Raport został wygenerowany i pobrany', 'success');
        } else {
          showMessage(result.error, 'error');
        }
      } catch (error) {
        showMessage('Nieoczekiwany błąd podczas generowania raportu', 'error');
      } finally {
        loading.value = false;
      }
    };

    const loadDoctors = async () => {
      try {
        const response = await doctorsApi.getDoctors();
        doctors.value = response.data;
      } catch (error) {
        console.error('Błąd wczytywania lekarzy:', error);
      }
    };

    const showMessage = (text, type) => {
      message.value = text;
      messageType.value = type;
      setTimeout(() => {
        message.value = '';
      }, 5000);
    };

    const formatDate = (dateString) => {
      return new Date(dateString).toLocaleDateString('pl-PL');
    };

    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('pl-PL', {
        style: 'currency',
        currency: 'PLN'
      }).format(amount);
    };

    // Ustaw domyślne daty (ostatnie 30 dni)
    const setDefaultDates = () => {
      const today = new Date();
      const thirtyDaysAgo = new Date(today);
      thirtyDaysAgo.setDate(today.getDate() - 30);
      
      filters.dateTo = today.toISOString().split('T')[0];
      filters.dateFrom = thirtyDaysAgo.toISOString().split('T')[0];
    };

    onMounted(() => {
      setDefaultDates();
      loadDoctors();
      loadVisits(); // Auto-load na start
    });

    return {
      loading,
      visits,
      doctors,
      selectedFormat,
      message,
      messageType,
      filters,
      formats,
      loadVisits,
      generateReport,
      formatDate,
      formatCurrency
    };
  }
};
</script>

<style scoped>
.visits-report-generator {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.btn-group .btn {
  margin-right: 5px;
}

.table-responsive {
  max-height: 400px;
}
</style>
```

**composables/useReports.js** (opcjonalny composable):
```javascript
import { ref } from 'vue';
import reportService from '@/services/ReportService';

export function useReports() {
  const loading = ref(false);
  const error = ref(null);

  const generateAndDownload = async (reportType, data, options = {}) => {
    loading.value = true;
    error.value = null;

    try {
      let result;
      
      switch (reportType) {
        case 'visits':
          result = await reportService.generateVisitsReport(data, options.format);
          break;
        case 'sales':
          result = await reportService.generateSalesReport(
            data, 
            options.dateFrom, 
            options.dateTo, 
            options.format
          );
          break;
        default:
          throw new Error(`Nieznany typ raportu: ${reportType}`);
      }

      if (result.success) {
        reportService.downloadBlob(result.data, result.filename);
        return { success: true };
      } else {
        error.value = result.error;
        return { success: false, error: result.error };
      }
    } catch (err) {
      error.value = err.message;
      return { success: false, error: err.message };
    } finally {
      loading.value = false;
    }
  };

  return {
    loading,
    error,
    generateAndDownload
  };
}
```

3. **Konfiguracja środowiska (.env)**:
```env
# Laravel
APP_NAME="Klinika Medyczna"
CLINIC_NAME="Centrum Medyczne Zdrowie"
CLINIC_ADDRESS="ul. Zdrowia 1"
CLINIC_POSTAL="00-001"
CLINIC_CITY="Warszawa"
CLINIC_TAX_ID="PL1234567890"

# Serwis raportów
JASPER_REPORT_SERVICE_URL=http://localhost:8080
JASPER_REPORT_TIMEOUT=30
```
