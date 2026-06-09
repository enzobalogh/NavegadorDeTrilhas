package com.example.trilhasapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trilhasapp.R;
import com.example.trilhasapp.database.DatabaseHelper;

public class MainActivity extends AppCompatActivity {

    private Button btnRegistrarTrilha;
    private Button btnConsultarTrilhas;
    private Button btnConfiguracoes;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inicializarBanco();

        inicializarViews();

        configurarBotoes();
    }

    private void inicializarBanco() {
        databaseHelper = new DatabaseHelper(getApplicationContext());

        databaseHelper.getReadableDatabase();
    }

    private void inicializarViews() {
        btnRegistrarTrilha  = findViewById(R.id.btn_registrar_trilha);
        btnConsultarTrilhas = findViewById(R.id.btn_consultar_trilhas);
        btnConfiguracoes    = findViewById(R.id.btn_configuracoes);
    }

    private void configurarBotoes() {

        btnRegistrarTrilha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegistrarTrilhaActivity.class);
                startActivity(intent);
            }
        });

        btnConsultarTrilhas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                Toast.makeText(MainActivity.this,
                        "Consultar Trilhas será implementado na Parte 2",
                        Toast.LENGTH_SHORT).show();

            }
        });

        btnConfiguracoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ConfiguracaoActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
