# Skaičiuotuvas (Android, Kotlin + Jetpack Compose)
 
„Windows 11 Standard“ stiliaus skaičiuotuvo programėlė „Android“ įrenginiams. Skaičiavimo variklis parašytas **nuo nulio** (be trečiųjų šalių bibliotekų) ir naudoja `BigDecimal`, todėl rezultatai neturi dvejetainių slankiojo kablelio paklaidų (pvz., `0,1 + 0,2 = 0,3`).
 
Projektas sukurtas kaip MIPT praktinis darbas #3. Kodas sugeneruotas dirbant su dirbtinio intelekto įrankiais, be rankinio redagavimo.
 
---
 
## Turinys
 
- [Galimybės](#galimybės)
- [Kaip naudoti](#kaip-naudoti)
- [Skaičiavimo taisyklės](#skaičiavimo-taisyklės)
- [Paleidimas](#paleidimas)
- [Projekto struktūra](#projekto-struktūra)
- [Lokalizacija](#lokalizacija)
- [Testai](#testai)
- [Naršyklės peržiūra (AI Studio)](#naršyklės-peržiūra-ai-studio)
- [Žinomi apribojimai](#žinomi-apribojimai)
---
 
## Galimybės
 
| Kategorija | Ką programėlė daro |
|---|---|
| Pagrindinės operacijos | Sudėtis `+`, atimtis `−`, daugyba `×`, dalyba `÷` |
| Vienvietės operacijos | Kvadratinė šaknis `²√x`, kėlimas kvadratu `x²`, trupmena (atvirkštinis skaičius) `1/x` |
| Procentai | `%` (elgsena priklauso nuo veiksmo, žr. [taisykles](#skaičiavimo-taisyklės)) |
| Redagavimas | Paskutinio simbolio trynimas `⌫`, dabartinės įvesties valymas `CE`, visko išvalymas `C`, ženklo keitimas `+/−` |
| Istorija | Visų atliktų skaičiavimų sąrašas einamosios sesijos metu |
| Atmintis | Vienos vietos atmintis: `MC`, `MR`, `M+`, `M−`, `MS` |
| Išvaizda | Šviesioji ir tamsioji temos pagal sistemos nustatymus, portreto ir gulsčias režimai |
| Kalbos | Anglų (numatytoji) ir lietuvių, su atitinkamais skaičių skirtukais |
 
Kiti svarbūs dalykai:
- **Tikslumas:** iki 16 reikšminių skaitmenų, vidiniai skaičiavimai atliekami su 24 skaitmenų tikslumu.
- **Dideli ir maži skaičiai** automatiškai rodomi moksline notacija (pvz., `1e+20`).
- **Klaidų apdorojimas:** dalyba iš nulio, neigiamo skaičiaus šaknis, perpildymas. Klaidos tekstas rodomas ekrane ir yra lokalizuotas.
- **Būsena išlieka** pasukus ekraną (naudojamas `ViewModel` su `StateFlow`).
---
 
## Kaip naudoti
 
### Skaičiavimas
1. Spauskite skaitmenis ir veiksmo mygtuką (`+`, `−`, `×`, `÷`), tada kitą skaičių ir `=`.
2. Viršutinėje mažesnėje eilutėje rodoma išraiška (pvz., `12 + 8 =`), apatinėje, didelėje, rezultatas.
3. Pakartotinis `=` paspaudimas pakartoja paskutinį veiksmą su tuo pačiu antruoju skaičiumi (pvz., `5 + 2 = = =` → `7`, `9`, `11`).
### Redagavimo mygtukai
 
| Mygtukas | Veiksmas |
|---|---|
| `⌫` | Ištrina paskutinį įvestą ženklą |
| `CE` | Išvalo tik dabartinę įvestį, palieka pradėtą veiksmą |
| `C` | Išvalo viską (įvestį, veiksmą, išraišką). Istorija ir atmintis nėra išvalomos |
| `+/−` | Pakeičia skaičiaus ženklą |
 
### Istorija
- Spauskite **laikrodžio piktogramą** viršutiniame dešiniajame kampe, kad atidarytumėte istorijos skydelį.
- Naujausi įrašai rodomi viršuje (pvz., `12 + 8 =` → `20`; `1/( 8 )` → `0,125`).
- **Paspaudus įrašą**, jo rezultatas perkeliamas į ekraną.
- **Šiukšliadėžės piktograma** išvalo visą istoriją.
- Skydelį galima uždaryti ir sistemos mygtuku „Atgal“.
- Istorija saugoma **tik programos veikimo metu**. Uždarius programą, ji dingsta.
### Atmintis
 
| Mygtukas | Veiksmas |
|---|---|
| `MS` | Įrašo ekrane rodomą skaičių į atmintį |
| `M+` / `M−` | Prideda prie atminties / atima iš jos ekrane rodomą skaičių |
| `MR` | Parodo atmintyje saugomą skaičių |
| `MC` | Išvalo atmintį |
| `M∨` | Neaktyvus mygtukas (tik atitinka maketą) |
 
`MC` ir `MR` neaktyvūs, kol atmintyje nieko nėra.
 
### Klaidos
Įvykus klaidai (pvz., dalyba iš nulio), skaitmenų ir veiksmų mygtukai laikinai išjungiami. Paspaudus `C` arba `CE` skaičiuotuvas grįžta į pradinę būseną.
 
---
 
## Skaičiavimo taisyklės
 
Programėlė elgiasi kaip „Windows 11“ standartinis skaičiuotuvas.
 
**Veiksmai vykdomi iš eilės** (be operatorių prioriteto):
 
```text
2 + 3 × 4 =   →   20     (ne 14)
```
 
**Procentai** priklauso nuo laukiančio veiksmo:
 
| Veiksmas | Pavyzdys | Rezultatas | Paaiškinimas |
|---|---|---|---|
| `+` arba `−` | `200 + 10 %` | `200 + 20 = 220` | Procentas skaičiuojamas nuo pirmojo skaičiaus |
| `×` arba `÷` | `200 × 10 %` | `200 × 0,1 = 20` | Skaičius dalijamas iš 100 |
| Nėra veiksmo | `50 %` | `0` | Kaip „Windows“ skaičiuotuve |
 
**Vienvietės operacijos** pritaikomos ekrane rodomam skaičiui ir iškart įrašomos į istoriją:
 
| Veiksmas | Pavyzdys |
|---|---|
| `1/x` | `8` → `0,125`, `0` → „Dalyba iš nulio negalima“ |
| `x²` | `7` → `49` |
| `²√x` | `9` → `3`, `−4` → „Neteisinga įvestis“ |
 
**Klaidos neįrašomos į istoriją.**
 
---
 
## Paleidimas
 
### Reikalavimai
- [Android Studio](https://developer.android.com/studio) (nauja versija, palaikanti AGP 8.7)
- JDK 17 (pateikiamas kartu su Android Studio)
- Emuliatorius arba fizinis įrenginys su **Android 8.0 (API 26)** ar naujesne versija
### Žingsniai
1. Atsisiųskite arba nuklonuokite šią repozitoriją.
2. Android Studio pasirinkite **File → Open** ir atidarykite aplanką **`Win11Calculator`** (ne repozitorijos šaknį).
3. Palaukite, kol baigsis **Gradle Sync** (bus automatiškai parsisiųstas Gradle 8.9 ir priklausomybės).
4. Pasirinkite įrenginį ir spauskite **Run (▶)**.
### Naudojamos versijos
 
| Komponentas | Versija |
|---|---|
| Kotlin | 2.0.21 |
| Android Gradle Plugin | 8.7.3 |
| Gradle | 8.9 |
| Compose BOM | 2024.12.01 |
| compileSdk / targetSdk | 35 |
| minSdk | 26 |
 
---
 
## Projekto struktūra
 
```text
.
├── Win11Calculator/                  # Android projektas (pagrindinis rezultatas)
│   └── app/src/
│       ├── main/java/com/example/calculator/
│       │   ├── MainActivity.kt       # Įėjimo taškas
│       │   ├── model/                # Būsena, veiksmai, operatoriai, klaidos, istorijos įrašas
│       │   ├── engine/               # CalculatorEngine ir NumberFormatter (BigDecimal)
│       │   ├── viewmodel/            # CalculatorViewModel (StateFlow)
│       │   └── ui/                   # Compose ekranas, komponentai ir temos
│       ├── main/res/
│       │   ├── values/strings.xml    # Anglų kalbos tekstai
│       │   └── values-lt/strings.xml # Lietuvių kalbos tekstai
│       └── test/                     # Variklio vienetų testai
├── src/, index.html, package.json    # Naršyklės peržiūra (React + Vite)
└── public/                           # Peržiūros ištekliai
```
 
### Architektūra
Programėlė suprojektuota kaip vienakryptis duomenų srautas:
 
```text
Mygtukas → CalculatorAction → CalculatorViewModel → CalculatorEngine.reduce(būsena, veiksmas)
                                     ↓
                          CalculatorUiState (StateFlow) → Compose UI
```
 
- `CalculatorEngine` yra gryna Kotlin klasė be „Android“ priklausomybių. Todėl ją lengva testuoti.
- Visa skaičiuotuvo būsena (`CalculatorState`) yra nekeičiamas objektas, o kiekvienas veiksmas sukuria naują.
- `CalculatorViewModel` laiko būseną ir perduoda ją UI.
---
 
## Lokalizacija
 
Visi mygtukų tekstai, klaidų pranešimai ir pritaikymo neįgaliesiems aprašymai saugomi `strings.xml` failuose, o kode nėra užkoduotų eilučių. Kalba parenkama pagal įrenginio sistemos kalbą.
 
| Kalba | Failas | Dešimtainis skirtukas | Tūkstančių skirtukas |
|---|---|---|---|
| Anglų (numatytoji) | `res/values/strings.xml` | `.` | `,` |
| Lietuvių | `res/values-lt/strings.xml` | `,` | neskiriamasis tarpas |
 
### Kaip pridėti naują kalbą
1. Sukurkite aplanką `res/values-<kalbos kodas>/` (pvz., `values-de`).
2. Nukopijuokite `values/strings.xml` į jį ir išverskite reikšmes.
3. Nustatykite `decimal_separator` ir `grouping_separator` pagal tos kalbos taisykles.
Mygtukų tekstus (pvz., `key_sqrt`, `key_ce`) galima keisti vien redaguojant šiuos failus, kodo liesti nereikia.
 
---
 
## Testai
 
Variklis padengtas **22 JUnit 4 vienetų testais** (`CalculatorEngineTest.kt`). Jie tikrina:
- pagrindines operacijas ir dešimtainių skaičių tikslumą;
- nuoseklų veiksmų vykdymą ir pakartotinį `=`;
- `1/x`, `x²` ir `²√x`, įskaitant labai mažus skaičius;
- `%` elgseną su `+`, `−`, `×` ir be veiksmo;
- `⌫`, `CE`, `C` ir `+/−`;
- klaidas (dalyba iš nulio, neigiama šaknis, perpildymas) ir jų neįrašymą į istoriją;
- atmintį, istorijos įrašo grąžinimą ir lokalizuotą skaičių formatavimą.
**Paleidimas:** Android Studio atidarykite `CalculatorEngineTest.kt` ir spauskite **Run**, arba dešiniuoju pelės klavišu spustelėkite aplanką `test` ir pasirinkite **Run Tests**.
 
---
 
## Naršyklės peržiūra (AI Studio)
 
Repozitorijos šaknyje yra papildoma **React + TypeScript** versija, sukurta „Google AI Studio“ greitai peržiūrai naršyklėje. Ji naudoja tą pačią skaičiavimo logiką, perrašytą TypeScript kalba (`src/engine/decimalEngine.ts`), ir leidžia pakeisti kalbą, temą bei orientaciją.
 
Tai pagalbinė peržiūra. Pagrindinis darbo rezultatas yra Android programėlė aplanke `Win11Calculator`.
 
Paleidimas (reikia [Node.js](https://nodejs.org/)):
 
```bash
npm install
npm run dev
```
 
Peržiūra bus pasiekiama adresu `http://localhost:3000`.
 
---
 
## Žinomi apribojimai
 
- Istorija ir atmintis nesaugomos po programos uždarymo (tik runtime).
- Atminties sąrašo mygtukas `M∨` neaktyvus, o atmintis yra vienos vietos.
- Kalba keičiama tik per įrenginio sistemos nustatymus, programoje perjungiklio nėra.
- Yra tik standartinis režimas (nėra mokslinio ar programuotojo režimo).
 
