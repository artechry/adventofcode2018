package com.afe.adventofcode2018.day02

import com.afe.adventofcode2018.linesFromResource
import com.afe.adventofcode2018.permutations
import java.nio.file.Paths
import java.util.*
import kotlin.streams.asSequence

fun main() {
    part1()
    part2()
}

private fun part1() {
    linesFromResource(Paths.get("com", "afe", "adventofcode2018", "day02", "input.txt"))
        .asSequence()
        .map { line -> line.groupingBy { it } }
        .map { groupingBy -> groupingBy.eachCount() }
        .map(Map<Char, Int>::values)
        .map { it.distinct() }
        .flatten()
        .filter { it > 1 }
        .groupingBy { it }
        .eachCount()
        .values
        .reduce { v1, v2 -> v1 * v2 }
        .apply(::println)
}

private fun part2() {
    linesFromResource(Paths.get("com", "afe", "adventofcode2018", "day02", "input.txt"))
        .asSequence()
        .permutations()
        .map(Pair<String, String>::isDifferentByExactlyOneChar)
        .filterIsInstance<DifferByExactlyOneCharRes.PositiveRes>()
        .map { it.pair.first.removeRange(it.differCharIndex, it.differCharIndex + 1)}
        .forEach(::println)
}

sealed class DifferByExactlyOneCharRes {
    object NegativeRes : DifferByExactlyOneCharRes()
    data class PositiveRes(
        val pair: Pair<String, String>,
        val differCharIndex: Int
    ) : DifferByExactlyOneCharRes()
}

private fun Pair<String, String>.isDifferentByExactlyOneChar(): DifferByExactlyOneCharRes =
    when {
        first.length != second.length -> DifferByExactlyOneCharRes.NegativeRes
        else -> {
            val size = first.length
            var indexOfDifChar: Int? = null
            val firstIter = first.iterator()
            val secondIter = second.iterator()
            for (i in 0 until size) {
                if (firstIter.next() != secondIter.next()) {
                    if (indexOfDifChar == null) {
                        indexOfDifChar = i
                    } else {
                        indexOfDifChar = null
                        break
                    }
                }
            }
            if (indexOfDifChar != null) {
                DifferByExactlyOneCharRes.PositiveRes(this, indexOfDifChar)
            } else {
                DifferByExactlyOneCharRes.NegativeRes
            }
        }
    }