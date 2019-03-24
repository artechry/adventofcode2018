package com.afe.adventofcode2018.day01

import com.afe.adventofcode2018.linesFromResource
import java.math.BigDecimal
import java.nio.file.Paths
import kotlin.streams.asSequence

fun main(args: Array<String>) {
    part1()
    part2()
}

private fun part1() {
    println(
        linesFromResource(Paths.get("com", "afe", "adventofcode2018", "day01", "input.txt"))
            .asSequence()
            .map(::BigDecimal)
            .fold(MutableBigDecimalFrequency() as Frequency, Frequency::change)
            .asBigDecimal()
    )
}

private fun part2() {
    val deltas = linesFromResource(Paths.get("com", "afe", "adventofcode2018", "day01", "input.txt"))
        .asSequence()
        .map(::BigDecimal)
        .toList()
        .toLoopEndlessSequence()
    var frequency: Frequency = MutableBigDecimalFrequency()
    val findFirstRepeatedFreq = FindFirstRepeatedFreq(frequency)
    for (delta in deltas) {
        frequency = frequency.change(delta)
        if (findFirstRepeatedFreq.addFreqAndCheckRepetition(frequency)) {
            break
        }
    }
    println(frequency.asBigDecimal())
}

private fun <T> Iterable<T>.toLoopEndlessSequence(): Sequence<T> = Sequence {
    object : Iterator<T> {
        var iter = this@toLoopEndlessSequence.iterator()

        override fun hasNext(): Boolean = iter.hasNext()

        override fun next(): T {
            val res = iter.next()
            if (!iter.hasNext()) {
                iter = this@toLoopEndlessSequence.iterator()
            }
            return res
        }
    }
}

private class FindFirstRepeatedFreq private constructor(
    private val freqSet: MutableSet<Frequency>
) {
    constructor(freq: Frequency) : this(
        HashSet<Frequency>().apply {
            add(freq)
        }
    )

    fun addFreqAndCheckRepetition(freq: Frequency): Boolean =
        !freqSet.add(ImmutableBigDecimalFrequency(freq.asBigDecimal()))

}

private interface Frequency {
    fun change(delta: Double): Frequency
    fun change(delta: BigDecimal): Frequency
    fun asBigDecimal(): BigDecimal
}

private operator fun BigDecimal.plus(other: Double): BigDecimal {
    return this.add(BigDecimal.valueOf(other))
}

private class ImmutableBigDecimalFrequency(private val value: BigDecimal) : Frequency {
    override fun change(delta: Double): Frequency = ImmutableBigDecimalFrequency(value + BigDecimal.valueOf(delta))

    override fun change(delta: BigDecimal): Frequency = ImmutableBigDecimalFrequency(value + delta)

    override fun asBigDecimal(): BigDecimal = value

    override fun equals(other: Any?): Boolean =
        when (other) {
            is Frequency -> this.asBigDecimal() == other.asBigDecimal()
            else -> false
        }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value.toString()
}

private class MutableBigDecimalFrequency(private var value: BigDecimal) : Frequency {
    constructor() : this(BigDecimal.ZERO)

    override fun change(delta: Double): Frequency {
        value += delta
        return this
    }

    override fun change(delta: BigDecimal): Frequency {
        value += delta
        return this
    }

    override fun asBigDecimal(): BigDecimal = value

    override fun equals(other: Any?): Boolean =
        when (other) {
            is Frequency -> this.asBigDecimal() == other.asBigDecimal()
            else -> false
        }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value.toString()
}
