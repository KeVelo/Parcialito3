package com.sv.ufg.fis.amb.p3_mg101121

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.rememberImagePainter
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    // Variables que controlan la cámara y sus configuraciones
    private lateinit var previewView: PreviewView // Vista de la cámara
    private var lensFacing = CameraSelector.LENS_FACING_BACK // Dirección de la cámara (trasera por defecto)
    private var flashEnabled = false // Estado del flash
    private lateinit var cameraExecutor: ExecutorService // Ejecutor para manejar la cámara en un hilo separado
    private lateinit var imageCapture: ImageCapture // Configuración para capturar fotos
    private var zoomLevel by mutableStateOf(1.0f) // Nivel de zoom inicial
    private var camera: Camera? = null // Objeto de la cámara
    private val capturedPhotos = mutableStateListOf<File>() // Lista de fotos capturadas

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        previewView = PreviewView(this) // Inicialización de la vista de cámara
        cameraExecutor = Executors.newSingleThreadExecutor() // Ejecutor para operaciones de la cámara

        setContent {
            var showGallery by remember { mutableStateOf(false) } // Estado para mostrar/ocultar la galería
            var selectedPhoto by remember { mutableStateOf<File?>(null) } // Foto seleccionada para ver en pantalla completa
            val context = LocalContext.current

            // Si la galería está abierta, muestra la pantalla de galería; de lo contrario, muestra la pantalla de cámara
            if (showGallery) {
                GalleryScreen(
                    onBack = { showGallery = false }, // Vuelve a la pantalla de cámara
                    onPhotoSelected = { selectedPhoto = it } // Muestra la foto seleccionada en vista completa
                )
            } else {
                selectedPhoto?.let { photo ->
                    // Pantalla de vista completa de la foto
                    FullImageView(
                        photoFile = photo,
                        onBack = { selectedPhoto = null },
                        onReturnToGallery = { showGallery = true }
                    )
                } ?: CameraScreen(
                    context = context,
                    onOpenGallery = { showGallery = true }, // Abre la galería
                    onCapturePhoto = { capturePhoto(context) }, // Captura una foto
                    onPhotoPreviewClick = { selectedPhoto = it } // Muestra la foto en vista completa
                )
            }

            // Inicia la cámara después de configurar la vista previa
            previewView.post {
                startCamera()
            }
        }
    }

    // Interfaz de usuario de la pantalla de cámara
    @Composable
    fun CameraScreen(
        context: Context,
        onOpenGallery: () -> Unit,
        onCapturePhoto: () -> Unit,
        onPhotoPreviewClick: (File) -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Vista previa de la cámara
            AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Control de Zoom
            Text(text = "Nivel de Zoom: ${"%.1f".format(zoomLevel)}")
            Slider(
                value = zoomLevel,
                onValueChange = { newZoom ->
                    zoomLevel = newZoom
                    camera?.cameraControl?.setZoomRatio(newZoom) // Actualiza el zoom de la cámara
                },
                valueRange = 1.0f..4.0f,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Fila de botones de control (Flash y Cambiar Cámara)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    flashEnabled = !flashEnabled // Alterna el estado del flash
                    updateFlashMode() // Actualiza el modo del flash en la cámara
                }) {
                    Text(text = if (flashEnabled) "Flash Activado" else "Flash Desactivado")
                }

                Button(onClick = { switchCamera() }) { // Alterna entre cámara frontal y trasera
                    Text(text = "Cambiar Cámara")
                }
            }

            // Fila de botones de acción (Capturar Foto y Abrir Galería)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onCapturePhoto) { // Captura una foto
                    Text(text = "Capturar Foto")
                }

                Button(onClick = onOpenGallery) { // Abre la galería de fotos capturadas
                    Text(text = "Abrir Galería")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vista previa de la última foto capturada
            capturedPhotos.lastOrNull()?.let { lastPhoto ->
                Image(
                    painter = rememberImagePainter(lastPhoto),
                    contentDescription = "Vista previa de la última foto",
                    modifier = Modifier
                        .size(100.dp)
                        .clickable { onPhotoPreviewClick(lastPhoto) } // Abre la foto seleccionada en pantalla completa
                )
            }
        }
    }

    // Inicia la cámara y configura las opciones de captura de imagen
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Configuración de la vista previa
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            // Selector de cámara (frontal o trasera)
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            // Configuración de captura de imagen
            imageCapture = ImageCapture.Builder()
                .setFlashMode(if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF)
                .build()

            try {
                // Desvincula cualquier uso anterior y vincula la nueva configuración de cámara
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e("CameraXApp", "Error al abrir la cámara: ${exc.message}", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // Actualiza el modo de flash según el estado de flashEnabled
    private fun updateFlashMode() {
        imageCapture.flashMode = if (flashEnabled) {
            ImageCapture.FLASH_MODE_ON
        } else {
            ImageCapture.FLASH_MODE_OFF
        }
    }

    // Alterna entre cámara frontal y trasera
    private fun switchCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        startCamera() // Reinicia la cámara para aplicar el cambio
    }

    // Captura una foto y la guarda en el almacenamiento externo del dispositivo
    private fun capturePhoto(context: Context) {
        val photoFile = File(
            context.externalMediaDirs.first(),
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    capturedPhotos.add(photoFile) // Añade la foto a la lista de fotos capturadas
                    Toast.makeText(context, "Foto capturada", Toast.LENGTH_SHORT).show()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraXApp", "Error al capturar foto: ${exception.message}", exception)
                    Toast.makeText(context, "Error al capturar", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Cierra el ejecutor de la cámara al destruir la actividad para liberar recursos
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    // Pantalla de galería que muestra las fotos capturadas en una cuadrícula
    @Composable
    fun GalleryScreen(
        onBack: () -> Unit,
        onPhotoSelected: (File) -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Galería de Fotos", modifier = Modifier.padding(bottom = 8.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Cerrar Galería")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cuadrícula de fotos capturadas
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize()
            ) {
                items(capturedPhotos) { photo ->
                    Image(
                        painter = rememberImagePainter(photo),
                        contentDescription = "Foto capturada",
                        modifier = Modifier
                            .padding(4.dp)
                            .size(100.dp)
                            .clickable { onPhotoSelected(photo) } // Abre la foto en pantalla completa
                    )
                }
            }
        }
    }

    // Pantalla de vista completa de una foto seleccionada
    @Composable
    fun FullImageView(
        photoFile: File,
        onBack: () -> Unit,
        onReturnToGallery: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = onReturnToGallery, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Regresar a Galería")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Imagen de la foto en pantalla completa
            Image(
                painter = rememberImagePainter(photoFile),
                contentDescription = "Vista completa de la foto",
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Cerrar Vista")
            }
        }
    }
}
