# DPTVHelper

Aplicación auxiliar experimental para Fire OS 6 / Android 7.1 (API 25).

## Compilación
El workflow de GitHub Actions compila `app-debug.apk` al subir cambios a `main`
o al ejecutarse manualmente desde Actions.

## Instalación y permiso
Instala el APK y concede el permiso por ADB:

```powershell
adb install app-debug.apk
adb shell pm grant com.example.dptvhelper android.permission.WRITE_SECURE_SETTINGS
```

Abre DPTV Helper y pulsa **ACTUALIZAR ESTADO**. Si el permiso figura como concedido,
pulsa **ACTIVAR DPTV CURSOR**.

Este proyecto no garantiza que Fire OS permita enlazar el servicio. Si el sistema
rechaza el ajuste o no enlaza el servicio, no se debe forzar su arranque mediante
`am startservice`: los servicios de accesibilidad están protegidos por Android.
