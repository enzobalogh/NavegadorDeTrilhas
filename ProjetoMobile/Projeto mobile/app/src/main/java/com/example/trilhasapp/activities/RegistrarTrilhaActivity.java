package com.example.trilhasapp.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.trilhasapp.R;
import com.example.trilhasapp.dao.TrilhaDao;
import com.example.trilhasapp.model.PontoTrilha;
import com.example.trilhasapp.model.Trilha;
import com.example.trilhasapp.utils.PreferencesManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity responsável por registrar uma trilha em tempo real.
 *
 * Esta versão usa OpenStreetMap/OSMDroid, então NÃO precisa de chave do Google Maps,
 * Google Cloud, cartão ou faturamento. O GPS é lido pelo LocationManager do Android.
 */
public class RegistrarTrilhaActivity extends AppCompatActivity implements LocationListener {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final long LOCATION_INTERVAL_MS = 2000;
    private static final float LOCATION_MIN_DISTANCE_M = 2f;

    private TextView tvVelocidadeAtual;
    private TextView tvVelocidadeMaxima;
    private TextView tvCronometro;
    private TextView tvDistanciaTotal;
    private Button btnIniciar;
    private Button btnFinalizar;

    private MapView mapView;
    private Marker marcadorUsuario;
    private Polygon circuloAcuracia;
    private Polyline linhaPercurso;

    private LocationManager locationManager;
    private PreferencesManager preferencesManager;
    private TrilhaDao trilhaDao;

    private final List<GeoPoint> pontosMapa = new ArrayList<>();
    private Location ultimaLocalizacao;

    private boolean monitorando = false;
    private long trilhaAtualId = -1;
    private long inicioMillis = 0;

    private double distanciaTotalKm = 0;
    private double velocidadeMaximaKmh = 0;

    private final Handler cronometroHandler = new Handler(Looper.getMainLooper());
    private final Runnable cronometroRunnable = new Runnable() {
        @Override
        public void run() {
            atualizarCronometro();
            cronometroHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuração exigida pelo OSMDroid para cache e identificação do app.
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_registrar_trilha);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_registrar_trilha);
        }

        preferencesManager = new PreferencesManager(this);
        trilhaDao = new TrilhaDao(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        inicializarViews();
        configurarMapa();
        configurarBotoes();

        if (temPermissaoLocalizacao()) {
            iniciarAtualizacaoLocalizacao();
            centralizarNaUltimaLocalizacao();
        } else {
            solicitarPermissaoLocalizacao();
        }
    }

    private void inicializarViews() {
        tvVelocidadeAtual = findViewById(R.id.tv_velocidade_atual);
        tvVelocidadeMaxima = findViewById(R.id.tv_velocidade_maxima);
        tvCronometro = findViewById(R.id.tv_cronometro);
        tvDistanciaTotal = findViewById(R.id.tv_distancia_total);
        btnIniciar = findViewById(R.id.btn_iniciar_trilha);
        btnFinalizar = findViewById(R.id.btn_finalizar_trilha);
        mapView = findViewById(R.id.map_registrar);
    }

    private void configurarMapa() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(17.0);

        // Centro inicial em Salvador apenas para não abrir o mapa vazio antes do primeiro GPS.
        GeoPoint salvador = new GeoPoint(-12.9777, -38.5016);
        mapView.getController().setCenter(salvador);

        linhaPercurso = new Polyline();
        linhaPercurso.setColor(Color.rgb(33, 150, 243));
        linhaPercurso.setWidth(8f);
        mapView.getOverlays().add(linhaPercurso);
    }

    private void configurarBotoes() {
        btnIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!temPermissaoLocalizacao()) {
                    solicitarPermissaoLocalizacao();
                    return;
                }
                abrirDialogNomeTrilha();
            }
        });

        btnFinalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalizarTrilha();
            }
        });
    }

    private boolean temPermissaoLocalizacao() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void solicitarPermissaoLocalizacao() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION_PERMISSION
        );
    }

    private void iniciarAtualizacaoLocalizacao() {
        if (!temPermissaoLocalizacao() || locationManager == null) {
            return;
        }

        try {
            boolean gpsAtivo = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean redeAtiva = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!gpsAtivo && !redeAtiva) {
                Toast.makeText(this, "Ative a localização/GPS do celular.", Toast.LENGTH_LONG).show();
                return;
            }

            if (gpsAtivo) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        LOCATION_INTERVAL_MS,
                        LOCATION_MIN_DISTANCE_M,
                        this
                );
            }

            if (redeAtiva) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        LOCATION_INTERVAL_MS,
                        LOCATION_MIN_DISTANCE_M,
                        this
                );
            }
        } catch (SecurityException ignored) {
            Toast.makeText(this, R.string.msg_permissao_localizacao, Toast.LENGTH_SHORT).show();
        }
    }

    private void pararAtualizacaoLocalizacao() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    private void centralizarNaUltimaLocalizacao() {
        if (!temPermissaoLocalizacao() || locationManager == null) {
            return;
        }

        try {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (location != null) {
                atualizarLocalizacao(location);
            }
        } catch (SecurityException ignored) {
            Toast.makeText(this, R.string.msg_permissao_localizacao, Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirDialogNomeTrilha() {
        final EditText input = new EditText(this);
        input.setHint(R.string.dialog_hint_nome_trilha);
        input.setSingleLine(true);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_titulo_nome_trilha)
                .setView(input)
                .setPositiveButton("Iniciar", (dialog, which) -> {
                    String nome = input.getText().toString().trim();
                    if (nome.isEmpty()) {
                        nome = "Trilha " + formatarDataAtual();
                    }
                    iniciarTrilha(nome);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void iniciarTrilha(String nome) {
        distanciaTotalKm = 0;
        velocidadeMaximaKmh = 0;
        pontosMapa.clear();
        ultimaLocalizacao = null;
        inicioMillis = System.currentTimeMillis();

        Trilha trilha = new Trilha(
                nome,
                formatarDataAtual(),
                null,
                0,
                0,
                0
        );

        trilhaAtualId = trilhaDao.inserirTrilha(trilha);
        monitorando = true;

        btnIniciar.setEnabled(false);
        btnFinalizar.setEnabled(true);
        cronometroHandler.post(cronometroRunnable);
        iniciarAtualizacaoLocalizacao();

        Toast.makeText(this, R.string.msg_trilha_iniciada, Toast.LENGTH_SHORT).show();
    }

    private void finalizarTrilha() {
        if (!monitorando) {
            return;
        }

        monitorando = false;
        cronometroHandler.removeCallbacks(cronometroRunnable);

        if (pontosMapa.isEmpty()) {
            Toast.makeText(this, R.string.msg_trilha_muito_curta, Toast.LENGTH_SHORT).show();
            btnIniciar.setEnabled(true);
            btnFinalizar.setEnabled(false);
            return;
        }

        long tempoMs = Math.max(1, System.currentTimeMillis() - inicioMillis);
        double horas = tempoMs / 3600000.0;
        double velocidadeMedia = distanciaTotalKm / horas;

        trilhaDao.atualizarResumoFinal(
                trilhaAtualId,
                formatarDataAtual(),
                velocidadeMedia,
                velocidadeMaximaKmh,
                distanciaTotalKm
        );

        btnIniciar.setEnabled(true);
        btnFinalizar.setEnabled(false);

        Toast.makeText(this, R.string.msg_trilha_salva, Toast.LENGTH_LONG).show();
    }

    private void atualizarLocalizacao(Location location) {
        if (location == null) {
            return;
        }

        GeoPoint pontoAtual = new GeoPoint(location.getLatitude(), location.getLongitude());
        atualizarMarcadorEAcuracia(location, pontoAtual);
        mapView.getController().animateTo(pontoAtual);

        if (preferencesManager.isCourseUp() && location.hasBearing()) {
            mapView.setMapOrientation(-location.getBearing());
        } else {
            mapView.setMapOrientation(0);
        }

        if (monitorando) {
            double velocidadeAtualKmh = calcularVelocidadeKmh(location);
            velocidadeMaximaKmh = Math.max(velocidadeMaximaKmh, velocidadeAtualKmh);

            if (ultimaLocalizacao != null) {
                float distanciaMetros = ultimaLocalizacao.distanceTo(location);
                // Ignora pequenos ruídos do GPS parado.
                if (distanciaMetros >= 1) {
                    distanciaTotalKm += distanciaMetros / 1000.0;
                }
            }

            pontosMapa.add(pontoAtual);
            linhaPercurso.setPoints(pontosMapa);

            PontoTrilha pontoTrilha = new PontoTrilha(
                    trilhaAtualId,
                    location.getLatitude(),
                    location.getLongitude(),
                    formatarDataAtual()
            );
            trilhaDao.inserirPonto(pontoTrilha);

            atualizarPainel(velocidadeAtualKmh);
        }

        ultimaLocalizacao = location;
        mapView.invalidate();
    }

    private double calcularVelocidadeKmh(Location location) {
        if (location.hasSpeed()) {
            return location.getSpeed() * 3.6;
        }

        if (ultimaLocalizacao != null) {
            float distanciaMetros = ultimaLocalizacao.distanceTo(location);
            long deltaTempoMs = location.getTime() - ultimaLocalizacao.getTime();
            if (deltaTempoMs > 0) {
                double metrosPorSegundo = distanciaMetros / (deltaTempoMs / 1000.0);
                return metrosPorSegundo * 3.6;
            }
        }

        return 0;
    }

    private void atualizarMarcadorEAcuracia(Location location, GeoPoint pontoAtual) {
        if (marcadorUsuario == null) {
            marcadorUsuario = new Marker(mapView);
            marcadorUsuario.setTitle("Você está aqui");
            Drawable icone = ContextCompat.getDrawable(this, R.drawable.ic_user_marker);
            if (icone != null) {
                marcadorUsuario.setIcon(icone);
            }
            marcadorUsuario.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(marcadorUsuario);
        }
        marcadorUsuario.setPosition(pontoAtual);

        if (circuloAcuracia != null) {
            mapView.getOverlays().remove(circuloAcuracia);
        }

        if (location.hasAccuracy()) {
            circuloAcuracia = new Polygon();
            circuloAcuracia.setPoints(Polygon.pointsAsCircle(pontoAtual, location.getAccuracy()));
            circuloAcuracia.setFillColor(Color.argb(45, 33, 150, 243));
            circuloAcuracia.setStrokeColor(Color.argb(140, 33, 150, 243));
            circuloAcuracia.setStrokeWidth(2f);
            mapView.getOverlays().add(circuloAcuracia);
        }
    }

    private void atualizarPainel(double velocidadeAtualKmh) {
        tvVelocidadeAtual.setText(String.format(Locale.getDefault(), "Vel. atual: %.1f km/h", velocidadeAtualKmh));
        tvVelocidadeMaxima.setText(String.format(Locale.getDefault(), "Vel. máx: %.1f km/h", velocidadeMaximaKmh));
        tvDistanciaTotal.setText(String.format(Locale.getDefault(), "Distância: %.2f km", distanciaTotalKm));
    }

    private void atualizarCronometro() {
        if (!monitorando) {
            return;
        }

        long tempoMs = System.currentTimeMillis() - inicioMillis;
        long segundos = tempoMs / 1000;
        long horas = segundos / 3600;
        long minutos = (segundos % 3600) / 60;
        long seg = segundos % 60;

        tvCronometro.setText(String.format(Locale.getDefault(), "Tempo: %02d:%02d:%02d", horas, minutos, seg));
    }

    private String formatarDataAtual() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        atualizarLocalizacao(location);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Toast.makeText(this, "GPS/localização ativado.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Toast.makeText(this, "GPS/localização desativado.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Método antigo mantido por compatibilidade com minSdk 17.
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarAtualizacaoLocalizacao();
                centralizarNaUltimaLocalizacao();
            } else {
                Toast.makeText(this, R.string.msg_permissao_localizacao, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (temPermissaoLocalizacao()) {
            iniciarAtualizacaoLocalizacao();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        // Se estiver registrando, mantém GPS ativo. Se não, economiza bateria.
        if (!monitorando) {
            pararAtualizacaoLocalizacao();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cronometroHandler.removeCallbacks(cronometroRunnable);
        pararAtualizacaoLocalizacao();
        trilhaDao.fechar();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
