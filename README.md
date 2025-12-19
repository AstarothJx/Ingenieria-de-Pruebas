# 🐾 PawsAndGo - App de Paseo de Perros

**PawsAndGo** es una aplicación nativa de Android desarrollada con **Kotlin** y **Jetpack Compose** que conecta a dueños de mascotas con paseadores verificados. La aplicación gestiona todo el ciclo del servicio: desde la reserva y selección de paseadores hasta el seguimiento en tiempo real con una simulación avanzada de GPS y chat interactivo.

---

## 📱 Características Principales

### 👤 Para Dueños (Owners)
* **Gestión de Mascotas:** Registro de perfiles de mascotas con fotos y detalles.
* **Reserva de Paseos:** Selección de paseadores basada en calificación, precio y disponibilidad.
* **Seguimiento en Vivo:** Visualización de una **ruta GPS simulada** con animación vectorial.
* **Chat Interactivo:** Comunicación en tiempo real con el paseador (con respuestas automáticas simuladas).
* **Sistema de Calificación:** Posibilidad de calificar y dar propina al paseador al finalizar.

### 🚶 Para Paseadores (Walkers)
* **Dashboard Financiero:** Resumen visual de ganancias (Tarifa base vs. Propinas) y estadísticas.
* **Gestión de Rutas:** Activación y desactivación de zonas de paseo.
* **Agenda:** Visualización de paseos programados y completados.
* **Simulación de Trabajo:** Ejecución del paseo con eventos automáticos (fotos, mensajes de estado).

---

## 🛠️ Tecnologías Utilizadas

* **Lenguaje:** Kotlin 100%
* **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetbrains/compose) (Material Design 3)
* **Arquitectura:** MVVM (Model-View-ViewModel) concept
* **Persistencia de Datos:** SharedPreferences (Simulación de Backend local con `DataRepository`)
* **Gráficos y Animación:**
    * `Canvas` y `PathMeasure` para la simulación vectorial de rutas GPS.
    * `animateFloatAsState` para interpolación de movimiento suave.
* **Concurrencia:** Kotlin Coroutines.

---

## 📸 Capturas de Pantalla

| Login / Registro | Home Dueño | Home Paseador |
|:---:|:---:|:---:|
| <img src="ruta/a/tu/imagen1.png" width="200"/> | <img src="ruta/a/tu/imagen2.png" width="200"/> | <img src="ruta/a/tu/imagen3.png" width="200"/> |

| Mapa en Vivo (GPS) | Chat Interactivo | Historial |
|:---:|:---:|:---:|
| <img src="ruta/a/tu/imagen4.png" width="200"/> | <img src="ruta/a/tu/imagen5.png" width="200"/> | <img src="ruta/a/tu/imagen6.png" width="200"/> |

> *Nota: Las imágenes son demostrativas de la interfaz construida con Jetpack Compose.*

---

## 🚀 Instalación y Uso

1.  **Clonar el repositorio:**
    ```bash
    git clone [https://github.com/TU_USUARIO/TU_REPO.git](https://github.com/TU_USUARIO/TU_REPO.git)
    ```
2.  **Abrir en Android Studio:**
    * Selecciona `File` > `Open` y busca la carpeta del proyecto.
3.  **Compilar:**
    * Espera a que Gradle sincronice las dependencias.
    * Ejecuta la app en un emulador o dispositivo físico (Min SDK 24+).

### 🧪 Usuarios de Prueba (Demo)
La aplicación cuenta con datos precargados para facilitar las pruebas:

* **Dueño:** Se crea automáticamente o puedes registrar uno nuevo.
* **Paseador:** Puedes iniciar sesión como paseador para ver el otro lado de la app (o usar los paseadores "Ana" o "Beto" generados por el sistema).

---

## 📍 Destacado Técnico: Simulación GPS

Uno de los retos principales fue crear una experiencia de "Paseo en Vivo" sin depender de una API de mapas de pago (como Google Maps API).

Se implementó una solución personalizada usando **Matemáticas Vectoriales**:
* Se dibuja un mapa urbano usando `Canvas`.
* Se define una ruta compleja usando curvas de Bézier (`CubicTo`, `QuadraticBezierTo`).
* Se utiliza `android.graphics.PathMeasure` para calcular la tangente y la coordenada exacta `(x,y)` del icono del perro en cada frame de la animación, permitiendo que el icono gire y avance orgánicamente sobre la ruta dibujada.

---

## 📄 Licencia

Este proyecto es de uso académico y personal.

---
**Desarrollado por [Tu Nombre]**
Estudiante de Ingeniería en Informática 💻
