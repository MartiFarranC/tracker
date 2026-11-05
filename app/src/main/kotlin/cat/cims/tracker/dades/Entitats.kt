package cat.cims.tracker.dades

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Un seguiment: qualsevol cosa de la vida diària que es vol controlar. És genèric: es fa amb dues peces que es poden
 * combinar.
 *
 * - **Estoc** ([ambEstoc]): unes unitats que es gasten soles cada dia (pastilles, lentilles, càpsules). L'estoc es desa
 *   tal com era el dia [estocData] i l'app hi resta el consum fins avui (domini/Calculs.kt).
 * - **Registres** ([ambRegistres]): cada cop que fas una cosa (tallar-te el cabell, posar gasolina, anar al metge).
 *   L'app en fa la mitjana i avisa quan toca tornar-hi.
 *
 * Els dies es desen com a `LocalDate.toEpochDay()`.
 */
@Entity(tableName = "seguiments")
data class Seguiment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nom: String,
    /** La marca, el vehicle, el metge... El que ajudi a reconèixer-lo. */
    val detall: String = "",
    /** La plantilla d'on surt (domini/Plantilla.kt). Només és informativa. */
    val plantilla: String = "PERSONALITZAT",
    val creat: Long,

    val ambEstoc: Boolean = false,
    /** El nom d'una unitat, en singular: «pastilla». */
    val nomUnitat: String = "",
    /** El nom d'un paquet, en singular: «caixa». Buit si no es compra en paquets. */
    val nomPaquet: String = "",
    val unitatsPerPaquet: Int = 0,
    /** Es gasten [consumQuantitat] unitats cada [consumCadaDies] dies, començant el dia [consumInici]. */
    val consumQuantitat: Double = 1.0,
    val consumCadaDies: Int = 1,
    val consumInici: Long = 0,
    /** Les unitats que hi havia al final del dia [estocData] (amb el consum d'aquell dia ja restat). */
    val estocUnitats: Double = 0.0,
    val estocData: Long = 0,
    /** Avisa quan queden aquests dies d'estoc o menys. */
    val avisEstocDies: Int = 7,

    val ambRegistres: Boolean = false,
    /** Cada quants dies toca, si és fix (una revisió anual). Null: la mitjana dels registres. */
    val intervalFixDies: Int? = null,
    /** Avisa aquests dies abans que toqui. */
    val avisAbansDies: Int = 0,
    /** Si cada registre porta una xifra (litres, euros, quilòmetres) i en quina unitat. */
    val ambValor: Boolean = false,
    val unitatValor: String = "",
)

/** Un cop que s'ha fet la cosa d'un seguiment: un dia i, si cal, una xifra i una nota. */
@Entity(
    tableName = "registres",
    foreignKeys = [ForeignKey(entity = Seguiment::class, parentColumns = ["id"], childColumns = ["seguimentId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("seguimentId")],
)
data class Registre(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val seguimentId: Long,
    val data: Long,
    val valor: Double? = null,
    val nota: String = "",
)
