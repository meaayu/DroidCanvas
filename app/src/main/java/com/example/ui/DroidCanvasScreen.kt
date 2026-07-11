package com.example.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FlipToFront
import androidx.compose.ui.draw.rotate
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.AlignHorizontalLeft
import androidx.compose.material.icons.filled.AlignVerticalTop
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatLineSpacing
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.FilterBAndW
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Slider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.Job
import kotlin.math.abs
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.BuildConfig
import com.example.data.Board
import com.example.data.CanvasItem
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.floor
import kotlin.math.ceil

@Composable
fun DroidCanvasScreen(
    viewModel: DroidCanvasViewModel
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val boards by viewModel.boards.collectAsState()
    val otherBoardsExist = boards.any { it.name.trim().lowercase() != "main board" }
    val visibleBoards = if (otherBoardsExist) {
        boards.filter { it.name.trim().lowercase() != "main board" }
    } else {
        boards
    }
    val activeBoardId by viewModel.currentBoardId.collectAsState()
    val isInitialLoadComplete by viewModel.isInitialLoadComplete.collectAsState()
    val canvasItems by viewModel.canvasItems.collectAsState()
    val drawingStrokes by viewModel.drawingStrokes.collectAsState()
    val selectedItemId by viewModel.selectedItemId.collectAsState()
    val isLoaded by viewModel.isLoaded.collectAsState()
    val isDrawModeEnabled = viewModel.isDrawModeEnabled

    val themeMode = viewModel.themeMode
    val darkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    var showCreateBoardDialog by remember { mutableStateOf(false) }
    var newBoardName by remember { mutableStateOf("") }
    var showBoardSwitcherDropdown by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var isOverflowToggled by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showManageBoardsDialog by remember { mutableStateOf(false) }
    var renamingBoard by remember { mutableStateOf<Board?>(null) }
    var renamingBoardName by remember { mutableStateOf("") }



    // Dynamic viewport measurement for centering imports
    var viewportWidth by remember { mutableStateOf(1000f) }
    var viewportHeight by remember { mutableStateOf(1000f) }

    var globalPointerCount by remember { mutableStateOf(0) }
    var globalIsMultiTouch by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var flingJob by remember { mutableStateOf<Job?>(null) }

    val activeBoard = boards.find { it.id == activeBoardId }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                viewModel.addImages(context, uris, viewportWidth, viewportHeight)
                Toast.makeText(context, "Importing ${uris.size} reference image(s)...", Toast.LENGTH_SHORT).show()
            }
        }
    )

    var fabVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        fabVisible = true
    }
    val fabScale by animateFloatAsState(
        targetValue = if (fabVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "fabScale"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    pickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .graphicsLayer(
                        scaleX = fabScale,
                        scaleY = fabScale
                    )
                    .testTag("add_image_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Images")
            }
        }
    ) { innerPadding ->
        val primaryColor = MaterialTheme.colorScheme.primary
        val gridDotColor = primaryColor.copy(alpha = 0.08f)
        val slateBackgroundColor = MaterialTheme.colorScheme.background
        val slateGridDotColor = primaryColor.copy(alpha = 0.38f)

        var isSidebarExpanded by remember { mutableStateOf(true) }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            val isWideScreen = maxWidth >= 720.dp

            Row(modifier = Modifier.fillMaxSize()) {
                if (isWideScreen && isSidebarExpanded) {
                    SidebarContent(
                        viewModel = viewModel,
                        viewportWidth = viewportWidth,
                        viewportHeight = viewportHeight,
                        density = density.density,
                        pickerLauncher = pickerLauncher,
                        onCloseSidebar = { isSidebarExpanded = false },
                        onCreateBoardClick = { showCreateBoardDialog = true },
                        onRenameBoardClick = { b ->
                            renamingBoard = b
                            renamingBoardName = b.name
                        },
                        onManageBoardsClick = { showManageBoardsDialog = true },
                        modifier = Modifier
                            .width(300.dp)
                            .fillMaxHeight()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(slateBackgroundColor)
                        .onSizeChanged { size ->
                            viewportWidth = size.width.toFloat()
                            viewportHeight = size.height.toFloat()
                        }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val pressedCount = event.changes.count { it.pressed }
                            globalPointerCount = pressedCount
                            if (pressedCount > 1) {
                                globalIsMultiTouch = true
                            } else if (pressedCount == 0) {
                                globalIsMultiTouch = false
                            }
                        }
                    }
                }
                .drawBehind {
                    // Deep elegant slate background that dynamically matches system Material You colors
                    drawRect(color = slateBackgroundColor)
                    
                    // Fine neon blue dots that adapt to theme accent
                    val gridSize = 45.dp.toPx()
                    val scale = viewModel.canvasScale
                    val transX = viewModel.canvasTranslateX
                    val transY = viewModel.canvasTranslateY
                    
                    val width = size.width
                    val height = size.height
                    
                    val startX = -transX / scale
                    val endX = (width - transX) / scale
                    val startY = -transY / scale
                    val endY = (height - transY) / scale
                    
                    val startGridX = startX - (startX % gridSize) - gridSize
                    val startGridY = startY - (startY % gridSize) - gridSize

                    val stepMultiplier = when {
                        scale < 0.22f -> 6
                        scale < 0.45f -> 3
                        scale < 0.75f -> 2
                        else -> 1
                    }
                    val effectiveGridSize = gridSize * stepMultiplier
                    
                    val dotColor = slateGridDotColor
                    
                    var currX = startGridX
                    if (viewModel.gridStyle == "dots") {
                        while (currX < endX + effectiveGridSize) {
                            val screenX = currX * scale + transX
                            var currY = startGridY
                            while (currY < endY + effectiveGridSize) {
                                val screenY = currY * scale + transY
                                drawCircle(
                                    color = dotColor,
                                    radius = (1.8f * scale).coerceIn(1.2f, 4.0f),
                                    center = Offset(screenX, screenY)
                                )
                                currY += effectiveGridSize
                            }
                            currX += effectiveGridSize
                        }
                    } else if (viewModel.gridStyle == "lines") {
                        // Draw vertical lines
                        while (currX < endX + effectiveGridSize) {
                            val screenX = currX * scale + transX
                            drawLine(
                                color = dotColor.copy(alpha = dotColor.alpha * 0.45f),
                                start = Offset(screenX, 0f),
                                end = Offset(screenX, height),
                                strokeWidth = (1.2f * scale).coerceIn(0.8f, 2.5f)
                            )
                            currX += effectiveGridSize
                        }
                        // Draw horizontal lines
                        var currY = startGridY
                        while (currY < endY + effectiveGridSize) {
                            val screenY = currY * scale + transY
                            drawLine(
                                color = dotColor.copy(alpha = dotColor.alpha * 0.45f),
                                start = Offset(0f, screenY),
                                end = Offset(width, screenY),
                                strokeWidth = (1.2f * scale).coerceIn(0.8f, 2.5f)
                            )
                            currY += effectiveGridSize
                        }
                    } else if (viewModel.gridStyle == "graph") {
                        // Draw horizontal & vertical lines with major lines every 5 subdivisions
                        while (currX < endX + effectiveGridSize) {
                            val screenX = currX * scale + transX
                            val index = (currX / gridSize).roundToInt()
                            val isMajor = index % 5 == 0
                            val alphaFactor = if (isMajor) 0.8f else 0.25f
                            val strokeWidthFactor = if (isMajor) 2.0f else 1.0f
                            drawLine(
                                color = dotColor.copy(alpha = dotColor.alpha * alphaFactor * 0.45f),
                                start = Offset(screenX, 0f),
                                end = Offset(screenX, height),
                                strokeWidth = (strokeWidthFactor * 1.2f * scale).coerceIn(0.8f, 3.5f)
                            )
                            currX += effectiveGridSize
                        }
                        var currY = startGridY
                        while (currY < endY + effectiveGridSize) {
                            val screenY = currY * scale + transY
                            val index = (currY / gridSize).roundToInt()
                            val isMajor = index % 5 == 0
                            val alphaFactor = if (isMajor) 0.8f else 0.25f
                            val strokeWidthFactor = if (isMajor) 2.0f else 1.0f
                            drawLine(
                                color = dotColor.copy(alpha = dotColor.alpha * alphaFactor * 0.45f),
                                start = Offset(0f, screenY),
                                end = Offset(width, screenY),
                                strokeWidth = (strokeWidthFactor * 1.2f * scale).coerceIn(0.8f, 3.5f)
                            )
                            currY += effectiveGridSize
                        }
                    }
                    

                }
                // Background Canvas gestures: Tap to unselect, transform gestures to zoom and pan with inertial scrolling
                .pointerInput(Unit) {
                    awaitEachGesture {
                        var zoom = 1f
                        var pan = Offset.Zero
                        var pastTouchSlop = false
                        val touchSlop = viewConfiguration.touchSlop
                        
                        // Cancel any active fling as soon as a pointer is placed down
                        flingJob?.cancel()
                        
                        val velocityTracker = VelocityTracker()
                        
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val activePointerId = down.id
                        velocityTracker.addPosition(down.uptimeMillis, down.position)
                        
                        var isTapCandidate = !down.isConsumed
                        
                        do {
                            val event = awaitPointerEvent()
                            val canceled = event.changes.any { it.isConsumed } && !globalIsMultiTouch && event.changes.size == 1
                            if (!canceled) {
                                val zoomChange = event.calculateZoom()
                                val panChange = event.calculatePan()
                                
                                zoom *= zoomChange
                                pan += panChange
                                
                                val centroidSize = event.calculateCentroidSize(useCurrent = false)
                                val zoomMotion = abs(1 - zoom) * centroidSize
                                val panMotion = pan.getDistance()
                                
                                if (!pastTouchSlop) {
                                    if (zoomMotion > touchSlop || panMotion > touchSlop) {
                                        pastTouchSlop = true
                                        isTapCandidate = false
                                    }
                                }
                                
                                if (pastTouchSlop) {
                                    // Add position of active pointer to velocity tracker for higher tracking accuracy
                                    val activeChange = event.changes.find { it.id == activePointerId }
                                    if (activeChange != null) {
                                        velocityTracker.addPosition(activeChange.uptimeMillis, activeChange.position)
                                    } else {
                                        val firstChange = event.changes.firstOrNull()
                                        if (firstChange != null) {
                                            velocityTracker.addPosition(firstChange.uptimeMillis, firstChange.position)
                                        }
                                    }
                                    
                                    val effectiveZoom = zoomChange
                                    val centroid = event.calculateCentroid(useCurrent = true)
                                    if (effectiveZoom != 1f || panChange != Offset.Zero) {
                                        val centroidCoord = if (centroid != Offset.Unspecified) centroid else down.position
                                        val zoomRes = CanvasZoomHelper.computeZoom(
                                            viewModel.canvasScale,
                                            effectiveZoom,
                                            panChange.x,
                                            panChange.y,
                                            viewModel.canvasTranslateX,
                                            viewModel.canvasTranslateY,
                                            centroidCoord.x,
                                            centroidCoord.y,
                                            0.01f,
                                            10f
                                        )
                                        if (zoomRes != null) {
                                            viewModel.canvasScale = zoomRes.nextScale
                                            viewModel.canvasTranslateX = zoomRes.nextTranslateX
                                            viewModel.canvasTranslateY = zoomRes.nextTranslateY
                                        }
                                    }
                                    
                                    event.changes.forEach {
                                        if (it.position != it.previousPosition) {
                                            it.consume()
                                        }
                                    }
                                }
                            } else {
                                isTapCandidate = false
                            }
                        } while (event.changes.any { it.pressed })
                        
                        if (isTapCandidate) {
                            if (System.currentTimeMillis() - viewModel.lastItemTapTime > 150) {
                                viewModel.selectItem(null)
                            }
                        } else {
                            val velocity = velocityTracker.calculateVelocity()
                            // Trigger dynamic physical deceleration on release
                            if (abs(velocity.x) > 150f || abs(velocity.y) > 150f) {
                                flingJob = coroutineScope.launch {
                                    var velocityX = velocity.x
                                    var velocityY = velocity.y
                                    var lastTime = withFrameNanos { it } / 1_000_000L
                                    
                                    while (abs(velocityX) > 15f || abs(velocityY) > 15f) {
                                        val currentTime = withFrameNanos { it } / 1_000_000L
                                        val dt = (currentTime - lastTime).coerceIn(1L, 50L)
                                        lastTime = currentTime
                                        
                                        // Compute decay physics and position updates using Java helper
                                        val step = CanvasPhysicsHelper.processDecay(
                                            velocityX,
                                            velocityY,
                                            viewModel.canvasTranslateX,
                                            viewModel.canvasTranslateY,
                                            dt,
                                            0.95
                                        )
                                        velocityX = step.nextVelocityX
                                        velocityY = step.nextVelocityY
                                        viewModel.canvasTranslateX = step.nextTranslateX
                                        viewModel.canvasTranslateY = step.nextTranslateY
                                    }
                                }
                            }
                        }
                    }
                }
        ) {
            // 2. THE INFINITE BOARD IMAGE LAYER
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = viewModel.canvasTranslateX
                        translationY = viewModel.canvasTranslateY
                        scaleX = viewModel.canvasScale
                        scaleY = viewModel.canvasScale
                        transformOrigin = TransformOrigin(0f, 0f)
                        compositingStrategy = CompositingStrategy.Auto
                    }
            ) {
                if (isLoaded) {
                    // Render each image on the canvas
                    canvasItems.forEach { item ->
                        CanvasItemView(
                            item = item,
                            isSelected = selectedItemId == item.id,
                            viewModel = viewModel,
                            globalIsMultiTouch = globalIsMultiTouch,
                            viewportWidth = viewportWidth,
                            viewportHeight = viewportHeight
                        )
                    }

                    // Render completed and active drawing strokes
                    val drawingStrokes by viewModel.drawingStrokes.collectAsState()
                    val activeStroke = viewModel.activeStroke
                    
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawingStrokes.forEach { stroke ->
                            val path = Path().apply {
                                if (stroke.points.isNotEmpty()) {
                                    moveTo(stroke.points.first().x, stroke.points.first().y)
                                    for (i in 1 until stroke.points.size) {
                                        lineTo(stroke.points[i].x, stroke.points[i].y)
                                    }
                                }
                            }
                            drawPath(
                                path = path,
                                color = Color(stroke.color),
                                style = Stroke(
                                    width = stroke.strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                        activeStroke?.let { stroke ->
                            val path = Path().apply {
                                if (stroke.points.isNotEmpty()) {
                                    moveTo(stroke.points.first().x, stroke.points.first().y)
                                    for (i in 1 until stroke.points.size) {
                                        lineTo(stroke.points[i].x, stroke.points[i].y)
                                    }
                                }
                            }
                            drawPath(
                                path = path,
                                color = Color(stroke.color),
                                style = Stroke(
                                    width = stroke.strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }
            }

            // 3. EMPTY STATE ILLUSTRATION
            if ((isLoaded && canvasItems.isEmpty()) || boards.isEmpty()) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(28.dp)
                        .widthIn(max = 340.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.95f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Artistic Overlapping Polaroid Preview
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            // Polaroid Card 1
                            Card(
                                modifier = Modifier
                                    .size(76.dp, 92.dp)
                                    .graphicsLayer(
                                        rotationZ = -10f
                                    )
                                    .offset(x = 10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                            ) {
                                Column(
                                    modifier = Modifier.padding(5.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(58.dp)
                                            .background(Color(0xFFF0F0F0)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Image,
                                            contentDescription = null,
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                            // Polaroid Card 2
                            Card(
                                modifier = Modifier
                                    .size(76.dp, 92.dp)
                                    .graphicsLayer(
                                        rotationZ = 8f
                                    )
                                    .offset(x = -10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E2E2))
                            ) {
                                Column(
                                    modifier = Modifier.padding(5.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(58.dp)
                                            .background(Color(0xFFE3F2FD)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Landscape,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Your Infinite Board is Empty",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Import reference photos or share them directly into DroidCanvas from other apps!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = {
                                pickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Import Images", fontWeight = FontWeight.Bold)
                        }

                    }
                }
            }

            // 3. DRAWING OVERLAY TO CAPTURE SINGLE TOUCHES WHEN DRAW MODE IS ACTIVE
            if (isDrawModeEnabled) {
                val isEraserModeEnabled = viewModel.isEraserModeEnabled
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(isEraserModeEnabled) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                
                                val startCanvasPoint = Offset(
                                    (down.position.x - viewModel.canvasTranslateX) / viewModel.canvasScale,
                                    (down.position.y - viewModel.canvasTranslateY) / viewModel.canvasScale
                                )
                                
                                if (isEraserModeEnabled) {
                                    viewModel.eraseStrokeAt(startCanvasPoint)
                                } else {
                                    viewModel.startNewStroke(startCanvasPoint)
                                }
                                down.consume()
                                
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val activeChanges = event.changes.filter { it.pressed }
                                    if (activeChanges.isEmpty()) {
                                        if (!isEraserModeEnabled) {
                                            viewModel.finishActiveStroke()
                                        }
                                        break
                                    }
                                    
                                    if (event.changes.size > 1) {
                                        if (!isEraserModeEnabled) {
                                            viewModel.finishActiveStroke()
                                        }
                                        break
                                    }
                                    
                                    val change = activeChanges.first()
                                    val canvasPoint = Offset(
                                        (change.position.x - viewModel.canvasTranslateX) / viewModel.canvasScale,
                                        (change.position.y - viewModel.canvasTranslateY) / viewModel.canvasScale
                                    )
                                    
                                    if (isEraserModeEnabled) {
                                        viewModel.eraseStrokeAt(canvasPoint)
                                    } else {
                                        viewModel.appendPointToActiveStroke(canvasPoint)
                                    }
                                    change.consume()
                                }
                            }
                        }
                )
            }

            // 3.5. DRAWING TOOLBAR (Floating at bottom center when draw mode is active)
            androidx.compose.animation.AnimatedVisibility(
                visible = isDrawModeEnabled,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 80.dp) // Offset above the Add Images FAB so they don't overlap!
            ) {
                DrawingToolbar(viewModel = viewModel)
            }

            // 4. UNIFIED CONTROL PANEL (Board Switcher, Zoom, Settings, Multi-select, Arrange)
            var showArrangeMenu by remember { mutableStateOf(false) }
            var controlPanelVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                controlPanelVisible = true
            }
            val controlPanelAlpha by animateFloatAsState(
                targetValue = if (controlPanelVisible) 1f else 0f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                label = "controlPanelAlpha"
            )
            val controlPanelOffsetY by animateDpAsState(
                targetValue = if (controlPanelVisible) 0.dp else (-40).dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "controlPanelOffsetY"
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(
                        top = 16.dp + innerPadding.calculateTopPadding(),
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
                    .let { baseModifier ->
                        if (controlPanelAlpha < 1f) {
                            baseModifier.graphicsLayer {
                                alpha = controlPanelAlpha
                            }
                        } else {
                            baseModifier
                        }
                    }
                    .offset(y = controlPanelOffsetY)
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .border(
                        BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    // Single Row: Board Switcher (Left) and Zoom, Overflow, Settings (Right)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isWideScreen) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSidebarExpanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSidebarExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { isSidebarExpanded = !isSidebarExpanded }
                                    .testTag("toggle_sidebar_button")
                            ) {
                                Box(
                                    modifier = Modifier.padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isSidebarExpanded) Icons.Default.Close else Icons.Default.Layers,
                                        contentDescription = "Toggle Sidebar",
                                        tint = if (isSidebarExpanded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        // Board selection capsule
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showBoardSwitcherDropdown = true }
                                .testTag("board_switcher_capsule")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Folder,
                                    contentDescription = "Board Selection",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = activeBoard?.name ?: "Loading...",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Open Board Switcher Menu",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )

                                // Dropdown for Board actions
                                DropdownMenu(
                                    expanded = showBoardSwitcherDropdown,
                                    onDismissRequest = { showBoardSwitcherDropdown = false },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                ) {
                                    Text(
                                        text = "SWITCH WORKSPACE",
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    visibleBoards.forEach { b ->
                                        val isActive = b.id == activeBoardId
                                        DropdownMenuItem(
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = if (isActive) Icons.Default.Check else Icons.Default.Folder,
                                                    contentDescription = null,
                                                    tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            },
                                            text = {
                                                Text(
                                                    text = b.name,
                                                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                                                )
                                            },
                                            onClick = {
                                                viewModel.selectBoard(b.id)
                                                showBoardSwitcherDropdown = false
                                            }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp)
                                            .height(1.dp)
                                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Settings,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        text = {
                                            Text("Manage Boards...", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        },
                                        onClick = {
                                            showBoardSwitcherDropdown = false
                                            showManageBoardsDialog = true
                                        }
                                    )
                                }
                            }
                        }

                        // Zoom Indicator
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.fitToContent(viewportWidth, viewportHeight, density.density) }
                                .testTag("zoom_reset_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.ZoomIn,
                                    contentDescription = "Reset Zoom",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                ZoomPercentageText(viewModel)
                            }
                        }

                        // Draw Mode Toggle Button
                        val isDrawModeEnabled = viewModel.isDrawModeEnabled
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDrawModeEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(
                                1.dp,
                                if (isDrawModeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.isDrawModeEnabled = !isDrawModeEnabled }
                                .testTag("draw_mode_toggle_button")
                        ) {
                            Box(
                                modifier = Modifier.padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Draw Mode",
                                    tint = if (isDrawModeEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Overflow (⋯) Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isOverflowToggled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(
                                1.dp,
                                if (isOverflowToggled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { isOverflowToggled = !isOverflowToggled }
                                .testTag("overflow_tools_toggle")
                        ) {
                            Box(
                                modifier = Modifier.padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "Toggle Toolbar Options",
                                    tint = if (isOverflowToggled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Settings Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showSettingsDialog = true }
                                .testTag("settings_button")
                        ) {
                            Box(
                                modifier = Modifier.padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Secondary Toolbar: (Arrange, Lock Board, Undo, Redo)
                    // Visible only in populated state (when board has content) AND when overflow is toggled.
                    androidx.compose.animation.AnimatedVisibility(
                        visible = (canvasItems.isNotEmpty() || drawingStrokes.isNotEmpty()) && isOverflowToggled,
                        enter = expandVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) + fadeIn(animationSpec = tween(150)),
                        exit = shrinkVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) + fadeOut(animationSpec = tween(150))
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))

                            // Delicate Divider
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                var showArrangeMenu by remember { mutableStateOf(false) }

                                // Auto-Arrange Dropdown Pill
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    border = BorderStroke(
                                        1.dp, 
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { showArrangeMenu = true }
                                        .testTag("auto_arrange_capsule")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.GridView,
                                            contentDescription = "Auto Arrange Canvas",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Arrange",
                                            color = MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Expand Arrange Options",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
         
                                        DropdownMenu(
                                            expanded = showArrangeMenu,
                                            onDismissRequest = { showArrangeMenu = false },
                                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        ) {
                                            Text(
                                                text = "AUTO-ARRANGE BOARD",
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            )
                                            DropdownMenuItem(
                                                leadingIcon = {
                                                    Icon(Icons.Default.GridView, contentDescription = null, modifier = Modifier.size(18.dp))
                                                },
                                                text = { Text("Perfect Grid Layout", fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                                                onClick = {
                                                    viewModel.autoArrangeGrid("GRID", density.density)
                                                    showArrangeMenu = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                leadingIcon = {
                                                    Icon(Icons.Default.ViewStream, contentDescription = null, modifier = Modifier.size(18.dp))
                                                },
                                                text = { Text("Horizontal Strip", fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                                                onClick = {
                                                    viewModel.autoArrangeGrid("STRIP", density.density)
                                                    showArrangeMenu = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Lock/Unlock Canvas Pill
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (viewModel.isLocked) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                                    border = BorderStroke(
                                        1.dp,
                                        if (viewModel.isLocked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { viewModel.toggleLock() }
                                        .testTag("lock_canvas_toggle_capsule")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = if (viewModel.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                            contentDescription = "Toggle Lock Board",
                                            tint = if (viewModel.isLocked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (viewModel.isLocked) "Locked" else "Lock Board",
                                            color = if (viewModel.isLocked) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }

                                val canUndo by viewModel.canUndo.collectAsState()
                                val canRedo by viewModel.canRedo.collectAsState()

                                // Undo Button
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (canUndo) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f),
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (canUndo) 0.6f else 0.3f)
                                    ),
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable(enabled = canUndo) { viewModel.undo() }
                                        .testTag("undo_button")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Undo,
                                            contentDescription = "Undo",
                                            tint = if (canUndo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                // Redo Button
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (canRedo) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f),
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (canRedo) 0.6f else 0.3f)
                                    ),
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable(enabled = canRedo) { viewModel.redo() }
                                        .testTag("redo_button")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Redo,
                                            contentDescription = "Redo",
                                            tint = if (canRedo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }



                }
            }
        }
    }

    // 6. BOARD CREATION DIALOG
    if (showCreateBoardDialog) {
        AlertDialog(
            onDismissRequest = { showCreateBoardDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Create New Board",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            text = {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = newBoardName,
                        onValueChange = { newBoardName = it },
                        label = { Text("Board Name") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("board_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newBoardName.isNotBlank()) {
                            viewModel.createBoard(newBoardName.trim())
                            newBoardName = ""
                            showCreateBoardDialog = false
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Create", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateBoardDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                }
            }
        )
    }

    // 6b. MANAGE BOARDS DIALOG
    if (showManageBoardsDialog) {
        var createBoardName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showManageBoardsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Manage Workspace Boards",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Quick add field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = createBoardName,
                            onValueChange = { createBoardName = it },
                            placeholder = { Text("New board name...") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = {
                                if (createBoardName.isNotBlank()) {
                                    viewModel.createBoard(createBoardName.trim())
                                    createBoardName = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            enabled = createBoardName.isNotBlank()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Board", modifier = Modifier.size(18.dp))
                        }
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )

                    // Board List
                    Text(
                        text = "YOUR BOARDS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        visibleBoards.forEach { b ->
                            val isActive = b.id == activeBoardId
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                                border = BorderStroke(
                                    1.dp,
                                    if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.selectBoard(b.id) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (isActive) Icons.Default.Check else Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = b.name,
                                            color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                renamingBoard = b
                                                renamingBoardName = b.name
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Rename Board",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        if (visibleBoards.size > 1) {
                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteBoard(b)
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete Board",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showManageBoardsDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (renamingBoard != null) {
        AlertDialog(
            onDismissRequest = { renamingBoard = null },
            title = {
                Text(
                    text = "Rename Board",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Enter a new name for this workspace:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = renamingBoardName,
                        onValueChange = { renamingBoardName = it },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = renamingBoard
                        if (target != null && renamingBoardName.isNotBlank()) {
                            viewModel.renameBoard(target, renamingBoardName.trim())
                            renamingBoard = null
                        }
                    },
                    enabled = renamingBoardName.isNotBlank(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Rename", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { renamingBoard = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                }
            }
        )
    }

    // 7. SETTINGS PANEL (Smooth Fullscreen Animated Overlay)
    AnimatedVisibility(
        visible = showSettingsDialog,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
        ) + fadeIn(animationSpec = tween(150)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
        ) + fadeOut(animationSpec = tween(150)),
        modifier = Modifier.fillMaxSize()
    ) {
        BackHandler(enabled = showSettingsDialog) {
            showSettingsDialog = false
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
                Scaffold(
                    topBar = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { showSettingsDialog = false }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Settings",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .navigationBarsPadding()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {


                        // SECTION 1: THEME & VISUAL STYLE
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Landscape,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "THEME & VISUAL STYLE",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                            }
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // 1. Application Theme
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "Application Theme",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        val themeModes = listOf(
                                            "system" to "System",
                                            "dark" to "Dark",
                                            "light" to "Light"
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            themeModes.forEach { (mode, label) ->
                                                val isSelected = viewModel.themeMode == mode
                                                val icon = when (mode) {
                                                    "dark" -> Icons.Default.DarkMode
                                                    "light" -> Icons.Default.LightMode
                                                    else -> Icons.Default.Settings
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(
                                                            if (isSelected) MaterialTheme.colorScheme.primary
                                                            else MaterialTheme.colorScheme.surfaceContainerHigh
                                                        )
                                                        .clickable { viewModel.themeMode = mode }
                                                        .padding(vertical = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = icon,
                                                            contentDescription = label,
                                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Text(
                                                            text = label,
                                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                            maxLines = 1
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(0.5.dp)
                                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    )

                                    // Material You Dynamic Color Switch
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Material You Dynamic Color",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Enable dynamic theme colors extracted from system wallpaper",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                        Switch(
                                            checked = viewModel.isDynamicColorEnabled,
                                            onCheckedChange = { viewModel.isDynamicColorEnabled = it }
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(0.5.dp)
                                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    )

                                    // Pitch Black Dark Theme Switch
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Pitch Black (AMOLED)",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Use pure black background in dark mode to save power",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                        Switch(
                                            checked = viewModel.isPitchBlackEnabled,
                                            onCheckedChange = { viewModel.isPitchBlackEnabled = it }
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(0.5.dp)
                                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    )

                                    // 2. Grid Style Selection
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "Canvas Background Grid Pattern",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        val styles = listOf(
                                            "dots" to "Dots",
                                            "lines" to "Lines",
                                            "graph" to "Graph",
                                            "none" to "None"
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            styles.forEach { (styleKey, label) ->
                                                val isSelected = viewModel.gridStyle == styleKey
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(
                                                            if (isSelected) MaterialTheme.colorScheme.primary
                                                            else MaterialTheme.colorScheme.surfaceContainerHigh
                                                        )
                                                        .clickable { viewModel.gridStyle = styleKey }
                                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = label,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }


                        // SECTION 2: CANVAS NAVIGATION & TACTILITY
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "CANVAS NAVIGATION & TACTILITY",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                            }
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // 1. Double Tap Zoom Level
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Double-Tap Zoom Target",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Target zoom level when double-tapping the canvas",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                )
                                            }
                                            Text(
                                                text = "${(viewModel.doubleTapZoomTarget * 100).toInt()}% (${"%.1fx".format(viewModel.doubleTapZoomTarget)})",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                        }
                                        Slider(
                                            value = viewModel.doubleTapZoomTarget,
                                            onValueChange = {
                                                viewModel.doubleTapZoomTarget = (kotlin.math.round(it * 10) / 10f)
                                            },
                                            valueRange = 1.0f..10.0f,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(0.5.dp)
                                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    )

                                    // 2. Haptic Feedback Toggle
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Tactile Haptic Feedback",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Vibrate subtly on tap, zoom, or snap actions",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                        Switch(
                                            checked = viewModel.isHapticEnabled,
                                            onCheckedChange = { viewModel.isHapticEnabled = it }
                                        )
                                    }
                                }
                            }
                        }


                        // SECTION 3: GRID & ALIGNMENT SNAPPING
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "GRID & ALIGNMENT SNAPPING",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                            }
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // 1. Snap To Grid Switch
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Snap to Grid",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Align reference boundaries dynamically to grid coordinates",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                        Switch(
                                            checked = viewModel.isSnapToGrid,
                                            onCheckedChange = { viewModel.isSnapToGrid = it }
                                        )
                                    }

                                    if (viewModel.isSnapToGrid) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(0.5.dp)
                                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                        )

                                        // 2. Snap Step Size Selector
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                text = "Grid Snap Step Size",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            val snapSizes = listOf(20f to "20px", 40f to "40px", 80f to "80px", 120f to "120px")
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                snapSizes.forEach { (size, label) ->
                                                    val isSelected = viewModel.snapStepSize == size
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .background(
                                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                                else MaterialTheme.colorScheme.surfaceContainerHigh
                                                            )
                                                            .clickable { viewModel.snapStepSize = size }
                                                            .padding(vertical = 10.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = label,
                                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(0.5.dp)
                                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    )

                                    // 3. Auto Arrange Gap
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "Auto-Arrange Spacing Gap",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        val spacingOptions = listOf(20f to "Tight", 40f to "Medium", 60f to "Standard", 100f to "Loose")
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            spacingOptions.forEach { (space, label) ->
                                                val isSelected = viewModel.arrangeSpacing == space
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(
                                                            if (isSelected) MaterialTheme.colorScheme.secondary
                                                            else MaterialTheme.colorScheme.surfaceContainerHigh
                                                        )
                                                        .clickable { viewModel.arrangeSpacing = space }
                                                        .padding(vertical = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = label,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }


                        // SECTION 4: STORAGE & BOARD MAINTENANCE
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "STORAGE & MAINTENANCE",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error,
                                    letterSpacing = 1.sp
                                )
                            }
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.25f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Clear Current Board",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Text(
                                            text = "Wipe and permanently delete all loaded reference images on the active canvas",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Button(
                                        onClick = { showClearConfirmDialog = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = MaterialTheme.colorScheme.onError
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Clear Board", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }


                        // SECTION 5: ABOUT & SYSTEM UPDATE
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "ABOUT DROIDCANVAS",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                            }
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Application Version",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Infinite Offline Reference Workspace",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                        Text(
                                            text = "v${BuildConfig.VERSION_NAME}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }



                                    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                try {
                                                    uriHandler.openUri("https://github.com/meaayu")
                                                } catch (e: Exception) {
                                                    // ignore gracefully
                                                }
                                            }
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Developer",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "Tap to view profile",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "aayu",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Link",
                                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }





    // 8. CLEAR BOARD CONFIRMATION DIALOG
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Clear Board?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            text = {
                Text(
                    text = "Are you sure you want to delete all reference images on this board? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearCurrentBoard()
                        showClearConfirmDialog = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Clear All", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                }
            }
        )
    }


}

private fun adjustPositionForScale(
    oldScale: Float,
    newScale: Float,
    posX: Float,
    posY: Float,
    widthPx: Float,
    heightPx: Float,
    rotationDegrees: Float,
    u: Float, // opposite corner relative X: -0.5f for left, 0.5f for right
    v: Float  // opposite corner relative Y: -0.5f for top, 0.5f for bottom
): Pair<Float, Float> {
    val result = CanvasMathHelper.adjustPositionForScale(
        oldScale,
        newScale,
        posX,
        posY,
        widthPx,
        heightPx,
        rotationDegrees,
        u,
        v
    )
    return Pair(result.posX, result.posY)
}

@Composable
fun CanvasItemView(
    item: CanvasItem,
    isSelected: Boolean,
    viewModel: DroidCanvasViewModel,
    globalIsMultiTouch: Boolean,
    viewportWidth: Float,
    viewportHeight: Float
) {
    val latestItem by rememberUpdatedState(item)
    val canvasScale = if (isSelected) viewModel.canvasScale else 1f
    val density = LocalDensity.current
    val context = LocalContext.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    var isLongPressMenuExpanded by remember { mutableStateOf(false) }

    var isDragging by remember { mutableStateOf(false) }
    var isResizing by remember { mutableStateOf(false) }

    var localPosX by remember { mutableStateOf(item.posX) }
    var localPosY by remember { mutableStateOf(item.posY) }
    var localScale by remember { mutableStateOf(item.scale) }
    var lastTapTime by remember { mutableStateOf(0L) }

    var itemCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var handleCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    var startTouchWindowPos by remember { mutableStateOf(Offset.Zero) }
    var anchorWindowPos by remember { mutableStateOf(Offset.Zero) }
    var startScale by remember { mutableStateOf(1f) }

    LaunchedEffect(item.posX, item.posY) {
        if (!isDragging) {
            localPosX = item.posX
            localPosY = item.posY
        }
    }

    LaunchedEffect(item.scale) {
        if (!isDragging) {
            localScale = item.scale
        }
    }

    // Map the stored native dimensions (width, height) to Dp values for structural consistency
    val nativeWidthDp = remember(item.width) { with(density) { item.width.toDp() } }
    val nativeHeightDp = remember(item.height) { with(density) { item.height.toDp() } }

    // Prevent images from starting too massive or tiny
    val maxBound = 320.dp
    val scaleFactor = remember(nativeWidthDp, nativeHeightDp) {
        val maxDimension = maxOf(nativeWidthDp, nativeHeightDp)
        if (maxDimension > maxBound) {
            maxBound.value / maxDimension.value
        } else {
            1f
        }
    }

    val displayWidth = nativeWidthDp * scaleFactor
    val displayHeight = nativeHeightDp * scaleFactor

    val displayWidthPx = remember(displayWidth, density) { with(density) { displayWidth.toPx() } }
    val displayHeightPx = remember(displayHeight, density) { with(density) { displayHeight.toPx() } }

    // --- Cool Entry & Selection Animations ---
    var isEntered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isEntered = true
    }
    
    val entryAlpha by animateFloatAsState(
        targetValue = if (isEntered) 1f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "entryAlpha"
    )
    val entryScale by animateFloatAsState(
        targetValue = if (isEntered) 1f else 0.82f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "entryScale"
    )
    val selectionScaleMultiplier by animateFloatAsState(
        targetValue = if (isSelected) 1.04f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "selectionScale"
    )
    val totalScale = entryScale * selectionScaleMultiplier

    val animatedElevation by animateDpAsState(
        targetValue = if (isSelected) 10.dp else 2.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "elevation"
    )
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "borderColor"
    )
    val animatedBorderWidth by animateDpAsState(
        targetValue = if (isSelected) ((2.5f / canvasScale).dp).coerceIn(1.5.dp, 5.dp) else 1.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "borderWidth"
    )

    Box(
        modifier = Modifier
            .offset { IntOffset(localPosX.roundToInt(), localPosY.roundToInt()) }
            .requiredSize(displayWidth * localScale, displayHeight * localScale)
            .graphicsLayer(
                rotationZ = item.rotation,
                scaleX = totalScale,
                scaleY = totalScale,
                alpha = entryAlpha
            )
            .onGloballyPositioned { itemCoordinates = it }
            .testTag("canvas_item_${item.id}")
    ) {
        // Use fullPath always to avoid switching resolution flicker during zoom, using thumbPath as placeholder
        val thumbnailPainter = rememberAsyncImagePainter(model = item.thumbPath)

        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = animatedElevation
            ),
            modifier = if (viewModel.isLocked) {
                Modifier
                    .fillMaxSize()
                    .pointerInput(item.id) {
                        detectTapGestures(
                            onDoubleTap = {
                                viewModel.toggleZoomOnItem(latestItem, viewportWidth, viewportHeight, density.density)
                            }
                        )
                    }
            } else {
                Modifier
                    .fillMaxSize()
                    .pointerInput(item.id, globalIsMultiTouch, isResizing) {
                        if (globalIsMultiTouch || isResizing) return@pointerInput
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            down.consume()
                            var dragActive = false
                            var isMultiTouch = false
                            var isLongPressTriggered = false
                            var accumulatedDrag = Offset.Zero
                            
                            val longPressJob = coroutineScope.launch {
                                try {
                                    delay(500)
                                    if (!isMultiTouch && accumulatedDrag.getDistance() < 10f && !dragActive && !globalIsMultiTouch) {
                                        isLongPressTriggered = true
                                        viewModel.selectItem(latestItem.id)
                                        isLongPressMenuExpanded = true
                                    }
                                } catch (e: Exception) {
                                    // Cancelled
                                }
                            }
                            
                            try {
                                do {
                                    val event = awaitPointerEvent()
                                    
                                    val pressedPointers = event.changes.filter { it.pressed }
                                    if (pressedPointers.size > 1 || globalIsMultiTouch) {
                                        isMultiTouch = true
                                        longPressJob.cancel()
                                    }
                                    
                                    if (isMultiTouch || globalIsMultiTouch) {
                                        if (dragActive) {
                                            dragActive = false
                                            isDragging = false
                                            viewModel.updateItemAbsolutePosition(latestItem, localPosX, localPosY)
                                        }
                                        continue
                                    }
                                    
                                    val dragChange = event.changes.firstOrNull { it.id == down.id }
                                    if (dragChange != null && dragChange.pressed) {
                                        dragChange.consume()
                                        val dragAmount = dragChange.position - dragChange.previousPosition
                                        if (dragAmount != Offset.Zero) {
                                            accumulatedDrag += dragAmount
                                            
                                            if (accumulatedDrag.getDistance() > 10f) {
                                                longPressJob.cancel()
                                            }
                                            
                                            val touchSlop = 8f
                                            if (!dragActive && accumulatedDrag.getDistance() > touchSlop && !isLongPressTriggered && !globalIsMultiTouch) {
                                                if (event.changes.filter { it.pressed }.size == 1) {
                                                    if (!latestItem.isPinned) {
                                                        dragActive = true
                                                        isDragging = true
                                                    }
                                                    viewModel.selectItem(latestItem.id)
                                                } else {
                                                    isMultiTouch = true
                                                }
                                            }
                                            
                                            if (dragActive && !globalIsMultiTouch) {
                                                dragChange.consume()
                                                
                                                // Transform local drag amount to parent (canvas) drag amount using Java helper
                                                val rotatedDrag = CanvasMathHelper.rotateDragAmount(dragAmount.x, dragAmount.y, latestItem.rotation)
                                                
                                                localPosX += rotatedDrag.posX
                                                localPosY += rotatedDrag.posY
                                            }
                                        }
                                    }
                                } while (event.changes.any { it.pressed })
                            } finally {
                                longPressJob.cancel()
                                if (dragActive) {
                                    isDragging = false
                                    viewModel.updateItemAbsolutePosition(latestItem, localPosX, localPosY)
                                }
                            }
                            
                            if (!isMultiTouch && !dragActive && !isLongPressTriggered && !globalIsMultiTouch) {
                                if (accumulatedDrag.getDistance() < 15f) {
                                    val displayWidthPx = with(density) { displayWidth.toPx() }
                                    val displayHeightPx = with(density) { displayHeight.toPx() }
                                    val itemCenterX = latestItem.posX + (displayWidthPx * localScale) / 2f
                                    val itemCenterY = latestItem.posY + (displayHeightPx * localScale) / 2f
                                    
                                    val currentTime = System.currentTimeMillis()
                                    if (currentTime - lastTapTime < 300) {
                                        if (viewModel.isHapticEnabled) {
                                            try {
                                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                            } catch (e: Exception) { /* ignore */ }
                                        }
                                        viewModel.toggleZoomOnItem(item, viewportWidth, viewportHeight, density.density)
                                    } else {
                                        if (viewModel.isHapticEnabled && !isSelected) {
                                            try {
                                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                            } catch (e: Exception) { /* ignore */ }
                                        }
                                        viewModel.lastItemTapTime = System.currentTimeMillis()
                                        viewModel.selectItem(item.id)
                                    }
                                    lastTapTime = currentTime
                                }
                            }
                        }
                    }
            }
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Dynamically load full resolution only when zoomed in, selected, or for animated GIFs, otherwise load fast lightweight thumbnail
                val isLargeScale = (localScale * viewModel.canvasScale) > 1.2f
                val shouldLoadFull = isSelected || isLargeScale || item.fullPath.endsWith(".gif", ignoreCase = true)
                val modelPath = if (shouldLoadFull) item.fullPath else item.thumbPath

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(modelPath)
                        .crossfade(true)
                        .allowHardware(true)
                        .build(),
                    placeholder = thumbnailPainter,
                    error = thumbnailPainter,
                    contentDescription = "Reference reference photo",
                    contentScale = ContentScale.Fit,
                    colorFilter = if (latestItem.isGrayscale) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                        .graphicsLayer(
                            scaleX = if (latestItem.flipHorizontal) -1f else 1f,
                            scaleY = if (latestItem.flipVertical) -1f else 1f
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            BorderStroke(
                                width = animatedBorderWidth,
                                color = animatedBorderColor
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                )
                if (latestItem.isPinned) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(((30 / canvasScale).dp).coerceIn(24.dp, 40.dp))
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f),
                                shape = CircleShape
                            )
                            .border(
                                width = (1 / canvasScale).dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(((14 / canvasScale).dp).coerceIn(10.dp, 20.dp))
                        )
                    }
                }
            }
        }

        // Selected handles and gestures on all four corners
        if (isSelected && !latestItem.isPinned) {
            val handleSize = ((28 / canvasScale).dp).coerceIn(20.dp, 36.dp)
            val touchSize = ((56 / canvasScale).dp).coerceIn(44.dp, 64.dp)
            val halfTouchOffset = touchSize / 2

            // 1. Top-Left Handle: Close / Delete with Long Press
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = -halfTouchOffset, y = -halfTouchOffset)
                    .size(touchSize)
                    .pointerInput(latestItem.id) {
                        detectTapGestures(
                            onTap = {
                                Toast.makeText(context, "Long press to delete", Toast.LENGTH_SHORT).show()
                            },
                            onLongPress = {
                                if (viewModel.isHapticEnabled) {
                                    try {
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    } catch (e: Exception) { /* ignore */ }
                                }
                                viewModel.deleteItem(latestItem)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(handleSize)
                        .background(Color(0xE6121212), CircleShape)
                        .border(BorderStroke((1f / canvasScale).dp, Color.White.copy(alpha = 0.9f)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete Item",
                        tint = Color.White,
                        modifier = Modifier.size(((12 / canvasScale).dp).coerceIn(8.dp, 16.dp))
                    )
                }
            }

            // 2. Top-Right Handle: Flip Horizontal
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = halfTouchOffset, y = -halfTouchOffset)
                    .size(touchSize)
                    .clickable {
                        if (viewModel.isHapticEnabled) {
                            try {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            } catch (e: Exception) { /* ignore */ }
                        }
                        viewModel.toggleFlipHorizontal(latestItem)
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(handleSize)
                        .background(Color(0xE6121212), CircleShape)
                        .border(BorderStroke((1f / canvasScale).dp, Color.White.copy(alpha = 0.9f)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Flip,
                        contentDescription = "Flip Horizontal",
                        tint = Color.White,
                        modifier = Modifier.size(((12 / canvasScale).dp).coerceIn(8.dp, 16.dp))
                    )
                }
            }

            // 3. Bottom-Left Handle: Flip Vertical
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = -halfTouchOffset, y = halfTouchOffset)
                    .size(touchSize)
                    .clickable {
                        if (viewModel.isHapticEnabled) {
                            try {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            } catch (e: Exception) { /* ignore */ }
                        }
                        viewModel.toggleFlipVertical(latestItem)
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(handleSize)
                        .background(Color(0xE6121212), CircleShape)
                        .border(BorderStroke((1f / canvasScale).dp, Color.White.copy(alpha = 0.9f)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Flip,
                        contentDescription = "Flip Vertical",
                        tint = Color.White,
                        modifier = Modifier
                            .size(((12 / canvasScale).dp).coerceIn(8.dp, 16.dp))
                            .rotate(90f)
                    )
                }
            }

            // 4. Bottom-Right Handle: Resize
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = halfTouchOffset, y = halfTouchOffset)
                    .size(touchSize)
                    .onGloballyPositioned { handleCoordinates = it }
                    .pointerInput(latestItem.id) {
                        detectDragGestures(
                            onDragStart = { downOffset ->
                                startScale = localScale
                                anchorWindowPos = itemCoordinates?.localToWindow(Offset.Zero) ?: Offset.Zero
                                startTouchWindowPos = handleCoordinates?.localToWindow(downOffset) ?: Offset.Zero
                                isDragging = true
                                isResizing = true
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val currentTouchWindowPos = handleCoordinates?.localToWindow(change.position) ?: Offset.Zero
                                val startDist = CanvasMathHelper.calculateDistance(startTouchWindowPos.x, startTouchWindowPos.y, anchorWindowPos.x, anchorWindowPos.y)
                                val currentDist = CanvasMathHelper.calculateDistance(currentTouchWindowPos.x, currentTouchWindowPos.y, anchorWindowPos.x, anchorWindowPos.y)
                                if (startDist > 0.1f) {
                                    val oldScale = localScale
                                    val newScale = CanvasMathHelper.calculateNewScale(startScale, startDist, currentDist, 0.05f, 10f)
                                    if (newScale != oldScale) {
                                        val (newPosX, newPosY) = adjustPositionForScale(
                                            oldScale = oldScale,
                                            newScale = newScale,
                                            posX = localPosX,
                                            posY = localPosY,
                                            widthPx = displayWidthPx,
                                            heightPx = displayHeightPx,
                                            rotationDegrees = latestItem.rotation,
                                            u = -0.5f,
                                            v = -0.5f
                                        )
                                        localPosX = newPosX
                                        localPosY = newPosY
                                        localScale = newScale
                                    }
                                }
                            },
                            onDragEnd = {
                                isDragging = false
                                isResizing = false
                                viewModel.updateItemScaleAndPositionDirectly(latestItem, localScale, localPosX, localPosY)
                            },
                            onDragCancel = {
                                isDragging = false
                                isResizing = false
                                viewModel.updateItemScaleAndPositionDirectly(latestItem, localScale, localPosX, localPosY)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(handleSize)
                        .background(Color(0xE6121212), CircleShape)
                        .border(BorderStroke((1f / canvasScale).dp, Color.White.copy(alpha = 0.9f)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInFull,
                        contentDescription = "Resize Bottom Right",
                        tint = Color.White,
                        modifier = Modifier.size(((12 / canvasScale).dp).coerceIn(8.dp, 16.dp))
                    )
                }
            }
        }

        // Long press Context Menu explicitly requested
        DropdownMenu(
            expanded = isLongPressMenuExpanded,
            onDismissRequest = { isLongPressMenuExpanded = false },
            modifier = Modifier
                .width(240.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)), shape = RoundedCornerShape(20.dp))
        ) {
            // Section 1: Workspace
            Text(
                text = "WORKSPACE",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            DropdownMenuItem(
                text = { Text("Duplicate", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicate reference",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = {
                    viewModel.duplicateItem(item)
                    isLongPressMenuExpanded = false
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 12.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            )

            // Section 2: Study Tools
            Text(
                text = "STUDY TOOLS",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 4.dp),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            DropdownMenuItem(
                text = { Text(if (latestItem.isPinned) "Unpin Reference" else "Pin Reference", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pin item",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = {
                    viewModel.togglePinItem(latestItem)
                    isLongPressMenuExpanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(if (latestItem.isGrayscale) "Show in Color" else "Study Values (Grayscale)", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Contrast,
                        contentDescription = "Toggle Grayscale",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = {
                    viewModel.toggleGrayscale(latestItem)
                    isLongPressMenuExpanded = false
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 12.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            )

            // Section 3: Arrangement
            Text(
                text = "ARRANGEMENT",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 4.dp),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            DropdownMenuItem(
                text = { Text("Bring to Front", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.FlipToFront,
                        contentDescription = "Bring reference to front",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = {
                    viewModel.bringToFront(item)
                    isLongPressMenuExpanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Send to Back", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Send reference to back",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = {
                    viewModel.sendToBack(item)
                    isLongPressMenuExpanded = false
                }
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 12.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            )
            
            // Section 4: Critical Action
            DropdownMenuItem(
                text = { Text("Delete Reference", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.error) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete reference",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = {
                    viewModel.deleteItem(item)
                    isLongPressMenuExpanded = false
                }
            )
        }

    }
}

@Composable
fun ZoomPercentageText(viewModel: DroidCanvasViewModel) {
    Text(
        text = "${(viewModel.canvasScale * 100).roundToInt()}%",
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold
    )
}

private data class BackgroundOption(
    val type: String,
    val label: String,
    val desc: String,
    val previewColor: Color
)

@Composable
fun DrawingToolbar(
    viewModel: DroidCanvasViewModel,
    modifier: Modifier = Modifier
) {
    val isEraserMode = viewModel.isEraserModeEnabled
    val currentWidth = viewModel.drawingWidth
    val currentColor = viewModel.drawingColor
    
    Card(
        modifier = modifier
            .padding(16.dp)
            .shadow(12.dp, shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pen button
                IconButton(
                    onClick = { viewModel.isEraserModeEnabled = false },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (!isEraserMode) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        contentColor = if (!isEraserMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Pen Tool"
                    )
                }
                
                // Eraser button
                IconButton(
                    onClick = { viewModel.isEraserModeEnabled = true },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isEraserMode) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        contentColor = if (isEraserMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Contrast,
                        contentDescription = "Eraser Tool"
                    )
                }
                
                // Vertical divider
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                
                // Color presets
                val colors = listOf(
                    0xFFF44336.toInt(), // Red
                    0xFFFFEB3B.toInt(), // Yellow
                    0xFF2196F3.toInt(), // Blue
                    0xFF4CAF50.toInt(), // Green
                    0xFF000000.toInt(), // Black
                    0xFFFFFFFF.toInt()  // White
                )
                
                colors.forEach { colorInt ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(colorInt))
                            .border(
                                width = if (currentColor == colorInt) 3.dp else 1.dp,
                                color = if (currentColor == colorInt) {
                                    if (colorInt == 0xFFFFFFFF.toInt()) Color.Black else MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                },
                                shape = CircleShape
                            )
                            .clickable {
                                viewModel.drawingColor = colorInt
                                viewModel.isEraserModeEnabled = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentColor == colorInt) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = if (colorInt == 0xFFFFEB3B.toInt() || colorInt == 0xFFFFFFFF.toInt()) Color.Black else Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                
                // Vertical divider
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                
                // Clear drawings button
                IconButton(
                    onClick = { viewModel.clearDrawingStrokes() },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear Drawings"
                    )
                }
            }
            
            // Stroke width slider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "Size",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = currentWidth,
                    onValueChange = { viewModel.drawingWidth = it },
                    valueRange = 2f..40f,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${currentWidth.roundToInt()}px",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(36.dp)
                )
            }
        }
    }
}

@Composable
fun SidebarSectionHeader(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                modifier = Modifier
                    .size(18.dp)
                    .rotate(if (isExpanded) 0f else -90f),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
        if (action != null) {
            Box(
                modifier = Modifier.clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                )
            ) {
                action()
            }
        }
    }
}

@Composable
fun SidebarContent(
    viewModel: DroidCanvasViewModel,
    viewportWidth: Float,
    viewportHeight: Float,
    density: Float,
    pickerLauncher: androidx.activity.result.ActivityResultLauncher<androidx.activity.result.PickVisualMediaRequest>,
    onCloseSidebar: () -> Unit,
    onCreateBoardClick: () -> Unit,
    onRenameBoardClick: (Board) -> Unit,
    onManageBoardsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val boards by viewModel.boards.collectAsState()
    val otherBoardsExist = boards.any { it.name.trim().lowercase() != "main board" }
    val visibleBoards = if (otherBoardsExist) {
        boards.filter { it.name.trim().lowercase() != "main board" }
    } else {
        boards
    }
    val activeBoardId by viewModel.currentBoardId.collectAsState()
    val canvasItems by viewModel.canvasItems.collectAsState()
    val isDrawModeEnabled = viewModel.isDrawModeEnabled
    val currentWidth = viewModel.drawingWidth
    val currentColor = viewModel.drawingColor
    val isEraserModeEnabled = viewModel.isEraserModeEnabled
    val selectedItemId by viewModel.selectedItemId.collectAsState()
    
    // Collapsible states
    var isWorkspacesExpanded by remember { mutableStateOf(true) }
    var isInspectorExpanded by remember { mutableStateOf(true) }
    var isViewportExpanded by remember { mutableStateOf(true) }
    var isLayersExpanded by remember { mutableStateOf(true) }
    var isDrawingExpanded by remember { mutableStateOf(true) }
    var isSettingsExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Sidebar Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DroidCanvas Pro",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Workspace Panel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            IconButton(
                onClick = onCloseSidebar,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Collapse Sidebar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 2: Workspace Boards (Quick Selection List)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SidebarSectionHeader(
                    title = "WORKSPACES",
                    isExpanded = isWorkspacesExpanded,
                    onToggle = { isWorkspacesExpanded = !isWorkspacesExpanded },
                    action = {
                        IconButton(
                            onClick = onCreateBoardClick,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New Workspace",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                )

                androidx.compose.animation.AnimatedVisibility(
                    visible = isWorkspacesExpanded,
                    enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        visibleBoards.forEach { board ->
                            val isActive = board.id == activeBoardId
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                                border = BorderStroke(
                                    1.dp,
                                    if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.selectBoard(board.id) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (isActive) Icons.Default.Check else Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = board.name,
                                            color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = { onRenameBoardClick(board) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Rename Board",
                                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        if (visibleBoards.size > 1) {
                                            IconButton(
                                                onClick = { viewModel.deleteBoard(board) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete Board",
                                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            )

            // Section 3: Canvas Navigation & Viewport Controller (Viewport scale + pan recentering)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SidebarSectionHeader(
                    title = "VIEWPORT CONTROLLER",
                    isExpanded = isViewportExpanded,
                    onToggle = { isViewportExpanded = !isViewportExpanded }
                )

                androidx.compose.animation.AnimatedVisibility(
                    visible = isViewportExpanded,
                    enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Scale / Zoom",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${(viewModel.canvasScale * 100).roundToInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        viewModel.canvasScale = (viewModel.canvasScale - 0.25f).coerceIn(0.1f, 4f)
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close, // Minus representation
                                        contentDescription = "Zoom Out",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                Slider(
                                    value = viewModel.canvasScale,
                                    onValueChange = { viewModel.canvasScale = it },
                                    valueRange = 0.1f..4.0f,
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(
                                    onClick = {
                                        viewModel.canvasScale = (viewModel.canvasScale + 0.25f).coerceIn(0.1f, 4f)
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add, // Plus representation
                                        contentDescription = "Zoom In",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.resetZoom() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reset View", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        viewModel.fitToContent(viewportWidth, viewportHeight, density)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CenterFocusStrong,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Fit All", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Section 4: Selected Image Inspector (Desktop-grade control center on Tablet!)
            val selectedItem = canvasItems.find { it.id == selectedItemId }
            if (selectedItem != null) {
                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SidebarSectionHeader(
                        title = "ELEMENT INSPECTOR",
                        isExpanded = isInspectorExpanded,
                        onToggle = { isInspectorExpanded = !isInspectorExpanded }
                    )

                    androidx.compose.animation.AnimatedVisibility(
                        visible = isInspectorExpanded,
                        enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            ),
                            border = BorderStroke(
                                1.5.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val itemIndex = canvasItems.indexOf(selectedItem)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Image,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Image #${itemIndex + 1}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    
                                    IconButton(
                                        onClick = { viewModel.selectItem(null) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Deselect",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                // 1. Fine Positioning Nudges
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Position (Canvas Space)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "X: ${selectedItem.posX.roundToInt()}  Y: ${selectedItem.posY.roundToInt()}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        listOf(
                                            "←" to { viewModel.updateItemPosition(selectedItem, -10f, 0f) },
                                            "→" to { viewModel.updateItemPosition(selectedItem, 10f, 0f) },
                                            "↑" to { viewModel.updateItemPosition(selectedItem, 0f, -10f) },
                                            "↓" to { viewModel.updateItemPosition(selectedItem, 0f, 10f) }
                                        ).forEach { (label, onClick) ->
                                            OutlinedButton(
                                                onClick = onClick,
                                                contentPadding = PaddingValues(0.dp),
                                                modifier = Modifier.weight(1f).height(28.dp),
                                                shape = RoundedCornerShape(6.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.primary
                                                ),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                            ) {
                                                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                // 2. Scale Control
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Scale Factor",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "${(selectedItem.scale * 100).roundToInt()}%",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            IconButton(
                                                onClick = { viewModel.updateItemAbsoluteScale(selectedItem, 1f) },
                                                modifier = Modifier.size(16.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = "Reset Scale",
                                                    modifier = Modifier.size(10.dp)
                                                )
                                            }
                                        }
                                    }
                                    Slider(
                                        value = selectedItem.scale,
                                        onValueChange = { viewModel.updateItemAbsoluteScale(selectedItem, it) },
                                        valueRange = 0.05f..3.0f,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // 3. Rotation Control
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Rotation Angle",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "${selectedItem.rotation.roundToInt()}°",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            IconButton(
                                                onClick = { viewModel.updateItemAbsoluteRotation(selectedItem, 0f) },
                                                modifier = Modifier.size(16.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = "Reset Rotation",
                                                    modifier = Modifier.size(10.dp)
                                                )
                                            }
                                        }
                                    }
                                    Slider(
                                        value = selectedItem.rotation,
                                        onValueChange = { viewModel.updateItemAbsoluteRotation(selectedItem, it) },
                                        valueRange = 0f..360f,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // 4. Image Filters & Locks (Grayscale, Flips, Pins)
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Transforms & Filters",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Grayscale Toggle
                                        val isGrayscale = selectedItem.isGrayscale
                                        IconButton(
                                            onClick = { viewModel.toggleGrayscale(selectedItem) },
                                            modifier = Modifier.size(36.dp),
                                            colors = IconButtonDefaults.iconButtonColors(
                                                containerColor = if (isGrayscale) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                                                contentColor = if (isGrayscale) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.FilterBAndW,
                                                contentDescription = "Grayscale Mode",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // Flip Horizontal
                                        val isFlippedH = selectedItem.flipHorizontal
                                        IconButton(
                                            onClick = { viewModel.toggleFlipHorizontal(selectedItem) },
                                            modifier = Modifier.size(36.dp),
                                            colors = IconButtonDefaults.iconButtonColors(
                                                containerColor = if (isFlippedH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                                                contentColor = if (isFlippedH) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Flip,
                                                contentDescription = "Flip Horizontal",
                                                modifier = Modifier.size(18.dp).rotate(90f)
                                            )
                                        }

                                        // Flip Vertical
                                        val isFlippedV = selectedItem.flipVertical
                                        IconButton(
                                            onClick = { viewModel.toggleFlipVertical(selectedItem) },
                                            modifier = Modifier.size(36.dp),
                                            colors = IconButtonDefaults.iconButtonColors(
                                                containerColor = if (isFlippedV) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                                                contentColor = if (isFlippedV) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Flip,
                                                contentDescription = "Flip Vertical",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // Pin state
                                        val isPinned = selectedItem.isPinned
                                        IconButton(
                                            onClick = { viewModel.togglePinItem(selectedItem) },
                                            modifier = Modifier.size(36.dp),
                                            colors = IconButtonDefaults.iconButtonColors(
                                                containerColor = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                                                contentColor = if (isPinned) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PushPin,
                                                contentDescription = "Pin Item",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                // 5. Layer Ordering Quick Actions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.bringToFront(selectedItem) },
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.primary
                                        ),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                                    ) {
                                        Icon(Icons.Default.FlipToFront, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Front", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.sendToBack(selectedItem) },
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.primary
                                        ),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                                    ) {
                                        Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Back", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // 6. Utility Actions (Center, Duplicate, Delete)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.centerOnItem(selectedItem, viewportWidth, viewportHeight, density)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1.5f).height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(Icons.Default.CenterFocusStrong, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Center Camera", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { viewModel.duplicateItem(selectedItem) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { viewModel.deleteItem(selectedItem) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Delete", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            )

            // Section 5: Reference Images (Layers)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SidebarSectionHeader(
                    title = "IMAGES ON BOARD (${canvasItems.size})",
                    isExpanded = isLayersExpanded,
                    onToggle = { isLayersExpanded = !isLayersExpanded },
                    action = {
                        IconButton(
                            onClick = {
                                pickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Import Images",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                )

                androidx.compose.animation.AnimatedVisibility(
                    visible = isLayersExpanded,
                    enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                ) {
                    if (canvasItems.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No images on this board.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.padding(12.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            canvasItems.forEachIndexed { index, item ->
                                val isSelected = viewModel.selectedItemId.value == item.id
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            viewModel.selectItem(item.id)
                                            viewModel.centerOnItem(item, viewportWidth, viewportHeight, density)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Image,
                                                contentDescription = null,
                                                tint = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Image #${index + 1} (${(item.scale * 100).roundToInt()}%)",
                                                color = MaterialTheme.colorScheme.onSurface,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            if (item.isPinned) {
                                                Icon(
                                                    imageVector = Icons.Default.PushPin,
                                                    contentDescription = "Pinned",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                            // Delete Item icon for quick access
                                            IconButton(
                                                onClick = { viewModel.deleteItem(item) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete Image",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            )

            // Section 6: Drawing Tool Controls
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SidebarSectionHeader(
                    title = "DRAWING TOOLS",
                    isExpanded = isDrawingExpanded,
                    onToggle = { isDrawingExpanded = !isDrawingExpanded },
                    action = {
                        Switch(
                            checked = viewModel.isDrawModeEnabled,
                            onCheckedChange = { viewModel.isDrawModeEnabled = it },
                            modifier = Modifier.scale(0.8f)
                        )
                    }
                )

                androidx.compose.animation.AnimatedVisibility(
                    visible = isDrawingExpanded,
                    enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                ) {
                    if (viewModel.isDrawModeEnabled) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Pen vs Eraser Row selection
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.isEraserModeEnabled = false },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (!isEraserModeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                        contentColor = if (!isEraserModeEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Brush", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pen", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { viewModel.isEraserModeEnabled = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isEraserModeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                        contentColor = if (isEraserModeEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Layers, contentDescription = "Eraser", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Eraser", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Colors Palette
                            if (!isEraserModeEnabled) {
                                Text(
                                    text = "Colors Preset",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val colors = listOf(
                                    0xFFF44336.toInt(), // Red
                                    0xFFFFEB3B.toInt(), // Yellow
                                    0xFF2196F3.toInt(), // Blue
                                    0xFF4CAF50.toInt(), // Green
                                    0xFF000000.toInt(), // Black
                                    0xFFFFFFFF.toInt()  // White
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    colors.forEach { colorInt ->
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .background(Color(colorInt))
                                                .border(
                                                    width = if (currentColor == colorInt) 3.dp else 1.dp,
                                                    color = if (currentColor == colorInt) {
                                                        if (colorInt == 0xFFFFFFFF.toInt()) Color.Black else MaterialTheme.colorScheme.primary
                                                    } else {
                                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                                    },
                                                    shape = CircleShape
                                                )
                                                .clickable {
                                                    viewModel.drawingColor = colorInt
                                                    viewModel.isEraserModeEnabled = false
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (currentColor == colorInt) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = if (colorInt == 0xFFFFEB3B.toInt() || colorInt == 0xFFFFFFFF.toInt()) Color.Black else Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Brush Width
                            Text(
                                text = "Brush Size (${currentWidth.roundToInt()}px)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Slider(
                                    value = currentWidth,
                                    onValueChange = { viewModel.drawingWidth = it },
                                    valueRange = 2f..40f,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Clear Board Drawings
                            OutlinedButton(
                                onClick = { viewModel.clearDrawingStrokes() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Clear drawings", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Clear Board Drawings", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Text(
                            text = "Enable drawing mode to unlock pen options.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            )

            // Section 7: Board settings & controls
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SidebarSectionHeader(
                    title = "BOARD SETTINGS",
                    isExpanded = isSettingsExpanded,
                    onToggle = { isSettingsExpanded = !isSettingsExpanded }
                )

                androidx.compose.animation.AnimatedVisibility(
                    visible = isSettingsExpanded,
                    enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Lock Board Button
                            Button(
                                onClick = { viewModel.toggleLock() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (viewModel.isLocked) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                                    contentColor = if (viewModel.isLocked) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = if (viewModel.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = "Lock",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (viewModel.isLocked) "Locked" else "Lock Board", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Auto-Arrange Button
                            Button(
                                onClick = { viewModel.autoArrangeGrid("GRID", density) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.GridView, contentDescription = "Arrange", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Grid Arrange", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Theme quick switches
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val styles = listOf(
                                "dots" to "Dots",
                                "lines" to "Lines",
                                "graph" to "Graph",
                                "none" to "None"
                            )
                            styles.forEach { (styleKey, label) ->
                                val isSelected = viewModel.gridStyle == styleKey
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceContainerHigh
                                        )
                                        .clickable { viewModel.gridStyle = styleKey }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
