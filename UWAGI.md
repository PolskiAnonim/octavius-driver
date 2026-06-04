# 🗃️ Formatowanie typów w PostgreSQL

PostgreSQL udostępnia cztery kluczowe funkcje obsługujące konwersję typów pomiędzy różnymi formatami. Warto zwrócić uwagę na ich wymagalność:

- `send` – konwersja na format binarny (nieobowiązkowa)
- `receive` – konwersja z formatu binarnego (nieobowiązkowa)
- **`input`** – konwersja na reprezentację tekstową (**wymagana**)
- **`output`** – konwersja z reprezentacji tekstowej (**wymagana**)

> [!NOTE]
> Funkcje `send` i `receive` mogą nie istnieć dla niektórych typów (szczególnie niestandardowych). Wtedy typ obsługiwany jest wyłącznie tekstowo. Wewnątrz tabeli `pg_type` dla takich typów `typsend == 0`.

## 📦 Uwagi dotyczące protokołu binarnego

1. **Typy niestandardowe:** Jeżeli typ niestandardowy nie posiada funkcji `send` i `receive`, przesył w formacie binarnym jest niemożliwy.
2. **Kolekcje (kompozyty, tablice, zakresy, multirange):** Jeśli choć jedno pole wewnątrz kolekcji nie posiada binarnej funkcji przesyłu, przy próbie przesyłu jako postać binarna wystąpi błąd.
3. **Domeny:** Zachowują swoje OID wewnątrz kontenerów (np. w kompozytach lub tablicach). Na głównym poziomie OID domeny nigdy nie jest widoczne (zastępuje je OID typu bazowego).

### 🛠️ Struktura binarnego przesyłu

* **Kompozyty:**
  `liczba kolumn + (OID + długość + dane binarne) * liczba kolumn`
* **Tekst:**
  Zapisany binarnie z wykorzystaniem kodowania ustawionego w bazie danych.

---
# Na początek obsługiwany będzie tylko przesył binarny wszystkiego

W przyszłości

Fallback tekstowy będzie tylko dla nowych "base type" które były dodane z C, reszta ma funkcje send i receive

Oraz prawdopodobnie dla typów które nie mają jawnego mapowania


Prawdopodobnie będzie trzeba także dodać:
* **Format przesyłu** – określany na podstawie dostępności binarnej obsługi typu (według powyższych reguł):
    * `Format 0` – Tekstowo
    * `Format 1` – Binarnie