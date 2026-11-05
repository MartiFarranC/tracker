# Disseny

## La família: jocs i apps

Tracker fa servir el mateix sistema que els jocs de la casa ([municipis](https://github.com/CimsProjects/municipis) i
[Andana 947](https://github.com/CimsProjects/andana947)), perquè es vegi que són de la mateixa empresa. El que canvia és el
to: els jocs criden i fan festa; les apps són tranquil·les i van al gra.

| | Jocs (municipis, Andana) | Apps (Tracker) |
|---|---|---|
| Fons / tinta | `#FFFFFF` / `#111111` (fosc: `#0F0F0F` / `#F2F2F2`) | Igual |
| Color fort | càlid i cridaner: groc `#FFD000`, magenta `#FF3D7F` | fred i tranquil: verd menta `#3DDC97` |
| On va el color fort | per tot: mapa, premis, botons | només a l'acció principal i al que **toca ara** |
| Tipografia | Archivo Black (títols i xifres) + Instrument Sans | Igual |
| Caveat (a mà) | frases fetes, pistes, comentaris | només als missatges de quan no hi ha res |
| Botons | vora de 2 dp, ombra dura de 4 dp | vora de 2 dp, ombra dura de 3 dp (secundaris, 2 dp) |
| Animacions | moltes: sacs, segells, núvols | poques: el botó s'enfonsa i contesta («Fet!») |
| Marca | — | placa petita «APP» al costat del nom |

Es manté: cantonades rectes, res de píndoles ni degradats, clar i fosc segons el mòbil, i textos en català.

## Pantalles

1. **Inici.** La data, el nom i la placa «APP». Els seguiments agrupats en *Toca ara*, *Aviat* i *Tot en ordre*. Cada
   fila té una xifra gran a l'esquerra (dies d'estoc, dies que falten o de retard), en verd ple si toca, verd suau si és
   aviat i gris si va bé. Les que toquen porten ombra dura.
2. **Nou seguiment.** Primer una plantilla (medicament, consumible, cabell, gasolina, revisió del vehicle, visita mèdica
   o personalitzat) i després el formulari, amb dues peces que es poden encendre o apagar: *estoc* i *registres*.
3. **Detall.** Una targeta per a l'estoc (què queda, per a quants dies, −1/+1, «He comprat», «Corregeix») i una per als
   registres (fa quant, quan toca, la mitjana, «Ho he fet avui», «Un altre dia…» i l'historial).
