package cat.cims.tracker.ui.inici

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.cims.tracker.R
import cat.cims.tracker.domini.Estat
import cat.cims.tracker.domini.ResumSeguiment
import cat.cims.tracker.ui.components.BotoPrincipal
import cat.cims.tracker.ui.components.Etiqueta
import cat.cims.tracker.ui.components.ombraDura
import cat.cims.tracker.ui.dataLlarga
import cat.cims.tracker.ui.textEstoc
import cat.cims.tracker.ui.textFa
import cat.cims.tracker.ui.textDies
import cat.cims.tracker.ui.theme.Caveat
import cat.cims.tracker.ui.theme.Colors
import kotlin.math.abs

@Composable
fun IniciScreen(resums: List<ResumSeguiment>, avui: Long, onObre: (Long) -> Unit, onNou: () -> Unit) {
    val toca = resums.filter { it.estat == Estat.TOCA }
    val aviat = resums.filter { it.estat == Estat.AVIAT }
    val resta = resums.filter { it.estat == Estat.BE || it.estat == Estat.SENSE_DADES }
    Column(Modifier.fillMaxSize().background(Colors.Fons).safeDrawingPadding()) {
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Capcalera(avui) }
            if (resums.isEmpty()) item { Buit() }
            seccio(R.string.seccio_toca, toca, onObre)
            seccio(R.string.seccio_aviat, aviat, onObre)
            seccio(R.string.seccio_be, resta, onObre)
        }
        Box(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            BotoPrincipal(stringResource(R.string.nou_seguiment), onNou, icona = Icons.Filled.Add)
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.seccio(titol: Int, llista: List<ResumSeguiment>, onObre: (Long) -> Unit) {
    if (llista.isEmpty()) return
    item(key = "titol$titol") { Etiqueta(stringResource(titol), Modifier.padding(top = 12.dp)) }
    items(llista, key = { it.seguiment.id }) { FilaSeguiment(it) { onObre(it.seguiment.id) } }
}

@Composable
private fun Capcalera(avui: Long) {
    Column(Modifier.padding(bottom = 4.dp)) {
        Etiqueta(dataLlarga(avui))
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.width(10.dp))
            // La marca de les apps de la casa: una placa petita amb el color fort (als jocs no hi és).
            Text(
                stringResource(R.string.marca_app),
                style = MaterialTheme.typography.labelSmall,
                color = Colors.TintaFort,
                modifier = Modifier.background(Colors.Fort).border(1.5.dp, Colors.Text).padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun Buit() {
    Column(Modifier.fillMaxWidth().padding(vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.buit_titol), fontFamily = Caveat, fontSize = 28.sp, color = Colors.Text)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.buit_text), style = MaterialTheme.typography.bodyMedium, color = Colors.TextSecundari)
    }
}

/** La xifra gran de la fila, la seva etiqueta i la línia de sota. */
private data class Xifra(val numero: String, val etiqueta: String, val linia: String)

@Composable
private fun xifra(r: ResumSeguiment): Xifra {
    val e = r.estoc
    val reg = r.resumRegistres
    // Si el seguiment té estoc i registres, mana el que toca abans.
    val usaEstoc = e?.diesCoberts != null &&
        (reg?.diesFinsPropera == null || e.diesCoberts - r.seguiment.avisEstocDies <= reg.diesFinsPropera - r.seguiment.avisAbansDies)
    return when {
        usaEstoc && e != null -> Xifra(
            numero = e.diesCoberts!!.coerceAtLeast(0).toString(),
            etiqueta = stringResource(R.string.xifra_dies_estoc),
            linia = stringResource(R.string.linia_queden, textEstoc(r.seguiment, e.unitats)),
        )
        reg?.diesFinsPropera != null -> Xifra(
            numero = abs(reg.diesFinsPropera).toString(),
            etiqueta = stringResource(if (reg.diesFinsPropera < 0) R.string.xifra_tard else R.string.xifra_falten),
            linia = stringResource(R.string.linia_darrer, textFa(reg.diesDesDelDarrer ?: 0), textDies(reg.interval?.toLong() ?: 0)),
        )
        reg?.diesDesDelDarrer != null -> Xifra(
            numero = reg.diesDesDelDarrer.toString(),
            etiqueta = stringResource(R.string.xifra_fa),
            linia = stringResource(R.string.linia_un_registre),
        )
        e != null -> Xifra("—", "", stringResource(R.string.linia_queden, textEstoc(r.seguiment, e.unitats)))
        else -> Xifra("—", "", stringResource(R.string.linia_sense_registres))
    }
}

@Composable
private fun FilaSeguiment(r: ResumSeguiment, onClick: () -> Unit) {
    val x = xifra(r)
    val toca = r.estat == Estat.TOCA
    val fonsXifra: Color = when (r.estat) {
        Estat.TOCA -> Colors.Fort
        Estat.AVIAT -> Colors.FortSuau
        else -> Colors.Superficie
    }
    val tintaXifra = if (toca) Colors.TintaFort else Colors.Text
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (toca) Modifier.ombraDura() else Modifier.padding(end = 3.dp, bottom = 3.dp))
            .background(Colors.Fons)
            .border(if (toca) 2.dp else 1.5.dp, if (toca) Colors.Text else Colors.Linia)
            .clickable(role = Role.Button, onClick = onClick)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            Modifier.width(84.dp).fillMaxHeight().heightIn(min = 84.dp).background(fonsXifra).padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(x.numero, style = MaterialTheme.typography.displaySmall, color = tintaXifra, maxLines = 1)
            if (x.etiqueta.isNotEmpty()) Text(x.etiqueta.uppercase(), style = MaterialTheme.typography.labelSmall, color = tintaXifra, maxLines = 1)
        }
        Column(Modifier.weight(1f).padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(r.seguiment.nom, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (r.seguiment.detall.isNotBlank()) {
                Text(r.seguiment.detall, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(4.dp))
            Text(x.linia, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}
