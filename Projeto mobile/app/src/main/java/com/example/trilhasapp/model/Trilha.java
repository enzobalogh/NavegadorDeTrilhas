package com.example.trilhasapp.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Modelo que representa uma trilha registrada pelo usuário.
 * Mapeia diretamente para a tabela 'trilha' no banco SQLite.
 */
public class Trilha {

    private long id;
    private String nome;
    private String dataInicio;   // ISO: "yyyy-MM-dd HH:mm:ss"
    private String dataFim;
    private double velocidadeMedia;
    private double velocidadeMaxima;
    private double distanciaTotal;

    public Trilha() {}

    public Trilha(String nome, String dataInicio, String dataFim,
                  double velocidadeMedia, double velocidadeMaxima, double distanciaTotal) {
        this.nome = nome;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.velocidadeMedia = velocidadeMedia;
        this.velocidadeMaxima = velocidadeMaxima;
        this.distanciaTotal = distanciaTotal;
    }

    public Trilha(long id, String nome, String dataInicio, String dataFim,
                  double velocidadeMedia, double velocidadeMaxima, double distanciaTotal) {
        this.id = id;
        this.nome = nome;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.velocidadeMedia = velocidadeMedia;
        this.velocidadeMaxima = velocidadeMaxima;
        this.distanciaTotal = distanciaTotal;
    }

    // ── Getters / Setters básicos ────────────────────────────────────────────

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDataInicio() { return dataInicio; }
    public void setDataInicio(String dataInicio) { this.dataInicio = dataInicio; }

    public String getDataFim() { return dataFim; }
    public void setDataFim(String dataFim) { this.dataFim = dataFim; }

    public double getVelocidadeMedia() { return velocidadeMedia; }
    public void setVelocidadeMedia(double v) { this.velocidadeMedia = v; }

    public double getVelocidadeMaxima() { return velocidadeMaxima; }
    public void setVelocidadeMaxima(double v) { this.velocidadeMaxima = v; }

    public double getDistanciaTotal() { return distanciaTotal; }
    public void setDistanciaTotal(double d) { this.distanciaTotal = d; }

    // ── Métodos auxiliares ───────────────────────────────────────────────────

    private static final SimpleDateFormat SDF =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    /**
     * Retorna a data/hora de início como java.util.Date, ou null se não disponível.
     * Usado por VisualizarTrilhaActivity para formatar a data no overlay.
     */
    public Date getDataHoraInicio() {
        if (dataInicio == null || dataInicio.isEmpty()) return null;
        try {
            return SDF.parse(dataInicio);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Calcula e retorna a duração da trilha no formato HH:mm:ss.
     * Se os dados forem insuficientes retorna "—".
     */
    public String getDuracaoFormatada() {
        if (dataInicio == null || dataFim == null) return "—";
        try {
            Date inicio = SDF.parse(dataInicio);
            Date fim    = SDF.parse(dataFim);
            if (inicio == null || fim == null) return "—";

            long diffMs  = fim.getTime() - inicio.getTime();
            if (diffMs < 0) diffMs = 0;

            long horas   = diffMs / 3_600_000;
            long minutos = (diffMs % 3_600_000) / 60_000;
            long segundos = (diffMs % 60_000) / 1_000;

            return String.format(Locale.getDefault(), "%02d:%02d:%02d",
                    horas, minutos, segundos);
        } catch (ParseException e) {
            return "—";
        }
    }

    @Override
    public String toString() {
        return "Trilha{id=" + id + ", nome='" + nome + "', dataInicio='" + dataInicio + "'}";
    }
}
