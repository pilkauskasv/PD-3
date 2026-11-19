# Standard Calculator (Android / Jetpack Compose)

„Windows 11 Standard“ stiliaus „Android“ skaičiuotuvo programėlė, sukurta su **Kotlin**, **Jetpack Compose** ir **Gradle Kotlin DSL**, naudojant nuo nulio parašytą `BigDecimal` skaičiavimo variklį.

---

## Pagrindinės funkcijos

- **Pagrindinės ir išplėstinės operacijos:**
  - Sudėtis (`+`), atimtis (`−`), daugyba (`×`), dalyba (`÷`)
  - Atvirkštinis skaičius (`1/x`), kėlimas kvadratu (`x²`), kvadratinė šaknis (`²√x`)
  - Procentai (`%`), ženklo keitimas (`+/−`)
  - Paskutinio simbolio trynimas (`⌫`), dabartinės įvesties valymas (`CE`), visiškas atstatymas (`C`)
- **„Windows 11 Standard“ elgsena:**
  - Nuoseklus veiksmų vykdymas iš kairės į dešinę (be operatorių prioriteto, pvz., `2 + 3 × 4 = 20`)
  - Kontekstinis `%` veikimas: prie `+` / `−` skaičiuoja procentą nuo pirmojo dėmens (`200 + 10% = 220`), o prie `×` / `÷` – dalija iš 100 (`200 × 10% = 20`)
  - Kartotinis `=` paspaudimas kartoja paskutinę operaciją
- **Tikslumas ir klaidų apdorojimas:**
  - Skaičiavimai vykdomi naudojant `BigDecimal` (16 reikšminių skaitmenų tikslumas, be dvejetainių slankiojo kablelio paklaidų)
  - Automatinis perėjimas į mokslinę notaciją (`e+` / `e-`) dideliems ar labai mažiems skaičiams
  - Klaidų apsauga: dalyba iš nulio (`1/0`, `÷ 0`), neigiamo skaičiaus šaknis, perpildymas (`Overflow`)
- **Vienos vietos atmintis:** `MC`, `MR`, `M+`, `M−`, `MS` (`M∨` rodomas kaip neaktyvus mygtukas)
- **Sesijos skaičiavimų istorija:**
  - Atidaroma viršutinio dešiniojo kampo laikrodžio piktograma
  - Paspaudus įrašą, reikšmė perkeliama atgal į ekraną
  - Šiukšlinės piktograma išvalo visą istoriją
- **Prisitaikanti sąsaja ir kalbos:**
  - **Šviesioji ir tamsioji temos** pagal sistemos nustatymus
  - **Portreto ir gulsčiojo (Landscape)** režimų išdėstymas; būsena išsaugoma per `ViewModel` (`StateFlow`)
  - **Lokalizacija:** anglų (`values/strings.xml`, skirtukas `.`) ir lietuvių (`values-lt/strings.xml`, skirtukas `,`)

---

## Projekto struktūra

```text
Win11Calculator/
├── app/src/main/java/com/example/calculator/
│   ├── MainActivity.kt              # Edge-to-edge Activity įėjimo taškas
│   ├── model/                       # Būsenos, veiksmų, operatorių ir klaidų modeliai
│   ├── engine/                      # BigDecimal variklis ir skaičių formatavimas
│   ├── viewmodel/                   # CalculatorViewModel (StateFlow)
│   └── ui/                          # Compose temos, ekranai ir komponentai
├── app/src/main/res/
│   ├── values/strings.xml           # Anglų kalbos tekstai
│   └── values-lt/strings.xml        # Lietuvių kalbos tekstai
└── app/src/test/java/com/example/calculator/engine/
    └── CalculatorEngineTest.kt      # 22 JUnit4 vienetų testai
```

---

## Paleidimas per „Android Studio“

1. Atidarykite aplanką `Win11Calculator` programoje **Android Studio** (*File → Open*).
2. Palaukite, kol **Gradle Sync** automatiniu būdu parsiųs **Gradle 8.9** ir sukonfigūruos priklausomybes.
3. Pasirinkite emuliatorių arba fizinį „Android“ įrenginį (minSdk 26 / Android 8.0+) ir spauskite **Run (`▶`)**.

### Vienetų testų paleidimas
„Android Studio“ aplinkoje atidarykite `CalculatorEngineTest.kt` ir spauskite `Run` arba terminale (projekto šaknyje su sukonfigūruotu Gradle):

```bash
./gradlew testDebugUnitTest
```

---

## GitHub aprašymas (Repository Description)

> Windows 11 Standard style Android calculator built with Kotlin, Jetpack Compose, and a custom BigDecimal engine (EN/LT localization, Light/Dark themes, runtime history, and memory).
