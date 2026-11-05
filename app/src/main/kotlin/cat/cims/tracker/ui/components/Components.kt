package cat.cims.tracker.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.cims.tracker.ui.theme.Colors
import cat.cims.tracker.ui.theme.LocalReduirAnimacions

/**
 * Els botons de la casa (municipis i Andana): vora de tinta de 2 dp i ombra dura. En prémer, s'enfonsen fins a tocar
 * l'ombra. A les apps l'ombra és de 3 dp, una mica més discreta que als jocs (4 dp).
 */
@Composable
private fun BotoAmbOmbra(
    onClick: () -> Unit,
    fons: Color,
    contingut: Color,
    modifier: Modifier,
    enabled: Boolean,
    ombra: Dp = 3.dp,
    alcadaMinima: Dp = 52.dp,
    ample: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val interaccio = remember { MutableInteractionSource() }
    val premut by interaccio.collectIsPressedAsState()
    val reduir = LocalReduirAnimacions.current
    val enfonsat by animateDpAsState(if (premut && enabled) ombra else 0.dp, tween(if (reduir) 0 else 70), label = "enfonsat")
    val tinta = Colors.Text
    val vora = if (enabled) tinta else Colors.Linia
    Box(
        modifier
            .then(if (ample) Modifier.fillMaxWidth() else Modifier)
            .padding(end = ombra, bottom = ombra)
            .drawBehind {
                if (enabled) {
                    val d = ombra.toPx()
                    drawRect(tinta, topLeft = Offset(d, d), size = size)
                }
            },
    ) {
        Row(
            Modifier
                .offset(enfonsat, enfonsat)
                .then(if (ample) Modifier.fillMaxWidth() else Modifier)
                .heightIn(min = alcadaMinima)
                .background(if (enabled) fons else Colors.Superficie2)
                .border(2.dp, vora)
                .clickable(interactionSource = interaccio, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompositionLocalProvider(LocalContentColor provides if (enabled) contingut else Colors.TextSecundari) { content() }
        }
    }
}

/** El text d'un botó; si canvia («Fet!»), el vell surt per dalt i el nou entra per baix: el botó contesta. */
@Composable
private fun TextQueContesta(text: String, estil: TextStyle) {
    val reduir = LocalReduirAnimacions.current
    AnimatedContent(
        targetState = text,
        transitionSpec = {
            if (reduir) {
                EnterTransition.None togetherWith ExitTransition.None
            } else {
                (slideInVertically(tween(240)) { it } + fadeIn(tween(180))) togetherWith (slideOutVertically(tween(180)) { -it } + fadeOut(tween(140)))
            }
        },
        label = "text del botó",
    ) { t -> Text(t, style = estil.copy(color = LocalContentColor.current)) }
}

@Composable
fun BotoPrincipal(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icona: ImageVector? = null,
    enabled: Boolean = true,
) {
    BotoAmbOmbra(onClick, Colors.Fort, Colors.TintaFort, modifier, enabled, alcadaMinima = 56.dp) {
        if (icona != null) {
            Icon(icona, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        TextQueContesta(text, MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp))
    }
}

@Composable
fun BotoSecundari(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icona: ImageVector? = null,
    enabled: Boolean = true,
    ample: Boolean = true,
) {
    BotoAmbOmbra(onClick, Colors.Fons, Colors.Text, modifier, enabled, ombra = 2.dp, alcadaMinima = 46.dp, ample = ample) {
        if (icona != null) {
            Icon(icona, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        TextQueContesta(text, MaterialTheme.typography.labelLarge)
    }
}

/** Botó d'icona quadrat de 48 dp. */
@Composable
fun BotoIcona(icona: ImageVector, descripcio: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    BotoAmbOmbra(onClick, Colors.Fons, Colors.Text, modifier, enabled = true, ombra = 2.dp, alcadaMinima = 46.dp, ample = false) {
        Icon(icona, contentDescription = descripcio, modifier = Modifier.size(20.dp))
    }
}

/** L'ombra dura de les peces importants. */
fun Modifier.ombraDura(ombra: Dp = 3.dp, color: Color? = null): Modifier = this
    .padding(end = ombra, bottom = ombra)
    .drawBehind {
        val d = ombra.toPx()
        drawRect(color ?: Colors.Text, topLeft = Offset(d, d), size = size)
    }

/** Una etiqueta petita en majúscules, per als títols de les seccions. */
@Composable
fun Etiqueta(text: String, modifier: Modifier = Modifier, color: Color = Colors.TextSecundari) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = color, modifier = modifier)
}

@Composable
fun Targeta(
    modifier: Modifier = Modifier,
    fons: Color = Colors.Fons,
    vora: Color = Colors.Text,
    content: @Composable () -> Unit,
) {
    Box(modifier.background(fons).border(1.5.dp, vora).padding(16.dp)) { content() }
}

/** Un camp de text amb la vora de tinta i cantonades rectes. */
@Composable
fun Camp(
    valor: String,
    onValor: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    numeric: Boolean = false,
    ajuda: String? = null,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValor,
        label = { Text(etiqueta) },
        supportingText = ajuda?.let { { Text(it) } },
        singleLine = true,
        keyboardOptions = if (numeric) KeyboardOptions(keyboardType = KeyboardType.Decimal) else KeyboardOptions.Default,
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Colors.Text,
            unfocusedBorderColor = Colors.TextSecundari,
            focusedLabelColor = Colors.Text,
            cursorColor = Colors.Text,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

/** Una fila amb un text i un interruptor. */
@Composable
fun Interruptor(titol: String, descripcio: String?, activat: Boolean, onCanvi: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .clickable(role = Role.Switch) { onCanvi(!activat) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(titol, style = MaterialTheme.typography.titleMedium)
            if (descripcio != null) Text(descripcio, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = activat,
            onCheckedChange = onCanvi,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Colors.TintaFort,
                checkedTrackColor = Colors.Fort,
                checkedBorderColor = Colors.Text,
                uncheckedThumbColor = Colors.Text,
                uncheckedTrackColor = Colors.Fons,
                uncheckedBorderColor = Colors.Text,
            ),
        )
    }
}
