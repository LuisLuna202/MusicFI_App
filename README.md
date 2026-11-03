# MusicFI

Aplicación Android nativa para consultar y reproducir archivos de audio locales. Está desarrollada con Kotlin, Jetpack Compose y Material 3.

## Funcionalidades

- Pantalla de bienvenida y navegación entre pantallas.
- Pantalla de inicio de sesión de demostración. **No existe un backend ni autenticación real**; se validan únicamente los campos vacíos.
- Solicitud del permiso de lectura de audio en tiempo de ejecución.
- Consulta de canciones locales mediante `MediaStore`.
- Búsqueda por título o artista, sin distinguir mayúsculas y minúsculas.
- Reproducción, pausa, canción anterior y siguiente.
- Lectura de portadas de álbum desde los URI de MediaStore, con icono alternativo cuando no hay portada.
- Liberación del reproductor al cambiar de canción y al salir de la pantalla.

## Modelo de capas

El código se separa en responsabilidades:

- `model/LocalSong.kt`: modelo de datos de una canción.\n- `domain/MusicLibraryUseCase.kt`: reglas de búsqueda y validación del formulario.
- `data/AudioRepository.kt`: contrato del repositorio y consulta de la biblioteca local mediante MediaStore.
- `data/AudioPlayer.kt`: contrato y adaptación de `MediaPlayer`.
- `MainActivity.kt`: actividad, navegación y componentes de interfaz Compose.

La interfaz no ejecuta directamente consultas SQL ni administra el ciclo de vida interno de `MediaPlayer`; delega esas tareas a las clases de datos.

## Imágenes

La aplicación conserva el logotipo `logo_musicfi.png`. Para cada canción se intenta obtener la portada del álbum mediante MediaStore. Si no existe o no se puede leer, se muestra un icono musical de respaldo. La carga de portada se realiza fuera del hilo principal.

## Permisos

- Android 13 o superior: `READ_MEDIA_AUDIO`.
- Android 12 o inferior: `READ_EXTERNAL_STORAGE`.

El usuario puede conceder el permiso desde la pantalla principal. Si lo deniega, se conserva la interfaz y se muestra una explicación.

## Pruebas

Pruebas unitarias (`app/src/test`):
- búsqueda vacía;
- búsqueda por título ignorando mayúsculas;
- búsqueda por artista y espacios;
- búsqueda sin coincidencias;
- validación de campos del formulario.

Pruebas de instrumentación (`app/src/androidTest`):
- contenido de la pantalla de bienvenida;
- navegación a la biblioteca;
- navegación a inicio de sesión.

## Compilar y probar

Requiere Android Studio con el Android SDK instalado y una conexión para descargar las dependencias la primera vez.

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

Las pruebas de instrumentación requieren un emulador o dispositivo Android conectado. La reproducción real y la lectura de portadas deben validarse con archivos de audio presentes en ese dispositivo.

## Estado de verificación de esta entrega

Se revisaron estáticamente los archivos fuente y la sintaxis XML de los recursos y del manifiesto. **No se pudo ejecutar Gradle en el entorno de revisión**: el wrapper intentó descargar Gradle 9.5.0 y la conexión de red falló (`UnknownHostException` para `services.gradle.org`). Tampoco hay un Android SDK ni un emulador disponible en este entorno. Por ello, las pruebas añadidas están preparadas en el proyecto, pero todavía deben ejecutarse en Android Studio o en una máquina con SDK y dispositivo/emulador; no se afirma que hayan pasado.
