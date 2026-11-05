package cat.cims.tracker.avisos

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import cat.cims.tracker.MainActivity
import cat.cims.tracker.R
import cat.cims.tracker.dades.BaseDades
import cat.cims.tracker.domini.Calculs
import cat.cims.tracker.domini.Estat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * Els avisos: un cop al dia (cap a les 9 del matí) es miren tots els seguiments i s'avisa dels que toquen: l'estoc que
 * s'acaba i les coses que toca tornar a fer.
 */
object Avisos {
    const val CANAL = "avisos"
    private const val FEINA = "revisio-diaria"

    fun creaCanal(context: Context) {
        val canal = NotificationChannel(CANAL, context.getString(R.string.canal_avisos), NotificationManager.IMPORTANCE_DEFAULT)
        canal.description = context.getString(R.string.canal_avisos_desc)
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    fun programa(context: Context) {
        val ara = LocalDateTime.now()
        var proper = ara.toLocalDate().atTime(LocalTime.of(9, 0))
        if (!proper.isAfter(ara)) proper = proper.plusDays(1)
        val peticio = PeriodicWorkRequestBuilder<RevisioDiaria>(1, TimeUnit.DAYS)
            .setInitialDelay(Duration.between(ara, proper).toMinutes(), TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(FEINA, ExistingPeriodicWorkPolicy.UPDATE, peticio)
    }

    /** Envia els avisos del que toca avui. Torna quants n'ha enviat. */
    suspend fun revisa(context: Context): Int {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return 0
        val dao = BaseDades.de(context).dao()
        val avui = LocalDate.now().toEpochDay()
        val registres = dao.totsElsRegistres()
        var enviats = 0
        dao.totsElsSeguiments().forEach { s ->
            val r = Calculs.resum(s, registres, avui)
            if (r.estat != Estat.TOCA) return@forEach
            val e = r.estoc
            val text = if (e?.estat == Estat.TOCA && e.diesCoberts != null) {
                if (e.unitats <= 0) context.getString(R.string.avis_estoc_acabat)
                else context.resources.getQuantityString(R.plurals.avis_estoc, e.diesCoberts.toInt(), e.diesCoberts.toInt())
            } else {
                val falten = r.resumRegistres?.diesFinsPropera ?: return@forEach
                when {
                    falten > 0 -> context.resources.getQuantityString(R.plurals.avis_registre_falten, falten.toInt(), falten.toInt())
                    else -> context.getString(R.string.avis_registre_toca, r.resumRegistres.diesDesDelDarrer ?: 0)
                }
            }
            notifica(context, s.id, s.nom, text)
            enviats++
        }
        return enviats
    }

    private fun notifica(context: Context, id: Long, titol: String, text: String) {
        val obre = PendingIntent.getActivity(
            context, id.toInt(),
            Intent(context, MainActivity::class.java).putExtra(MainActivity.EXTRA_SEGUIMENT, id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val avis = NotificationCompat.Builder(context, CANAL)
            .setSmallIcon(R.drawable.ic_avis)
            .setContentTitle(titol)
            .setContentText(text)
            .setContentIntent(obre)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(id.toInt(), avis)
        } catch (_: SecurityException) {
            // Sense permís, no s'avisa.
        }
    }
}

class RevisioDiaria(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        Avisos.revisa(applicationContext)
        return Result.success()
    }
}
