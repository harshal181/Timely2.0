package com.om.timely.faculty.subjects.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.core.presentation.util.Constants
import com.om.timely.faculty.subjects.domain.model.Subject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubjectSheet(
    subject: Subject?,
    onDismissRequest: () -> Unit,
    onSave: (Subject) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf(subject?.name ?: "") }
    var abbreviation by remember { mutableStateOf(subject?.abbreviation ?: "") }
    var code by remember { mutableStateOf(subject?.code ?: "") }
    var semester by remember { mutableIntStateOf(subject?.semester ?: 1) }
    var department by remember {
        mutableStateOf(subject?.department ?: Constants.DEPARTMENTS["CSE"]!!)
    }

    var type by remember { mutableStateOf(subject?.type ?: "core") }
    var lecture by remember { mutableStateOf(subject?.lecture?.toString() ?: "0") }
    var tutorial by remember { mutableStateOf(subject?.tutorial?.toString() ?: "0") }
    var practical by remember { mutableStateOf(subject?.practical?.toString() ?: "0") }
    var electiveGroup by remember { mutableStateOf(subject?.electiveGroup ?: "") }
    var isSelected by remember { mutableStateOf(subject?.isSelected ?: false) }

    var colorArgb by remember { mutableIntStateOf(subject?.colorArgb ?: 0xFF0891B2.toInt()) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (subject == null) "Add New Subject" else "Edit Subject",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1F2937)
                )

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFF3F4F6), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF4B5563),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFF3F4F6))
            )

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LabeledTextField(
                    label = "Subject Name",
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "e.g., Data Structures"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        LabeledTextField(
                            label = "Abbreviation",
                            value = abbreviation,
                            onValueChange = { abbreviation = it },
                            placeholder = "e.g., DS"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        LabeledTextField(
                            label = "Subject Code",
                            value = code,
                            onValueChange = { code = it },
                            placeholder = "e.g., CS301"
                        )
                    }
                }

                // Dropdowns Row (Semester & Dept)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Semester Picker
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Semester", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        Spacer(modifier = Modifier.height(8.dp))

                        var semExpanded by remember { mutableStateOf(false) }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF9FAFB))
                                .clickable { semExpanded = true }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Semester $semester", fontSize = 14.sp, color = Color(0xFF111827))
                                Icon(Icons.Default.ArrowDropDown, null, tint = Color(0xFF9CA3AF))
                            }

                            DropdownMenu(expanded = semExpanded, onDismissRequest = { semExpanded = false }, modifier = Modifier.background(Color.White)) {
                                (1..8).forEach { sem ->
                                    DropdownMenuItem(
                                        text = { Text("Semester $sem") },
                                        onClick = { semester = sem; semExpanded = false }
                                    )
                                }
                            }
                        }
                    }

                    // Department Picker
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Department", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        Spacer(modifier = Modifier.height(8.dp))

                        var deptExpanded by remember { mutableStateOf(false) }
                        val displayDept = Constants.DEPARTMENTS.entries
                            .find { it.value == department }?.key ?: department
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF9FAFB))
                                .clickable { deptExpanded = true }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(displayDept, fontSize = 14.sp, color = Color(0xFF111827))
                                Icon(Icons.Default.ArrowDropDown, null, tint = Color(0xFF9CA3AF))
                            }

                            DropdownMenu(expanded = deptExpanded, onDismissRequest = { deptExpanded = false }, modifier = Modifier.background(Color.White)) {
                                Constants.DEPARTMENTS.forEach { (shortName, fullName) ->
                                    DropdownMenuItem(
                                        text = { Text(shortName) },
                                        onClick = { department = fullName; deptExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }

                // Type Picker
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Subject Type", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                    Spacer(modifier = Modifier.height(8.dp))

                    var typeExpanded by remember { mutableStateOf(false) }
                    // Matched with FastAPI literals
                    val types = listOf("core", "lab", "elective", "project", "internship")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF9FAFB))
                            .clickable { typeExpanded = true }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(type.replaceFirstChar { it.uppercase() }, fontSize = 14.sp, color = Color(0xFF111827))
                            Icon(Icons.Default.ArrowDropDown, null, tint = Color(0xFF9CA3AF))
                        }

                        DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }, modifier = Modifier.background(Color.White)) {
                            types.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text(t.replaceFirstChar { it.uppercase() }) },
                                    onClick = { type = t; typeExpanded = false }
                                )
                            }
                        }
                    }
                }

                // Dynamic Elective Group Field
                if (type == "elective") {
                    LabeledTextField(
                        label = "Elective Group",
                        value = electiveGroup,
                        onValueChange = { electiveGroup = it },
                        placeholder = "e.g., Professional Elective 1"
                    )
                }

                // L-T-P Hours Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        LabeledTextField(
                            label = "Lecture",
                            value = lecture,
                            onValueChange = { lecture = it },
                            placeholder = "0",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        LabeledTextField(
                            label = "Tutorial",
                            value = tutorial,
                            onValueChange = { tutorial = it },
                            placeholder = "0",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        LabeledTextField(
                            label = "Practical",
                            value = practical,
                            onValueChange = { practical = it },
                            placeholder = "0",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }

                // Is Selected Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Selected by default", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        Text("Automatically assign this to students", fontSize = 12.sp, color = Color(0xFF6B7280))
                    }
                    Switch(
                        checked = isSelected,
                        onCheckedChange = { isSelected = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF8B5CF6))
                    )
                }

                // Color Picker Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(colorArgb)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = abbreviation.take(2).uppercase(),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Ensure you still have the SubjectColorPicker component available in your code
                    SubjectColorPicker(
                        modifier = Modifier.weight(1f),
                        initialColor = Color(colorArgb),
                        onColorChanged = { newColor ->
                            colorArgb = android.graphics.Color.argb(
                                (newColor.alpha * 255).toInt(),
                                (newColor.red * 255).toInt(),
                                (newColor.green * 255).toInt(),
                                (newColor.blue * 255).toInt()
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                val canSave = name.isNotBlank() && abbreviation.isNotBlank() && code.isNotBlank()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = Color(0xFF374151), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val newSub = Subject(
                                id = subject?.id ?: 0, // Using 0 for unsaved new items
                                syllabusId = subject?.syllabusId ?: 0,
                                name = name,
                                code = code,
                                lecture = lecture.toIntOrNull() ?: 0,
                                tutorial = tutorial.toIntOrNull() ?: 0,
                                practical = practical.toIntOrNull() ?: 0,
                                type = type,
                                abbreviation = abbreviation,
                                semester = semester,
                                department = department,
                                electiveGroup = if (type == "elective") electiveGroup.takeIf { it.isNotBlank() } else null,
                                isSelected = isSelected,
                                colorArgb = colorArgb
                            )
                            onSave(newSub)
                        },
                        enabled = canSave,
                        modifier = Modifier.weight(1.5f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Save Subject", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default // Added this to support number pads
) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFF9CA3AF)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = keyboardOptions,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedBorderColor = Color(0xFF8B5CF6),
                cursorColor = Color(0xFF8B5CF6),
            ),
            singleLine = true
        )
    }
}