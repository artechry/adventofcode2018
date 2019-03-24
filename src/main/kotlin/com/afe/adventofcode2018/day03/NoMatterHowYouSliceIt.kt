package com.afe.adventofcode2018.day03

import com.afe.adventofcode2018.linesFromResource
import com.afe.adventofcode2018.permutations
import java.nio.file.Paths
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.streams.asSequence

fun main() {
    part1()
    part2()
}

private fun part1() {
    val fabricArea =
        linesFromResource(Paths.get("com", "afe", "adventofcode2018", "day03", "input.txt"))
            .asSequence()
            .toElfFabricClaims()
            .fold(FabricArea(), FabricArea::occupy)
    fabricArea
        .getArea()
        .values
        .filterIsInstance<FabricAreaCell.OccupiedCell>()
        .filter { it.owners.size > 1 }
        .count()
        .apply(::println)
}

private fun part2() {
    linesFromResource(Paths.get("com", "afe", "adventofcode2018", "day03", "input.txt"))
        .asSequence()
        .toElfFabricClaims()
        .permutations()
        .filter { pair -> pair.first.elf.index != pair.second.elf.index }
        .fold(HashMap<Pair<Int, Int>, Interception>() as MutableMap<Pair<Int, Int>, Interception>) { map, pair ->
            val key = Pair(pair.first.elf.index, pair.second.elf.index)
            if (!map.containsKey(key)) {
                val interception = claimsInterception(pair.first.claim, pair.second.claim)
                map[key] = interception
                map[key.reverse()] = interception
            }
            map
        }
        .entries
        .groupingBy { it.key.first }
        .fold(LinkedList<Interception>() as MutableList<Interception>) { acc, entry ->
            acc.add(entry.value)
            acc
        }
        .mapValues { it.value.filter{ interception -> interception !is Interception.EMPTY } }
//        .mapValues { it.value.all { interception -> interception is Interception.EMPTY } }
        .entries
//        .filter { it.value }
        .forEach(::println)
}

private fun Sequence<String>.toElfFabricClaims(): Sequence<ElfFabricClaim> =
    this
        .map { elfFabricClaimInfoRegex.matchEntire(it) }
        .map { it?.destructured }
        .filterNotNull()
        .map {
            it.let { (indexStr, fromTopStr, fromLeftStr, widthStr, heightStr) ->
                ElfFabricClaim(
                    Elf(indexStr.toInt()),
                    Claim(
                        Position(
                            fromLeftStr.toInt(),
                            fromTopStr.toInt()
                        ),
                        Area(
                            widthStr.toInt(),
                            heightStr.toInt()
                        )
                    )
                )
            }
        }

private fun <T> Pair<T, T>.reverse(): Pair<T, T> =
    Pair(this.second, this.first)

private val elfFabricClaimInfoRegex = "#(\\d+) @ (\\d+),(\\d+): (\\d+)x(\\d+)".toRegex()

private data class Position(
    val fromTop: Int,
    val fromLeft: Int
) {
    companion object {
        val ZERO = Position(0, 0)
    }
}

private data class Area(
    val width: Int,
    val height: Int
) {
    companion object {
        val EMPTY = Area(0, 0)
    }
}

private data class Claim(
    val position: Position,
    val area: Area
)

private data class ElfFabricClaim(
    val elf: Elf,
    val claim: Claim
)

private sealed class FabricAreaCell {
    object VacantCell : FabricAreaCell()
    data class OccupiedCell(val owners: Set<Elf>) : FabricAreaCell()
}

private data class Elf(val index: Int)

private class FabricArea private constructor(
    private val area: MutableMap<Position, FabricAreaCell>,
    private var maxPosition: Position
) {
    constructor() : this(HashMap(), Position(0, 0))

    fun occupy(elfFabricClaim: ElfFabricClaim): FabricArea = this.apply {
        val elfFabricAreaMaxPosition =
            elfFabricClaim.claim.position + Position(elfFabricClaim.claim.area.height, elfFabricClaim.claim.area.width)
        maxPosition = when {
            elfFabricAreaMaxPosition.fromTop > maxPosition.fromTop && elfFabricAreaMaxPosition.fromLeft >= maxPosition.fromLeft -> Position(
                elfFabricAreaMaxPosition.fromTop,
                elfFabricAreaMaxPosition.fromLeft
            )
            elfFabricAreaMaxPosition.fromTop >= maxPosition.fromTop && elfFabricAreaMaxPosition.fromLeft > maxPosition.fromLeft -> Position(
                elfFabricAreaMaxPosition.fromTop,
                elfFabricAreaMaxPosition.fromLeft
            )
            elfFabricAreaMaxPosition.fromTop > maxPosition.fromTop -> Position(
                elfFabricAreaMaxPosition.fromTop,
                maxPosition.fromLeft
            )
            elfFabricAreaMaxPosition.fromLeft > maxPosition.fromLeft -> Position(
                maxPosition.fromTop,
                elfFabricAreaMaxPosition.fromLeft
            )
            else -> maxPosition
        }
        positionsInClaim(elfFabricClaim.claim).forEach { position ->
            area.compute(position) { _, nullableValue ->
                val value = nullableValue ?: FabricAreaCell.VacantCell
                when (value) {
                    is FabricAreaCell.VacantCell -> FabricAreaCell.OccupiedCell(setOf(elfFabricClaim.elf))
                    is FabricAreaCell.OccupiedCell -> FabricAreaCell.OccupiedCell(value.owners.plus(elfFabricClaim.elf))
                }
            }
        }
    }

    fun getArea(): Map<Position, FabricAreaCell> = area

    fun asString(): String =
        (0 until maxPosition.fromTop).asSequence()
            .map { fromTop ->
                (0 until maxPosition.fromLeft).asSequence()
                    .map { fromLeft ->
                        val cell = area[Position(fromTop, fromLeft)] ?: FabricAreaCell.VacantCell
                        when (cell) {
                            is FabricAreaCell.VacantCell -> "."
                            is FabricAreaCell.OccupiedCell -> when {
                                cell.owners.size > 1 -> "X"
                                else -> "O"
                            }
                        }
                    }
                    .joinToString(separator = "")
            }
            .joinToString(separator = "\n")
}

private operator fun Position.plus(position: Position): Position = Position(
    fromTop + position.fromTop,
    fromLeft + position.fromLeft
)

private operator fun Position.minus(position: Position): Position = Position(
    fromTop - position.fromTop,
    fromLeft - position.fromLeft
)

private operator fun Position.unaryMinus(): Position =
    Position(-this.fromTop, -this.fromLeft)

private fun positionsInClaim(claim: Claim): Sequence<Position> =
    claim.area.positions()
        .map { claim.position + it }

private fun Area.positions(): Sequence<Position> =
    (0 until height).asSequence().flatMap { fromTop ->
        (0 until width).asSequence().map { fromLeft ->
            Position(fromTop, fromLeft)
        }
    }

private sealed class Interception {
    object EMPTY : Interception()
    data class ClaimInterception(val claim: Claim) : Interception()
}

private fun Claim.bottomRight(): Position =
    this.position + Position(this.area.height, this.area.width)

private fun Area.asPosition(): Position =
    Position(this.height, this.width)

private fun Position.asArea(): Area =
    Area(this.fromLeft, this.fromTop)

private fun max(posA: Position, posB: Position): Position = Position(
    max(posA.fromTop, posB.fromTop),
    max(posA.fromLeft, posB.fromLeft)
)

private fun min(posA: Position, posB: Position): Position = Position(
    min(posA.fromTop, posB.fromTop),
    min(posA.fromLeft, posB.fromLeft)
)

private fun claimsInterception(claimA: Claim, claimB: Claim): Interception {
    val areaVectorA = claimA.area.asPosition()
    val difBottomRightAAndTopLeftB = claimA.position + areaVectorA - claimB.position
    return if (difBottomRightAAndTopLeftB.fromTop < 0 || difBottomRightAAndTopLeftB.fromLeft < 0) {
        Interception.EMPTY
    } else {
        val difBottomLeftBAndTopLeftA = claimB.position + claimB.area.asPosition() - claimA.position
        if (difBottomLeftBAndTopLeftA.fromTop < 0 || difBottomLeftBAndTopLeftA.fromLeft < 0) {
            Interception.EMPTY
        } else {
            val bottomRightA = claimA.bottomRight()
            val bottomRightB = claimB.bottomRight()
            Interception.ClaimInterception(
                Claim(
                    bottomRightA + max(bottomRightA - claimB.position, -areaVectorA),
                    (claimA.position + min(bottomRightB - claimA.position, areaVectorA)).asArea()
                )
            )
        }
    }
}
