package com.devst.app;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

public class ConfigActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config); // Asegúrate que el layout se llame así

        // Añade una Toolbar al XML de PerfilActivity si no la tiene
        MaterialToolbar toolbar = findViewById(R.id.toolbar_config);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    // Este método se encarga de que la flecha "atrás" funcione
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Vuelve a la pantalla anterior
        return true;
    }

}