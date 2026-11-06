package com.example.dptvhelper;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final String TARGET_PACKAGE = "io.github.crealivity.dptvcursor";
    private static final String TARGET_CLASS =
            "io.github.crealivity.dptvcursor.services.MouseEventService";
    private static final ComponentName TARGET =
            new ComponentName(TARGET_PACKAGE, TARGET_CLASS);

    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(28, 32, 28, 24);

        TextView title = new TextView(this);
        title.setText("DPTV Helper");
        title.setTextSize(26);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        TextView info = new TextView(this);
        info.setText("Auxiliar para registrar el servicio de accesibilidad de DPTV Cursor.");
        info.setTextSize(16);
        info.setGravity(Gravity.CENTER);
        info.setPadding(0, 18, 0, 18);
        layout.addView(info);

        status = new TextView(this);
        status.setTextSize(16);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 12, 0, 20);
        layout.addView(status);

        Button enable = new Button(this);
        enable.setText("ACTIVAR DPTV CURSOR");
        layout.addView(enable);

        Button disable = new Button(this);
        disable.setText("DESACTIVAR DPTV CURSOR");
        layout.addView(disable);

        Button refresh = new Button(this);
        refresh.setText("ACTUALIZAR ESTADO");
        layout.addView(refresh);

        setContentView(layout);

        enable.setOnClickListener(v -> enableService());
        disable.setOnClickListener(v -> disableService());
        refresh.setOnClickListener(v -> refreshStatus());
        refreshStatus();
    }

    private boolean hasWritePermission() {
        return checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS")
                == PackageManager.PERMISSION_GRANTED;
    }

    private List<String> readEnabledServices() {
        String raw = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

        List<String> result = new ArrayList<>();
        if (raw != null && !raw.trim().isEmpty()) {
            for (String item : raw.split(":")) {
                if (!item.trim().isEmpty()) result.add(item.trim());
            }
        }
        return result;
    }

    private boolean isTarget(String flattened) {
        ComponentName parsed = ComponentName.unflattenFromString(flattened);
        return TARGET.equals(parsed);
    }

    private String join(List<String> entries) {
        StringBuilder out = new StringBuilder();
        for (String entry : entries) {
            if (out.length() > 0) out.append(':');
            out.append(entry);
        }
        return out.toString();
    }

    private boolean targetServiceExists() {
        try {
            ServiceInfo info = getPackageManager().getServiceInfo(
                    TARGET, PackageManager.GET_META_DATA);
            return info != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void enableService() {
        if (!hasWritePermission()) {
            message("Falta WRITE_SECURE_SETTINGS. Concede el permiso mediante ADB.");
            refreshStatus();
            return;
        }
        if (!targetServiceExists()) {
            message("No se encontró MouseEventService. Comprueba que DPTV Cursor esté instalado.");
            refreshStatus();
            return;
        }

        try {
            List<String> services = readEnabledServices();
            boolean found = false;
            for (String service : services) {
                if (isTarget(service)) {
                    found = true;
                    break;
                }
            }
            if (!found) services.add(TARGET.flattenToString());

            boolean listSaved = Settings.Secure.putString(
                    getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                    join(services));

            boolean globalSaved = Settings.Secure.putInt(
                    getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED,
                    1);

            message("Escritura de lista: " + listSaved +
                    " | Activación global: " + globalSaved +
                    ". Actualiza el estado y revisa DPTV Cursor.");
        } catch (SecurityException e) {
            message("Fire OS rechazó la escritura: " + e.getMessage());
        } catch (Exception e) {
            message("Error: " + e.getMessage());
        }
        refreshStatus();
    }

    private void disableService() {
        if (!hasWritePermission()) {
            message("Falta WRITE_SECURE_SETTINGS.");
            refreshStatus();
            return;
        }

        try {
            List<String> remaining = new ArrayList<>();
            for (String service : readEnabledServices()) {
                if (!isTarget(service)) remaining.add(service);
            }

            boolean listSaved = Settings.Secure.putString(
                    getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                    join(remaining));

            // No se desactiva el interruptor global: podría afectar otros servicios.
            message("Se retiró DPTV Cursor de la lista: " + listSaved +
                    ". Los demás servicios y el interruptor global se conservan.");
        } catch (Exception e) {
            message("Error: " + e.getMessage());
        }
        refreshStatus();
    }

    private void refreshStatus() {
        if (status == null) return;

        boolean permission = hasWritePermission();
        boolean installed = targetServiceExists();
        List<String> services = readEnabledServices();
        boolean enabled = false;
        for (String service : services) {
            if (isTarget(service)) {
                enabled = true;
                break;
            }
        }
        int global = Settings.Secure.getInt(
                getContentResolver(),
                Settings.Secure.ACCESSIBILITY_ENABLED, 0);

        status.setText(
                "WRITE_SECURE_SETTINGS: " + (permission ? "CONCEDIDO" : "NO CONCEDIDO") +
                "\nServicio DPTV instalado: " + (installed ? "SÍ" : "NO") +
                "\nServicio registrado: " + (enabled ? "SÍ" : "NO") +
                "\nAccesibilidad global: " + (global == 1 ? "ACTIVADA" : "DESACTIVADA"));
    }

    private void message(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
    }
}
