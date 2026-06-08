package com.example.trilhasapp.model;

/**
 * Modelo que representa uma trilha registrada pelo usuário.
 * Mapeia diretamente para a tabela 'trilha' no banco SQLite.
 */
public class Trilha {

    // -------------------------------------------------------------------------
    // Atributos — correspondem às colunas da tabela 'trilha'
    // -------------------------------------------------------------------------

    private long id;                  // Chave primária (auto-increment)
    private String nome;              // Nome dado à trilha pelo usuário
    private String dataInicio;        // Data/hora de início (ISO 8601: "yyyy-MM-dd HH:mm:ss")
    private String dataFim;           // Data/hora de término
    private double velocidadeMedia;   // Velocidade média em km/h
    private double velocidadeMaxima;  // Velocidade máxima atingida em km/h
    private double distanciaTotal;    // Distância total percorrida em km

    // -------------------------------------------------------------------------
    // Construtores
    // -------------------------------------------------------------------------

    /** Construtor padrão exigido para instanciar sem argumentos (ex.: leitura do banco). */
    public Trilha() {}

    /**
     * Construtor completo para criação de uma trilha nova (sem id — gerado pelo banco).
     *
     * @param nome             Nome da trilha
     * @param dataInicio       Data/hora de início
     * @param dataFim          Data/hora de término
     * @param velocidadeMedia  Velocidade média em km/h
     * @param velocidadeMaxima Velocidade máxima em km/h
     * @param distanciaTotal   Distância total em km
     */
    public Trilha(String nome, String dataInicio, String dataFim,
                  double velocidadeMedia, double velocidadeMaxima, double distanciaTotal) {
        this.nome = nome;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.velocidadeMedia = velocidadeMedia;
        this.velocidadeMaxima = velocidadeMaxima;
        this.distanciaTotal = distanciaTotal;
    }

    /**
     * Construtor completo com id — utilizado ao recuperar registros do banco.
     */
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

    // -------------------------------------------------------------------------
    // Getters e Setters
    // -------------------------------------------------------------------------

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDataInicio() { return dataInicio; }
    public void setDataInicio(String dataInicio) { this.dataInicio = dataInicio; }

    public String getDataFim() { return dataFim; }
    public void setDataFim(String dataFim) { this.dataFim = dataFim; }

    public double getVelocidadeMedia() { return velocidadeMedia; }
    public void setVelocidadeMedia(double velocidadeMedia) { this.velocidadeMedia = velocidadeMedia; }

    public double getVelocidadeMaxima() { return velocidadeMaxima; }
    public void setVelocidadeMaxima(double velocidadeMaxima) { this.velocidadeMaxima = velocidadeMaxima; }

    public double getDistanciaTotal() { return distanciaTotal; }
    public void setDistanciaTotal(double distanciaTotal) { this.distanciaTotal = distanciaTotal; }

    // -------------------------------------------------------------------------
    // toString — útil para debug e logs
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "Trilha{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", dataInicio='" + dataInicio + '\'' +
                ", dataFim='" + dataFim + '\'' +
                ", velocidadeMedia=" + velocidadeMedia +
                ", velocidadeMaxima=" + velocidadeMaxima +
                ", distanciaTotal=" + distanciaTotal +
                '}';
    }
}
