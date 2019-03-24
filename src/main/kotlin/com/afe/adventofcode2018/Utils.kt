package com.afe.adventofcode2018

import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.stream.Stream

object PathFinder {
    private fun uriFromResources(strPath: String): URI = javaClass.classLoader.getResource(strPath).toURI()

    fun fromResources(strPath: String): Path = Paths.get(uriFromResources(strPath))
}

fun linesFromResource(strPath: String): Stream<String> =
    Files.lines(PathFinder.fromResources(strPath))!!

fun linesFromResource(path: Path): Stream<String> =
    linesFromResource(path.toString())

fun longLinesStream(inputPath1: Path): Stream<List<Long>> {
    return linesFromResource(inputPath1)
        .map { line ->
            line
                .split("\t")
                .map(String::toLong)
        }
}

fun Stream<Long>.sum(): Long {
    return reduce<Long>(
        0,
        { acc, value -> acc + value },
        { acc1, acc2 -> acc1 + acc2 }
    ) ?: 0
}

fun Char.toDigit(): Int = toInt() - 48

fun asDigits(str: String): Sequence<Long> = str.asSequence().filter(Char::isDigit).map(Char::toDigit).map(Int::toLong)

@Suppress("UNCHECKED_CAST")
fun <T> Sequence<T>.permutations(): Sequence<Pair<T, T>> = Sequence {
    object : Iterator<Pair<T, T>> {
        val origSeqIter = this@permutations.iterator()
        val pulledElements: LinkedList<T> = LinkedList()
        var pulledElementsIterator: Iterator<T> = pulledElements.iterator()
        var currentElem: T? = null
        var currentElemPulled = false

        override fun hasNext(): Boolean = when {
            pulledElementsIterator.hasNext() -> true
            origSeqIter.hasNext() -> true
            else -> false
        }

        override fun next(): Pair<T, T> {
            when {
                !currentElemPulled -> {
                    pullNewElement()
                    currentElemPulled = true
                }
                !pulledElementsIterator.hasNext() -> pullNewElement()
            }
            return Pair(currentElem as T, pulledElementsIterator.next())
        }

        private fun pullNewElement() {
            currentElem = origSeqIter.next()
            pulledElements.add(currentElem as T)
            pulledElementsIterator = pulledElements.iterator()
        }
    }
}
