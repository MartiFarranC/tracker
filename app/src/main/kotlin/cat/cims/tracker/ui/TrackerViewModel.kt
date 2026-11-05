package cat.cims.tracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cat.cims.tracker.dades.BaseDades
import cat.cims.tracker.dades.Registre
import cat.cims.tracker.dades.Seguiment
import cat.cims.tracker.domini.Calculs
import cat.cims.tracker.domini.ResumSeguiment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class TrackerViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = BaseDades.de(app).dao()

    /** El dia d'avui. Es torna a llegir cada cop que s'obre l'app, perquè l'estoc es vagi gastant. */
    val avui = MutableStateFlow(LocalDate.now().toEpochDay())

    /** Null mentre es carrega. */
    val resums: StateFlow<List<ResumSeguiment>?> = combine(dao.seguiments(), dao.registres(), avui) { seguiments, registres, dia ->
        seguiments.map { Calculs.resum(it, registres, dia) }.sortedBy { it.urgencia }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun actualitzaAvui() {
        avui.value = LocalDate.now().toEpochDay()
    }

    fun desa(seguiment: Seguiment, enDesar: (Long) -> Unit = {}) = viewModelScope.launch {
        val id = if (seguiment.id == 0L) dao.afegeix(seguiment) else seguiment.id.also { dao.actualitza(seguiment) }
        enDesar(id)
    }

    fun esborra(seguiment: Seguiment) = viewModelScope.launch { dao.esborra(seguiment) }

    fun afegeixRegistre(seguimentId: Long, data: Long, valor: Double?, nota: String) = viewModelScope.launch {
        dao.afegeix(Registre(seguimentId = seguimentId, data = data, valor = valor, nota = nota.trim()))
    }

    fun esborraRegistre(registre: Registre) = viewModelScope.launch { dao.esborra(registre) }

    /** Suma (o resta) unitats a l'estoc d'avui: una compra, una presa de més, un dia que no se n'ha pres. */
    fun sumaEstoc(seguimentId: Long, unitats: Double) = canviaEstoc(seguimentId) { it + unitats }

    /** Posa l'estoc d'avui a una xifra exacta. */
    fun fixaEstoc(seguimentId: Long, unitats: Double) = canviaEstoc(seguimentId) { unitats }

    private fun canviaEstoc(seguimentId: Long, canvi: (Double) -> Double) = viewModelScope.launch {
        val s = dao.seguiment(seguimentId) ?: return@launch
        val dia = avui.value
        dao.actualitza(Calculs.ambEstoc(s, canvi(Calculs.unitatsAvui(s, dia)), dia))
    }
}
