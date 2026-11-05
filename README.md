# Tracker

App Android per no quedar-se mai sense les coses de cada dia i recordar les que toca tornar a fer: les pastilles, tallar-se
el cabell, la gasolina, les revisions del cotxe, les visites al metge…

Tot és **genèric**: cada seguiment es fa amb dues peces que es poden combinar.

- **Estoc que es gasta sol.** Unitats, paquets (p. ex. caixes de 28 pastilles), quantes se'n gasten i cada quants dies.
  L'app resta el consum cada dia i avisa quan queden pocs dies. Es pot sumar una compra, corregir l'estoc o ajustar ±1.
- **Registres.** Cada cop que fas una cosa, ho apuntes (avui o un altre dia, amb una xifra opcional: euros, litres…).
  L'app compta els dies, en fa la mitjana i avisa quan toca tornar-hi. També pot anar amb un interval fix (cada any).

## Instal·lar la versió de prova

Cada canvi a `main` compila l'app a GitHub Actions i publica l'APK a la versió
[**prova**](../../releases/tag/prova). Baixa `tracker.apk` des del mòbil i instal·la-la (cal permetre instal·lar apps
d'origen desconegut). Les versions noves s'instal·len a sobre i mantenen les dades.

## Compilar

Obre la carpeta amb Android Studio, o:

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

## Estructura

```
app/src/main/kotlin/cat/cims/tracker/
  dades/      Room (seguiments i registres) i les plantilles
  domini/     les regles (estoc, mitjanes, quan toca), en Kotlin pur i amb tests
  avisos/     la revisió diària amb WorkManager i les notificacions
  ui/         pantalles en Compose i el tema de la casa
docs/disseny.md   l'estètica (com els jocs de la casa, però d'app)
```

## Més endavant

- Supabase (comptes i còpia al núvol). Room continuarà sent la font principal i l'app funcionarà sense connexió.
- Publicació a Google Play.
