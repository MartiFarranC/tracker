package cat.cims.tracker

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cat.cims.tracker.ui.TrackerViewModel
import cat.cims.tracker.ui.detall.AccionsDetall
import cat.cims.tracker.ui.detall.DetallScreen
import cat.cims.tracker.ui.editar.EditarScreen
import cat.cims.tracker.ui.inici.IniciScreen
import cat.cims.tracker.ui.theme.Colors
import cat.cims.tracker.ui.theme.TrackerTheme

class MainActivity : ComponentActivity() {
    private val vm: TrackerViewModel by viewModels()

    /** El seguiment que s'ha d'obrir en tocar un avís. */
    private val obrir = mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        obrir.value = intent.seguiment()
        setContent { TrackerTheme { App(vm, obrir.value) { obrir.value = null } } }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        obrir.value = intent.seguiment()
    }

    private fun Intent.seguiment(): Long? = getLongExtra(EXTRA_SEGUIMENT, -1).takeIf { it > 0 }

    companion object {
        const val EXTRA_SEGUIMENT = "seguiment"
    }
}

/** Les pantalles: la llista, el detall d'un seguiment i el formulari (nou o edició). */
private const val INICI = 0
private const val DETALL = 1
private const val EDITAR = 2

@Composable
private fun App(vm: TrackerViewModel, obrir: Long?, onObert: () -> Unit) {
    val resums by vm.resums.collectAsStateWithLifecycle()
    val avui by vm.avui.collectAsStateWithLifecycle()
    var pantalla by rememberSaveable { mutableStateOf(INICI) }
    /** El seguiment obert, o 0 per a un de nou. */
    var id by rememberSaveable { mutableStateOf(0L) }
    var tornaAlDetall by rememberSaveable { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        vm.actualitzaAvui()
        onPauseOrDispose { }
    }

    val permis = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) permis.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    LaunchedEffect(obrir) {
        if (obrir != null) {
            id = obrir
            pantalla = DETALL
            onObert()
        }
    }

    BackHandler(enabled = pantalla != INICI) {
        pantalla = if (pantalla == EDITAR && tornaAlDetall) DETALL else INICI
    }

    Box(Modifier.fillMaxSize().background(Colors.Fons)) {
        val llista = resums ?: return@Box
        when (pantalla) {
            INICI -> IniciScreen(
                llista, avui,
                onObre = { id = it; pantalla = DETALL },
                onNou = { id = 0; tornaAlDetall = false; pantalla = EDITAR },
            )
            DETALL -> {
                val r = llista.firstOrNull { it.seguiment.id == id }
                // Just després de crear-lo, la llista encara no el té: s'espera un moment.
                    ?: return@Box
                DetallScreen(
                    r, avui,
                    AccionsDetall(
                        onEnrere = { pantalla = INICI },
                        onEdita = { tornaAlDetall = true; pantalla = EDITAR },
                        onEsborra = { vm.esborra(r.seguiment); pantalla = INICI },
                        onSumaEstoc = { vm.sumaEstoc(id, it) },
                        onFixaEstoc = { vm.fixaEstoc(id, it) },
                        onRegistra = { dia, valor, nota -> vm.afegeixRegistre(id, dia, valor, nota) },
                        onEsborraRegistre = { vm.esborraRegistre(it) },
                    ),
                )
            }
            EDITAR -> {
                val original = if (id == 0L) null else llista.firstOrNull { it.seguiment.id == id }?.seguiment
                EditarScreen(
                    original, avui,
                    onDesa = { s, darrerCop ->
                        vm.desa(s) { nouId ->
                            if (darrerCop != null) vm.afegeixRegistre(nouId, darrerCop, null, "")
                            id = nouId
                            pantalla = DETALL
                        }
                    },
                    onEnrere = { pantalla = if (tornaAlDetall) DETALL else INICI },
                )
            }
        }
    }
}
