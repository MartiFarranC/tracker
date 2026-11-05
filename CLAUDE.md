# CLAUDE.md

Tracker: app Android (Kotlin, Compose, Room, WorkManager) per seguir coses de la vida diària amb dues peces genèriques,
**estoc** que es gasta sol i **registres** amb mitjana i avís. Vegeu `README.md`.

## Normes

- **Idioma:** tot en català: textos de l'app (sempre a `strings.xml`), documentació, comentaris i commits.
- **Estètica:** `docs/disseny.md`. La de municipis i Andana, però en versió app (verd menta `#3DDC97`, més calma).
- **Regles:** totes a `domini/Calculs.kt`, en Kotlin pur i amb tests a `app/src/test`.
- **Dades:** Room és la font principal i l'app funciona sense connexió. Quan hi hagi Supabase, només `supabase-kt`, mai la
  clau `service_role` a l'app ni al repositori. Cap SDK d'analítica ni de publicitat.
- **Abans d'acabar una tasca:** compila, i passa els tests i el lint.
- **Git:** una branca per *feature*, commits petits en català, sense línies `Co-authored-by`.
