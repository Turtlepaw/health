import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Map
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.wear.compose.material.AppCard
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewSmallRound
import com.turtlepaw.heartconnect.presentation.theme.ExerciseTheme
import org.mapsforge.core.graphics.Paint
import org.mapsforge.core.graphics.Style
import org.mapsforge.core.model.BoundingBox
import org.mapsforge.core.model.LatLong
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.android.util.AndroidUtil
import org.mapsforge.map.android.view.MapView
import org.mapsforge.map.layer.cache.TileCache
import org.mapsforge.map.layer.overlay.Polyline
import org.mapsforge.map.layer.renderer.TileRendererLayer
import org.mapsforge.map.reader.MapFile
import org.mapsforge.map.rendertheme.internal.MapsforgeThemes
import java.io.File

@Composable
fun OfflineRouteMap(
    context: Context,
    mapFilePath: String, // Path to .map file
    route: List<Pair<Double, Double>>, // Route coordinates
    modifier: Modifier = Modifier
) {
    AndroidGraphicFactory.createInstance(context.applicationContext) // Init Mapsforge

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp), // Constrain to Card size
        factory = { ctx ->
            MapView(ctx).apply {
                isClickable = false // Disable user interaction
                val mapFile = File(mapFilePath)

                // Initialize tile cache and map file
                if (mapFile.exists()) {
                    val tileCache: TileCache = AndroidUtil.createTileCache(
                        ctx, "mapcache",
                        this.model.displayModel.tileSize, 1f,
                        this.model.frameBufferModel.overdrawFactor
                    )
                    val tileLayer = TileRendererLayer(
                        tileCache,
                        MapFile(mapFile),
                        this.model.mapViewPosition,
                        AndroidGraphicFactory.INSTANCE
                    )
                    tileLayer.setXmlRenderTheme(MapsforgeThemes.DEFAULT)
                    this.layerManager.layers.add(tileLayer)
                }

                // Draw route as a polyline
                val polyline = createRoutePolyline(route)
                this.layerManager.layers.add(polyline)

                // Adjust zoom and center to fit the route
                if (route.isNotEmpty()) {
                    val mapPosition = boundingBox.centerPoint // Get the center LatLong
                    this.setCenter(mapPosition)                // Center the map
                    this.setZoomLevel(14)                      // Adjust zoom level manually


                    // use boundingBox
                }
            }
        }
    )
}

// Helper function to calculate bounding box with padding
fun calculateBoundingBox(
    route: List<Pair<Double, Double>>,
    paddingFactor: Double = 0.0
): BoundingBox {
    val minLat = route.minOf { it.first }
    val maxLat = route.maxOf { it.first }
    val minLon = route.minOf { it.second }
    val maxLon = route.maxOf { it.second }

    // Add padding to bounding box
    val latPadding = (maxLat - minLat) * paddingFactor
    val lonPadding = (maxLon - minLon) * paddingFactor

    return BoundingBox(
        maxLat + latPadding, // North
        minLon - lonPadding, // West
        minLat - latPadding, // South
        maxLon + lonPadding  // East
    )
}

// Helper function to create a polyline from route coordinates
fun createRoutePolyline(route: List<Pair<Double, Double>>): Polyline {
    val paint: Paint = AndroidGraphicFactory.INSTANCE.createPaint().apply {
        color = -0xff0100 // Red color
        strokeWidth = 5f
        setStyle(Style.STROKE)
    }

    val polyline = Polyline(paint, AndroidGraphicFactory.INSTANCE)
    route.forEach { (lat, lon) ->
        polyline.latLongs.add(LatLong(lat, lon))
    }
    return polyline
}

// Composable Map Card
@Composable
fun MapCard(context: Context, coordinates: List<Pair<Double, Double>>?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(25.dp),
        contentAlignment = Alignment.Center
    ) {
        AppCard(
            content = {
                if (coordinates != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        OfflineRouteMap(
                            context = context,
                            mapFilePath = "${context.filesDir}/your-region.map", // Ensure path to .map file
                            route = coordinates
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No data to display", style = MaterialTheme.typography.body1)
                    }
                }
            },
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            backgroundPainter = CardDefaults.cardBackgroundPainter(
                startBackgroundColor = MaterialTheme.colors.surface,
                endBackgroundColor = MaterialTheme.colors.surface
            ),
            appName = { Text("Route Map") },
            appImage = {
                Icon(
                    imageVector = Icons.Rounded.Map,
                    contentDescription = "Map Icon",
                    tint = MaterialTheme.colors.primary
                )
            },
            title = {},
            enabled = false,
            time = {}
        )
    }
}

@WearPreviewSmallRound
@Composable
fun MapCardPreview() {
    val sampleCoordinates = listOf(
        Pair(37.7749, -122.4194), // Example coordinates
        Pair(37.7751, -122.4185),
        Pair(37.7754, -122.4175),
        Pair(37.7757, -122.4168)
    )

    ExerciseTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(25.dp),
            contentAlignment = Alignment.Center
        ) {
            MapCard(context = LocalContext.current, coordinates = sampleCoordinates)
        }
    }
}
