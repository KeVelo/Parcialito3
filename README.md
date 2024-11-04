# Proyecto de Captura de Fotos con CameraX y Jetpack Compose

Este proyecto es una aplicación de Android que permite capturar fotos utilizando **CameraX** y una interfaz de usuario construida con **Jetpack Compose**. La aplicación cuenta con funciones para ajustar el zoom, alternar entre la cámara frontal y trasera, activar/desactivar el flash, y ver las fotos capturadas en una galería dentro de la aplicación.

## Características

- **Captura de Fotos**: Permite capturar fotos utilizando la cámara del dispositivo.
- **Galería Interna**: Muestra las fotos capturadas en una galería dentro de la aplicación.
- **Zoom Ajustable**: Control de zoom integrado en la interfaz de usuario.
- **Flash Activable**: Permite activar o desactivar el flash de la cámara.
- **Cambio de Cámara**: Alterna entre la cámara frontal y trasera.
- **Interfaz con Jetpack Compose**: Interfaz moderna y reactiva, creada usando Jetpack Compose.

## Tecnologías Utilizadas

- **Kotlin**: Lenguaje de programación principal para el desarrollo de la app.
- **CameraX**: Biblioteca para manejo de la cámara en Android de manera simplificada.
- **Jetpack Compose**: Biblioteca de interfaz de usuario declarativa para Android.
- **Coil**: Biblioteca de carga de imágenes para mostrar las fotos capturadas en la galería.

## Estructura del Proyecto

- **MainActivity**: Actividad principal que configura la interfaz de usuario y controla la funcionalidad de la cámara.
- **CameraScreen**: Composable que muestra la vista previa de la cámara, controles de flash, zoom, cambio de cámara y captura de foto.
- **GalleryScreen**: Composable que muestra las fotos capturadas en una cuadrícula.
- **FullImageView**: Composable que muestra una foto en pantalla completa al seleccionar una imagen en la galería.

## Instalación y Configuración

1. **Clonar el Repositorio**:
   Clona este repositorio en tu máquina local.

   git clone https://github.com/tu_usuario/tu_repositorio.git

2. Abrir en Android Studio: Abre Android Studio y selecciona la opción Open an existing project. Navega a la carpeta del proyecto clonado y ábrelo.

3. Sincronizar Dependencias: Una vez que el proyecto se abre en Android Studio, sincroniza las dependencias en build.gradle si es necesario.

4. Ejecutar en un Emulador o Dispositivo Real: Conecta un dispositivo Android o usa un emulador para ejecutar la aplicación desde Android Studio.

## Uso de la Aplicación
### Vista de Cámara:

La aplicación inicia en la vista de cámara.
La vista de cámara incluye controles para ajustar el zoom, activar/desactivar el flash y alternar entre la cámara frontal y trasera.
Presiona el botón Capturar Foto para tomar una foto. La foto se guardará en el almacenamiento específico de la aplicación.

### Galería de Fotos:

Presiona el botón Abrir Galería para ver las fotos capturadas en una galería interna.
En la galería, puedes ver una cuadrícula de las fotos capturadas. Selecciona una foto para verla en pantalla completa.

### Opciones en Vista Completa:

Al ver una foto en pantalla completa, puedes regresar a la galería o cerrar la vista completa.
