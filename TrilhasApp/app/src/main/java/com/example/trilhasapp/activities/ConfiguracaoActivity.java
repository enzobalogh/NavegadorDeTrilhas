package com.example.trilhasapp.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trilhasapp.R;
import com.example.trilhasapp.utils.PreferencesManager;

/**
 * Tela de configurações do aplicativo.
 *
 * Permite ao usuário personalizar:
 *   1. Tipo de mapa       → Vetorial | Satélite
 *   2. Modo de navegação  → North Up | Course Up
 *
 * As preferências são persistidas via {@link PreferencesManager} (SharedPreferences),
 * portanto sobrevivem ao fechamento do aplicativo.
 */
public class ConfiguracaoActivity extends AppCompatActivity {

    // -------------------------------------------------------------------------
    // Views
    // -------------------------------------------------------------------------

    // RadioGroup e RadioButtons para tipo de mapa
    private RadioGroup   rgTipoMapa;
    private RadioButton  rbVetorial;
    private RadioButton  rbSatelite;

    // RadioGroup e RadioButtons para modo de navegação
    private RadioGroup   rgNavegacao;
    private RadioButton  rbNorthUp;
    private RadioButton  rbCourseUp;

    // Botão para salvar as configurações
    private Button btnSalvar;

    // -------------------------------------------------------------------------
    // Utilitário de preferências
    // -------------------------------------------------------------------------

    private PreferencesManager preferencesManager;

    // =========================================================================
    // Ciclo de vida
    // =========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracao);

        // Habilita o botão "Voltar" na ActionBar (seta ←)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_configuracoes);
        }

        // Instancia o gerenciador de preferências
        preferencesManager = new PreferencesManager(this);

        // Liga Java com XML
        inicializarViews();

        // Carrega os valores previamente salvos e marca os RadioButtons corretos
        carregarConfiguracoesSalvas();

        // Configura o botão salvar
        configurarBotaoSalvar();
    }

    /**
     * Vincula as variáveis Java aos elementos do layout XML.
     */
    private void inicializarViews() {
        rgTipoMapa  = findViewById(R.id.rg_tipo_mapa);
        rbVetorial  = findViewById(R.id.rb_vetorial);
        rbSatelite  = findViewById(R.id.rb_satelite);

        rgNavegacao = findViewById(R.id.rg_navegacao);
        rbNorthUp   = findViewById(R.id.rb_north_up);
        rbCourseUp  = findViewById(R.id.rb_course_up);

        btnSalvar   = findViewById(R.id.btn_salvar_configuracoes);
    }

    /**
     * Lê as preferências salvas anteriormente e reflete no estado dos RadioButtons.
     * Se nenhum valor foi salvo ainda, os padrões definidos no PreferencesManager são usados.
     */
    private void carregarConfiguracoesSalvas() {
        // Tipo de mapa
        String tipoMapa = preferencesManager.getMapType();
        if (PreferencesManager.MAP_TYPE_SATELLITE.equals(tipoMapa)) {
            rbSatelite.setChecked(true);
        } else {
            // Padrão: Vetorial
            rbVetorial.setChecked(true);
        }

        // Modo de navegação
        String modoNavegacao = preferencesManager.getNavigationMode();
        if (PreferencesManager.NAVIGATION_COURSE_UP.equals(modoNavegacao)) {
            rbCourseUp.setChecked(true);
        } else {
            // Padrão: North Up
            rbNorthUp.setChecked(true);
        }
    }

    /**
     * Configura o listener do botão Salvar.
     * Lê qual RadioButton está marcado em cada grupo e persiste via PreferencesManager.
     */
    private void configurarBotaoSalvar() {
        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarConfiguracoes();
            }
        });
    }

    /**
     * Lê os RadioButtons selecionados, converte para as constantes do PreferencesManager
     * e persiste as escolhas do usuário.
     */
    private void salvarConfiguracoes() {
        // --- Tipo de mapa ---
        int idMapaSelecionado = rgTipoMapa.getCheckedRadioButtonId();
        String tipoMapaSelecionado;

        if (idMapaSelecionado == R.id.rb_satelite) {
            tipoMapaSelecionado = PreferencesManager.MAP_TYPE_SATELLITE;
        } else {
            tipoMapaSelecionado = PreferencesManager.MAP_TYPE_VECTOR;
        }

        // --- Modo de navegação ---
        int idNavegacaoSelecionado = rgNavegacao.getCheckedRadioButtonId();
        String modoNavegacaoSelecionado;

        if (idNavegacaoSelecionado == R.id.rb_course_up) {
            modoNavegacaoSelecionado = PreferencesManager.NAVIGATION_COURSE_UP;
        } else {
            modoNavegacaoSelecionado = PreferencesManager.NAVIGATION_NORTH_UP;
        }

        // Persiste as preferências
        preferencesManager.saveMapType(tipoMapaSelecionado);
        preferencesManager.saveNavigationMode(modoNavegacaoSelecionado);

        // Feedback visual ao usuário
        Toast.makeText(this, R.string.msg_configuracoes_salvas, Toast.LENGTH_SHORT).show();

        // Volta para a MainActivity após salvar
        finish();
    }

    // =========================================================================
    // Menu / ActionBar
    // =========================================================================

    /**
     * Trata o clique no botão "Voltar" da ActionBar (seta ←).
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Simula o comportamento do botão Back do sistema
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
