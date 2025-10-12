package com.devst.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class HomeActivity extends AppCompatActivity {

    // Variables
    private String emailUsuario = "";
    private TextView tvBienvenida;

    //VARIABLES PARA LA CAMARA
    private Button btnLinterna;
    private CameraManager camara;
    private String camaraID = null;
    private boolean luz = false;

    // Activity Result (para recibir datos de PerfilActivity)
    private final ActivityResultLauncher<Intent> editarPerfilLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String nombre = result.getData().getStringExtra("nombre_editado");
                    if (nombre != null) {
                        tvBienvenida.setText("Hola, " + nombre);
                    }
                }
            });

    // Launcher para pedir permiso de cámara en tiempo de ejecución
    private final ActivityResultLauncher<String> permisoCamaraLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    alternarluz(); // si conceden permiso, intentamos prender/apagar

                } else {
                    Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Referencias
        tvBienvenida = findViewById(R.id.tvBienvenida);
        Button btnIrPerfil = findViewById(R.id.btnIrPerfil);
        Button btnAbrirWeb = findViewById(R.id.btnAbrirWeb);
        Button btnEnviarCorreo = findViewById(R.id.btnEnviarCorreo);
        Button btnCompartir = findViewById(R.id.btnCompartir);
        btnLinterna = findViewById(R.id.btnLinterna);
        Button btnCamara = findViewById(R.id.btnCamara);
        Button btnConfig = findViewById(R.id.btnConfig);
        Button btnLlamar = findViewById(R.id.btnLlamar);
        Button btnAgregarEvento = findViewById(R.id.btnAgregarEvento);
        Button btnWifiSettings = findViewById(R.id.btnWifiSettings);
        Button btnContactos = findViewById(R.id.btnContactos);

        // Recibir dato del Login
        emailUsuario = getIntent().getStringExtra("email_usuario");
        if (emailUsuario == null) emailUsuario = "";
        tvBienvenida.setText("Bienvenido: " + emailUsuario);

        // Evento: Intent explícito → ProfileActivity (esperando resultado)
        btnIrPerfil.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, PerfilActivity.class);
            i.putExtra("email_usuario", emailUsuario);
            editarPerfilLauncher.launch(i);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // Evento: Intent implícito → abrir web
        btnAbrirWeb.setOnClickListener(v -> {
            Uri uri = Uri.parse("https://www.santotomas.cl");
            Intent viewWeb = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(viewWeb);
        });

        // Evento: Intent implícito → enviar correo
        btnEnviarCorreo.setOnClickListener(v -> {
            Intent email = new Intent(Intent.ACTION_SENDTO);
            email.setData(Uri.parse("mailto:")); // Solo apps de correo
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{emailUsuario});
            email.putExtra(Intent.EXTRA_SUBJECT, "Prueba desde la app");
            email.putExtra(Intent.EXTRA_TEXT, "Hola, esto es un intento de correo.");
            startActivity(Intent.createChooser(email, "Enviar correo con:"));
        });

        // Evento: Intent implícito → compartir texto
        btnCompartir.setOnClickListener(v -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, "Hola desde mi app Android 😎");
            startActivity(Intent.createChooser(share, "Compartir usando:"));
        });

        //Evento: Intent implícito → llamar
        btnLlamar.setOnClickListener(v -> {
            Intent llamar = new Intent(Intent.ACTION_DIAL);
            startActivity(llamar);
        });

        //Evento: Intent implícito → seleccionar un contacto
        btnContactos.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            selectorContactoLauncher.launch(intent);
        });

        //Evento: Intent implícito → abrir configuración
        btnConfig.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ConfigActivity.class);
            startActivity(intent);
        });
        //Evento: Intent explícito → Agregar evento
        btnAgregarEvento.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, DetalleActivity.class);
            intent.putExtra("TITULO_EVENTO", "Reunión de Equipo");
            intent.putExtra("DESCRIPCION", "Discutir avances del prototipo.");
            startActivity(intent);
        });

        // Evento: Intent implícito → abrir ajustes de Wi-Fi
        btnWifiSettings.setOnClickListener(v -> {
            // Esta acción abre directamente la configuración de Wi-Fi del dispositivo
            Intent intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
            startActivity(intent);
        });



        //Linterna Inicializamos la camara

        camara = (CameraManager) getSystemService(CAMERA_SERVICE);

        try {
            for (String id : camara.getCameraIdList()) {
                CameraCharacteristics cc = camara.getCameraCharacteristics(id);
                Boolean disponibleFlash = cc.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                Integer lensFacing = cc.get(CameraCharacteristics.LENS_FACING);
                if (Boolean.TRUE.equals(disponibleFlash)
                        && lensFacing != null
                        && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    camaraID = id; // prioriza la cámara trasera con flash
                    break;
                }
            }
        } catch (CameraAccessException e) {
            Toast.makeText(this, "No se puede acceder a la cámara", Toast.LENGTH_SHORT).show();
        }

        btnLinterna.setOnClickListener(v -> {
            if (camaraID == null) {
                Toast.makeText(this, "Este dispositivo no tiene flash disponible", Toast.LENGTH_SHORT).show();
                return;
            }
            // Verifica permiso en tiempo de ejecución
            boolean camGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED;

            if (camGranted) {
                alternarluz();
            } else {
                permisoCamaraLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        btnCamara.setOnClickListener(v ->
                startActivity(new Intent(this, CamaraActivity.class))
        );

    }

    // Launcher para seleccionar un contacto y recibir su URI
    private final ActivityResultLauncher<Intent> selectorContactoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri contactUri = result.getData().getData();
                    // Ahora que tenemos la URI, podemos leer los datos del contacto
                    leerDatosDelContacto(contactUri);
                }
            });

    private void leerDatosDelContacto(Uri contactUri) {
        String[] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME };

        try (Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                String nombreContacto = cursor.getString(nameIndex);

                Toast.makeText(this, "Contacto seleccionado: " + nombreContacto, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo leer el contacto.", Toast.LENGTH_SHORT).show();
        }
    }


    //Linterna
    private void alternarluz() {
        try {
            luz = !luz;
            camara.setTorchMode(camaraID, luz);
            btnLinterna.setText(luz ? "Apagar Linterna" : "Encender Linterna");
        } catch (CameraAccessException e) {
            Toast.makeText(this, "Error al controlar la linterna", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (camaraID != null && luz) {
            try {
                camara.setTorchMode(camaraID, false);
                luz = false;
                if (btnLinterna != null) btnLinterna.setText("Encender Linterna");
            } catch (CameraAccessException ignored) {}
        }
    }












    // ===== Menú en HomeActivity =====
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_perfil) {
            // Ir al perfil (explícito)
            Intent i = new Intent(this, PerfilActivity.class);
            i.putExtra("email_usuario", emailUsuario);
            editarPerfilLauncher.launch(i);
            return true;
        } else if (id == R.id.action_web) {
            // Abrir web (implícito)
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://developer.android.com")));
            return true;
        } else if (id == R.id.action_salir) {
            finish(); // Cierra HomeActivity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}