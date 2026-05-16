package com.tripmate.app.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.tripmate.app.network.GoogleMapsService
import com.tripmate.app.network.PlaceCandidate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import com.tripmate.app.ui.components.SectionHeader
import com.tripmate.app.ui.components.HeaderIconButton

private const val GOOGLE_API_KEY = "AIzaSyAYxvfsXjC-sRwnlCjFcy_4DqnJK0snncg"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var currentCity by remember { mutableStateOf("Locating...") }
    var nearbyGems by remember { mutableStateOf<List<GemPlace>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedGem by remember { mutableStateOf<GemPlace?>(null) }
    var showGemSheet by remember { mutableStateOf(false) }

    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val mapsService = remember { retrofit.create(GoogleMapsService::class.java) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            scope.launch {
                val location = getCurrentLocation(context)
                userLocation = location
                if (location != null) {
                    fetchCityAndGems(context, location, mapsService) { city, gems ->
                        currentCity = city
                        nearbyGems = gems
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        // We wait for the user to click the button to request location
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .offset(x = 200.dp, y = (-100).dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .offset(x = (-100).dp, y = 400.dp)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f), CircleShape)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 120.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 64.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        "EXPLORE THE WORLD",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    "Discover Your\nNext Adventure",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    lineHeight = 48.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                placeholder = { Text("Search for destinations, hotels...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            val isSearching = searchQuery.isNotBlank()
            
            // 1. Trending Section (Filtered)
            data class TrendingItem(val name: String, val image: String, val days: String, val locations: String, val cost: String, val costLocal: String, val currency: String)
            val trending = listOf(
                TrendingItem("Bali", "https://images.unsplash.com/photo-1537996194471-e657df975ab4", "7", "12", "₹66,000", "IDR 12M", "IDR"),
                TrendingItem("Kyoto", "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e", "5", "8", "₹99,000", "JPY 180K", "JPY"),
                TrendingItem("Rome", "https://images.unsplash.com/photo-1552832230-c0197dd311b5", "6", "15", "₹1,08,000", "EUR 1,200", "EUR")
            ).filter { !isSearching || it.name.contains(searchQuery, ignoreCase = true) }

            if (trending.isNotEmpty()) {
                SectionHeader(if (isSearching) "Search Results in Trending" else "Trending Now")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(trending) { item ->
                        TrendingCard(item.name, item.image, item.days, item.locations, item.cost) {
                            selectedGem = GemPlace(
                                item.name, 
                                "A breathtaking journey through ${item.name}. Experience the culture, local food, and hidden spots with this curated premium package.", 
                                item.image, 0.0, 0.0,
                                days = item.days,
                                areas = item.locations,
                                costINR = item.cost,
                                costLocal = item.costLocal,
                                currencyCode = item.currency
                            )
                            showGemSheet = true
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // 2. Categories Section
            SectionHeader("Travel Categories")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                val categories = listOf("Beaches","Mountains","Castles","Adventure","Food")
                items(categories) { cat ->
                    CategoryIcon(
                        icon = when(cat) {
                            "Beaches" -> Icons.Default.BeachAccess
                            "Mountains" -> Icons.Default.Landscape
                            "Castles" -> Icons.Default.Castle
                            "Adventure" -> Icons.Default.Hiking
                            else -> Icons.Default.Restaurant
                        },
                        label = cat,
                        selected = selectedCategory == cat,
                        onClick = {
                            selectedCategory = if (selectedCategory == cat) null else cat
                            searchQuery = selectedCategory ?: ""
                            val mocks = generateGemsForCategory(cat)
                            nearbyGems = mocks
                            com.tripmate.app.data.MockDataProvider.showMessage("Loaded ${mocks.size} $cat places")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 3. Hidden Gems Section (Filtered)
            val filteredGems = nearbyGems.filter { !isSearching || it.name.contains(searchQuery, ignoreCase = true) }
            
            SectionHeader(if (isSearching) "Local Results" else "Hidden Gems Near You")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clickable(enabled = !isLoading) {
                        isLoading = true
                        scope.launch {
                            // 1. Check Permission
                            val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            val hasCoarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            
                            if (!hasFineLocation && !hasCoarseLocation) {
                                com.tripmate.app.data.MockDataProvider.showMessage("Requesting Location Permission...")
                                locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                                isLoading = false
                                return@launch
                            }

                            // 2. Fetch Accurate Location
                            com.tripmate.app.data.MockDataProvider.showMessage("Searching for precise location...")
                            val location = getCurrentLocation(context)
                            userLocation = location
                            
                            if (location != null) {
                                fetchCityAndGems(context, location, mapsService) { city, gems ->
                                    currentCity = city
                                    nearbyGems = gems
                                    isLoading = false
                                    com.tripmate.app.data.MockDataProvider.showMessage("Success! Discovered gems in $city")
                                }
                            } else {
                                isLoading = false
                                com.tripmate.app.data.MockDataProvider.showMessage("Failed to get accurate location. Try again.")
                                // Fallback if user insists
                                if (currentCity == "Locating...") currentCity = "Chennai"
                            }
                        }
                    },
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp), 
                            color = MaterialTheme.colorScheme.onPrimaryContainer, 
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.MyLocation, 
                            null, 
                            modifier = Modifier.size(32.dp), 
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(Modifier.width(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (isLoading) "Detecting Location..." else if (currentCity == "Locating...") "Find Gems Near You" else "Gems in $currentCity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            if (isLoading) "Fetching precise coordinates..." else "AI-powered real-time recommendations",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            if (filteredGems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredGems) { gem ->
                        GemSmallCard(gem) {
                            selectedGem = gem
                            showGemSheet = true
                        }
                    }
                }
            } else if (isSearching && trending.isEmpty()) {
                // No results state
                Column(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                    Spacer(Modifier.height(16.dp))
                    Text("No places found for \"$searchQuery\"", color = MaterialTheme.colorScheme.secondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    TextButton(onClick = { searchQuery = "" }) { Text("Clear Search") }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 4. Personalized Section
            val suggestions = listOf(
                Triple("Hike the Alps", "Based on your interest in mountains", "Alps"),
                Triple("Sushi in Tokyo", "Recommended for foodies", "Tokyo"),
                Triple("Jazz in New Orleans", "Matches your music taste", "New Orleans")
            ).filter { !isSearching || it.first.contains(searchQuery, ignoreCase = true) || it.third.contains(searchQuery, ignoreCase = true) }

            if (suggestions.isNotEmpty()) {
                SectionHeader("Personalized for You")
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    suggestions.forEach { (title, subtitle, _) ->
                        SuggestionCard(title, subtitle) {
                            val trips = com.tripmate.app.data.MockDataProvider.trips
                            if (trips.isNotEmpty()) {
                                com.tripmate.app.data.MockDataProvider.addEvent(
                                    com.tripmate.app.models.Event(System.currentTimeMillis().toString(), trips.first().id, title, "TBD", "TBD")
                                )
                                com.tripmate.app.data.MockDataProvider.showMessage("Added $title to your trip!")
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(120.dp))
        }

        if (showGemSheet && selectedGem != null) {
            ModalBottomSheet(
                onDismissRequest = { showGemSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                GemDetailContent(selectedGem!!)
            }
        }
    }
}


@Composable
fun GemDetailContent(gem: GemPlace) {
    var showTripSelection by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
    ) {
        AsyncImage(
            model = gem.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(gem.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Text(gem.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = {
                    // Open in Google Maps app (fallback to browser)
                    val gmmIntentUri = Uri.parse("geo:${gem.lat},${gem.lng}?q=${Uri.encode(gem.name)}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    try {
                        context.startActivity(mapIntent)
                    } catch (e: Exception) {
                        // fallback to browser if Maps app not installed
                        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(gem.name)}")
                        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Directions, null)
                Spacer(Modifier.width(8.dp))
                Text("Open in Maps")
            }
            Button(
                onClick = { showTripSelection = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Add to Trip")
            }
        }
        
        if (gem.days != null) {
            Spacer(modifier = Modifier.height(32.dp))
            Text("TRAVEL INSIGHTS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Duration", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            Text("${gem.days} Days", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Locations", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            Text("${gem.areas} Spots", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Best Time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            Text("Nov - Mar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CurrencyExchange, null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Budget Conversion", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(gem.costLocal ?: "", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp).padding(horizontal = 4.dp), tint = MaterialTheme.colorScheme.primary)
                                Text(gem.costINR ?: "", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("TripMate AI: Optimized for 1 ${gem.currencyCode} = ₹${if (gem.name == "Bali") "0.0055" else if (gem.name == "Kyoto") "0.55" else "90"}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }

    if (showTripSelection) {
        val trips = com.tripmate.app.data.MockDataProvider.trips
        AlertDialog(
            onDismissRequest = { showTripSelection = false },
            title = { Text("Select Trip to Add Place") },
            text = {
                if (trips.isEmpty()) {
                    Text("No active trips. Please create a trip first.")
                } else {
                    LazyColumn {
                        items(trips) { trip ->
                            TextButton(
                                onClick = { 
                                    val newEvent = com.tripmate.app.models.Event(
                                        id = System.currentTimeMillis().toString(),
                                        tripId = trip.id,
                                        title = "Visit ${gem.name}",
                                        date = "TBD",
                                        time = "TBD"
                                    )
                                    com.tripmate.app.data.MockDataProvider.addEvent(newEvent)
                                    com.tripmate.app.data.MockDataProvider.showMessage("${gem.name} added to ${trip.destination}")
                                    showTripSelection = false 
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(trip.destination, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showTripSelection = false }) { Text("Cancel") } }
        )
    }
}

private suspend fun fetchCityAndGems(
    context: android.content.Context,
    location: Location, 
    service: GoogleMapsService,
    onResult: (String, List<GemPlace>) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("ExploreScreen", "Fetching city for ${location.latitude},${location.longitude}")
            val geocodeRes = service.reverseGeocode("${location.latitude},${location.longitude}", GOOGLE_API_KEY)
            if (!geocodeRes.isSuccessful) {
                android.util.Log.e("ExploreScreen", "Geocoding failed: ${geocodeRes.errorBody()?.string()}")
            }
            val results = geocodeRes.body()?.results ?: emptyList()
            val firstResult = results.firstOrNull()
            
            // Try to find the most specific city/area name from Google Results
            var city = firstResult?.addressComponents?.let { components ->
                components.find { it.types.contains("sublocality_level_1") }?.longName
                    ?: components.find { it.types.contains("locality") }?.longName
                    ?: components.find { it.types.contains("administrative_area_level_2") }?.longName
                    ?: components.find { it.types.contains("administrative_area_level_1") }?.longName
            } ?: firstResult?.addressComponents?.firstOrNull()?.longName ?: "Unknown City"

            // ULTIMATE FALLBACK: Use Android system Geocoder if Google API returned "Unknown"
            if (city == "Unknown City") {
                try {
                    val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val geoCity = addresses?.firstOrNull()?.let { addr ->
                        addr.locality ?: addr.subAdminArea ?: addr.adminArea
                    }
                    if (geoCity != null) city = geoCity
                } catch (e: Exception) {
                    android.util.Log.e("ExploreScreen", "System Geocoder failed", e)
                }
            }

            android.util.Log.d("ExploreScreen", "City found: $city, fetching gems...")
            fetchGemsForCity(city, service) { gems ->
                onResult(city, gems)
            }
        } catch (e: Exception) {
            android.util.Log.e("ExploreScreen", "Error in fetchCityAndGems", e)
        }
    }
}

private suspend fun fetchGemsForCity(
    city: String,
    service: GoogleMapsService,
    onResult: (List<GemPlace>) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("ExploreScreen", "Fetching gems for $city")
            val placeNames = getSimulatedAIPlaces(city)

            val gems = placeNames.map { aiPlace ->
                android.util.Log.d("ExploreScreen", "Finding place: ${aiPlace.name}")
                val placeRes = service.findPlaceFromText(aiPlace.name, apiKey = GOOGLE_API_KEY)
                val body = placeRes.body()
                android.util.Log.d("ExploreScreen", "API Status for ${aiPlace.name}: ${body?.status ?: "NULL"}")
                val candidate = body?.candidates?.firstOrNull()
                
                if (candidate == null) {
                    android.util.Log.w("ExploreScreen", "No candidate found for ${aiPlace.name}, status: ${body?.status}")
                }

                val photoRef = candidate?.photos?.firstOrNull()?.photoReference
                val imageUrl = if (photoRef != null) {
                    "https://maps.googleapis.com/maps/api/place/photo?maxwidth=800&photoreference=$photoRef&key=$GOOGLE_API_KEY"
                } else {
                    "https://images.unsplash.com/photo-1469474968028-56623f02e42e?auto=format&fit=crop&w=800&q=80"
                }

                GemPlace(
                    name = candidate?.name ?: aiPlace.name,
                    description = aiPlace.description,
                    imageUrl = imageUrl,
                    lat = candidate?.geometry?.location?.lat ?: 0.0,
                    lng = candidate?.geometry?.location?.lng ?: 0.0
                )
            }

            withContext(Dispatchers.Main) {
                onResult(gems)
            }
        } catch (e: Exception) {
            android.util.Log.e("ExploreScreen", "Error in fetchGemsForCity", e)
        }
    }
}

private fun getSimulatedAIPlaces(city: String): List<AIPlaceResponse> {
    return listOf(
        AIPlaceResponse("Marina Beach", "One of the longest urban beaches in the world."),
        AIPlaceResponse("Shore Temple", "Historic architectural marvel by the sea."),
        AIPlaceResponse("Vedanthangal Bird Sanctuary", "A paradise for bird watchers."),
        AIPlaceResponse("Mahabalipuram", "Famous for its intricate rock carvings."),
        AIPlaceResponse("VGP Universal Kingdom", "Popular theme park for family fun.")
    )
}

@SuppressLint("MissingPermission")
private suspend fun getCurrentLocation(context: Context): Location? {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    return try {
        withContext(Dispatchers.IO) {
            // 1. Try to get a fresh accurate location
            val location = com.google.android.gms.tasks.Tasks.await(
                fusedLocationClient.getCurrentLocation(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                    null
                )
            )
            
            // 2. Fallback to last known location if fresh location fails
            location ?: com.google.android.gms.tasks.Tasks.await(fusedLocationClient.lastLocation)
        }
    } catch (e: Exception) {
        android.util.Log.e("ExploreScreen", "Error getting current location", e)
        null
    }
}

@Composable
fun TrendingCard(
    name: String, 
    imageUrl: String, 
    days: String, 
    locations: String, 
    cost: String, 
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .size(200.dp, 280.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box {
            AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)))))
            
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp)) {
                    Text("Trending", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
                Spacer(Modifier.height(8.dp))
                Text(name, color = Color.White, fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineSmall)
                
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, modifier = Modifier.size(12.dp), tint = Color.White.copy(alpha = 0.7f))
                    Spacer(Modifier.width(4.dp))
                    Text("$days Days", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.Default.Place, null, modifier = Modifier.size(12.dp), tint = Color.White.copy(alpha = 0.7f))
                    Spacer(Modifier.width(4.dp))
                    Text("$locations Areas", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                }
                
                Spacer(Modifier.height(4.dp))
                Text("From $cost", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primaryContainer, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CategoryIcon(icon: ImageVector, label: String, selected: Boolean = false, onClick: () -> Unit = {}) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier
                .size(72.dp)
                .clickable { onClick() },
            shape = RoundedCornerShape(22.dp),
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            shadowElevation = if (selected) 8.dp else 4.dp,
            tonalElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon, 
                    contentDescription = null, 
                    modifier = Modifier.size(32.dp), 
                    tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            label, 
            style = MaterialTheme.typography.labelMedium, 
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, 
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun GemSmallCard(gem: GemPlace, onClick: () -> Unit) {
    Card(
        modifier = Modifier.size(width = 140.dp, height = 180.dp).clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column {
            AsyncImage(model = gem.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)), contentScale = ContentScale.Crop)
            Column(modifier = Modifier.padding(12.dp)) {
                Text(gem.name, style = MaterialTheme.typography.labelLarge, maxLines = 1, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("Explore", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun SuggestionCard(title: String, subtitle: String, onAdd: () -> Unit = {}) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        tonalElevation = 4.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
            IconButton(onClick = { onAdd() }, colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))) {
                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

data class GemPlace(
    val name: String, 
    val description: String, 
    val imageUrl: String, 
    val lat: Double, 
    val lng: Double,
    val days: String? = null,
    val areas: String? = null,
    val costLocal: String? = null,
    val costINR: String? = null,
    val currencyCode: String? = null
)
data class AIPlaceResponse(val name: String, val description: String)

private fun generateGemsForCategory(category: String): List<GemPlace> {
    return when(category) {
        "Beaches" -> listOf(
            GemPlace("Marina Beach", "Sandy urban beach", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e", 13.0455, 80.2820),
            GemPlace("Radhanagar Beach", "Andaman's dreamy white sand", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800", 11.6594, 92.7378)
        )
        "Mountains" -> listOf(
            GemPlace("Rohtang Pass", "Snowy mountain pass", "https://images.unsplash.com/photo-1501785888041-af3ef285b470", 32.3790, 77.2806),
            GemPlace("Kedarkantha", "Scenic trek in Himalayas", "https://images.unsplash.com/photo-1501785888041-af3ef285b470?auto=format&fit=crop&w=800", 31.2298, 78.2701)
        )
        "Castles" -> listOf(
            GemPlace("Bangalore Palace", "Historic royal palace", "https://images.unsplash.com/photo-1505691723518-36a69b6e2f5f", 12.9980, 77.5928),
            GemPlace("Bhangarh Fort", "Mysterious ruined fort", "https://images.unsplash.com/photo-1505691723518-36a69b6e2f5f?auto=format&fit=crop&w=800", 27.0965, 76.2770)
        )
        "Adventure" -> listOf(
            GemPlace("Valley of Flowers", "Alpine meadows and flowers", "https://images.unsplash.com/photo-1441974231531-c6227db76b6e", 30.7289, 79.6056),
            GemPlace("Zanskar River", "White water rafting", "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?auto=format&fit=crop&w=800", 33.46, 76.02)
        )
        "Food" -> listOf(
            GemPlace("Chennai Street Food", "Local favorites and snacks", "https://images.unsplash.com/photo-1541542684-2f0e551d5b6f", 13.0827, 80.2707),
            GemPlace("Kolkata Biryani", "Famous regional biryani", "https://images.unsplash.com/photo-1541542684-2f0e551d5b6f?auto=format&fit=crop&w=800", 22.5726, 88.3639)
        )
        else -> listOf(
            GemPlace("Local Spot", "Hidden local favorite", "https://images.unsplash.com/photo-1469474968028-56623f02e42e?auto=format&fit=crop&w=800&q=80", 0.0, 0.0)
        )
    }
}
