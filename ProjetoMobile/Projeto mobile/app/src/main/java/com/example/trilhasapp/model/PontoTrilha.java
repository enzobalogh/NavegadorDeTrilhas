package com.example.trilhasapp.model;

/**
 * Modelo que representa um ponto geográfico registrado durante uma trilha.
 * Mapeia para a tabela 'ponto_trilha' no banco SQLite.
 * Cada ponto está associado a uma trilha via chave estrangeira (trilhaId).
 */
public class PontoTrilha {

    // -------------------------------------------------------------------------
    // Atributos — correspondem às colunas da tabela 'ponto_trilha'
    // -------------------------------------------------------------------------

    private long id;          // Chave primária (auto-increment)
    private long trilhaId;    // FK → tabela 'trilha' (id da trilha pai)
    private double latitude;  // Latitude do ponto em graus decimais (ex: -12.9714)
    private double longitude; // Longitude do ponto em graus decimais (ex: -38.5014)
    private String timestamp; // Momento do registro (ISO 8601: "yyyy-MM-dd HH:mm:ss")

    // -------------------------------------------------------------------------
    // Construtores
    // -------------------------------------------------------------------------

    /** Construtor padrão. */
    public PontoTrilha() {}

    /**
     * Construtor para inserção de novo ponto (id gerado pelo banco).
     *
     * @param trilhaId  Id da trilha à qual este ponto pertence
     * @param latitude  Latitude em graus decimais
     * @param longitude Longitude em graus decimais
     * @param timestamp Data/hora do registro
     */
    public PontoTrilha(long trilhaId, double latitude, double longitude, String timestamp) {
        this.trilhaId = trilhaId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    /**
     * Construtor completo com id — usado ao recuperar pontos do banco.
     */
    public PontoTrilha(long id, long trilhaId, double latitude, double longitude, String timestamp) {
        this.id = id;
        this.trilhaId = trilhaId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    // -------------------------------------------------------------------------
    // Getters e Setters
    // -------------------------------------------------------------------------

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTrilhaId() { return trilhaId; }
    public void setTrilhaId(long trilhaId) { this.trilhaId = trilhaId; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    // -------------------------------------------------------------------------
    // toString — útil para debug
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "PontoTrilha{" +
                "id=" + id +
                ", trilhaId=" + trilhaId +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
