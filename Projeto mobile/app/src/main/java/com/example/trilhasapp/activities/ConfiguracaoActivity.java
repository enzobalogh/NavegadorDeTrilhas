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

public class ConfiguracaoActivity extends AppCompatActivity {

    private RadioGroup   rgTipoMapa;
    private RadioButton  rbVetorial;
    private RadioButton  rbSatelite;

    private RadioGroup   rgNavegacao;
    private RadioButton  rbNorthUp;
    private RadioButton  rbCourseUp;

    private Button btnSalvar;

    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracao);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_configuracoes);
        }

        preferencesManager = new PreferencesManager(this);

        inicializarViews();

        carregarConfiguracoesSalvas();

        configurarBotaoSalvar();
    }

    private void inicializarViews() {
        rgTipoMapa  = findViewById(R.id.rg_tipo_mapa);
        rbVetorial  = findViewById(R.id.rb_vetorial);
        rbSatelite  = findViewById(R.id.rb_satelite);

        rgNavegacao = findViewById(R.id.rg_navegacao);
        rbNorthUp   = findViewById(R.id.rb_north_up);
        rbCourseUp  = findViewById(R.id.rb_course_up);

        btnSalvar   = findViewById(R.id.btn_salvar_configuracoes);
    }

    private void carregarConfiguracoesSalvas() {
        
        String tipoMapa = preferencesManager.getMapType();
        if (PreferencesManager.MAP_TYPE_SATELLITE.equals(tipoMapa)) {
            rbSatelite.setChecked(true);
        } else {
            
            rbVetorial.setChecked(true);
        }

        String modoNavegacao = preferencesManager.getNavigationMode();
        if (PreferencesManager.NAVIGATION_COURSE_UP.equals(modoNavegacao)) {
            rbCourseUp.setChecked(true);
        } else {
            
            rbNorthUp.setChecked(true);
        }
    }

    private void configurarBotaoSalvar() {
        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarConfiguracoes();
            }
        });
    }

    private void salvarConfiguracoes() {
        
        int idMapaSelecionado = rgTipoMapa.getCheckedRadioButtonId();
        String tipoMapaSelecionado;

        if (idMapaSelecionado == R.id.rb_satelite) {
            tipoMapaSelecionado = PreferencesManager.MAP_TYPE_SATELLITE;
        } else {
            tipoMapaSelecionado = PreferencesManager.MAP_TYPE_VECTOR;
        }

        int idNavegacaoSelecionado = rgNavegacao.getCheckedRadioButtonId();
        String modoNavegacaoSelecionado;

        if (idNavegacaoSelecionado == R.id.rb_course_up) {
            modoNavegacaoSelecionado = PreferencesManager.NAVIGATION_COURSE_UP;
        } else {
            modoNavegacaoSelecionado = PreferencesManager.NAVIGATION_NORTH_UP;
        }

        preferencesManager.saveMapType(tipoMapaSelecionado);
        preferencesManager.saveNavigationMode(modoNavegacaoSelecionado);

        Toast.makeText(this, R.string.msg_configuracoes_salvas, Toast.LENGTH_SHORT).show();

        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
