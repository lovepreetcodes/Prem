package com.prem.daybyday

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.health.connect.client.PermissionController
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.prem.daybyday.data.*
import com.prem.daybyday.health.HealthConnectSteps
import com.prem.daybyday.ui.PremScreen
import com.prem.daybyday.ui.WorkoutScreen
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = WeightDatabase.get(this)
        val health = HealthConnectSteps(this)
        setContent {
            val weights by db.weightDao().observeAll().collectAsState(initial = emptyList())
            val goal by db.goalDao().observeGoal().collectAsState(initial = null)
            val sessions by db.workoutDao().observeSessions().collectAsState(initial = emptyList())
            var workoutHistory by remember { mutableStateOf<List<WorkoutSet>>(emptyList()) }
            var showWorkout by remember { mutableStateOf(false) }
            var steps by remember { mutableLongStateOf(0L) }
            var healthConnected by remember { mutableStateOf(false) }
            var healthMessage by remember { mutableStateOf("Connect Health Connect to read Samsung Health steps") }
            var selectedDate by remember { mutableStateOf(LocalDate.now()) }
            val scope = rememberCoroutineScope()
            val permissionLauncher = rememberLauncherForActivityResult(PermissionController.createRequestPermissionResultContract()) { granted ->
                healthConnected = granted.containsAll(health.permissions)
                if (healthConnected) scope.launch { runCatching { steps = health.stepsForDate(selectedDate); healthMessage = "Synced from Health Connect" }.onFailure { healthMessage = "Could not read steps. Open Health Connect and check permissions." } }
                else healthMessage = "Step permission not granted"
            }
            LaunchedEffect(selectedDate) {
                runCatching { if (health.availability() == androidx.health.connect.client.HealthConnectClient.SDK_AVAILABLE && health.hasPermission()) { healthConnected = true; steps = health.stepsForDate(selectedDate); healthMessage = if (selectedDate == LocalDate.now()) "Today's steps from Health Connect" else "Historical steps from Health Connect, when available" } }
                    .onFailure { healthMessage = "Health Connect is not ready. Check its permissions and Samsung Health sync." }
            }
            LaunchedEffect(showWorkout, sessions) { if (showWorkout) { val all = mutableListOf<WorkoutSet>(); for (session in sessions) all.addAll(db.workoutDao().setsForSession(session.id)); workoutHistory = all } }
            if (showWorkout) {
                WorkoutScreen(onBack = { showWorkout = false }, onSave = { type, sets -> scope.launch { db.workoutDao().saveWorkout(WorkoutSession(date = LocalDate.now().toString(), workoutType = type), sets); val all = mutableListOf<WorkoutSet>(); for (session in sessions) all.addAll(db.workoutDao().setsForSession(session.id)); workoutHistory = all; showWorkout = false } }, history = workoutHistory, sessionsCount = sessions.size)
            } else {
                PremScreen(
                    selectedDate = selectedDate, onDateSelected = { selectedDate = it }, weights = weights, steps = steps,
                    healthConnected = healthConnected, healthMessage = healthMessage,
                    onConnectHealth = { scope.launch { when (health.availability()) { androidx.health.connect.client.HealthConnectClient.SDK_AVAILABLE -> permissionLauncher.launch(health.permissions); else -> healthMessage = "Install or update Health Connect, then enable Samsung Health sharing." } } },
                    onRefreshSteps = { scope.launch { runCatching { steps = health.stepsForDate(selectedDate); healthMessage = "Synced from Health Connect" }.onFailure { healthMessage = "Unable to refresh steps" } } },
                    onSaveWeight = { date, kg -> scope.launch { db.weightDao().upsert(WeightEntry(date.toString(), kg)) } },
                    onDeleteWeight = { date -> scope.launch { db.weightDao().delete(date.toString()) } },
                    goalWeight = goal?.targetWeightKg,
                    goalStartWeight = goal?.startWeightKg,
                    onSaveGoal = { target -> scope.launch { val baseline = weights.lastOrNull()?.kilograms ?: target; db.goalDao().save(UserGoal(targetWeightKg = target, startWeightKg = baseline)) } },
                    onOpenWorkout = { showWorkout = true }
                )
            }
        }
    }
}
