package com.cremcashcamfin.collateralappraiser

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.cremcashcamfin.collateralappraiser.helper.DBHelper
import com.cremcashcamfin.collateralappraiser.helper.ImageHelper
import com.cremcashcamfin.collateralappraiser.helper.MapHelper
import com.cremcashcamfin.collateralappraiser.ui.theme.CollateralAppraiserTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class CollateralActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CollateralAppraiserTheme {
                val clientName = intent.getStringExtra("client_name") ?: "Unknown Client"
                val controlNo = intent.getStringExtra("control_no") ?: "Unknown Control No"
                val indID = intent.getStringExtra("client_id") ?: "Unknown ID"

                var colClass by remember { mutableStateOf("Loading...") }

                val scrollState = rememberScrollState()

                val context = LocalContext.current
                var hasLocationPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                }

                var hasCameraPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                }

                val cameraPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->
                    hasCameraPermission = isGranted
                }

                val locationPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->
                    hasLocationPermission = isGranted
                }

                LaunchedEffect(Unit) {
                    if (!hasLocationPermission) {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                    if (!hasCameraPermission) {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }


                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text(text = "Collateral Details") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp)
                            .verticalScroll(scrollState)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        var propertyLocation by remember { mutableStateOf<Location?>(null) }
                        ClientInfoCard(indID = indID, name = clientName, controlNo = controlNo, colClass = colClass)
                        if (hasLocationPermission) {
                            CurrentLocationMapCard(onLocationReceived = { loc ->
                                propertyLocation = loc
                                Toast.makeText(
                                    context,
                                    "${loc?.latitude} ${loc?.longitude}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            })
                        } else {
                            Text("Location permission is required to show the map.")
                        }

                        if (propertyLocation != null) {
                            PhotoCaptureCard(
                                colClass = colClass,
                                controlNo = controlNo,
                                indID = indID,
                                propLatitude = propertyLocation!!.latitude,
                                propLongitude = propertyLocation!!.longitude
                            )
                        } else {
                            Text("Waiting for location before capturing photo...")
                        }
                    }
                }

                LaunchedEffect(controlNo) {
                    withContext(Dispatchers.IO) {
                        val result = DBHelper.getCollateralClass(controlNo)
                        withContext(Dispatchers.Main) {
                            colClass = result ?: "Unknown"
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun ClientInfoCard(indID: String, name: String, controlNo: String, colClass: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                InfoItem(icon = Icons.Default.AssignmentInd, label = "ID", value = indID)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                InfoItem(icon = Icons.Default.Person, label = "Client Name", value = name)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                InfoItem(icon = Icons.Default.Badge, label = "Control No.", value = controlNo)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                InfoItem(icon = Icons.Default.Assignment, label = "Collateral Class", value = colClass)
            }
        }
    }
}

@Composable
fun InfoItem(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun CurrentLocationMapCard(modifier: Modifier = Modifier, onLocationReceived: (Location?) -> Unit) {
    val context = LocalContext.current
    var location by remember { mutableStateOf<Location?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(Unit) {
        val current = MapHelper.getCurrentLocation(context)
        location = current
        isLoading = false
        onLocationReceived(current)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            location != null -> {
                val latLng = LatLng(location!!.latitude, location!!.longitude)
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    Marker(state = rememberMarkerState(position = latLng))
                }

                LaunchedEffect(Unit) {
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                }
            }
            else -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Failed to load location")
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun PhotoCaptureCard(modifier: Modifier = Modifier, colClass: String, controlNo: String, indID: String, propLatitude: Double, propLongitude: Double) {
    val context = LocalContext.current
    val activity = context as? Activity

    val empID = activity?.intent?.getStringExtra("emp_id") ?: "Unknown"

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var currentLocation by remember { mutableStateOf<Location?>(null) }
    val photoLocations = remember { mutableStateListOf<Location>() }
    var totalDistanceMeters  by remember { mutableStateOf(0.0) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && imageUri != null) {
            val source = ImageDecoder.createSource(context.contentResolver, imageUri!!)
            bitmap = ImageDecoder.decodeBitmap(source)

            if (colClass == "REM") {
                currentLocation?.let { location ->
                    photoLocations.add(location)
                    if (photoLocations.size >= 2) {
                        var distance = 0.0
                        for (i in 0 until photoLocations.size - 1) {
                            val start = photoLocations[i]
                            val end = photoLocations[i + 1]
                            distance += start.distanceTo(end)
                        }
                        totalDistanceMeters += distance
                    }
                }
            }
        }
        isLoading = false
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "📸 Collateral Photo",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Photo Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    ) {
                        MapHelper.fetchPropertyLocation(context, fusedLocationClient) { location ->
                            if (location != null) {
                                currentLocation = location
                                val uri = ImageHelper.createImageUri(context)
                                imageUri = uri
                                isLoading = true
                                uri?.let { launcher.launch(it) }
                            } else {
                                Toast.makeText(context, "Unable to get location", Toast.LENGTH_SHORT).show()
                            }
                        }

                    } else {
                        Toast.makeText(context, "Camera and Location permission are required", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = title.isNotBlank() && description.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Take Photo")
            }

            Spacer(modifier = Modifier.height(20.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                bitmap != null -> {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = "Captured Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            if (bitmap != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        if (title.isNotBlank() && description.isNotBlank()) {
                            val ext = ImageHelper.getExtensionFromUri(context, imageUri!!) ?: "jpg"
                            val imageBytes = ImageHelper.bitmapToByteArray(bitmap!!)
                            val timestamp = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                            val fileName = "COLL${indID}_${colClass.uppercase()}_${timestamp}_${title}.$ext"

                            val latitude = currentLocation?.latitude
                            val longitude = currentLocation?.longitude

                            CoroutineScope(Dispatchers.IO).launch {
                                val success = DBHelper.saveCollateralPhoto(
                                    colClass = colClass,
                                    indID = indID,
                                    controlNo = controlNo,
                                    filename = fileName,
                                    title = title,
                                    description = description,
                                    ext = ext,
                                    imageBytes = imageBytes,
                                    latitude = latitude,
                                    longitude = longitude
                                )

                                if (colClass == "REM"){
                                    if (success) {
                                        DBHelper.savePropertyCoordinate(
                                            indID = indID,
                                            controlNo = controlNo,
                                            latitude = propLatitude,
                                            longitude = propLongitude,
                                            empID = empID
                                        )
                                    }
                                }

                                withContext(Dispatchers.Main) {
                                    if (success) {
                                        Toast.makeText(context, "Saved successfully!", Toast.LENGTH_SHORT).show()
                                        title = ""
                                        description = ""
                                        imageUri = null
                                        bitmap = null
                                        isLoading = false
                                    } else {
                                        Toast.makeText(context, "Failed to save.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save to Database")
                }
            }

            if (colClass == "REM") {
                if (photoLocations.size > 1) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ){
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Distance Tracker",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Total distance walked around property: %.2f meters".format(totalDistanceMeters),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CollateralAppraiserTheme {
        ClientInfoCard(indID = "001", name = "Juan Dela Cruz", controlNo = "CTRL-001", colClass = "REM")
    }
}
