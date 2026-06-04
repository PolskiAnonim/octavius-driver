# 📝 Plan wdrożenia: Rejestr Typów (Type Registry)

Rejestr służy do śledzenia mapowań typów pomiędzy PostgreSQL a sterownikiem JDBC oraz określania odpowiedniego sposobu ich przesyłu.

## 1. Co przechowujemy w Rejestrze?

Każdy typ zarejestrowany w systemie powinien zawierać następujące informacje:

* **OID** – unikalny identyfikator typu w bazie.
* **Nazwa typu** – oryginalna nazwa z PostgreSQL.
* **Schemat** – przestrzeń nazw (schema), w której typ jest zdefiniowany.

### Specyficzne metadane dla poszczególnych rodzajów typów:

| Rodzaj typu             | Wymagane dodatkowe metadane                                        |
|:------------------------|:-------------------------------------------------------------------|
| **Typy bazowe (Base)**  | Funkcje do odczytu z formatu binarnego (w przyszłości tekstowego). |
| **Tablice (Arrays)**    | OID typu elementu bazowego (`typelem` w `pg_type`).                |
| **Zakresy (Ranges)**    | OID podtypu (`subtype`) – np. OID `int4` dla `int4range`.          |
| **Kompozyty (Records)** | OID-y atrybutów oraz ich nazwy w odpowiedniej kolejności.          |
| **Domeny (Domains)**    | OID typu bazowego (`typbasetype`).                                 |
| **Słowniki (Enums)**    | Przypisana wartość i/lub jej pozycja.                              |

## 2. Kiedy aktualizować rejestr?

- **Podczas startu:** Inicjalne wczytanie informacji o typach dla zoptymalizowanego działania.
- **Na żądanie (jawnie):** Przez dedykowaną metodę `reloadTypes()`, która odświeży rejestr, np. po utworzeniu nowych typów w bazie podczas działania aplikacji.

## Struktura rejestru dla typów podstawowych
```kotlin
    interface TypeHandler<T : Any> {                                                                                                                                                                                                                                                                           
        val pgTypeName: String                                                                                                                                                                                                                                                                                 
        val pgSchema: String get() = ""                                                                                                                                                                                                                                                                        
        val kotlinClass: KClass<T>                                                                                                                                                                                                                                                                             
        val isDefaultForKotlinType: Boolean get() = false                                                                                                                                                                                                                                                      
                                                                                                                                                                                                                                                                                                               
        // Zoptymalizowana ścieżka główna:                                                                                                                                                                                                                                                                     
        val fromBinary: ((PgBuffer) -> T)? get() = null                                                                                                                                                                                                                                                        
        val toBinary: ((T, PgBuffer) -> Unit)? get() = null                                                                                                                                                                                                                                                       
    }                       
```


# Row a Mapa/Klasa

Row MUSI mieć metody do zmiany go w klasę (refleksją lub jawnym mapperem) ORAZ do zmiany go w mapę
Domyślnie będzie zostawał jako ROW musi być jakiś automatyzm dodany
szczególnie dla zagnieżdżonych