package com.prem.daybyday.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prem.daybyday.data.WeightEntry
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Bg = Color(0xFF080A0B)
private val Panel = Color(0xFF14191B)
private val Green = Color(0xFF48D6A0)
private val Blue = Color(0xFF63AEFF)
private val Muted = Color(0xFF929B9E)

@Composable
fun PremScreen(selectedDate: LocalDate, onDateSelected: (LocalDate)->Unit, weights: List<WeightEntry>, steps: Long,
               healthConnected: Boolean, healthMessage: String, onConnectHealth: ()->Unit, onRefreshSteps: ()->Unit,
               onSaveWeight: (LocalDate,Double)->Unit, onDeleteWeight: (LocalDate)->Unit, goalWeight: Double?, goalStartWeight: Double?, onSaveGoal: (Double)->Unit, onOpenWorkout: ()->Unit) {
    var month by remember { mutableStateOf(YearMonth.now()) }
    var calendarExpanded by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }
    var weightText by remember { mutableStateOf("") }
    var showGoalDialog by remember { mutableStateOf(false) }
    var goalText by remember { mutableStateOf(goalWeight?.toString() ?: "") }
    val latest = weights.lastOrNull()?.kilograms
    val selectedWeight = weights.firstOrNull { it.date == selectedDate.toString() }?.kilograms
    MaterialTheme(colorScheme = darkColorScheme(background=Bg, surface=Panel, primary=Green, secondary=Blue, onBackground=Color.White, onSurface=Color.White)) {
        Scaffold(containerColor=Bg) { pad ->
            Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal=18.dp)) {
                Row(Modifier.fillMaxWidth().padding(top=18.dp,bottom=18.dp), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically) {
                    Column { Text("Prem.", fontSize=30.sp, fontWeight=FontWeight.Bold, color=Color.White); Text("YOUR DAILY PROGRESS", fontSize=10.sp, letterSpacing=2.sp, color=Muted) }
                    Row(verticalAlignment=Alignment.CenterVertically) { IconButton(onClick=onOpenWorkout) { Icon(Icons.Default.FitnessCenter, contentDescription="Workout", tint=Green) }; Icon(Icons.Default.Settings, contentDescription="Settings", tint=Muted) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically) {
                    Text(if (calendarExpanded) month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)) else selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)), fontSize=18.sp, fontWeight=FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick={ if (calendarExpanded) month=month.minusMonths(1) else onDateSelected(selectedDate.minusWeeks(1)) }) { Icon(Icons.Default.ChevronLeft, null, tint=Muted) }
                        IconButton(onClick={ if (calendarExpanded) month=month.plusMonths(1) else onDateSelected(selectedDate.plusWeeks(1)) }) { Icon(Icons.Default.ChevronRight, null, tint=Muted) }
                    }
                }
                val dates: List<LocalDate?> = if (calendarExpanded) {
                    val firstOffset = month.atDay(1).dayOfWeek.value - 1
                    val raw = List(firstOffset) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
                    raw + List((7 - raw.size % 7) % 7) { null }
                } else {
                    val monday = selectedDate.minusDays((selectedDate.dayOfWeek.value - 1).toLong())
                    (0L..6L).map { monday.plusDays(it) }
                }
                Row(Modifier.fillMaxWidth()) {
                    listOf("M","T","W","T","F","S","S").forEach { day ->
                        Text(day, Modifier.weight(1f), color=Muted, fontSize=11.sp, textAlign=androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
                dates.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth()) {
                        week.forEach { date ->
                            Box(Modifier.weight(1f).padding(vertical=3.dp).height(48.dp).then(if(date!=null) Modifier.clickable { onDateSelected(date) } else Modifier), contentAlignment=Alignment.Center) {
                                if (date != null) {
                                    val entry = weights.firstOrNull { it.date == date.toString() }
                                    Column(horizontalAlignment=Alignment.CenterHorizontally, verticalArrangement=Arrangement.Center,
                                        modifier=Modifier.background(if(date==selectedDate) Color(0xFF1B4B3A) else Color.Transparent, RoundedCornerShape(10.dp)).padding(horizontal=5.dp, vertical=4.dp)) {
                                        Text(date.dayOfMonth.toString(), color=if(date==selectedDate) Color.White else Color(0xFFD7DEDF), fontSize=13.sp, fontWeight=if(date==LocalDate.now()) FontWeight.Bold else FontWeight.Normal)
                                        if (entry != null) Text("%.1f".format(entry.kilograms), color=Green, fontSize=8.sp, maxLines=1)
                                        else Box(Modifier.padding(top=2.dp).size(4.dp).background(Color.Transparent, CircleShape))
                                    }
                                }
                            }
                        }
                    }
                }
                TextButton(onClick={ calendarExpanded = !calendarExpanded; if (calendarExpanded) month = YearMonth.from(selectedDate) }, modifier=Modifier.align(Alignment.CenterHorizontally)) {
                    Icon(if(calendarExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint=Blue)
                    Spacer(Modifier.width(4.dp))
                    Text(if(calendarExpanded) "Collapse calendar" else "Expand calendar", color=Blue, fontSize=11.sp)
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                    StatCard(Modifier.weight(1f), "${if(selectedDate==LocalDate.now()) "TODAY'S STEPS" else "SELECTED DAY"}", steps.toString(), "Samsung Health", Green, Icons.Default.DirectionsWalk)
                    StatCard(Modifier.weight(1f), "CURRENT WEIGHT", latest?.let { "%.1f kg".format(it) } ?: "—", "Last recorded", Blue, Icons.Default.MonitorWeight)
                    GoalProgressCard(Modifier.weight(1f), latest, goalWeight, goalStartWeight, onClick = { goalText = goalWeight?.toString() ?: ""; showGoalDialog = true })
                }
                Row(Modifier.fillMaxWidth().padding(top=10.dp), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically) {
                    Text(if(healthConnected) "● Health Connect connected" else "○ Steps not connected", color=if(healthConnected) Green else Muted, fontSize=11.sp)
                    TextButton(onClick=if(healthConnected) onRefreshSteps else onConnectHealth) { Text(if(healthConnected) "Refresh" else "Connect", color=Green) }
                }
                Text(healthMessage, color=Muted, fontSize=10.sp)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically) {
                    Column { Text("Weight trend", fontSize=19.sp, fontWeight=FontWeight.SemiBold); Text("Your recorded progress", fontSize=11.sp, color=Muted) }
                    Text("${weights.size} entries", color=Blue, fontSize=11.sp)
                }
                Spacer(Modifier.height(8.dp))
                WeightTrendSummary(weights)
                Spacer(Modifier.height(8.dp))
                WeightChart(weights)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically) {
                    Column { Text(selectedDate.format(DateTimeFormatter.ofPattern("EEE, d MMM")), color=Color.White, fontWeight=FontWeight.SemiBold); Text(selectedWeight?.let { "Recorded: %.1f kg".format(it) } ?: "No weight recorded for this date", color=Muted, fontSize=12.sp) }
                    Row { if(selectedWeight!=null) TextButton(onClick={onDeleteWeight(selectedDate)}) { Text("Delete", color=Color(0xFFFF7777)) }; Button(onClick={weightText=selectedWeight?.toString() ?: ""; showWeightDialog=true}, colors=ButtonDefaults.buttonColors(containerColor=Green, contentColor=Bg), shape=RoundedCornerShape(12.dp)) { Text(if(selectedWeight==null) "Log weight" else "Edit weight") } }
                }
                Spacer(Modifier.height(12.dp))
                Text("Your data stays on this device", color=Muted, fontSize=10.sp, modifier=Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
    if(showGoalDialog) AlertDialog(onDismissRequest={showGoalDialog=false}, containerColor=Panel, title={Text("Set goal weight")}, text={OutlinedTextField(value=goalText,onValueChange={goalText=it},label={Text("Target weight in kg")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),singleLine=true)}, confirmButton={TextButton(onClick={goalText.toDoubleOrNull()?.takeIf { it in 25.0..350.0 }?.let { onSaveGoal(it); showGoalDialog=false }}) {Text("Save",color=Green)}}, dismissButton={TextButton(onClick={showGoalDialog=false}) {Text("Cancel")}})
    if(showWeightDialog) AlertDialog(onDismissRequest={showWeightDialog=false}, containerColor=Panel, title={Text("Log weight")}, text={ OutlinedTextField(value=weightText,onValueChange={weightText=it},label={Text("Weight in kg")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),singleLine=true) }, confirmButton={ TextButton(onClick={weightText.toDoubleOrNull()?.takeIf { it in 25.0..350.0 }?.let { onSaveWeight(selectedDate,it); showWeightDialog=false } }) { Text("Save", color=Green) } }, dismissButton={TextButton(onClick={showWeightDialog=false}) { Text("Cancel") } })
}

@Composable private fun GoalProgressCard(modifier: Modifier, current: Double?, target: Double?, start: Double?, onClick: () -> Unit) {
    val progress = if (current != null && target != null && start != null && start > target) ((start-current)/(start-target)).coerceIn(0.0,1.0).toFloat() else if (current != null && target != null && current <= target) 1f else 0f
    Column(modifier.background(Panel, RoundedCornerShape(18.dp)).clickable(onClick=onClick).padding(12.dp)) {
        Icon(Icons.Default.Flag, null, tint=Green, modifier=Modifier.size(18.dp)); Spacer(Modifier.height(4.dp))
        Text("GOAL PROGRESS", color=Muted, fontSize=8.sp, letterSpacing=0.7.sp)
        Box(Modifier.fillMaxWidth().height(54.dp), contentAlignment=Alignment.Center) {
            Canvas(Modifier.size(52.dp)) { drawArc(Color(0xFF30383A), -90f, 360f, false, style=androidx.compose.ui.graphics.drawscope.Stroke(5.dp.toPx())); drawArc(Green, -90f, 360f * progress, false, style=androidx.compose.ui.graphics.drawscope.Stroke(5.dp.toPx(), cap=StrokeCap.Round)) }
            Text("${(progress*100).toInt()}%", color=Color.White, fontSize=11.sp, fontWeight=FontWeight.Bold)
        }
        Text(target?.let { "Target %.1f".format(it) } ?: "Set goal", color=Color.White, fontSize=13.sp, fontWeight=FontWeight.Bold, maxLines=1)
        Text(if(current!=null && target!=null) "%.2f kg left".format((current-target).coerceAtLeast(0.0)) else "Tap to set", color=Green, fontSize=9.sp)
    }
}

@Composable private fun StatCard(modifier: Modifier, label:String, value:String, subtitle:String, accent:Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(modifier.background(Panel, RoundedCornerShape(18.dp)).padding(14.dp)) {
        Icon(icon, null, tint=accent, modifier=Modifier.size(22.dp)); Spacer(Modifier.height(12.dp)); Text(label, color=Muted, fontSize=9.sp, letterSpacing=1.sp); Text(value, color=Color.White, fontSize=22.sp, fontWeight=FontWeight.Bold, maxLines=1); Text(subtitle, color=accent, fontSize=10.sp)
    }
}

@Composable private fun WeightTrendSummary(weights: List<WeightEntry>) {
    val ordered = weights.sortedBy { it.date }
    val recent = ordered.takeLast(7).map { it.kilograms }
    val previous = ordered.dropLast(7).takeLast(7).map { it.kilograms }
    val delta = if (recent.isNotEmpty() && previous.isNotEmpty()) recent.average() - previous.average() else null
    Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(14.dp)).padding(13.dp), horizontalArrangement=Arrangement.SpaceBetween) {
        Column { Text("7-DAY WEIGHT TREND", color=Muted, fontSize=9.sp, letterSpacing=0.8.sp); Text(delta?.let { (if(it>0) "+" else "") + "%.2f kg vs prior week".format(it) } ?: "Need two weeks of entries", color=if(delta==null) Muted else if(delta<=0) Green else Color(0xFFFFB86B), fontSize=13.sp, fontWeight=FontWeight.SemiBold) }
        Text(if(recent.isNotEmpty()) "Avg %.2f kg".format(recent.average()) else "—", color=Color.White, fontSize=13.sp)
    }
}

@Composable private fun WeightChart(weights:List<WeightEntry>) {
    var rangeDays by remember { mutableIntStateOf(30) }
    val cutoff = LocalDate.now().minusDays(rangeDays.toLong()).toString()
    val visible = weights.filter { it.date >= cutoff }
    Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).padding(14.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.End) { listOf(7,30,90).forEach { days -> TextButton(onClick={rangeDays=days}, contentPadding=PaddingValues(horizontal=7.dp, vertical=0.dp)) { Text("${days}D", color=if(rangeDays==days) Green else Muted, fontSize=10.sp) } } }
        if(visible.size < 2) { Box(Modifier.fillMaxWidth().height(118.dp), contentAlignment=Alignment.Center) { Text("Log at least two weights to see your trend", color=Muted, fontSize=12.sp) } }
        else {
            val values=visible.map { it.kilograms }; val min=values.minOrNull()!!; val max=values.maxOrNull()!!; val range=(max-min).coerceAtLeast(0.5)
            Canvas(Modifier.fillMaxWidth().height(118.dp)) {
                val points=values.mapIndexed { i,v -> Offset(if(values.size==1) 0f else size.width*i/(values.size-1), size.height-12f-((v-min)/range).toFloat()*(size.height-24f)) }
                for(row in 0..2) { val y=size.height*row/2f; drawLine(Color(0xFF293235), Offset(0f,y), Offset(size.width,y), 1.dp.toPx()) }
                val path=Path().apply { points.forEachIndexed { i,p -> if(i==0) moveTo(p.x,p.y) else lineTo(p.x,p.y) } }
                drawPath(path, Green, style=androidx.compose.ui.graphics.drawscope.Stroke(width=3.dp.toPx(), cap=StrokeCap.Round))
                points.lastOrNull()?.let { drawCircle(Green, 4.dp.toPx(), it) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) { Text("%.1f kg".format(max), color=Muted, fontSize=10.sp); Text("%.1f kg".format(min), color=Muted, fontSize=10.sp) }
        }
    }
}
