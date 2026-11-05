package cat.cims.tracker.ui.editar

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cat.cims.tracker.R
import cat.cims.tracker.dades.Plantilla
import cat.cims.tracker.dades.Seguiment
import cat.cims.tracker.domini.Calculs
import cat.cims.tracker.ui.components.BotoIcona
import cat.cims.tracker.ui.components.BotoPrincipal
import cat.cims.tracker.ui.components.BotoSecundari
import cat.cims.tracker.ui.components.Camp
import cat.cims.tracker.ui.components.Etiqueta
import cat.cims.tracker.ui.components.Interruptor
import cat.cims.tracker.ui.components.SelectorData
import cat.cims.tracker.ui.dataCurta
import cat.cims.tracker.ui.theme.Colors
import cat.cims.tracker.ui.xifra

/** Un número escrit per l'usuari, amb coma o amb punt. */
private fun numero(text: String): Double? = text.trim().replace(',', '.').toDoubleOrNull()

private fun text(n: Double): String = xifra(n).replace(".", "")
private fun text(n: Int): String = n.toString()

/**
 * Crear o editar un seguiment. Si és nou, primer es tria una plantilla, que només omple el formulari.
 * [onDesa] rep el seguiment i, si és nou i té registres, el dia de l'últim cop (o null).
 */
@Composable
fun EditarScreen(original: Seguiment?, avui: Long, onDesa: (Seguiment, Long?) -> Unit, onEnrere: () -> Unit) {
    val context = LocalContext.current
    var base by remember { mutableStateOf(original) }
    val actual = base
    if (actual == null) {
        TriaPlantilla(onTria = { base = it.seguiment(context, avui) }, onEnrere = onEnrere)
        return
    }
    BackHandler(enabled = original == null) { base = null }
    Formulari(actual, nou = original == null, avui = avui, onDesa = onDesa, onEnrere = { if (original == null) base = null else onEnrere() })
}

@Composable
private fun BarraSuperior(titol: String, onEnrere: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        BotoIcona(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.enrere), onEnrere)
        Spacer(Modifier.width(12.dp))
        Text(titol, style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
private fun TriaPlantilla(onTria: (Plantilla) -> Unit, onEnrere: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Colors.Fons).safeDrawingPadding()) {
        BarraSuperior(stringResource(R.string.nou_seguiment), onEnrere)
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(stringResource(R.string.tria_plantilla), style = MaterialTheme.typography.bodyMedium, color = Colors.TextSecundari)
            Plantilla.entries.forEach { p ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, if (p == Plantilla.PERSONALITZAT) Colors.Text else Colors.Linia)
                        .clickable(role = Role.Button) { onTria(p) }
                        .padding(16.dp),
                ) {
                    Text(stringResource(p.titol), style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(2.dp))
                    Text(stringResource(p.descripcio), style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun Formulari(s: Seguiment, nou: Boolean, avui: Long, onDesa: (Seguiment, Long?) -> Unit, onEnrere: () -> Unit) {
    var nom by rememberSaveable { mutableStateOf(s.nom) }
    var detall by rememberSaveable { mutableStateOf(s.detall) }

    var ambEstoc by rememberSaveable { mutableStateOf(s.ambEstoc) }
    var nomUnitat by rememberSaveable { mutableStateOf(s.nomUnitat) }
    var nomPaquet by rememberSaveable { mutableStateOf(s.nomPaquet) }
    var perPaquet by rememberSaveable { mutableStateOf(if (s.unitatsPerPaquet > 0) text(s.unitatsPerPaquet) else "") }
    var quantitat by rememberSaveable { mutableStateOf(text(s.consumQuantitat)) }
    var cadaDies by rememberSaveable { mutableStateOf(text(s.consumCadaDies)) }
    var avisEstoc by rememberSaveable { mutableStateOf(text(s.avisEstocDies)) }
    // Només si encara no tenia estoc: el que hi ha ara.
    val demanaEstoc = nou || !s.ambEstoc
    var paquetsAra by rememberSaveable { mutableStateOf("") }
    var soltesAra by rememberSaveable { mutableStateOf("") }
    var avuiJaGastat by rememberSaveable { mutableStateOf(false) }

    var ambRegistres by rememberSaveable { mutableStateOf(s.ambRegistres) }
    var ambIntervalFix by rememberSaveable { mutableStateOf(s.intervalFixDies != null) }
    var intervalFix by rememberSaveable { mutableStateOf(s.intervalFixDies?.let(::text) ?: "30") }
    var avisAbans by rememberSaveable { mutableStateOf(text(s.avisAbansDies)) }
    var ambValor by rememberSaveable { mutableStateOf(s.ambValor) }
    var unitatValor by rememberSaveable { mutableStateOf(s.unitatValor) }
    var darrerCop by rememberSaveable { mutableStateOf<Long?>(null) }
    var triantData by remember { mutableStateOf(false) }

    var error by remember { mutableStateOf<Int?>(null) }

    fun desa() {
        val q = numero(quantitat)
        val c = numero(cadaDies)?.toInt()
        val pp = numero(perPaquet)?.toInt() ?: 0
        error = when {
            nom.isBlank() -> R.string.error_nom
            !ambEstoc && !ambRegistres -> R.string.error_cap
            ambEstoc && nomUnitat.isBlank() -> R.string.error_unitat
            ambEstoc && (q == null || q < 0 || c == null || c < 1) -> R.string.error_consum
            ambRegistres && ambIntervalFix && (numero(intervalFix)?.toInt() ?: 0) < 1 -> R.string.error_interval
            else -> null
        }
        if (error != null) return
        var resultat = s.copy(
            nom = nom.trim(),
            detall = detall.trim(),
            ambEstoc = ambEstoc,
            nomUnitat = nomUnitat.trim(),
            nomPaquet = nomPaquet.trim(),
            unitatsPerPaquet = pp,
            consumQuantitat = q ?: s.consumQuantitat,
            consumCadaDies = c ?: s.consumCadaDies,
            avisEstocDies = numero(avisEstoc)?.toInt()?.coerceAtLeast(0) ?: 0,
            ambRegistres = ambRegistres,
            intervalFixDies = if (ambIntervalFix) numero(intervalFix)?.toInt() else null,
            avisAbansDies = numero(avisAbans)?.toInt()?.coerceAtLeast(0) ?: 0,
            ambValor = ambValor,
            unitatValor = unitatValor.trim(),
        )
        if (ambEstoc && demanaEstoc) {
            val unitats = (numero(paquetsAra) ?: 0.0) * pp + (numero(soltesAra) ?: 0.0)
            // Si avui encara no se n'ha gastat, l'estoc és el d'ahir al vespre: l'app ja hi restarà el d'avui.
            val dia = if (avuiJaGastat) avui else avui - 1
            resultat = resultat.copy(estocUnitats = unitats, estocData = dia, consumInici = avui)
        } else if (ambEstoc && (resultat.consumCadaDies != s.consumCadaDies || resultat.consumQuantitat != s.consumQuantitat)) {
            // Si canvia el consum, l'estoc d'avui es desa tal com és i es torna a comptar des d'aquí.
            resultat = Calculs.ambEstoc(resultat, Calculs.unitatsAvui(s, avui), avui).copy(consumInici = avui + 1)
        }
        onDesa(resultat, if (ambRegistres) darrerCop else null)
    }

    if (triantData) SelectorData(darrerCop ?: avui, avui, onTria = { darrerCop = it }, onTanca = { triantData = false })

    Column(Modifier.fillMaxSize().background(Colors.Fons).safeDrawingPadding().imePadding()) {
        BarraSuperior(stringResource(if (nou) R.string.nou_seguiment else R.string.edita), onEnrere)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Camp(nom, { nom = it }, stringResource(R.string.camp_nom))
            Camp(detall, { detall = it }, stringResource(R.string.camp_detall), ajuda = stringResource(R.string.camp_detall_ajuda))

            Separador()
            Interruptor(stringResource(R.string.amb_estoc), stringResource(R.string.amb_estoc_desc), ambEstoc, { ambEstoc = it })
            if (ambEstoc) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Camp(nomUnitat, { nomUnitat = it }, stringResource(R.string.camp_unitat), Modifier.weight(1f))
                    Camp(nomPaquet, { nomPaquet = it }, stringResource(R.string.camp_paquet), Modifier.weight(1f))
                }
                if (nomPaquet.isNotBlank()) {
                    Camp(
                        perPaquet, { perPaquet = it },
                        stringResource(R.string.camp_per_paquet, Calculs.plural(nomUnitat), nomPaquet).replaceFirstChar { it.uppercase() }, numeric = true,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Camp(quantitat, { quantitat = it }, stringResource(R.string.camp_quantitat, Calculs.plural(nomUnitat)).replaceFirstChar { it.uppercase() }, Modifier.weight(1f), numeric = true)
                    Camp(cadaDies, { cadaDies = it }, stringResource(R.string.camp_cada_dies), Modifier.weight(1f), numeric = true)
                }
                if (demanaEstoc) {
                    Etiqueta(stringResource(R.string.que_tens_ara), Modifier.padding(top = 8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (nomPaquet.isNotBlank()) {
                            Camp(paquetsAra, { paquetsAra = it }, Calculs.plural(nomPaquet).replaceFirstChar { it.uppercase() }, Modifier.weight(1f), numeric = true)
                        }
                        Camp(
                            soltesAra, { soltesAra = it },
                            if (nomPaquet.isNotBlank()) stringResource(R.string.camp_soltes, Calculs.plural(nomUnitat)).replaceFirstChar { it.uppercase() } else Calculs.plural(nomUnitat).replaceFirstChar { it.uppercase() },
                            Modifier.weight(1f), numeric = true,
                        )
                    }
                    Interruptor(stringResource(R.string.avui_ja_gastat), stringResource(R.string.avui_ja_gastat_desc), avuiJaGastat, { avuiJaGastat = it })
                }
                Camp(avisEstoc, { avisEstoc = it }, stringResource(R.string.camp_avis_estoc), numeric = true)
            }

            Separador()
            Interruptor(stringResource(R.string.amb_registres), stringResource(R.string.amb_registres_desc), ambRegistres, { ambRegistres = it })
            if (ambRegistres) {
                Interruptor(stringResource(R.string.interval_fix), stringResource(R.string.interval_fix_desc), ambIntervalFix, { ambIntervalFix = it })
                if (ambIntervalFix) Camp(intervalFix, { intervalFix = it }, stringResource(R.string.camp_interval), numeric = true)
                Camp(avisAbans, { avisAbans = it }, stringResource(R.string.camp_avis_abans), numeric = true)
                Interruptor(stringResource(R.string.amb_valor), stringResource(R.string.amb_valor_desc), ambValor, { ambValor = it })
                if (ambValor) Camp(unitatValor, { unitatValor = it }, stringResource(R.string.camp_unitat_valor))
                if (nou) {
                    Spacer(Modifier.height(4.dp))
                    BotoSecundari(
                        darrerCop?.let { stringResource(R.string.darrer_cop_triat, dataCurta(it, avui)) } ?: stringResource(R.string.darrer_cop),
                        { triantData = true },
                    )
                }
            }
            error?.let { Text(stringResource(it), color = Colors.Error, style = MaterialTheme.typography.bodyMedium) }
            Spacer(Modifier.height(8.dp))
        }
        Box(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            BotoPrincipal(stringResource(R.string.desa), ::desa)
        }
    }
}

@Composable
private fun Separador() {
    HorizontalDivider(Modifier.padding(vertical = 8.dp), thickness = 1.dp, color = Colors.Linia)
}
