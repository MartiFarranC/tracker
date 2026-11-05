package cat.cims.tracker.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/** Si l'app es pinta en fosc. Segueix el mòbil; ho posa TrackerTheme. */
object Tema {
    var fosc by mutableStateOf(false)
}

/**
 * Els colors de la casa (docs/disseny.md): blanc i negre i un sol color fort, com municipis (groc) i Andana (magenta).
 * Els jocs tenen colors càlids i cridaners; les apps, un color fred i tranquil: aquí, el verd menta `#3DDC97`.
 */
object Colors {
    private fun c(clar: Long, fosc: Long) = Color(if (Tema.fosc) fosc else clar)

    /** L'únic color fort: l'acció principal i el que demana atenció («toca»). */
    val Fort = Color(0xFF3DDC97)

    /** Text i icones sobre el color fort. */
    val TintaFort = Color(0xFF111111)

    val Fons: Color get() = c(0xFFFFFFFF, 0xFF0F0F0F)
    val Superficie: Color get() = c(0xFFF5F5F5, 0xFF1A1A1A)
    val Superficie2: Color get() = c(0xFFECECEC, 0xFF242424)
    val Linia: Color get() = c(0xFFE4E4E4, 0xFF2A2A2A)

    /** La tinta: textos, vores i ombres dures. */
    val Text: Color get() = c(0xFF111111, 0xFFF2F2F2)
    val TextSecundari: Color get() = c(0xFF6B6B6B, 0xFF9A9A9A)

    /** El color fort com a text o vora sobre el fons: en clar no es llegiria, i es fa servir la tinta. */
    val FortText: Color get() = if (Tema.fosc) Fort else Text

    /** Fons tènue del color fort («aviat»). */
    val FortSuau: Color get() = lerp(Fons, Fort, if (Tema.fosc) .16f else .22f)

    val Error: Color get() = c(0xFFC62D1F, 0xFFF08A7A)
}
