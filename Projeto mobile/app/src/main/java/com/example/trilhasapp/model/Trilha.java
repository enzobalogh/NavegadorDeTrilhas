package com.example.trilhasapp.model;

public class Trilha {

    private long id;                  
    private String nome;              
    private String dataInicio;        
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
