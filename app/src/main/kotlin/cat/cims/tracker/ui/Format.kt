package cat.cims.tracker.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import cat.cims.tracker.R
import cat.cims.tracker.dades.Seguiment
import cat.cims.tracker.domini.Calculs
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private val catala: Locale = Locale.forLanguageTag("ca")
private val dataCurta = DateTimeFormatter.ofPattern("d MMM yyyy", catala)
private val dataCurtaSenseAny = DateTimeFormatter.ofPattern("d MMM", catala)
private val dataLlarga = DateTimeFormatter.ofPattern("EEEE, d MMMM", catala)

fun dia(epoch: Long): LocalDate = LocalDate.ofEpochDay(epoch)

/** «5 de nov.» (amb l'any si no és l'actual). */
fun dataCurta(epoch: Long, avui: Long): String {
    val d = dia(epoch)
    return (if (d.year == dia(avui).year) dataCurtaSenseAny else dataCurta).format(d)
}

/** «Dijous, 5 de novembre». */
fun dataLlarga(epoch: Long): String = dataLlarga.format(dia(epoch)).replaceFirstChar { it.uppercase(catala) }

private val decimals = DecimalFormat("#,##0.##", DecimalFormatSymbols(catala))

/** Una xifra sense decimals inútils: 14, 0,5, 1.250. */
fun xifra(n: Double): String = decimals.format(n)

/** «2 caixes i 14 pastilles», «1 caixa», «3 pastilles». */
@Composable
fun textEstoc(s: Seguiment, unitats: Double): String {
    val (paquets, soltes) = Calculs.enPaquets(unitats, if (s.nomPaquet.isBlank()) 0 else s.unitatsPerPaquet)
    val textUnitats = "${xifra(soltes)} ${Calculs.nom(s.nomUnitat, soltes)}"
    if (paquets == 0) return textUnitats
    val textPaquets = "$paquets ${Calculs.nom(s.nomPaquet, paquets.toDouble())}"
    return if (soltes <= 0.0) textPaquets else stringResource(R.string.estoc_i, textPaquets, textUnitats)
}

/** «Avui», «Demà», «D'aquí a 3 dies», «Fa 2 dies que tocava». */
@Composable
fun textFalten(dies: Long): String = when {
    dies == 0L -> stringResource(R.string.falten_avui)
    dies == 1L -> stringResource(R.string.falten_dema)
    dies > 1 -> pluralStringResource(R.plurals.falten_dies, dies.toInt(), dies.toInt())
    else -> pluralStringResource(R.plurals.tocava_fa, abs(dies).toInt(), abs(dies).toInt())
}

/** «Avui», «Ahir», «Fa 12 dies». */
@Composable
fun textFa(dies: Long): String = when (dies) {
    0L -> stringResource(R.string.fa_avui)
    1L -> stringResource(R.string.fa_ahir)
    else -> pluralStringResource(R.plurals.fa_dies, dies.toInt(), dies.toInt())
}

@Composable
fun textDies(dies: Long): String = pluralStringResource(R.plurals.dies, dies.toInt(), dies.toInt())
