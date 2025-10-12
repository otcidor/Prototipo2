package com.devst.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

public class DetalleActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);

        // --- CÓDIGO A AÑADIR PARA LA TOOLBAR ---
        MaterialToolbar toolbar = findViewById(R.id.toolbar_detalle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // ------------------------------------------

        TextView tvTitulo = findViewById(R.id.tvTituloDetalle);
        TextView tvDescripcion = findViewById(R.id.tvDescripcionDetalle);

        // Recuperar los datos del Intent
        String titulo = getIntent().getStringExtra("TITULO_EVENTO");
        String descripcion = getIntent().getStringExtra("DESCRIPCION");

        // Mostrarlos
        tvTitulo.setText(titulo);
        tvDescripcion.setText(descripcion);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Vuelve a la pantalla anterior
        return true;
    }
}