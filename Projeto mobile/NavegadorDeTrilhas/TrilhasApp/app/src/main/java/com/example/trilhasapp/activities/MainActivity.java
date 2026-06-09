package com.example.trilhasapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trilhasapp.R;
import com.example.trilhasapp.database.DatabaseHelper;

/**
 * Tela principal do aplicativo de gerenciamento de trilhas.
 *
 * Responsabilidades:
 *   - Exibir o menu principal com os três botões de navegação
 *   - Inicializar o banco de dados na primeira execução
 *   - Direcionar o usuário para as demais Activities
 */
public class MainActivity extends AppCompatActivity {

    // Referências aos botões do layout
    private Button btnRegistrarTrilha;
    private Button btnConsultarTrilhas;
    private Button btnConfiguracoes;

    // Helper do banco — instanciar aqui garante que as tabelas sejam criadas
    // antes de qualquer outra operação no app
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa o banco (cria o arquivo .db e as tabelas na primeira execução)
        inicializarBanco();

        // Liga as referências Java aos elementos do XML
        inicializarViews();

        // Configura os listeners de clique
        configurarBotoes();
    }

    /**
     * Instancia o DatabaseHelper, o que aciona o onCreate() do banco
     * caso seja a primeira vez que o app é executado.
     */
    private void inicializarBanco() {
        databaseHelper = new DatabaseHelper(getApplicationContext());

        // Chama getReadableDatabase() para forçar a criação imediata do banco.
        // Sem isso, o banco só seria criado na primeira leitura/escrita real.
        databaseHelper.getReadableDatabase();
    }

    /**
     * Vincula as variáveis Java com os elementos visuais definidos no XML de layout.
     */
    private void inicializarViews() {
        btnRegistrarTrilha  = findViewById(R.id.btn_registrar_trilha);
        btnConsultarTrilhas = findViewById(R.id.btn_consultar_trilhas);
        btnConfiguracoes    = findViewById(R.id.btn_configuracoes);
    }

    /**
     * Define o comportamento de cada botão ao ser clicado.
     */
    private void configurarBotoes() {

        // Botão "Registrar Trilha"
        // Navega para RegistrarTrilhaActivity (será implementada na Parte 2)
        btnRegistrarTrilha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO (Parte 2): substituir Toast pela Intent para RegistrarTrilhaActivity
                Toast.makeText(MainActivity.this,
                        "Registrar Trilha será implementado na Parte 2",
                        Toast.LENGTH_SHORT).show();

                // Exemplo de como será a navegação:
                // Intent intent = new Intent(MainActivity.this, RegistrarTrilhaActivity.class);
                // startActivity(intent);
            }
        });

        // Botão "Consultar Trilhas"
        // Navega para ConsultarTrilhasActivity (será implementada na Parte 2)
        btnConsultarTrilhas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO (Parte 2): substituir Toast pela Intent para ConsultarTrilhasActivity
                Toast.makeText(MainActivity.this,
                        "Consultar Trilhas será implementado na Parte 2",
                        Toast.LENGTH_SHORT).show();

                // Exemplo de como será a navegação:
                // Intent intent = new Intent(MainActivity.this, ConsultarTrilhasActivity.class);
                // startActivity(intent);
            }
        });

        // Botão "Configurações"
        // Navega para ConfiguracaoActivity
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
        // Fecha o banco ao destruir a Activity para liberar recursos
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
