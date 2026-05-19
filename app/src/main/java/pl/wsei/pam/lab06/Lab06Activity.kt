package pl.wsei.pam.lab06

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import pl.wsei.pam.lab06.data.Priority
import pl.wsei.pam.lab06.data.TodoTask
import pl.wsei.pam.lab06.ui.AppViewModelProvider
import pl.wsei.pam.lab06.ui.FormScreen
import pl.wsei.pam.lab06.ui.ListViewModel
import pl.wsei.pam.lab06.ui.theme.Lab01Theme
import pl.wsei.pam.MainActivity.Companion.container

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab01Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ListScreen(
    navController: NavController,
    viewModel: ListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // Obserwujemy strumień danych z bazy danych przez ViewModel
    val uiState by viewModel.listUiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                shape = CircleShape,
                onClick = { navController.navigate("form") },
                content = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add task",
                        modifier = Modifier.scale(1.5f)
                    )
                }
            )
        },
        topBar = {
            AppTopBar(
                navController = navController,
                title = "Lista zadań",
                showBackIcon = false,
                route = "list",
            )
        },
        content = { paddingValues ->
            if (uiState.items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("Brak zadań w bazie danych.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(8.dp)
                ) {
                    items(items = uiState.items, key = { it.id }) { item ->
                        ListItem(
                            item = item,
                            onCheckedChange = { isDone ->
                                // Zapisujemy zmianę stanu bezpośrednio w bazie danych
                                viewModel.updateTask(item, isDone)
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
fun MainScreen() {
    val postNotificationPermission =
        rememberPermissionState(
            permission = Manifest.permission.POST_NOTIFICATIONS
        )

    LaunchedEffect(key1 = true) {

        if (!postNotificationPermission.status.isGranted) {

            postNotificationPermission.launchPermissionRequest()
        }
    }

    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "list") {
        composable("list") { 
            ListScreen(navController = navController) 
        }
        composable("form") { 
            // Korzystamy z FormScreen (zdefiniowanego w FormViewModel.kt), który obsługuje zapis do Room
            FormScreen(navController = navController) 
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    navController: NavController,
    title: String,
    showBackIcon: Boolean,
    route: String,
    onSaveClick: () -> Unit = { }
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary
        ),
        title = { Text(text = title) },
        navigationIcon = {
            if (showBackIcon) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        actions = {
            // Przycisk "Zapisz" widoczny tylko na ekranie formularza
            // W FormScreen() z FormViewModel.kt route paska ustawiony jest tak, by przycisk się pojawił
            if (title == "Dodaj zadanie" || route == "form") {
                OutlinedButton(
                    onClick = onSaveClick,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "Zapisz",
                        fontSize = 14.sp
                    )
                }
            }else {
                IconButton(onClick = {
                    container.notificationHandler.showSimpleNotification()
                }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Ustawienia",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Główna",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

        }
    )
}

@Composable
fun ListItem(item: TodoTask, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Termin: ${item.deadline}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Priorytet: ${item.priority}",
                    style = MaterialTheme.typography.bodySmall,
                    color = when (item.priority) {
                        Priority.High -> Color.Red
                        Priority.Medium -> Color(0xFFFFA500)
                        Priority.Low -> Color.Green
                    }
                )
            }

            if (item.isDone) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Done",
                    tint = Color.Green,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }

            Checkbox(
                checked = item.isDone,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
