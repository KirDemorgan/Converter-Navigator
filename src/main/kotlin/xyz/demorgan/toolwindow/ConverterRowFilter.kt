package xyz.demorgan.toolwindow

object ConverterRowFilter {

    fun matchesText(searchText: String, query: String): Boolean {
        val normalized = query.trim().lowercase()
        return normalized.isEmpty() || searchText.contains(normalized)
    }

    fun matches(row: ConverterRow, query: String): Boolean = matchesText(row.searchText, query)

    fun filter(rows: List<ConverterRow>, query: String): List<ConverterRow> =
        rows.filter { matches(it, query) }
}
