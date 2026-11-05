package cat.cims.tracker.ui.detall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cat.cims.tracker.R
import cat.cims.tracker.dades.Registre
import cat.cims.tracker.dades.Seguiment
import cat.cims.tracker.domini.Calculs
import cat.cims.tracker.domini.Estat
import cat.cims.tracker.domini.ResumEstoc
import cat.cims.tracker.domini.ResumRegistres
import cat.cims.tracker.domini.ResumSeguiment
import cat.cims.tracker.ui.components.BotoIcona
import cat.cims.tracker.ui.components.BotoPrincipal
import cat.cims.tracker.ui.components.BotoSecundari
import cat.cims.tracker.ui.components.Camp
import cat.cims.tracker.ui.components.Etiqueta
import cat.cims.tracker.ui.components.SelectorData
import cat.cims.tracker.ui.components.Targeta
import cat.cims.tracker.ui.components.ombraDura
import cat.cims.tracker.ui.dataCurta
import cat.cims.tracker.ui.textDies
import cat.cims.tracker.ui.textEstoc
import cat.cims.tracker.ui.textFa
import cat.cims.tracker.ui.textFalten
import cat.cims.tracker.ui.theme.Colors
import cat.cims.tracker.ui.xifra
import kotlinx.coroutines.delay

private fun numero(text: String): Double? = text.trim().replace(',', '.').toDoubleOrNull()

/** Les accions de la pantalla, perquè la pantalla no depengui del ViewModel. */
class AccionsDetall(
    val onEnrere: () -> Unit,
    val onEdita: () -> Unit,
    val onEsborra: () -> Unit,
    val onSumaEstoc: (Double) -> Unit,
    val onFixaEstoc: (Double) -> Unit,
    val onRegistra: (data: Long, valor: Double?, nota: String) -> Unit,
    val onEsborraRegistre: (Registre) -> Unit,
)

@Composable
fun DetallScreen(r: ResumSeguiment, avui: Long, accions: AccionsDetall) {
    val s = r.seguiment
    var esborrant by remember { mutableStateOf(false) }
    if (esborrant) {
        AlertDialog(
            onDismissRequest = { esborrant = false },
            title = { Text(stringResource(R.string.esborrar_titol, s.nom)) },
            text = { Text(stringResource(R.string.esborrar_text)) },
            confirmButton = { TextButton(onClick = { esborrant = false; accions.onEsborra() }) { Text(stringResource(R.string.esborra), color = Colors.Error) } },
            dismissButton = { TextButton(onClick = { esborrant = false }) { Text(stringResource(R.string.cancela), color = Colors.Text) } },
        )
    }
    Column(Modifier.fillMaxSize().background(Colors.Fons).safeDrawingPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            BotoIcona(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.enrere), accions.onEnrere)
            Spacer(Modifier.weight(1f))
            BotoIcona(Icons.Filled.Edit, stringResource(R.string.edita), accions.onEdita)
            Spacer(Modifier.width(8.dp))
            BotoIcona(Icons.Filled.Delete, stringResource(R.string.esborra), { esborrant = true })
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column {
                Text(s.nom, style = MaterialTheme.typography.headlineLarge)
                if (s.detall.isNotBlank()) Text(s.detall, style = MaterialTheme.typography.bodyLarge, color = Colors.TextSecundari)
            }
            r.estoc?.let { SeccioEstoc(s, it, accions) }
            r.resumRegistres?.let { SeccioRegistres(s, it, r.registres, avui, accions) }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SeccioEstoc(s: Seguiment, e: ResumEstoc, accions: AccionsDetall) {
    var comprant by remember { mutableStateOf(false) }
    var corregint by remember { mutableStateOf(false) }
    val toca = e.estat == Estat.TOCA
    Targeta(
        Modifier.fillMaxWidth().then(if (toca) Modifier.ombraDura() else Modifier),
        fons = if (toca) Colors.Fort else Colors.Fons,
    ) {
        val tinta = if (toca) Colors.TintaFort else Colors.Text
        Column {
            Etiqueta(stringResource(R.string.et_queden), color = tinta)
            Text(textEstoc(s, e.unitats), style = MaterialTheme.typography.headlineMedium, color = tinta)
            Spacer(Modifier.height(4.dp))
            val dies = e.diesCoberts
            val finsAl = e.finsAl
            val linia = when {
                dies == null || finsAl == null -> stringResource(R.string.estoc_no_es_gasta)
                dies < 0 || e.unitats <= 0 -> stringResource(R.string.estoc_acabat)
                dies == 0L -> stringResource(R.string.estoc_avui_ultim)
                else -> stringResource(R.string.estoc_per_a, textDies(dies), dataCurta(finsAl, finsAl - dies))
            }
            Text(linia, style = MaterialTheme.typography.bodyLarge, color = tinta)
            Text(
                stringResource(R.string.consum, xifra(s.consumQuantitat), Calculs.nom(s.nomUnitat, s.consumQuantitat), consumCada(s.consumCadaDies)),
                style = MaterialTheme.typography.bodySmall,
                color = if (toca) Colors.TintaFort else Colors.TextSecundari,
            )
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BotoSecundari(stringResource(R.string.menys_u), { accions.onSumaEstoc(-1.0) }, Modifier.weight(1f))
        BotoSecundari(stringResource(R.string.mes_u), { accions.onSumaEstoc(1.0) }, Modifier.weight(1f))
    }
    Text(stringResource(R.string.ajust_ajuda), style = MaterialTheme.typography.bodySmall)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BotoSecundari(stringResource(R.string.he_comprat), { comprant = true }, Modifier.weight(1f))
        BotoSecundari(stringResource(R.string.corregeix), { corregint = true }, Modifier.weight(1f))
    }
    if (comprant) DialegEstoc(s, R.string.he_comprat, R.string.he_comprat_text, { comprant = false }) { accions.onSumaEstoc(it) }
    if (corregint) DialegEstoc(s, R.string.corregeix, R.string.corregeix_text, { corregint = false }) { accions.onFixaEstoc(it) }
}

@Composable
private fun consumCada(dies: Int): String =
    if (dies <= 1) stringResource(R.string.cada_dia) else pluralStringResource(R.plurals.cada_dies, dies, dies)

/** Demana paquets i unitats soltes, i en torna el total d'unitats. */
@Composable
private fun DialegEstoc(s: Seguiment, titol: Int, text: Int, onTanca: () -> Unit, onAccepta: (Double) -> Unit) {
    var paquets by remember { mutableStateOf("") }
    var soltes by remember { mutableStateOf("") }
    val ambPaquets = s.nomPaquet.isNotBlank() && s.unitatsPerPaquet > 0
    AlertDialog(
        onDismissRequest = onTanca,
        title = { Text(stringResource(titol)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(text))
                if (ambPaquets) Camp(paquets, { paquets = it }, Calculs.plural(s.nomPaquet).replaceFirstChar { it.uppercase() }, numeric = true)
                Camp(
                    soltes, { soltes = it },
                    if (ambPaquets) stringResource(R.string.camp_soltes, Calculs.plural(s.nomUnitat)).replaceFirstChar { it.uppercase() } else Calculs.plural(s.nomUnitat).replaceFirstChar { it.uppercase() },
                    numeric = true,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val total = (numero(paquets) ?: 0.0) * (if (ambPaquets) s.unitatsPerPaquet else 0) + (numero(soltes) ?: 0.0)
                onAccepta(total)
                onTanca()
            }) { Text(stringResource(R.string.accepta), color = Colors.Text) }
        },
        dismissButton = { TextButton(onClick = onTanca) { Text(stringResource(R.string.cancela), color = Colors.TextSecundari) } },
    )
}

@Composable
private fun SeccioRegistres(s: Seguiment, rr: ResumRegistres, registres: List<Registre>, avui: Long, accions: AccionsDetall) {
    val toca = rr.estat == Estat.TOCA
    var fet by remember { mutableStateOf(false) }
    var triantData by remember { mutableStateOf(false) }
    var demanantValor by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(fet) { if (fet) { delay(1600); fet = false } }

    fun registra(dia: Long) {
        if (s.ambValor) demanantValor = dia else { accions.onRegistra(dia, null, ""); if (dia == avui) fet = true }
    }

    Targeta(
        Modifier.fillMaxWidth().then(if (toca) Modifier.ombraDura() else Modifier),
        fons = if (toca) Colors.Fort else Colors.Fons,
    ) {
        val tinta = if (toca) Colors.TintaFort else Colors.Text
        val secundari = if (toca) Colors.TintaFort else Colors.TextSecundari
        Column {
            Etiqueta(stringResource(R.string.darrer_cop_etiqueta), color = tinta)
            Text(rr.diesDesDelDarrer?.let { textFa(it) } ?: stringResource(R.string.mai), style = MaterialTheme.typography.headlineMedium, color = tinta)
            Spacer(Modifier.height(4.dp))
            when {
                rr.propera != null && rr.diesFinsPropera != null -> Text(
                    stringResource(R.string.proper_cop, dataCurta(rr.propera, avui), textFalten(rr.diesFinsPropera).lowercase()),
                    style = MaterialTheme.typography.bodyLarge, color = tinta,
                )
                rr.nombre == 1 -> Text(stringResource(R.string.linia_un_registre), style = MaterialTheme.typography.bodyLarge, color = tinta)
                else -> Text(stringResource(R.string.linia_sense_registres), style = MaterialTheme.typography.bodyLarge, color = tinta)
            }
            val interval = when {
                s.intervalFixDies != null -> stringResource(R.string.interval_fix_text, textDies(s.intervalFixDies.toLong()))
                rr.mitjana != null -> stringResource(R.string.interval_mitjana_text, xifra(rr.mitjana), pluralStringResource(R.plurals.registres, rr.nombre, rr.nombre))
                else -> null
            }
            interval?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = secundari) }
            if (s.ambValor && rr.mitjanaValor != null && rr.totalValor != null) {
                Text(
                    stringResource(R.string.valors_text, xifra(rr.mitjanaValor), s.unitatValor, xifra(rr.totalValor), s.unitatValor),
                    style = MaterialTheme.typography.bodySmall, color = secundari,
                )
            }
        }
    }
    BotoPrincipal(stringResource(if (fet) R.string.fet else R.string.ho_he_fet_avui), { registra(avui) })
    BotoSecundari(stringResource(R.string.un_altre_dia), { triantData = true })

    if (triantData) SelectorData(avui, avui, onTria = { registra(it) }, onTanca = { triantData = false })
    demanantValor?.let { dia -> DialegValor(s, dia, avui, onTanca = { demanantValor = null }) { v, n -> accions.onRegistra(dia, v, n); if (dia == avui) fet = true } }

    if (registres.isNotEmpty()) {
        Etiqueta(stringResource(R.string.historial), Modifier.padding(top = 8.dp))
        Column {
            registres.forEachIndexed { i, reg ->
                val anterior = registres.getOrNull(i + 1)
                FilaRegistre(s, reg, anterior?.let { reg.data - it.data }, avui) { accions.onEsborraRegistre(reg) }
                HorizontalDivider(thickness = 1.dp, color = Colors.Linia)
            }
        }
    }
}

@Composable
private fun FilaRegistre(s: Seguiment, reg: Registre, desDelAnterior: Long?, avui: Long, onEsborra: () -> Unit) {
    var confirmant by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(dataCurta(reg.data, avui), style = MaterialTheme.typography.titleMedium)
            val parts = buildList {
                if (desDelAnterior != null) add(stringResource(R.string.des_de_l_anterior, desDelAnterior))
                if (reg.valor != null) add("${xifra(reg.valor)} ${s.unitatValor}".trim())
                if (reg.nota.isNotBlank()) add(reg.nota)
            }
            if (parts.isNotEmpty()) Text(parts.joinToString(" · "), style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = { confirmant = true }) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.esborra_registre), tint = Colors.TextSecundari)
        }
    }
    if (confirmant) {
        AlertDialog(
            onDismissRequest = { confirmant = false },
            title = { Text(stringResource(R.string.esborra_registre)) },
            text = { Text(dataCurta(reg.data, avui)) },
            confirmButton = { TextButton(onClick = { confirmant = false; onEsborra() }) { Text(stringResource(R.string.esborra), color = Colors.Error) } },
            dismissButton = { TextButton(onClick = { confirmant = false }) { Text(stringResource(R.string.cancela), color = Colors.Text) } },
        )
    }
}

@Composable
private fun DialegValor(s: Seguiment, dia: Long, avui: Long, onTanca: () -> Unit, onAccepta: (Double?, String) -> Unit) {
    var valor by remember { mutableStateOf("") }
    var nota by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onTanca,
        title = { Text(dataCurta(dia, avui)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Camp(valor, { valor = it }, stringResource(R.string.camp_valor, s.unitatValor), numeric = true)
                Camp(nota, { nota = it }, stringResource(R.string.camp_nota))
            }
        },
        confirmButton = { TextButton(onClick = { onAccepta(numero(valor), nota); onTanca() }) { Text(stringResource(R.string.desa), color = Colors.Text) } },
        dismissButton = { TextButton(onClick = onTanca) { Text(stringResource(R.string.cancela), color = Colors.TextSecundari) } },
    )
}
