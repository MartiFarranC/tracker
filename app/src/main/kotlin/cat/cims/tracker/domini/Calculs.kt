package cat.cims.tracker.domini

import cat.cims.tracker.dades.Registre
import cat.cims.tracker.dades.Seguiment
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

/** Com va un seguiment: si encara hi ha marge, si s'acosta o si ja toca fer alguna cosa. */
enum class Estat { TOCA, AVIAT, BE, SENSE_DADES }

/** L'estat de l'estoc avui. [diesCoberts]: els dies, a partir de demà, que encara tens. Null si no es gasta. */
data class ResumEstoc(
    val unitats: Double,
    val diesCoberts: Long?,
    /** L'últim dia que en tens. */
    val finsAl: Long?,
    val estat: Estat,
)

/** El resum dels registres d'un seguiment. */
data class ResumRegistres(
    val nombre: Int,
    val darrer: Long?,
    val diesDesDelDarrer: Long?,
    /** La mitjana de dies entre dos registres seguits. Null si n'hi ha menys de dos. */
    val mitjana: Double?,
    /** L'interval que es fa servir per calcular el proper: el fix o la mitjana arrodonida. */
    val interval: Int?,
    val propera: Long?,
    /** Els dies que falten per al proper (negatiu si ja ha passat). */
    val diesFinsPropera: Long?,
    val mitjanaValor: Double?,
    val totalValor: Double?,
    val estat: Estat,
)

data class ResumSeguiment(
    val seguiment: Seguiment,
    val registres: List<Registre>,
    val estoc: ResumEstoc?,
    val resumRegistres: ResumRegistres?,
) {
    val estat: Estat = listOfNotNull(estoc?.estat, resumRegistres?.estat).minByOrNull { it.ordinal } ?: Estat.SENSE_DADES

    /** Per ordenar la llista: com més petit, més urgent. */
    val urgencia: Long = listOfNotNull(
        estoc?.diesCoberts?.minus(seguiment.avisEstocDies),
        resumRegistres?.diesFinsPropera?.minus(seguiment.avisAbansDies),
    ).minOrNull() ?: Long.MAX_VALUE
}

/** Les regles de l'app, en Kotlin pur perquè es puguin provar sense el mòbil. */
object Calculs {
    private const val EPSILON = 1e-9

    /** Quantes vegades s'ha gastat fins al dia [dia] inclòs, si es gasta cada [cadaDies] dies des de [inici]. */
    fun consumsFins(dia: Long, inici: Long, cadaDies: Int): Long =
        if (dia < inici) 0 else (dia - inici) / max(1, cadaDies) + 1

    /** Les unitats que queden avui: les de [Seguiment.estocData] menys el que s'ha gastat després. */
    fun unitatsAvui(s: Seguiment, avui: Long): Double {
        val consums = (consumsFins(avui, s.consumInici, s.consumCadaDies) - consumsFins(s.estocData, s.consumInici, s.consumCadaDies)).coerceAtLeast(0)
        return max(0.0, s.estocUnitats - consums * s.consumQuantitat)
    }

    fun estoc(s: Seguiment, avui: Long): ResumEstoc {
        val unitats = unitatsAvui(s, avui)
        if (s.consumQuantitat <= 0) return ResumEstoc(unitats, null, null, Estat.BE)
        val cada = max(1, s.consumCadaDies)
        // Quantes vegades més se'n pot gastar, i el dia que ja no n'hi haurà prou.
        val vegades = floor(unitats / s.consumQuantitat + EPSILON).toLong()
        val fets = consumsFins(avui, s.consumInici, cada)
        val diaQueFalten = s.consumInici + (fets + vegades) * cada
        val finsAl = diaQueFalten - 1
        val dies = finsAl - avui
        val estat = when {
            dies <= s.avisEstocDies -> Estat.TOCA
            dies <= s.avisEstocDies * 2L -> Estat.AVIAT
            else -> Estat.BE
        }
        return ResumEstoc(unitats, dies, finsAl, estat)
    }

    fun registres(s: Seguiment, registres: List<Registre>, avui: Long): ResumRegistres {
        val dies = registres.map { it.data }.sorted()
        val intervals = dies.zipWithNext { a, b -> b - a }.filter { it > 0 }
        val mitjana = if (intervals.isEmpty()) null else intervals.average()
        val interval = s.intervalFixDies?.takeIf { it > 0 } ?: mitjana?.roundToInt()?.coerceAtLeast(1)
        val darrer = dies.lastOrNull()
        val propera = if (darrer != null && interval != null) darrer + interval else null
        val falten = propera?.minus(avui)
        val valors = registres.mapNotNull { it.valor }
        val estat = when {
            falten == null -> Estat.SENSE_DADES
            falten <= s.avisAbansDies -> Estat.TOCA
            falten <= s.avisAbansDies + max(3, (interval ?: 0) / 5) -> Estat.AVIAT
            else -> Estat.BE
        }
        return ResumRegistres(
            nombre = dies.size,
            darrer = darrer,
            diesDesDelDarrer = darrer?.let { avui - it },
            mitjana = mitjana,
            interval = interval,
            propera = propera,
            diesFinsPropera = falten,
            mitjanaValor = if (valors.isEmpty()) null else valors.average(),
            totalValor = if (valors.isEmpty()) null else valors.sum(),
            estat = estat,
        )
    }

    fun resum(s: Seguiment, registres: List<Registre>, avui: Long): ResumSeguiment {
        val propis = registres.filter { it.seguimentId == s.id }
        return ResumSeguiment(
            seguiment = s,
            registres = propis.sortedByDescending { it.data },
            estoc = if (s.ambEstoc) estoc(s, avui) else null,
            resumRegistres = if (s.ambRegistres) registres(s, propis, avui) else null,
        )
    }

    /**
     * Deixa l'estoc desat amb data d'avui i [nouesUnitats]. Es fa servir en comprar, corregir o ajustar, perquè el
     * consum es continuï comptant des d'avui.
     */
    fun ambEstoc(s: Seguiment, novesUnitats: Double, avui: Long): Seguiment =
        s.copy(estocUnitats = max(0.0, novesUnitats), estocData = avui)

    /** Les unitats repartides en paquets sencers i soltes. */
    fun enPaquets(unitats: Double, perPaquet: Int): Pair<Int, Double> {
        if (perPaquet <= 0) return 0 to unitats
        val paquets = floor(unitats / perPaquet + EPSILON).toInt()
        return paquets to max(0.0, unitats - paquets * perPaquet)
    }

    /** El plural català d'un nom senzill: pastilla → pastilles, càpsula → càpsules, revisió → revisions. */
    fun plural(nom: String): String {
        val n = nom.trim()
        if (n.isEmpty()) return n
        val minuscula = n.lowercase()
        return when {
            minuscula.endsWith("ca") -> n.dropLast(2) + "ques"
            minuscula.endsWith("ga") -> n.dropLast(2) + "gues"
            minuscula.endsWith("ça") -> n.dropLast(2) + "ces"
            minuscula.endsWith("ja") -> n.dropLast(2) + "ges"
            minuscula.endsWith("a") -> n.dropLast(1) + "es"
            minuscula.endsWith("ió") -> n.dropLast(2) + "ions"
            minuscula.endsWith("s") || minuscula.endsWith("x") -> n
            else -> n + "s"
        }
    }

    /** El nom en singular o en plural segons la quantitat. */
    fun nom(singular: String, quantitat: Double): String =
        if (quantitat > 1 - EPSILON && quantitat < 1 + EPSILON) singular else plural(singular)
}
