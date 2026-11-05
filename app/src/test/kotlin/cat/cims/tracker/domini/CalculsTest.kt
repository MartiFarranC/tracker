package cat.cims.tracker.domini

import cat.cims.tracker.dades.Registre
import cat.cims.tracker.dades.Seguiment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculsTest {
    private val dia0 = 20_000L

    private fun pastilles(unitats: Double, data: Long = dia0, quantitat: Double = 1.0, cada: Int = 1, avis: Int = 7) = Seguiment(
        nom = "Pastilles", creat = dia0, ambEstoc = true, nomUnitat = "pastilla", nomPaquet = "caixa", unitatsPerPaquet = 28,
        consumQuantitat = quantitat, consumCadaDies = cada, consumInici = dia0, estocUnitats = unitats, estocData = data, avisEstocDies = avis,
    )

    @Test
    fun `l'estoc es gasta sol cada dia`() {
        val s = pastilles(42.0)
        assertEquals(42.0, Calculs.unitatsAvui(s, dia0), 0.0)
        assertEquals(41.0, Calculs.unitatsAvui(s, dia0 + 1), 0.0)
        assertEquals(32.0, Calculs.unitatsAvui(s, dia0 + 10), 0.0)
        assertEquals(0.0, Calculs.unitatsAvui(s, dia0 + 100), 0.0)
    }

    @Test
    fun `si avui encara no s'ha gastat, es resta el d'avui`() {
        val s = pastilles(42.0, data = dia0 - 1)
        assertEquals(41.0, Calculs.unitatsAvui(s, dia0), 0.0)
    }

    @Test
    fun `el consum cada dos dies manté el ritme encara que es corregeixi l'estoc`() {
        val s = pastilles(10.0, cada = 2)
        // Es gasta els dies 0, 2, 4... El dia 0 ja està restat.
        assertEquals(10.0, Calculs.unitatsAvui(s, dia0 + 1), 0.0)
        assertEquals(9.0, Calculs.unitatsAvui(s, dia0 + 2), 0.0)
        val corregit = Calculs.ambEstoc(s, 20.0, dia0 + 3)
        assertEquals(20.0, Calculs.unitatsAvui(corregit, dia0 + 3), 0.0)
        assertEquals(19.0, Calculs.unitatsAvui(corregit, dia0 + 4), 0.0)
    }

    @Test
    fun `els dies que queden i l'avís`() {
        val e = Calculs.estoc(pastilles(10.0), dia0)
        assertEquals(10L, e.diesCoberts)
        assertEquals(dia0 + 10, e.finsAl)
        assertEquals(Estat.AVIAT, e.estat)
        assertEquals(Estat.TOCA, Calculs.estoc(pastilles(7.0), dia0).estat)
        assertEquals(Estat.BE, Calculs.estoc(pastilles(60.0), dia0).estat)
        assertEquals(0L, Calculs.estoc(pastilles(0.0), dia0).diesCoberts)
    }

    @Test
    fun `mitja pastilla al dia dura el doble`() {
        val e = Calculs.estoc(pastilles(10.0, quantitat = .5), dia0)
        assertEquals(20L, e.diesCoberts)
    }

    @Test
    fun `caixes i pastilles soltes`() {
        assertEquals(1 to 14.0, Calculs.enPaquets(42.0, 28))
        assertEquals(2 to 0.0, Calculs.enPaquets(56.0, 28))
        assertEquals(0 to 5.0, Calculs.enPaquets(5.0, 0))
    }

    private fun cabell(fix: Int? = null, avis: Int = 0) = Seguiment(id = 1, nom = "Cabell", creat = dia0, ambRegistres = true, intervalFixDies = fix, avisAbansDies = avis)
    private fun reg(dia: Long, valor: Double? = null) = Registre(seguimentId = 1, data = dia, valor = valor)

    @Test
    fun `la mitjana dels registres diu quan toca`() {
        val r = Calculs.registres(cabell(), listOf(reg(dia0), reg(dia0 + 30), reg(dia0 + 62)), dia0 + 70)
        assertEquals(31.0, r.mitjana!!, 0.0)
        assertEquals(dia0 + 93, r.propera)
        assertEquals(23L, r.diesFinsPropera)
        assertEquals(8L, r.diesDesDelDarrer)
        assertEquals(Estat.BE, r.estat)
    }

    @Test
    fun `amb un sol registre encara no se sap quan toca`() {
        val r = Calculs.registres(cabell(), listOf(reg(dia0)), dia0 + 5)
        assertNull(r.propera)
        assertEquals(Estat.SENSE_DADES, r.estat)
    }

    @Test
    fun `l'interval fix mana sobre la mitjana`() {
        val r = Calculs.registres(cabell(fix = 365, avis = 30), listOf(reg(dia0)), dia0 + 340)
        assertEquals(dia0 + 365, r.propera)
        assertEquals(Estat.TOCA, r.estat)
    }

    @Test
    fun `quan ja ha passat, toca`() {
        val r = Calculs.registres(cabell(), listOf(reg(dia0), reg(dia0 + 30)), dia0 + 65)
        assertEquals(-5L, r.diesFinsPropera)
        assertEquals(Estat.TOCA, r.estat)
    }

    @Test
    fun `les xifres dels registres`() {
        val r = Calculs.registres(cabell(), listOf(reg(dia0, 40.0), reg(dia0 + 10, 50.0)), dia0 + 10)
        assertEquals(45.0, r.mitjanaValor!!, 0.0)
        assertEquals(90.0, r.totalValor!!, 0.0)
    }

    @Test
    fun `plurals en català`() {
        assertEquals("pastilles", Calculs.plural("pastilla"))
        assertEquals("caixes", Calculs.plural("caixa"))
        assertEquals("càpsules", Calculs.plural("càpsula"))
        assertEquals("revisions", Calculs.plural("revisió"))
        assertEquals("paquets", Calculs.plural("paquet"))
        assertEquals("lentilles", Calculs.plural("lentilla"))
        assertEquals("unitats", Calculs.plural("unitat"))
        assertEquals("pastilla", Calculs.nom("pastilla", 1.0))
        assertEquals("pastilles", Calculs.nom("pastilla", 0.5))
    }
}
