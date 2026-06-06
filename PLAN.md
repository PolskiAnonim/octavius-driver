# Row a Mapa/Klasa

Row MUSI mieć metody do zmiany go w klasę (refleksją lub jawnym mapperem) ORAZ do zmiany go w mapę
Domyślnie będzie zostawał jako ROW musi być jakiś automatyzm dodany
szczególnie dla zagnieżdżonych

Podobnie każy kontener będzie miał metody przekształcające

# Dwie warstwy serializacji parametrów

- TypeSerializer - odpowiada za serializację najniższego poziomu
binary in, binary out
będą możliwe do dodania jawnie
przez połączenie dodawane do globalnego rejestru
- TypeConverter - dodatkowa warstwa konwertująca parametry na typy które posiadają TypeSerializer