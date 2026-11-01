package com.prem.daybyday.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prem.daybyday.data.WorkoutSet
import java.time.LocalDate

private val WBg = Color(0xFF080A0B)
private val WPanel = Color(0xFF14191B)
private val WGreen = Color(0xFF48D6A0)
private val WMuted = Color(0xFF929B9E)
private val upper = listOf("Flat Bench Press", "Incline Bench Press", "Cable Flyes", "Lateral Raises", "Cross-Grip Lat Pulldowns", "Bicep Curls", "Tricep Pushdowns")
private val lower = listOf("Leg Extensions", "Hack Squat", "Lunges", "Hip Abductor", "Standing Cable Calf Raises", "Cable Crunches")

@Composable
fun WorkoutScreen(onBack: () -> Unit, onSave: (String, List<WorkoutSet>) -> Unit, history: List<WorkoutSet>, sessionsCount: Int, sessionTypes: Map<Long, String>) {
    var type by remember { mutableStateOf("Upper") }
    var selectedExercise by remember { mutableStateOf<String?>(null) }
    val exercises = if (type == "Upper") upper else lower
    val values = remember(type) { mutableStateMapOf<String, List<Pair<String, String>>>().apply { exercises.forEach { put(it, List(3) { "" to "" }) } } }
    val latestByExercise = history.groupBy { it.exerciseName }
    MaterialTheme(colorScheme = darkColorScheme(background = WBg, surface = WPanel, primary = WGreen, onSurface = Color.White)) {
        Column(Modifier.fillMaxSize().background(WBg).padding(horizontal = 18.dp)) {
            Row(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                Column(Modifier.weight(1f)) { Text("Workout", fontSize = 26.sp, fontWeight = FontWeight.Bold); Text("TRACK YOUR STRENGTH", color = WMuted, fontSize = 10.sp, letterSpacing = 1.5.sp) }
                Text("$sessionsCount sessions", color = WMuted, fontSize = 11.sp)
            }
            Row(Modifier.fillMaxWidth().background(WPanel, RoundedCornerShape(14.dp)).padding(4.dp)) {
                listOf("Upper", "Lower").forEach { item ->
                    Box(Modifier.weight(1f).background(if (type == item) Color(0xFF244B3D) else Color.Transparent, RoundedCornerShape(10.dp)).clickable { type = item; selectedExercise = null }.padding(12.dp), contentAlignment = Alignment.Center) { Text("$item Body", color = Color.White, fontWeight = FontWeight.SemiBold) }
                }
            }
            Spacer(Modifier.height(14.dp))
            if (selectedExercise == null) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    WorkoutTypeTrend("UPPER BODY", history.filter { sessionTypes[it.sessionId] == "Upper" }, Modifier.weight(1f))
                    WorkoutTypeTrend("LOWER BODY", history.filter { sessionTypes[it.sessionId] == "Lower" }, Modifier.weight(1f))
                }
                Spacer(Modifier.height(14.dp))
                Text("Choose your workout", color = WMuted, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    items(exercises) { exercise ->
                        val previous = latestByExercise[exercise]?.maxByOrNull { it.id }
                        Row(Modifier.fillMaxWidth().background(WPanel, RoundedCornerShape(15.dp)).clickable { selectedExercise = exercise }.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text(exercise, color = Color.White, fontWeight = FontWeight.SemiBold); Text(previous?.let { "Previous: ${it.weightKg} kg × ${it.reps}" } ?: "No previous record · 3 sets", color = WMuted, fontSize = 11.sp) }
                            Text("Log  ›", color = WGreen, fontSize = 13.sp)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { val all = exercises.flatMap { ex -> values[ex].orEmpty().mapIndexedNotNull { i, pair -> val kg = pair.first.toDoubleOrNull(); val reps = pair.second.toIntOrNull(); if (kg != null && reps != null && kg >= 0 && reps > 0) WorkoutSet(sessionId = 0, exerciseName = ex, setNumber = i + 1, weightKg = kg, reps = reps) else null } }; if (all.isNotEmpty()) onSave(type, all) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = WGreen, contentColor = WBg)) { Icon(Icons.Default.Check, null); Spacer(Modifier.width(8.dp)); Text("Save completed workout") }
            } else {
                val ex = selectedExercise!!
                Row(verticalAlignment = Alignment.CenterVertically) { Text(ex, Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 19.sp); TextButton(onClick = { selectedExercise = null }) { Text("Done", color = WGreen) } }
                Text("Enter your working weight and reps for each set.", color = WMuted, fontSize = 12.sp)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("SET", color = WMuted, fontSize = 10.sp); Text("WEIGHT (KG)", color = WMuted, fontSize = 10.sp); Text("REPS", color = WMuted, fontSize = 10.sp) }
                repeat(3) { i ->
                    val current = values[ex]?.get(i) ?: ("" to "")
                    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("${i + 1}", Modifier.width(24.dp), color = WGreen, fontWeight = FontWeight.Bold)
                        OutlinedTextField(value = current.first, onValueChange = { v -> values[ex] = values[ex].orEmpty().toMutableList().also { it[i] = v to current.second } }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), placeholder = { Text("kg") })
                        OutlinedTextField(value = current.second, onValueChange = { v -> values[ex] = values[ex].orEmpty().toMutableList().also { it[i] = current.first to v } }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), placeholder = { Text("reps") })
                    }
                }
                latestByExercise[ex]?.maxByOrNull { it.id }?.let { Text("Last logged: ${it.weightKg} kg × ${it.reps}", color = WMuted, fontSize = 11.sp) }
                ExerciseProgress(ex, history.filter { it.exerciseName == ex })
                Button(onClick = { selectedExercise = null }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = WGreen, contentColor = WBg)) { Text("Done · Return to exercises") }
                Text("Your entries are saved when you tap Save completed workout.", color = WMuted, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun WorkoutTypeTrend(title: String, records: List<WorkoutSet>, modifier: Modifier = Modifier) {
    val trend = records.groupBy { it.sessionId }.toSortedMap().values.mapNotNull { sets -> sets.maxOfOrNull { it.weightKg }?.toFloat() }
    Column(modifier.background(WPanel, RoundedCornerShape(14.dp)).padding(10.dp)) {
        Text(title, color = WMuted, fontSize = 9.sp, letterSpacing = .7.sp, maxLines = 1)
        Text(if (trend.isNotEmpty()) "%.1f kg best set".format(trend.last()) else "No data yet", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 5.dp))
        MiniTrend(trend, WGreen)
        Text("${trend.size} sessions", color = WMuted, fontSize = 9.sp)
    }
}

@Composable
private fun ExerciseProgress(name: String, records: List<WorkoutSet>) {
    val recent = records.sortedBy { it.id }.takeLast(9)
    Column(Modifier.fillMaxWidth().padding(top = 18.dp).background(WPanel, RoundedCornerShape(14.dp)).padding(12.dp)) {
        Text("STRENGTH HISTORY", color = WMuted, fontSize = 10.sp, letterSpacing = 1.sp)
        if (recent.isEmpty()) Text("Your performance graph will appear after your first saved workout.", color = WMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
        else {
            Text("Weight lifted · kg", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
            MiniTrend(recent.map { it.weightKg.toFloat() }, WGreen)
            Text("Repetitions", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
            MiniTrend(recent.map { it.reps.toFloat() }, Color(0xFF63AEFF))
            val bestWeight = recent.maxOf { it.weightKg }; val bestReps = recent.maxOf { it.reps }
            Text("Best logged set: $bestWeight kg × $bestReps reps", color = WGreen, fontSize = 11.sp)
        }
    }
}

@Composable
private fun MiniTrend(values: List<Float>, tint: Color) {
    Canvas(Modifier.fillMaxWidth().height(54.dp).padding(vertical = 4.dp)) {
        for (i in 0..2) { val y = size.height * i / 2f; drawLine(Color(0xFF30383A), Offset(0f, y), Offset(size.width, y), 1.dp.toPx()) }
        if (values.size >= 2) {
            val min = values.minOrNull()!!; val range = (values.maxOrNull()!! - min).coerceAtLeast(1f)
            val pts = values.mapIndexed { i, v -> Offset(size.width * i / (values.size - 1), size.height - 4.dp.toPx() - ((v-min)/range) * (size.height - 8.dp.toPx())) }
            val path = Path().apply { pts.forEachIndexed { i, p -> if (i == 0) moveTo(p.x,p.y) else lineTo(p.x,p.y) } }
            drawPath(path, tint, style = Stroke(width = 2.dp.toPx()))
            pts.forEach { drawCircle(tint, 3.dp.toPx(), it) }
        } else drawCircle(tint, 4.dp.toPx(), Offset(size.width/2, size.height/2))
    }
}
