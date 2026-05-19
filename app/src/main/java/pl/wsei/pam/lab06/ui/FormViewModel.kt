package pl.wsei.pam.lab06.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import pl.wsei.pam.lab06.AppTopBar
import pl.wsei.pam.lab06.data.CurrentDateProvider
import pl.wsei.pam.lab06.data.LocalDateConverter
import pl.wsei.pam.lab06.data.Priority
import pl.wsei.pam.lab06.data.TodoTask
import pl.wsei.pam.lab06.data.TodoTaskRepository
import java.time.LocalDate

class FormViewModel(private val repository: TodoTaskRepository, private val currentDateProvider: CurrentDateProvider) : ViewModel() {

    var todoTaskUiState by mutableStateOf(TodoTaskUiState())
        private set

    suspend fun save() {
        if (validate()) {
            repository.insertItem(todoTaskUiState.todoTask.toTodoTask())
        }
    }

    fun updateUiState(todoTaskForm: TodoTaskForm) {
        todoTaskUiState = TodoTaskUiState(todoTask = todoTaskForm, isValid = validate(todoTaskForm))
    }

    private fun validate(uiState: TodoTaskForm = todoTaskUiState.todoTask): Boolean {
        if(uiState.title.isBlank())
            return false;

        val today = currentDateProvider.currentDate

        if(uiState.deadline < LocalDateConverter.toMillis(today))
        return false;

        return true;
    }
}

data class TodoTaskUiState(
    var todoTask: TodoTaskForm = TodoTaskForm(),
    val isValid: Boolean = false
)

data class TodoTaskForm(
    val id: Int = 0,
    val title: String = "",
    val deadline: Long = LocalDateConverter.toMillis(LocalDate.now()),
    val isDone: Boolean = false,
    val priority: String = Priority.Low.name
)

fun TodoTaskForm.toTodoTask(): TodoTask = TodoTask(
    id = id,
    title = title,
    deadline = LocalDateConverter.fromMillis(deadline),
    isDone = isDone,
    priority = Priority.valueOf(priority)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoTaskInputForm(
    item: TodoTaskForm,
    onValueChange: (TodoTaskForm) -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Tytuł zadania", style = MaterialTheme.typography.labelLarge)
        OutlinedTextField(
            value = item.title,
            onValueChange = { onValueChange(item.copy(title = it)) },
            modifier = Modifier.fillMaxWidth()
        )
        
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = item.deadline,
        )
        var showDialog by remember { mutableStateOf(false) }

        Text("Termin", style = MaterialTheme.typography.labelLarge)
        OutlinedButton(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(LocalDateConverter.fromMillis(item.deadline).toString())
        }

        if (showDialog) {
            DatePickerDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog = false
                        datePickerState.selectedDateMillis?.let {
                            onValueChange(item.copy(deadline = it))
                        }
                    }) { Text("OK") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Text("Priorytet", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Priority.entries.forEach { p ->
                FilterChip(
                    selected = item.priority == p.name,
                    onClick = { onValueChange(item.copy(priority = p.name)) },
                    label = { Text(p.name) }
                )
            }
        }
    }
}

@Composable
fun FormScreen(
    navController: NavController,
    viewModel: FormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            AppTopBar(
                navController = navController,
                title = "Dodaj zadanie",
                showBackIcon = true,
                route = "form", // Ustawiamy na "form", aby pokazać przycisk Zapisz
                onSaveClick = {
                    coroutineScope.launch {
                        viewModel.save()
                        navController.popBackStack()
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            TodoTaskInputForm(
                item = viewModel.todoTaskUiState.todoTask,
                onValueChange = viewModel::updateUiState
            )
        }
    }
}
