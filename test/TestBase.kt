import org.jetbrains.kotlinx.lincheck.*
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.*
import org.jetbrains.kotlinx.lincheck.strategy.stress.*
import org.junit.*
import org.junit.runners.*
import kotlin.reflect.*

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
abstract class TestBase(
    val sequentialSpecification: KClass<*>,
    val checkObstructionFreedom: Boolean = true,
) {
    @Test
    fun modelCheckingTest() = ModelCheckingOptions()
        .iterations(200)
        .invocationsPerIteration(10_000)
        .actorsBefore(1)
        .threads(3)
        .actorsPerThread(2)
        .actorsAfter(0)
        .checkObstructionFreedom(checkObstructionFreedom)
        .sequentialSpecification(sequentialSpecification.java)
        .check(this::class.java)

    @Test
    fun stressTest() = StressOptions()
        .iterations(200)
        .invocationsPerIteration(25_000)
        .actorsBefore(1)
        .threads(3)
        .actorsPerThread(2)
        .actorsAfter(0)
        .sequentialSpecification(sequentialSpecification.java)
        .check(this::class.java)
}
