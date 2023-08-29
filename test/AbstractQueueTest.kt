@file:Suppress("unused")

import org.jetbrains.kotlinx.lincheck.annotations.*
import org.jetbrains.kotlinx.lincheck.paramgen.*

@Param(name = "element", gen = IntGen::class, conf = "0:3")
class MSQueueTest : TestBase(
    sequentialSpecification = IntQueueSequential::class,
    checkObstructionFreedom = true,
) {
    private val queue = MSQueue<Int>()

    @Operation
    fun enqueue(@Param(name = "element") element: Int) = queue.enqueue(element)

    @Operation
    fun dequeue() = queue.dequeue()

    @Validate
    fun validate() = queue.validate()
}

class IntQueueSequential {
    private val q = ArrayList<Int>()

    fun enqueue(element: Int) {
        q.add(element)
    }

    fun dequeue() = q.removeFirstOrNull()
    fun remove(element: Int) = q.remove(element)
}
