# SendaURJC - Seguridad Percibida en el Campus

**SendaURJC** es un prototipo funcional de aplicación Android diseñado para mejorar la seguridad percibida de la comunidad universitaria en el Campus de Móstoles de la URJC. La aplicación combina geolocalización, cálculo de rutas seguras y herramientas de reporte ciudadano para ofrecer un entorno más protegido.

## 📝 Descripción del Software
Prototipo completo con ubicación simulada, rutas, creación de incidencias editables, modo oscuro, contactos de emergencia, modo alerta simulado con alerta de prueba, modo de acompañamiento simulado, SSO simulado y datos de LumenSmart simulados. 

Los círculos rojos que se observan en el mapa simbolizan zonas inseguras que evitan las rutas y en el producto final solo será visible para los administradores.

---

## 🚀 Funcionalidades Principales

### 1. 🗺️ Navegación y Rutas Seguras
- **Cálculo de Rutas:** Integración con **OSRM** para rutas peatonales.
- **Seguridad Percibida:** Clasificación de tramos de ruta mediante colores:
  - 🟢 **Verde:** Zonas seguras y bien iluminadas.
  - 🔴 **Rojo:** Zonas con baja iluminación o "puntos oscuros" (detectados dinámicamente mediante `MockLumenSmartDataSource`).
- **Búsqueda de Destinos:** Buscador integrado con puntos de interés (POIs) del campus.

### 2. 🚨 Modo Alerta y Protección
- **Monitoreo en Tiempo Real:** Servicio en primer plano (**Foreground Service**) que vigila la ubicación GPS.
- **Detección de Inactividad:** Si el usuario se detiene más de 30 segundos en una zona no segura, se activa una **Pre-alerta**.
- **Gestión de Contactos:** Permite configurar contactos de emergencia que serían notificados automáticamente.

### 3. 🤝 Modo "Voy Contigo"
- Simulación de acompañamiento por parte de voluntarios de la comunidad universitaria.
- Interfaz interactiva de búsqueda y asignación de acompañantes.

### 4. 🛠️ Gestión de Incidencias (Reporte Ciudadano)
- **Reportar:** FAB para notificar problemas como "Farola rota", "Zona sucia" o "Mal aspecto".
- **Persistencia:** Almacenamiento local mediante **Room**.
- **Visualización:** Los reportes aparecen como marcadores amarillos en el mapa para informar a otros usuarios.

---

## 🛠️ Stack Tecnológico

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Mapas:** [osmdroid](https://github.com/osmdroid/osmdroid) (OpenStreetMap)
- **Red:** Retrofit + OkHttp para consumos de APIs externas:
  - **OSRM:** Para el motor de enrutamiento.
  - **Overpass API:** Para la extracción dinámica de datos geográficos (edificios, zonas verdes).
- **Base de Datos:** Room (Persistencia de incidencias y datos locales).
- **Arquitectura:** MVVM (Model-View-ViewModel).

---

## 📂 Estructura del Proyecto

```text
com.sendaurjc
├── data/           # Repositorios, DAOs (Room), Modelos de Red y Mock Data
├── domain/         # Lógica de negocio (Routing, Repositorios de dominio)
├── service/        # Servicios de Android (AlertForegroundService)
├── ui/             # UI Layer (Compose Screens, ViewModels, Navigation)
│   ├── screen/     # Main, Login, Emergency, Incident Management, etc.
│   └── theme/      # Definición de colores y estilos (soporte Modo Oscuro)
└── util/           # Utilidades de Geometría y Traducción
```

---

## ⚙️ Instalación y Configuración

Existen 2 opciones para instalar la aplicación:

1. **Compilar desde el código fuente:**
   - **Requisitos:** Android Studio Hedgehog (o superior) y JDK 17.
   - **Clonar y Sincronizar:** Importa el proyecto y realiza un *Gradle Sync*.
   - **Ejecutar:** Conecta un dispositivo o inicia un emulador y pulsa "Run".

2. **Descargar el último release e instalarlo en un dispositivo Android:**
   - Ve a la sección de **Releases** de este repositorio.
   - Descarga el último archivo APK disponible e instálalo en tu dispositivo.

---

## 📍 Simulación de Ubicación (Testing)
Para probar la aplicación en el Campus de Móstoles (donde están cargados los datos de prueba):
- **Latitud:** `40.335`
- **Longitud:** `-3.875`

Para disparar la **Pre-alerta**, activa el Modo Alerta y permanece estático en una zona roja durante 30 segundos.

---

## 🌐 Fuentes de Datos

- **OpenStreetMap:** Base cartográfica.
- **Overpass API:** Se utiliza para descargar en tiempo real polígonos de edificios y zonas verdes para mejorar la precisión del mapa de seguridad (Ver `OBTENER_DATOS_MAPA.md`).
- **Internacionalización:** Soporte multi-idioma cargado dinámicamente desde `languages.json`.

---

## 📦 Generación de APK (Manual)
Puedes generar el APK de depuración ejecutando:
```bash
./gradlew assembleDebug
```
El archivo resultante se encontrará en `app/build/outputs/apk/debug/app-debug.apk`.
