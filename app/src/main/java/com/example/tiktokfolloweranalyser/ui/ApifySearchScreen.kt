package com.example.tiktokfolloweranalyser.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.tiktokfolloweranalyser.data.apify.ApifyFollowerItem
import com.example.tiktokfolloweranalyser.data.apify.ApifyRepository
import com.example.tiktokfolloweranalyser.data.apify.AvatarThumb
import kotlinx.coroutines.launch

@Composable
fun ApifySearchScreen() {
    // Token is now hardcoded in Repository
    var handle by remember { mutableStateOf("") }
    var maxFollowers by remember { mutableStateOf("20") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchResults by remember { mutableStateOf<List<ApifyFollowerItem>>(emptyList()) }
    
    val scope = rememberCoroutineScope()
    // In a real app, Repository should be injected or singleton
    val repository = remember { ApifyRepository() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        
        Text("Scrape User Followers", style = MaterialTheme.typography.headlineSmall)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TextField(
            value = handle,
            onValueChange = { handle = it },
            label = { Text("TikTok Handle (e.g. samsulkarim59)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = maxFollowers,
            onValueChange = { 
                if (it.all { char -> char.isDigit() }) {
                    maxFollowers = it 
                }
            },
            label = { Text("Max Followers (Target)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                if (handle.isBlank()) {
                    errorMessage = "Please enter a TikTok Handle"
                    return@Button
                }
                val maxCount = maxFollowers.toIntOrNull() ?: 20
                
                isLoading = true
                errorMessage = null
                scope.launch {
                    val result = repository.scrapeFollowers(handle, maxCount)
                    result.onSuccess { items ->
                         searchResults = items
                    }.onFailure { error ->
                        errorMessage = error.message
                    }
                    isLoading = false
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Scrape Followers")
            }
        }
        
        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Error: $it", color = MaterialTheme.colorScheme.error)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(searchResults) { item ->
                ApifyItemCard(item)
            }
        }
    }
}

@Composable
fun ApifyItemCard(item: ApifyFollowerItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            // url_list is a Map<String, String> in the JSON (e.g. "0": "url")
            val avatarUrl = item.avatarThumb?.urlList?.values?.firstOrNull()
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = item.nickname, style = MaterialTheme.typography.titleMedium)
                Text(text = "@${item.uniqueId}", style = MaterialTheme.typography.bodyMedium)
                item.followerCount?.let { count ->
                    Text(text = "Followers: $count", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
