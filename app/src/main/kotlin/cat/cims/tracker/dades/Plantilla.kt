package cat.cims.tracker.dades

import android.content.Context
import androidx.annotation.StringRes
import cat.cims.tracker.R

/**
 * Els punts de partida per crear un seguiment. Només omplen el formulari: tot es pot canviar, i «Personalitzat» deixa
 * fer-lo des de zero amb les mateixes peces (estoc i registres).
 */
enum class Plantilla(@param:StringRes val titol: Int, @param:StringRes val descripcio: Int) {
    MEDICAMENT(R.string.plantilla_medicament, R.string.plantilla_medicament_desc),
    CONSUMIBLE(R.string.plantilla_consumible, R.string.plantilla_consumible_desc),
    CABELL(R.string.plantilla_cabell, R.string.plantilla_cabell_desc),
    GASOLINA(R.string.plantilla_gasolina, R.string.plantilla_gasolina_desc),
    REVISIO(R.string.plantilla_revisio, R.string.plantilla_revisio_desc),
    METGE(R.string.plantilla_metge, R.string.plantilla_metge_desc),
    PERSONALITZAT(R.string.plantilla_personalitzat, R.string.plantilla_personalitzat_desc);

    fun seguiment(context: Context, avui: Long): Seguiment {
        val base = Seguiment(nom = "", plantilla = name, creat = avui, consumInici = avui, estocData = avui)
        fun s(id: Int) = context.getString(id)
        return when (this) {
            MEDICAMENT -> base.copy(
                ambEstoc = true, nomUnitat = s(R.string.unitat_pastilla), nomPaquet = s(R.string.paquet_caixa),
                unitatsPerPaquet = 28, avisEstocDies = 7,
            )
            CONSUMIBLE -> base.copy(
                ambEstoc = true, nomUnitat = s(R.string.unitat_unitat), nomPaquet = s(R.string.paquet_paquet),
                unitatsPerPaquet = 10, avisEstocDies = 5,
            )
            CABELL -> base.copy(nom = s(R.string.nom_cabell), ambRegistres = true)
            GASOLINA -> base.copy(nom = s(R.string.nom_gasolina), ambRegistres = true, ambValor = true, unitatValor = "€")
            REVISIO -> base.copy(nom = s(R.string.nom_revisio), ambRegistres = true, intervalFixDies = 365, avisAbansDies = 30)
            METGE -> base.copy(nom = s(R.string.nom_metge), ambRegistres = true, intervalFixDies = 180, avisAbansDies = 14)
            PERSONALITZAT -> base.copy(ambRegistres = true)
        }
    }
}
