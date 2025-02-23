package com.zeuroux.launchly.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zeuroux.launchly.globals.GlobalData
import com.zeuroux.launchly.globals.architectures
import com.zeuroux.launchly.globals.releaseTypes
import com.zeuroux.launchly.version.VersionData
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionChooser(onChoose: (String) -> Unit) {
    val sortByOptions = listOf("Version Code", "Version Name", "Release Type", "Architecture")
    val sortOrderOptions = listOf("Descending", "Ascending")
    val showChooser = remember { GlobalData.showVersionChooser }
    val filters = remember { mutableStateListOf<String>() }
    val query = remember { mutableStateOf("") }
    val sortBy = remember { mutableStateOf(sortByOptions.first()) }
    val sortOrder = remember { mutableStateOf(sortOrderOptions.first()) }
    val selected = remember { mutableStateOf<String?>(null) }
    val onFinish = { showChooser.value = false }
    val context = LocalContext.current
    var isRefreshing by remember { mutableStateOf(false) }
    val onRefresh = {
        isRefreshing = true
        GlobalData.getVersionDB(context).fetchVersions(context) {
            isRefreshing = false
        }
    }
    val error by remember { GlobalData.getVersionDB(context).error }.collectAsState()
    LaunchedEffect(showChooser.value) {
        filters.clear()
        query.value = ""
        sortBy.value = sortByOptions.first()
        sortOrder.value = sortOrderOptions.first()
        selected.value = null
    }
    BaseBottomSheet(showChooser.value, onFinish, true) {
        Surface {
            Column(Modifier.fillMaxSize()) {
                ScreenTopBar("Choose Version", {
                    AnimatedVisibility(!selected.value.isNullOrEmpty()) {
                        IconButton({
                            onChoose(selected.value!!)
                            onFinish()
                        }) { Icon(Icons.Default.Check, "Confirm") }
                    }
                }, true, onFinish)
                val filterOptions = releaseTypes + architectures
                SearchBar(query, filterOptions, sortByOptions, sortOrderOptions,filters, sortBy, sortOrder)
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    contentAlignment = Alignment.Center
                ) {
                    if (error != null) {
                        Text(error!!, style = MaterialTheme.typography.bodyMedium)
                    } else {
                        VersionList(
                            selected.value,
                            filters,
                            query,
                            sortBy,
                            sortOrder
                        ) { selected.value = it }
                    }
                }
            }
        }
    }

}

@Composable
private fun VersionList(
    selected: String?,
    filters: SnapshotStateList<String>,
    query: MutableState<String>,
    sortBy: MutableState<String>,
    sortOrder: MutableState<String>,
    onChoose: (String) -> Unit
) {
    val context = LocalContext.current
    val versionDB = remember { GlobalData.getVersionDB(context) }
    val versions by remember { versionDB.versions }.collectAsState(emptyMap())
    val filteredVersions by remember(
        versions,
        filters.toList(),
        query.value,
        sortBy.value,
        sortOrder.value
    ) {
        derivedStateOf {
            val trimmedQuery = query.value.trim()
            val typeFilters = filters.toSet().intersect(releaseTypes.toSet())
            val archFilters = filters.toSet().intersect(architectures.toSet())
            versions.entries.asSequence()
                .filter { (versionCode, version) ->
                    (typeFilters.isEmpty() || version.type in typeFilters) &&
                            (archFilters.isEmpty() || version.architecture in archFilters) &&
                            ("${version}${versionCode}".contains(trimmedQuery, ignoreCase = true))
                }
                .map { it.key }
                .toList()
                .sortedWith(
                    compareBy<String> { versionCode ->
                        when (sortBy.value) {
                            "Version Name" -> versions[versionCode]?.name
                            "Release Type" -> versions[versionCode]?.type
                            "Architecture" -> versions[versionCode]?.architecture
                            else -> versionCode
                        }
                    }.let { if (sortOrder.value == "Descending") it.reversed() else it }
                )
        }
    }
    val visibleItems = remember { mutableStateListOf<String>() }
    LaunchedEffect(versions) {
        visibleItems.clear()
        filteredVersions.forEach { versionCode ->
            delay(8L)
            visibleItems.add(versionCode)
        }
    }
    LazyVerticalGrid(GridCells.Fixed(2), Modifier.fillMaxSize()) {
        items(filteredVersions, { it.toLong() }) { versionCode ->
            val isVisible by remember { derivedStateOf { visibleItems.contains(versionCode) } }
            AnimatedVisibility(isVisible, enter = fadeIn() + scaleIn(initialScale = 0.8f)) {
                val animatedBorder =
                    animateDpAsState(if (selected == versionCode) 2.dp else 0.dp).value

                VersionCard(versionCode, versions[versionCode]!!, Modifier
                    .padding(8.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .clickable { onChoose(if (selected == versionCode) "" else versionCode) }
                    .border(
                        animatedBorder,
                        if (animatedBorder != 0.dp) MaterialTheme.colorScheme.primary else Color.Transparent,
                        MaterialTheme.shapes.medium
                    )
                    .padding(8.dp)
                    .animateContentSize())
            }
        }
    }
}

@Composable
fun VersionCard(versionCode: String, version: VersionData, modifier: Modifier) {
    Column(modifier) {
        Text(version.name, style = MaterialTheme.typography.headlineLarge)
        Text(
            "Version Code: $versionCode\nRelease Type: ${version.type}\nArchitecture: ${version.architecture}",
            fontWeight = FontWeight.Light,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun SearchBar(
    searchQuery: MutableState<String>,
    filterOptions: List<String> = emptyList(),
    sortByOptions: List<String> = emptyList(),
    sortOrderOptions: List<String> = emptyList(),
    filters: MutableList<String> = remember { mutableStateListOf() },
    sortBy: MutableState<String> = remember { mutableStateOf("") },
    sortOrder: MutableState<String> = remember { mutableStateOf("") }
) {
    TextField(
        value = searchQuery.value,
        onValueChange = { searchQuery.value = it },
        singleLine = true,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        leadingIcon = { Icon(Icons.Default.Search, "Search") },
        trailingIcon = {
            Row {
                if (filterOptions.isNotEmpty()) {
                    SearchOption(Icons.Default.FilterList, "Filters") {
                        CategoryTitle("Filters")
                        filterOptions.forEach { option -> FilterOption(option, filters) }
                    }
                }
                if (sortByOptions.isNotEmpty() || sortOrderOptions.isNotEmpty()) {
                    SearchOption(Icons.AutoMirrored.Default.Sort, "Sort") {
                        if (sortByOptions.isNotEmpty()) {
                            SortOptions("Sort By", sortByOptions, sortBy)
                        }
                        if (sortOrderOptions.isNotEmpty()) {
                            SortOptions("Sort Order", sortOrderOptions, sortOrder)
                        }
                    }
                }
            }
        },
        colors = TextFieldDefaults.colors().copy(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = MaterialTheme.shapes.extraLarge
    )
}

@Composable
fun SearchOption(iconVector: ImageVector, description: String = "", options: @Composable () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    IconButton({ expanded = true }) { Icon(iconVector, description) }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) { options() }
}

@Composable
private fun FilterOption(option: String, filters: MutableList<String>) {
    var checked by remember { mutableStateOf(filters.contains(option)) }
    val toggle = {
        checked = !checked
        if (checked) filters.add(option) else filters.remove(option)
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { toggle() }
            .padding(start = 16.dp),
        Arrangement.SpaceBetween,
        Alignment.CenterVertically
    ) {
        Text(option)
        Checkbox(checked, { toggle() })
    }
}

@Composable
private fun SortOptions(title: String, options: List<String>, selected: MutableState<String>) {
    Column {
        CategoryTitle(title)
        options.forEach { option ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { selected.value = option }
                    .padding(start = 16.dp),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically
            ) {
                Text(option)
                RadioButton(selected.value == option, { selected.value = option })
            }
        }
    }
}
