package com.om.timely.faculty.syllabus_extractor.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.faculty.syllabus_extractor.presentation.SubjectField
import com.om.timely.faculty.syllabus_extractor.presentation.SyllabusExtractorAction
import com.om.timely.faculty.syllabus_extractor.presentation.SubjectExtractionState
import com.om.timely.faculty.syllabus_extractor.presentation.model.ExtractedSubjectUi
import com.om.timely.faculty.syllabus_extractor.presentation.model.SemesterUi
import com.om.timely.ui.theme.TimelyTheme
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ReviewSyllabusScreen(
    state: SubjectExtractionState,
    onAction: (SyllabusExtractorAction) -> Unit
) {
    if (state.extractedSemesters.isEmpty()) return

    val currentSem = state.extractedSemesters.getOrNull(state.selectedSemesterTabIndex) ?: return

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item("header") {
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Help", tint = MaterialTheme.colorScheme.tertiary)
                        Column(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Review & Edit Your Syllabus", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                            }
                            Text(
                                "Switch between semesters using the tabs. Hover over any subject and click the pencil icon to edit name, code, type, and hours. Elective groups are collapsible for a cleaner view.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            stickyHeader {
                Column(modifier = Modifier.background(Color.White)) {
                    ScrollableTabRow(
                        selectedTabIndex = state.selectedSemesterTabIndex,
                        edgePadding = 16.dp,
                        divider = {},
                        containerColor = Color.Transparent,
                        indicator = { } // Remove default underline indicator
                    ) {
                        state.extractedSemesters.forEachIndexed { index, sem ->
                            val selected = index == state.selectedSemesterTabIndex
                            Tab(
                                selected = selected,
                                onClick = { onAction(SyllabusExtractorAction.OnSemesterTabSelected(index)) },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                        .border(
                                            1.dp,
                                            if (selected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 20.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Sem ${sem.semester}",
                                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Filter and total text
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FilterAlt, contentDescription = "Filter", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            var expanded by remember { mutableStateOf(false) }
                            val allTypes = listOf("All Types", "Core", "Lab", "Elective", "Project")
                            
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = it }
                            ) {
                                OutlinedTextField(
                                    value = state.subjectTypeFilter,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier.menuAnchor().width(160.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    singleLine = true
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    allTypes.forEach { selectionOption ->
                                        DropdownMenuItem(
                                            text = { Text(selectionOption) },
                                            onClick = {
                                                onAction(SyllabusExtractorAction.OnFilterChanged(selectionOption))
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        val filteredSubjects = currentSem.subjects.filter {
                            state.subjectTypeFilter == "All Types" || it.type.equals(state.subjectTypeFilter, ignoreCase=true)
                        }
                        Text("${filteredSubjects.size} subjects", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.2f))
                }
            }

            val filteredSubjects = currentSem.subjects.filter {
                state.subjectTypeFilter == "All Types" || it.type.equals(state.subjectTypeFilter, ignoreCase=true)
            }

            val (electives, core) = filteredSubjects.partition {
                it.type.equals("elective", ignoreCase = true) && !it.electiveGroup.isNullOrBlank()
            }

            val groupedElectives = electives.groupBy { it.electiveGroup!! }

            items(core, key = { it.id }) { subject ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SubjectCard(subject = subject, onAction = onAction)
                }
            }

            groupedElectives.forEach { (groupName, subjects) ->
                item(key = groupName) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ElectiveGroupContainer(groupName = groupName, subjects = subjects, onAction = onAction)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = state.syllabusName,
                    onValueChange = { onAction(SyllabusExtractorAction.OnSyllabusNameChange(it)) },
                    label = { Text("Syllabus Name (e.g. Even 2026 Scheme)") },
                    placeholder = { Text("Leave blank for default") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Inventory2,
                                contentDescription = null,
                                modifier = Modifier.padding(8.dp).size(18.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Save as local draft",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Allow editing later from Management",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = state.isSaveAsDraftEnabled,
                        onCheckedChange = { onAction(SyllabusExtractorAction.OnToggleSaveAsDraft(it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }

                Button(
                    onClick = { onAction(SyllabusExtractorAction.OnConfirmAndSaveAll) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !state.isSavingFinal,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isSavingFinal) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    } else {
                        Icon(
                            if (state.isSaveAsDraftEnabled) Icons.Default.Inventory2 else Icons.Default.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (state.isSaveAsDraftEnabled) "Save Draft Locally" else "Push to Database",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ElectiveGroupContainer(groupName: String, subjects: List<ExtractedSubjectUi>, onAction: (SyllabusExtractorAction) -> Unit) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f), RoundedCornerShape(16.dp))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Layers, contentDescription = null, tint = Color(0xFFF57C00))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = groupName, 
                    fontWeight = FontWeight.Bold, 
                    modifier = Modifier.weight(1f), 
                    fontSize = 16.sp, 
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp)) {
                    Text("${subjects.size} options", fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                }
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    subjects.forEach { subj -> SubjectCard(subj, onAction) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectCard(subject: ExtractedSubjectUi, onAction: (SyllabusExtractorAction) -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent, 
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            if (subject.isEditing) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha=0.3f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        
                        OutlinedTextField(
                            value = subject.name,
                            onValueChange = { onAction(SyllabusExtractorAction.OnSubjectFieldChanged(subject.id, SubjectField.NAME, it)) },
                            label = { Text("Subject Name") },
                            shape = RoundedCornerShape(12.dp),
                            isError = subject.nameError != null,
                            supportingText = { subject.nameError?.let { Text(it) } },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = subject.courseCode ?: "",
                            onValueChange = { onAction(SyllabusExtractorAction.OnSubjectFieldChanged(subject.id, SubjectField.CODE, it)) },
                            label = { Text("Code") },
                            shape = RoundedCornerShape(12.dp),
                            isError = subject.codeError != null,
                            supportingText = { subject.codeError?.let { Text(it) } },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val allTypes = listOf("Core", "Lab", "Elective", "Project")
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it }
                        ) {
                            OutlinedTextField(
                                value = subject.type,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                allTypes.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            onAction(SyllabusExtractorAction.OnSubjectFieldChanged(subject.id, SubjectField.TYPE, selectionOption))
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        if (subject.type.equals("elective", ignoreCase = true)) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = subject.electiveGroup ?: "",
                                onValueChange = { onAction(SyllabusExtractorAction.OnSubjectFieldChanged(subject.id, SubjectField.ELECTIVE_GROUP, it)) },
                                label = { Text("Elective Group") },
                                shape = RoundedCornerShape(12.dp),
                                isError = subject.electiveGroupError != null,
                                supportingText = { subject.electiveGroupError?.let { Text(it) } },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Hours per Week (L / T / P)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = subject.lecture,
                                onValueChange = { onAction(SyllabusExtractorAction.OnSubjectFieldChanged(subject.id, SubjectField.LECTURE, it)) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                trailingIcon = { Text("L", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = subject.tutorial,
                                onValueChange = { onAction(SyllabusExtractorAction.OnSubjectFieldChanged(subject.id, SubjectField.TUTORIAL, it)) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                trailingIcon = { Text("T", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = subject.practical,
                                onValueChange = { onAction(SyllabusExtractorAction.OnSubjectFieldChanged(subject.id, SubjectField.PRACTICAL, it)) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                trailingIcon = { Text("P", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        subject.hoursError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            OutlinedButton(
                                onClick = { onAction(SyllabusExtractorAction.OnCancelSubjectEdit(subject.id)) },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) { 
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) 
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Button(
                                onClick = { onAction(SyllabusExtractorAction.OnSaveSubjectEdit(subject.id)) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) { 
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save Changes") 
                            }
                        }
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically, 
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    val typeLower = subject.type.lowercase()
                    val (icon, pillColor, pillBg) = when {
                        typeLower.contains("lab") || typeLower.contains("practical") -> Triple(Icons.Default.Science, Color(0xFF1976D2), Color(0xFFE3F2FD))
                        typeLower.contains("elective") -> Triple(Icons.Default.Layers, Color(0xFFF57C00), Color(0xFFFFF3E0))
                        typeLower.contains("project") -> Triple(Icons.Default.Build, Color(0xFF7B1FA2), Color(0xFFF3E5F5))
                        else -> Triple(Icons.Default.MenuBook, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.4f))
                    }
                    
                    Surface(
                        color = pillBg,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(0.5.dp, pillColor.copy(alpha=0.3f))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal=12.dp, vertical=6.dp)) {
                            Icon(icon, contentDescription=null, modifier = Modifier.size(14.dp), tint=pillColor)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(subject.type.replaceFirstChar { it.uppercase() }, fontSize=13.sp, color=pillColor, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = subject.name, 
                            fontWeight = FontWeight.Medium, 
                            fontSize = 16.sp, 
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            val code = subject.courseCode
                            if (!code.isNullOrBlank()) {
                                Text(
                                    text = "# $code", 
                                    style = MaterialTheme.typography.bodySmall, 
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                            
                            val group = subject.electiveGroup
                            if (!group.isNullOrBlank()) {
                                if (!code.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("•", color = MaterialTheme.colorScheme.outlineVariant, fontSize = 10.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = group, 
                                    style = MaterialTheme.typography.bodySmall, 
                                    color = Color(0xFFF57C00),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "L:${subject.lecture}  T:${subject.tutorial}  P:${subject.practical}", 
                        style = MaterialTheme.typography.labelMedium, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.8f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.3f),
                        shape = RoundedCornerShape(12.dp),
                        onClick = { onAction(SyllabusExtractorAction.OnEditSubjectClicked(subject.id)) }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.padding(10.dp).size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

// --- PREVIEW ---

@Preview(showBackground = true)
@Composable
private fun ReviewSyllabusScreenPreview() {
    TimelyTheme {
        val dummySubjects = listOf(
            ExtractedSubjectUi(
                id = UUID.randomUUID().toString(),
                name = "Engineering Mathematics-ll",
                courseCode = "MA201",
                lecture = "3", tutorial = "1", practical = "0",
                type = "Core",
                electiveGroup = null
            ),
            ExtractedSubjectUi(
                id = UUID.randomUUID().toString(),
                name = "Engineering Physics",
                courseCode = "PH201",
                lecture = "3", tutorial = "0", practical = "0",
                type = "Core",
                electiveGroup = null,
                isEditing = false // Testing edit mode
            ),
            ExtractedSubjectUi(
                id = UUID.randomUUID().toString(),
                name = "Engineering Physics Lab",
                courseCode = "PH201L",
                lecture = "0", tutorial = "0", practical = "2",
                type = "Lab",
                electiveGroup = null
            ),
            ExtractedSubjectUi(
                id = UUID.randomUUID().toString(),
                name = "Cloud Computing",
                courseCode = "PE601A",
                lecture = "3", tutorial = "0", practical = "0",
                type = "Elective",
                electiveGroup = "Program Elective-II"
            ),
            ExtractedSubjectUi(
                id = UUID.randomUUID().toString(),
                name = "Distributed Systems",
                courseCode = "PE601B",
                lecture = "3", tutorial = "0", practical = "0",
                type = "Elective",
                electiveGroup = "Program Elective-II"
            )
        )

        val dummyState = SubjectExtractionState(
            currentStep = SubjectExtractionState.Step.REVIEW,
            selectedSemesterTabIndex = 0,
            extractedSemesters = listOf(
                SemesterUi(semester = "2", subjects = dummySubjects),
                SemesterUi(semester = "4", subjects = emptyList())
            )
        )

        ReviewSyllabusScreen(
            state = dummyState,
            onAction = {}
        )
    }
}