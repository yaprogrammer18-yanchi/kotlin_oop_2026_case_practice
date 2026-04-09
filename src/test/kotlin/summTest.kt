import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import example.summa

class SummTest{
    @Test
    fun `test sum`(){
        val a = 10
        val b = 20
    assertEquals(summa(a, b), 30)
    }
}