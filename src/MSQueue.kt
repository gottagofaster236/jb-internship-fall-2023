import java.util.concurrent.atomic.AtomicStampedReference

/**
 * Implementation of the Michael-Scott concurrent queue algorithm based on the pseudocode from
 * [this paper](https://www.cs.rochester.edu/~scott/papers/1996_PODC_queues.pdf)
 */
class MSQueue<E> : Queue<E> {
    private val head: AtomicStampedReference<Node<E>>
    private val tail: AtomicStampedReference<Node<E>>

    init {
        val node = Node<E>(null)  // Allocate a free node
        head = AtomicStampedReference(node, 0)
        tail = AtomicStampedReference(node, 0)  // Both Head and Tail point to it
    }

    override fun enqueue(element: E) {
        val node = Node(element)  // Allocate a new node from the free list
        while (true) {  // Keep trying until Enqueue is done
            val tailCount = IntArray(1)
            val tailPtr = this.tail.get(tailCount)  // Read Tail.ptr and Tail.count together
            val nextCount = IntArray(1)
            val nextPtr = tailPtr.next.get(nextCount)  // Read next ptr and count fields together
            if (tailCount[0] == this.tail.stamp) {  // Are tail and next consistent?
                if (nextPtr === null) {  // Was Tail pointing to the last node?
                    // Try to link node at the end of the linked list
                    if (tailPtr.next.compareAndSet(nextPtr, node, nextCount[0], nextCount[0] + 1)) {
                        // Enqueue is done. Try to swing Tail to the inserted node
                        this.tail.compareAndSet(tailPtr, node, tailCount[0], tailCount[0] + 1)
                        break
                    }
                } else {  // Tail was not pointing to the last node
                    // Try to swing Tail to the next node
                    this.tail.compareAndSet(tailPtr, nextPtr, tailCount[0], tailCount[0] + 1)
                }
            }
        }
    }

    override fun dequeue(): E? {
        while (true) {  // Keep trying until Dequeue is done
            val headCount = IntArray(1)
            val headPtr = this.head.get(headCount)  // Read Head
            val tailCount = IntArray(1)
            val tailPtr = this.tail.get(tailCount)  // Read Tail
            val nextPtr = headPtr.next.reference  //  Read Head.ptr–>next
            if (headCount[0] == this.head.stamp) {  // Are head, tail, and next consistent?
                if (headPtr === tailPtr) {  // Is queue empty or Tail falling behind?
                    if (nextPtr === null) {  // Is queue empty?
                        return null  // Queue is empty, couldn't dequeue
                    }
                    // Tail is falling behind. Try to advance it
                    this.tail.compareAndSet(tailPtr, nextPtr, tailCount[0], tailCount[0] + 1)
                } else {
                    // Read value before CAS, otherwise another dequeue might free the next node
                    val result = checkNotNull(nextPtr).element
                    //  Try to swing Head to the next node
                    if (this.head.compareAndSet(headPtr, nextPtr, headCount[0], headCount[0] + 1)) {
                        // It is safe now to free the old dummy node
                        nextPtr.element = null
                        // Queue was not empty, dequeue succeeded
                        return result
                    }
                }
            }
        }
    }

    // FOR TEST PURPOSE, DO NOT CHANGE IT.
    override fun validate() {
        check(tail.reference.next.reference == null) {
            "At the end of the execution, `tail.next` must be `null`"
        }
        check(head.reference.element == null) {
            "At the end of the execution, the dummy node shouldn't store an element"
        }
    }

    private class Node<E>(
        var element: E?
    ) {
        val next = AtomicStampedReference<Node<E>?>(null, 0)
    }
}
