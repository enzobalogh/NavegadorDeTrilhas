package com.example.trilhasapp.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RegistrarTrilhaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final long LOCATION_INTERVAL_MS = 3000;
    private static final long LOCATION_FASTEST_INTERVAL_MS = 1500;

    private TextView tvVelocidadeAtual;
    private TextView tvVelocidadeMaxima;
    private TextView tvCronometro;
    private TextView tvDistanciaTotal;
    private Button btnIniciar;
    private Button btnFinalizar;

    private GoogleMap googleMap;
    private Marker marcadorUsuario;
    private Circle circuloAcuracia;
    private Polyline linhaPercurso;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    private PreferencesManager preferencesManager;
    private TrilhaDao trilhaDao;

    private final List<LatLng> pontosMapa = new ArrayList<>();
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
        setContentView(R.layout.activity_registrar_trilha);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_registrar_trilha);
        }

        preferencesManager = new PreferencesManager(this);
        trilhaDao = new TrilhaDao(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        inicializarViews();
        configurarBotoes();
        configurarLocationRequest();
        configurarLocationCallback();
        carregarMapa();
    }

    private void inicializarViews() {
        tvVelocidadeAtual = findViewById(R.id.tv_velocidade_atual);
        tvVelocidadeMaxima = findViewById(R.id.tv_velocidade_maxima);
        tvCronometro = findViewById(R.id.tv_cronometro);
        tvDistanciaTotal = findViewById(R.id.tv_distancia_total);
        btnIniciar = findViewById(R.id.btn_iniciar_trilha);
        btnFinalizar = findViewById(R.id.btn_finalizar_trilha);
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

    private void configurarLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(LOCATION_INTERVAL_MS);
        locationRequest.setFastestInterval(LOCATION_FASTEST_INTERVAL_MS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void configurarLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    atualizarLocalizacao(location);
                }
            }
        };
    }

    private void carregarMapa() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_registrar);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        aplicarConfiguracoesDoMapa();

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (temPermissaoLocalizacao()) {
            ativarLocalizacaoNoMapa();
            centralizarNaUltimaLocalizacao();
        } else {
            solicitarPermissaoLocalizacao();
        }
    }

    private void aplicarConfiguracoesDoMapa() {
        if (googleMap == null) {
            return;
        }

        if (preferencesManager.isSatelliteMap()) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
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

    private void ativarLocalizacaoNoMapa() {
        if (googleMap == null || !temPermissaoLocalizacao()) {
            return;
        }

        try {
            googleMap.setMyLocationEnabled(true);
        } catch (SecurityException ignored) {
            Toast.makeText(this, R.string.msg_permissao_localizacao, Toast.LENGTH_SHORT).show();
        }
    }

    private void centralizarNaUltimaLocalizacao() {
        if (!temPermissaoLocalizacao()) {
            return;
        }

        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    atualizarMarcadorEAcuracia(location, latLng);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f));
                }
            });
        } catch (SecurityException ignored) {
            Toast.makeText(this, R.string.msg_permissao_localizacao, Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirDialogNomeTrilha() {
        final EditText input = new EditText(this);
        input.setHint(R.string.dialog_hint_nome_trilha);
        input.setSingleLine(true);
        input.setText(gerarNomePadrao());
        input.setSelectAllOnFocus(true);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_titulo_nome_trilha)
                .setView(input)
                .setPositiveButton("Iniciar", (dialog, which) -> {
                    String nome = input.getText().toString().trim();
                    if (nome.isEmpty()) {
                        nome = gerarNomePadrao();
                    }
                    iniciarTrilha(nome);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private String gerarNomePadrao() {
        return "Trilha " + new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(new Date());
    }

    private void iniciarTrilha(String nome) {
        if (monitorando) {
            return;
        }

        resetarDadosDaTela();

        inicioMillis = System.currentTimeMillis();
        String dataInicio = formatarDataHora(new Date(inicioMillis));

        Trilha trilha = new Trilha(nome, dataInicio, null, 0, 0, 0);
        trilhaAtualId = trilhaDao.inserirTrilha(trilha);

        if (trilhaAtualId == -1) {
            Toast.makeText(this, "Erro ao criar a trilha no banco.", Toast.LENGTH_SHORT).show();
            return;
        }

        monitorando = true;
        btnIniciar.setEnabled(false);
        btnFinalizar.setEnabled(true);

        iniciarAtualizacoesDeLocalizacao();
        cronometroHandler.post(cronometroRunnable);

        Toast.makeText(this, R.string.msg_trilha_iniciada, Toast.LENGTH_SHORT).show();
    }

    private void iniciarAtualizacoesDeLocalizacao() {
        if (!temPermissaoLocalizacao()) {
            solicitarPermissaoLocalizacao();
            return;
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );
        } catch (SecurityException ignored) {
            Toast.makeText(this, R.string.msg_permissao_localizacao, Toast.LENGTH_SHORT).show();
        }
    }

    private void atualizarLocalizacao(Location location) {
        if (googleMap == null || location == null) {
            return;
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        atualizarMarcadorEAcuracia(location, latLng);
        atualizarCamera(location, latLng);

        if (!monitorando || trilhaAtualId == -1) {
            return;
        }

        if (ultimaLocalizacao != null) {
            float distanciaMetros = ultimaLocalizacao.distanceTo(location);

            if (distanciaMetros >= 2) {
                distanciaTotalKm += distanciaMetros / 1000.0;
            }
        }

        double velocidadeAtualKmh = calcularVelocidadeKmh(location);
        if (velocidadeAtualKmh > velocidadeMaximaKmh) {
            velocidadeMaximaKmh = velocidadeAtualKmh;
        }

        trilhaDao.inserirPonto(new PontoTrilha(
                trilhaAtualId,
                location.getLatitude(),
                location.getLongitude(),
                formatarDataHora(new Date(location.getTime()))
        ));

        pontosMapa.add(latLng);
        desenharLinhaPercurso();
        ultimaLocalizacao = location;
        atualizarTextos(velocidadeAtualKmh);
    }

    private double calcularVelocidadeKmh(Location location) {
        if (location.hasSpeed()) {
            return location.getSpeed() * 3.6;
        }
        return 0;
    }

    private void atualizarMarcadorEAcuracia(Location location, LatLng latLng) {
        BitmapDescriptor icone = criarMarcadorPersonalizado();

        if (marcadorUsuario == null) {
            marcadorUsuario = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Você está aqui")
                    .icon(icone)
                    .anchor(0.5f, 0.5f));
        } else {
            marcadorUsuario.setPosition(latLng);
        }

        if (circuloAcuracia == null) {
            circuloAcuracia = googleMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(location.hasAccuracy() ? location.getAccuracy() : 0)
                    .fillColor(Color.argb(70, 51, 170, 255))
                    .strokeColor(Color.argb(170, 25, 118, 210))
                    .strokeWidth(2f));
        } else {
            circuloAcuracia.setCenter(latLng);
            circuloAcuracia.setRadius(location.hasAccuracy() ? location.getAccuracy() : 0);
        }
    }

    private BitmapDescriptor criarMarcadorPersonalizado() {
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_user_marker);
        if (drawable == null) {
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        }

        int width = drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : 96;
        int height = drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : 96;
        drawable.setBounds(0, 0, width, height);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void atualizarCamera(Location location, LatLng latLng) {
        float bearing = 0f;
        if (preferencesManager.isCourseUp() && location.hasBearing()) {
            bearing = location.getBearing();
        }

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new com.google.android.gms.maps.model.CameraPosition.Builder()
                        .target(latLng)
                        .zoom(17f)
                        .bearing(bearing)
                        .tilt(0f)
                        .build()
        ));
    }

    private void desenharLinhaPercurso() {
        if (pontosMapa.size() < 2) {
            return;
        }

        if (linhaPercurso == null) {
            linhaPercurso = googleMap.addPolyline(new PolylineOptions()
                    .addAll(pontosMapa)
                    .width(8f)
                    .color(ContextCompat.getColor(this, R.color.colorPrimary)));
        } else {
            linhaPercurso.setPoints(pontosMapa);
        }
    }

    private void atualizarTextos(double velocidadeAtualKmh) {
        tvVelocidadeAtual.setText(String.format(Locale.getDefault(), "%.1f km/h", velocidadeAtualKmh));
        tvVelocidadeMaxima.setText(String.format(Locale.getDefault(), "%.1f km/h", velocidadeMaximaKmh));
        tvDistanciaTotal.setText(String.format(Locale.getDefault(), "%.2f km", distanciaTotalKm));
    }

    private void atualizarCronometro() {
        if (!monitorando) {
            return;
        }

        long tempoTotalSegundos = (System.currentTimeMillis() - inicioMillis) / 1000;
        long horas = tempoTotalSegundos / 3600;
        long minutos = (tempoTotalSegundos % 3600) / 60;
        long segundos = tempoTotalSegundos % 60;

        tvCronometro.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", horas, minutos, segundos));
    }

    private void finalizarTrilha() {
        if (!monitorando) {
            return;
        }

        fusedLocationClient.removeLocationUpdates(locationCallback);
        cronometroHandler.removeCallbacks(cronometroRunnable);
        monitorando = false;

        if (pontosMapa.isEmpty()) {
            Toast.makeText(this, R.string.msg_trilha_muito_curta, Toast.LENGTH_SHORT).show();
        }

        long fimMillis = System.currentTimeMillis();
        double horas = Math.max((fimMillis - inicioMillis) / 3600000.0, 0.0001);
        double velocidadeMedia = distanciaTotalKm / horas;

        trilhaDao.atualizarResumoFinal(
                trilhaAtualId,
                formatarDataHora(new Date(fimMillis)),
                velocidadeMedia,
                velocidadeMaximaKmh,
                distanciaTotalKm
        );

        btnIniciar.setEnabled(true);
        btnFinalizar.setEnabled(false);

        Toast.makeText(this, R.string.msg_trilha_salva, Toast.LENGTH_SHORT).show();
    }

    private void resetarDadosDaTela() {
        pontosMapa.clear();
        ultimaLocalizacao = null;
        distanciaTotalKm = 0;
        velocidadeMaximaKmh = 0;
        trilhaAtualId = -1;

        tvVelocidadeAtual.setText("0,0 km/h");
        tvVelocidadeMaxima.setText("0,0 km/h");
        tvCronometro.setText("00:00:00");
        tvDistanciaTotal.setText("0,00 km");

        if (linhaPercurso != null) {
            linhaPercurso.remove();
            linhaPercurso = null;
        }
    }

    private String formatarDataHora(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ativarLocalizacaoNoMapa();
                centralizarNaUltimaLocalizacao();
            } else {
                Toast.makeText(this, R.string.msg_permissao_localizacao, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (monitorando) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            cronometroHandler.removeCallbacks(cronometroRunnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (monitorando && temPermissaoLocalizacao()) {
            iniciarAtualizacoesDeLocalizacao();
            cronometroHandler.post(cronometroRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
        cronometroHandler.removeCallbacks(cronometroRunnable);
        trilhaDao.fechar();
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
