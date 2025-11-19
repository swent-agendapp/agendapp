package com.android.sample.ui.calendar.utils

import com.android.sample.model.calendar.Event
import java.time.Instant

/**
 * Utility object for calculating horizontal overlap layouts for events.
 *
 * It assumes that all events belong to the same day column (this is enforced by the caller). The
 * algorithm works in three steps:
 * 1) Sort events by start time and build "clusters" of events that are connected by overlaps.
 * 2) Inside each cluster, assign a base column index to every event so that overlapping events
 *    never share the same column.
 * 3) For each event, expand it to the right over neighbour columns that are always free during its
 *    whole duration. This gives a variable widthFraction and a horizontal offsetFraction.
 */
object EventOverlapCalculator {

  /**
   * Layout information for a single event within a column.
   *
   * @param widthFraction Fraction of the column width this event should occupy (0.0 to 1.0).
   * @param offsetFraction Horizontal offset fraction from the left edge of the column (0.0 to 1.0).
   * @param overlapGroup Index of the temporal cluster this event belongs to. This is useful for
   *   debugging or future extensions.
   * @param baseColumnIndex The left-most discrete column index assigned to this event.
   * @param columnSpan Number of discrete columns that this event effectively occupies.
   * @param totalColumns Total number of columns used inside this cluster.
   */
  data class EventLayout(
      val widthFraction: Float,
      val offsetFraction: Float,
      val overlapGroup: Int,
      val baseColumnIndex: Int,
      val columnSpan: Int,
      val totalColumns: Int,
  )

  /**
   * Calculates layout information for a set of events that will be rendered in the same day column.
   *
   * The function returns a map that gives, for each [Event], the fraction of the column width it
   * should use and its horizontal offset. Events that overlap in time are first placed into
   * discrete columns so that they never share the same column. Then each event expands over
   * additional neighbour columns that are free during its full duration.
   *
   * @param events List of events in a single day column.
   * @return Map from [Event] to its layout information.
   */
  fun calculateEventLayouts(events: List<Event>): Map<Event, EventLayout> {
    if (events.isEmpty()) return emptyMap()

    // Sort events by start time for a stable temporal order
    val sortedEvents = events.sortedBy { it.startDate }

    // Build temporal clusters of events that are connected by overlaps
    val clusters = buildClusters(sortedEvents)

    // This array stores the base column index for each event in the sorted list
    val baseColumns = IntArray(sortedEvents.size)

    // Final result
    val layoutMap = mutableMapOf<Event, EventLayout>()

    // Process each cluster independently
    clusters.forEachIndexed { groupIndex, clusterIndices ->
      // Step 2: assign base columns for this cluster using a greedy algorithm
      val totalColumns =
          assignBaseColumnsForCluster(
              sortedEvents = sortedEvents,
              clusterIndices = clusterIndices,
              baseColumns = baseColumns,
          )

      // Step 3: compute the final layout (width and offset) for each event in this cluster
      computeLayoutsForCluster(
          sortedEvents = sortedEvents,
          clusterIndices = clusterIndices,
          baseColumns = baseColumns,
          totalColumns = totalColumns,
          groupIndex = groupIndex,
          layoutMap = layoutMap,
      )
    }

    return layoutMap
  }

  /**
   * Builds temporal clusters of events.
   *
   * Each cluster is a list of indices into [sortedEvents]. Events are in the same cluster if there
   * is a chain of overlaps between them. Clusters are processed independently later.
   */
  private fun buildClusters(sortedEvents: List<Event>): List<List<Int>> {
    if (sortedEvents.isEmpty()) return emptyList()

    val clusters = mutableListOf<List<Int>>()
    var currentCluster = mutableListOf<Int>()

    // We keep track of the maximal end time inside the current cluster.
    var clusterEnd: Instant = sortedEvents[0].endDate
    currentCluster.add(0)

    for (index in 1 until sortedEvents.size) {
      val event = sortedEvents[index]

      // If the new event starts before the cluster end, it is connected to the cluster.
      if (event.startDate < clusterEnd) {
        currentCluster.add(index)
        if (event.endDate > clusterEnd) {
          clusterEnd = event.endDate
        }
      } else {
        // No temporal connection: close the current cluster and start a new one.
        clusters.add(currentCluster)
        currentCluster = mutableListOf(index)
        clusterEnd = event.endDate
      }
    }

    // Add the last cluster
    if (currentCluster.isNotEmpty()) {
      clusters.add(currentCluster)
    }

    return clusters
  }

  /**
   * Assigns a base column index to each event inside a cluster.
   *
   * The algorithm is greedy: for every event (in chronological order), we choose the smallest
   * column index that does not conflict with the event currently placed in that column.
   *
   * @param sortedEvents Events sorted by start time.
   * @param clusterIndices Indices of events that belong to the cluster.
   * @param baseColumns Output array filled with the base column index for each event.
   * @return The number of columns used in this cluster.
   */
  private fun assignBaseColumnsForCluster(
      sortedEvents: List<Event>,
      clusterIndices: List<Int>,
      baseColumns: IntArray,
  ): Int {
    // For each column we store the end time of the last event placed in that column.
    val columnLastEnd = mutableListOf<Instant>()

    clusterIndices.forEach { eventIndex ->
      val event = sortedEvents[eventIndex]
      var columnIndex = 0

      // Find the first column that does not overlap this event.
      while (true) {
        if (columnIndex >= columnLastEnd.size) {
          // No existing column fits: create a new column.
          columnLastEnd.add(event.endDate)
          baseColumns[eventIndex] = columnIndex
          break
        } else {
          val lastEnd = columnLastEnd[columnIndex]
          // If the last event in this column ends before or at the start of this event,
          // we can reuse this column.
          if (lastEnd <= event.startDate) {
            columnLastEnd[columnIndex] = event.endDate
            baseColumns[eventIndex] = columnIndex
            break
          } else {
            // Column is still busy: try the next one.
            columnIndex++
          }
        }
      }
    }

    return columnLastEnd.size
  }

  /**
   * Computes the final layout (width and horizontal offset) for every event in a cluster.
   *
   * Each event starts from its base column, then expands to the right over neighbour columns that
   * are always free (no event in that column overlaps in time with the current event).
   */
  private fun computeLayoutsForCluster(
      sortedEvents: List<Event>,
      clusterIndices: List<Int>,
      baseColumns: IntArray,
      totalColumns: Int,
      groupIndex: Int,
      layoutMap: MutableMap<Event, EventLayout>,
  ) {
    clusterIndices.forEach { eventIndex ->
      val event = sortedEvents[eventIndex]
      val baseColumn = baseColumns[eventIndex]

      // Find the right boundary (exclusive) of the column span for this event.
      val rightBoundary =
          findRightBoundary(
              sortedEvents = sortedEvents,
              clusterIndices = clusterIndices,
              baseColumns = baseColumns,
              eventIndex = eventIndex,
              totalColumns = totalColumns,
          )

      val columnSpan = rightBoundary - baseColumn

      // Convert column information into fractions of the full column width.
      val widthFraction = columnSpan.toFloat() / totalColumns.toFloat()
      val offsetFraction = baseColumn.toFloat() / totalColumns.toFloat()

      layoutMap[event] =
          EventLayout(
              widthFraction = widthFraction,
              offsetFraction = offsetFraction,
              overlapGroup = groupIndex,
              baseColumnIndex = baseColumn,
              columnSpan = columnSpan,
              totalColumns = totalColumns,
          )
    }
  }

  /**
   * Finds the first blocking column to the right of the base column for a given event.
   *
   * The event can extend over a column only if there is no other event in that column that overlaps
   * it in time. The returned value is the first column index that blocks the extension, or
   * [totalColumns] if no column blocks it.
   */
  private fun findRightBoundary(
      sortedEvents: List<Event>,
      clusterIndices: List<Int>,
      baseColumns: IntArray,
      eventIndex: Int,
      totalColumns: Int,
  ): Int {
    val baseColumn = baseColumns[eventIndex]
    val event = sortedEvents[eventIndex]

    var boundary = totalColumns

    // Try to extend over columns to the right of the base column.
    for (column in baseColumn + 1 until totalColumns) {
      val hasBlockingEvent =
          clusterIndices.any { otherIndex ->
            baseColumns[otherIndex] == column && eventsOverlap(event, sortedEvents[otherIndex])
          }

      if (hasBlockingEvent) {
        boundary = column
        break
      }
    }

    return boundary
  }

  /**
   * Determines if two events overlap in time.
   *
   * Intervals are treated as [start, end). This means that if one event ends exactly when another
   * starts, they do not overlap.
   */
  private fun eventsOverlap(
      event1: Event,
      event2: Event,
  ): Boolean {
    val start1 = event1.startDate
    val end1 = event1.endDate
    val start2 = event2.startDate
    val end2 = event2.endDate

    // Non-empty intersection of [start1, end1[ and [start2, end2[
    return start1 < end2 && end1 > start2
  }
}
