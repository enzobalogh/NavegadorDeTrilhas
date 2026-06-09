package com.example.trilhasapp.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trilhasapp.R;
import com.example.trilhasapp.database.DatabaseHelper;
import com.example.trilhasapp.model.PontoTrilha;
import com.example.trilhasapp.model.Trilha;
import com.example.trilhasapp.utils.PreferencesManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class VisualizarTrilhaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseHelper db;
    private Trilha trilha;
    private List<PontoTrilha> pontos;

    private TextView tvDataHoraInicio;
    private TextView tvVelocidadeMedia;
    private TextView tvVelocidadeMaxima;
    private TextView tvDistanciaTotal;
    private TextView tvDuracao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_trilha);

        setSupportActionBar(findViewById(R.id.toolbar_visualizar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        long trilhaId = getIntent().getLongExtra("trilha_id", -1);
        if (trilhaId == -1) {
            Toast.makeText(this, "Trilha não encontrada.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db     = new DatabaseHelper(this);
        trilha = db.buscarTrilhaPorId(trilhaId);
        pontos = db.listarPontos(trilhaId);

        if (trilha == null || pontos == null || pontos.isEmpty()) {
            Toast.makeText(this, "Esta trilha não possui pontos registrados.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvDataHoraInicio  = findViewById(R.id.tv_overlay_data);
        tvVelocidadeMedia = findViewById(R.id.tv_overlay_vel_media);
        tvVelocidadeMaxima= findViewById(R.id.tv_overlay_vel_max);
        tvDistanciaTotal  = findViewById(R.id.tv_overlay_distancia);
        tvDuracao         = findViewById(R.id.tv_overlay_duracao);

        preencherOverlay();

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map_visualizar);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    // ── Overlay ───────────────────────────────────────────────────────────────

    private void preencherOverlay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy  HH:mm:ss", Locale.getDefault());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(trilha.getNome());
        }

        String dataInicio = trilha.getDataHoraInicio() != null
                ? sdf.format(trilha.getDataHoraInicio()) : "--/--/----  --:--:--";

        tvDataHoraInicio.setText("Início: " + dataInicio);

        tvVelocidadeMedia.setText(String.format(Locale.getDefault(),
                "Vel. média: %.1f km/h", trilha.getVelocidadeMedia()));

        tvVelocidadeMaxima.setText(String.format(Locale.getDefault(),
                "Vel. máxima: %.1f km/h", trilha.getVelocidadeMaxima()));

        tvDistanciaTotal.setText(String.format(Locale.getDefault(),
                "Distância: %.2f km", trilha.getDistanciaTotal()));

        tvDuracao.setText("Duração: " + trilha.getDuracaoFormatada());
    }

    // ── Mapa ──────────────────────────────────────────────────────────────────

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Aplica o tipo de mapa definido na tela de Configuração
        PreferencesManager prefs = new PreferencesManager(this);
        if (prefs.getTipoMapa() == PreferencesManager.MAPA_SATELITE) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        desenharTrajeto();
    }

    private void desenharTrajeto() {
        PolylineOptions polyline = new PolylineOptions()
                .color(Color.parseColor("#2979FF"))
                .width(8f)
                .geodesic(true);

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        for (PontoTrilha ponto : pontos) {
            LatLng latLng = new LatLng(ponto.getLatitude(), ponto.getLongitude());
            polyline.add(latLng);
            boundsBuilder.include(latLng);
        }

        mMap.addPolyline(polyline);

        // Marcador início (verde)
        LatLng inicio = new LatLng(
                pontos.get(0).getLatitude(),
                pontos.get(0).getLongitude());
        mMap.addMarker(new MarkerOptions()
                .position(inicio)
                .title("Início")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        // Marcador fim (vermelho)
        LatLng fim = new LatLng(
                pontos.get(pontos.size() - 1).getLatitude(),
                pontos.get(pontos.size() - 1).getLongitude());
        mMap.addMarker(new MarkerOptions()
                .position(fim)
                .title("Fim")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // Ajusta câmera para mostrar todo o trajeto
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
