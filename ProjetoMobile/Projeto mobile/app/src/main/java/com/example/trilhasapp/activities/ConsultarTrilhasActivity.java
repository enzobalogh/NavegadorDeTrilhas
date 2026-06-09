package com.example.trilhasapp.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trilhasapp.R;
import com.example.trilhasapp.adapter.TrilhaAdapter;
import com.example.trilhasapp.database.DatabaseHelper;
import com.example.trilhasapp.model.Trilha;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ConsultarTrilhasActivity extends AppCompatActivity
        implements TrilhaAdapter.OnTrilhaActionListener {

    private RecyclerView recyclerView;
    private TrilhaAdapter adapter;
    private DatabaseHelper db;
    private List<Trilha> listaTrilhas;
    private View layoutVazio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_trilhas);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Minhas Trilhas");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = new DatabaseHelper(this);

        layoutVazio   = findViewById(R.id.layout_vazio);
        recyclerView  = findViewById(R.id.recycler_trilhas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fabApagarTodas = findViewById(R.id.fab_apagar_todas);
        fabApagarTodas.setOnClickListener(v -> confirmarApagarTodas());

        carregarTrilhas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarTrilhas();
    }

    private void carregarTrilhas() {
        listaTrilhas = db.listarTodas();

        if (listaTrilhas.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            layoutVazio.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            layoutVazio.setVisibility(View.GONE);
            adapter = new TrilhaAdapter(listaTrilhas, this);
            recyclerView.setAdapter(adapter);
        }
    }

    // ── Ações do Adapter ──────────────────────────────────────────────────────

    @Override
    public void onConsultar(Trilha trilha) {
        Intent intent = new Intent(this, VisualizarTrilhaActivity.class);
        intent.putExtra("trilha_id", trilha.getId());
        startActivity(intent);
    }

    @Override
    public void onEditar(Trilha trilha) {
        mostrarDialogoEditar(trilha);
    }

    @Override
    public void onApagar(Trilha trilha) {
        new AlertDialog.Builder(this)
                .setTitle("Apagar Trilha")
                .setMessage("Deseja apagar a trilha \"" + trilha.getNome() + "\"?")
                .setPositiveButton("Apagar", (dialog, which) -> {
                    db.deletarTrilha(trilha.getId());
                    Toast.makeText(this, "Trilha apagada.", Toast.LENGTH_SHORT).show();
                    carregarTrilhas();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ── Diálogo: editar nome ──────────────────────────────────────────────────

    private void mostrarDialogoEditar(Trilha trilha) {
        View view = getLayoutInflater().inflate(R.layout.dialog_editar_nome, null);
        EditText etNome = view.findViewById(R.id.et_novo_nome);
        etNome.setText(trilha.getNome());
        etNome.setSelection(etNome.getText().length());

        new AlertDialog.Builder(this)
                .setTitle("Editar Nome")
                .setView(view)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String novoNome = etNome.getText().toString().trim();
                    if (novoNome.isEmpty()) {
                        Toast.makeText(this, "Nome não pode ser vazio.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    db.atualizarNomeTrilha(trilha.getId(), novoNome);
                    Toast.makeText(this, "Nome atualizado.", Toast.LENGTH_SHORT).show();
                    carregarTrilhas();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ── Menu: apagar por intervalo / apagar todas ─────────────────────────────

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_consultar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_apagar_intervalo) {
            mostrarDialogoIntervalo();
            return true;
        } else if (id == R.id.action_apagar_todas) {
            confirmarApagarTodas();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void mostrarDialogoIntervalo() {
        Calendar calInicio = Calendar.getInstance();
        Calendar calFim    = Calendar.getInstance();
        SimpleDateFormat sdfExib = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        // Formato ISO para comparação no banco (coluna data_inicio é TEXT ISO 8601)
        SimpleDateFormat sdfIso  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

        new DatePickerDialog(this, (view, y, m, d) -> {
            calInicio.set(y, m, d, 0, 0, 0);
            calInicio.set(Calendar.MILLISECOND, 0);

            new DatePickerDialog(this, (view2, y2, m2, d2) -> {
                calFim.set(y2, m2, d2, 23, 59, 59);
                calFim.set(Calendar.MILLISECOND, 999);

                if (calFim.before(calInicio)) {
                    Toast.makeText(this, "Data final deve ser após a inicial.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String msg = "Apagar trilhas de " + sdfExib.format(calInicio.getTime())
                        + " até " + sdfExib.format(calFim.getTime()) + "?";

                new AlertDialog.Builder(this)
                        .setTitle("Apagar por Intervalo")
                        .setMessage(msg)
                        .setPositiveButton("Apagar", (dialog, which) -> {
                            int removidas = db.deletarTrilhasPorIntervalo(
                                    sdfIso.format(calInicio.getTime()),
                                    sdfIso.format(calFim.getTime()));
                            Toast.makeText(this, removidas + " trilha(s) apagada(s).",
                                    Toast.LENGTH_SHORT).show();
                            carregarTrilhas();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();

            }, calFim.get(Calendar.YEAR), calFim.get(Calendar.MONTH),
                    calFim.get(Calendar.DAY_OF_MONTH)).show();

        }, calInicio.get(Calendar.YEAR), calInicio.get(Calendar.MONTH),
                calInicio.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void confirmarApagarTodas() {
        if (listaTrilhas == null || listaTrilhas.isEmpty()) {
            Toast.makeText(this, "Não há trilhas para apagar.", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Apagar Todas")
                .setMessage("Tem certeza que deseja apagar TODAS as trilhas? Esta ação não pode ser desfeita.")
                .setPositiveButton("Apagar tudo", (dialog, which) -> {
                    db.deletarTodasTrilhas();
                    Toast.makeText(this, "Todas as trilhas foram apagadas.", Toast.LENGTH_SHORT).show();
                    carregarTrilhas();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
