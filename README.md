# SendaURJC MVP (Mock)

Prototipo Android de seguridad percibida para el Campus de Móstoles URJC, **sin backend real**.

## Stack
- Kotlin + Jetpack Compose
- OpenStreetMap con OSMDroid
- OSRM público para cálculo de rutas peatonales
- Room para incidencias persistentes

## Funcionalidades MVP implementadas
1. **Login simulado** (delay 1.5s, sin validación real).
2. **Rutas seguras** usando OSRM + segmentación cada 20m + coloreado:
   - Verde: seguro.
   - Rojo: cruza zona oscura de `MockLumenSmartDataSource`.
3. **Modo Voy Contigo**:
   - Botón con diálogo de búsqueda 3s.
   - Toast final de voluntario asignado (simulación).
4. **Modo Alerta**:
   - Foreground Service con monitoreo GPS.
   - Pre-alerta tras 30s sin movimiento (>5m).
   - Notificación de confirmación con acciones “Estoy bien” / “Emergencia”.
   - Si no hay respuesta en 10s, notificación final simulada.
   - Contacto hardcodeado: **Contacto URJC - 666555444**.
5. **Gestión de incidencias**:
   - FAB “Reportar” con tipos: Farola rota, Zona sucia, Mal aspecto.
   - Persistencia Room.
   - Marcador amarillo en mapa.

## Cómo ejecutar
1. Abrir proyecto en Android Studio Hedgehog o superior.
2. Sincronizar Gradle.
3. Ejecutar en emulador/dispositivo con GPS.

## Simular ubicación GPS en Campus de Móstoles
Centro recomendado:
- **Lat:** `40.335`
- **Lon:** `-3.875`

### Emulador Android Studio
1. Abrir `Extended controls` > `Location`.
2. Introducir `40.335, -3.875`.
3. Para probar pre-alerta, mantén esa posición estática 30s.
4. Para probar rutas seguras, haz **long press** en otro punto del mapa dentro del campus.

## APK debug
Generar con:
```bash
./gradlew assembleDebug
```
APK esperada:
`app/build/outputs/apk/debug/app-debug.apk`
