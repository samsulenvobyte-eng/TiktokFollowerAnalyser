package com.example.tiktokfolloweranalyser.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.tiktokfolloweranalyser.data.TikTokUserData
import com.example.tiktokfolloweranalyser.data.TikTokRepository
import com.example.tiktokfolloweranalyser.data.User
import com.example.tiktokfolloweranalyser.domain.AnalysisLogic
import kotlinx.coroutines.launch

import com.example.tiktokfolloweranalyser.data.database.TikTokDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TikTokDashboardScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { TikTokDatabase.getDatabase(context) }
    val repository = remember { TikTokRepository(db.tikTokDao()) }
    
    var selectedTab by remember { mutableStateOf(0) }
    
    var userData by remember { mutableStateOf<TikTokUserData?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                isLoading = true
                errorMessage = null
                scope.launch {
                    val result = repository.processZipFile(it, context.contentResolver)
                    result.onSuccess { data ->
                        userData = data
                    }.onFailure { error ->
                        errorMessage = error.message
                    }
                    isLoading = false
                }
            }
        }
    )
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("TikTok Analyser") }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Import") },
                    label = { Text("Import") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Scrape") },
                    label = { Text("Scrape") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (selectedTab == 0) {
                // Import Tab Logic
                if (userData == null) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(onClick = { filePickerLauncher.launch(arrayOf("application/zip")) }) {
                            Text("Load TikTok Data (Zip)")
                        }
                        if (isLoading) {
                            Spacer(modifier = Modifier.height(16.dp))
                            CircularProgressIndicator()
                        }
                        errorMessage?.let {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "Error: $it", color = MaterialTheme.colorScheme.error)
                        }
                    }
                } else {
                    DashboardContent(userData!!) {
                        userData = null // Reset
                    }
                }
            } else {
                // Scrape Tab Logic
                ApifySearchScreen()
            }
        }
    }
}

@Composable
fun DashboardContent(userData: TikTokUserData, onReset: () -> Unit) {
    val profile = userData.profileAndSettings.profileInfo.profileMap
    val notFollowingBack = remember(userData) { AnalysisLogic.getNotFollowingBack(userData) }
    val following = userData.profileAndSettings.following.followingList ?: emptyList()
    var selectedTab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header Stats
        Card(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(profile.profilePhoto)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = "Username: ${profile.userName}", style = MaterialTheme.typography.titleMedium)
                        Text(text = "Region: ${profile.accountRegion}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Followers: ${profile.followerCount}")
                    Text("Following: ${profile.followingCount}")
                    Text("Not Back: ${notFollowingBack.size}")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onReset) {
                    Text("Load New File")
                }
            }
        }

        // Tabs
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Not Following Back") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Following (${following.size})") })
        }

        // List
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val listToShow = if (selectedTab == 0) notFollowingBack else following
            items(listToShow) { user ->
                UserItem(user)
            }
        }
    }
}

@Composable
fun UserItem(user: User) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .padding(4.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = user.userName, style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Since: ${user.date}", style = MaterialTheme.typography.bodySmall)
                }
            }
            Button(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tiktok.com/@${user.userName}"))
                context.startActivity(intent)
            }) {
                Text("Unfollow")
            }
        }
    }
}
