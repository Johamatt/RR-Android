package com.example.sport_geo_app.utils

import com.mapbox.geojson.Point
import kotlin.math.sqrt

/**
 * Simplifies the list of points using the Ramer-Douglas-Peucker algorithm.
 *
 * @param points The original list of points to simplify.
 * @param epsilon The distance threshold for simplification.
 * @return A simplified list of points.
 */
fun simplifyPoints(points: List<Point>, epsilon: Double = 0.0001): List<Point> {
    if (points.size < 3) return points

    val first = points.first()
    val last = points.last()

    val indexes = mutableListOf<Int>()
    indexes.add(0)
    indexes.add(points.size - 1)

    val stack = mutableListOf<Pair<Int, Int>>()
    stack.add(Pair(0, points.size - 1))

    val result = mutableListOf<Point>()

    while (stack.isNotEmpty()) {
        val (startIndex, endIndex) = stack.removeAt(stack.size - 1)

        val (maxDistIndex, maxDist) = getMaxDistanceIndex(points, startIndex, endIndex)

        if (maxDist > epsilon) {
            indexes.add(maxDistIndex)
            stack.add(Pair(startIndex, maxDistIndex))
            stack.add(Pair(maxDistIndex, endIndex))
        }
    }

    val sortedIndexes = indexes.sorted()
    sortedIndexes.forEach { index ->
        result.add(points[index])
    }

    return result
}

/**
 * Calculates the maximum distance from a point to the line segment formed by two other points.
 *
 * @param points The list of points.
 * @param startIndex The index of the start point of the segment.
 * @param endIndex The index of the end point of the segment.
 * @return A pair containing the index of the point with the maximum distance and the distance.
 */
fun getMaxDistanceIndex(points: List<Point>, startIndex: Int, endIndex: Int): Pair<Int, Double> {
    var maxDist = 0.0
    var index = startIndex

    val start = points[startIndex]
    val end = points[endIndex]

    for (i in (startIndex + 1) until endIndex) {
        val dist = perpendicularDistance(start, end, points[i])
        if (dist > maxDist) {
            maxDist = dist
            index = i
        }
    }

    return Pair(index, maxDist)
}

/**
 * Calculates the perpendicular distance from a point to a line segment.
 *
 * @param start The start point of the line segment.
 * @param end The end point of the line segment.
 * @param point The point to measure the distance from.
 * @return The perpendicular distance from the point to the line segment.
 */
fun perpendicularDistance(start: Point, end: Point, point: Point): Double {
    val x0 = point.longitude()
    val y0 = point.latitude()
    val x1 = start.longitude()
    val y1 = start.latitude()
    val x2 = end.longitude()
    val y2 = end.latitude()

    val numerator = Math.abs((y2 - y1) * x0 - (x2 - x1) * y0 + x2 * y1 - y2 * x1)
    val denominator = sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1))

    return numerator / denominator
}
