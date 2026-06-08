package com.example.trilhasapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Helper para criação e gerenciamento do banco de dados SQLite do aplicativo.
 *
 * Herda de {@link SQLiteOpenHelper}, que cuida automaticamente de:
 *   - Criar o banco na primeira execução (onCreate)
 *   - Migrar o esquema quando a versão muda (onUpgrade)
 *
 * Tabelas criadas:
 *   - trilha       → armazena cabeçalho e estatísticas de cada trilha
 *   - ponto_trilha → armazena cada coordenada GPS coletada durante a trilha
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    // -------------------------------------------------------------------------
    // Configurações do banco
    // -------------------------------------------------------------------------

    /** Nome do arquivo .db criado no armazenamento interno do app. */
    public static final String DATABASE_NAME = "trilhas.db";

    /**
     * Versão do esquema. Incremente sempre que alterar CREATE TABLE.
     * O Android chamará onUpgrade() automaticamente nos dispositivos
     * que tiverem uma versão anterior instalada.
     */
    public static final int DATABASE_VERSION = 1;

    // =========================================================================
    // Tabela: trilha
    // =========================================================================

    /** Nome da tabela principal de trilhas. */
    public static final String TABLE_TRILHA = "trilha";

    // Nomes das colunas
    public static final String COL_TRILHA_ID              = "id";
    public static final String COL_TRILHA_NOME            = "nome";
    public static final String COL_TRILHA_DATA_INICIO     = "data_inicio";
    public static final String COL_TRILHA_DATA_FIM        = "data_fim";
    public static final String COL_TRILHA_VEL_MEDIA       = "velocidade_media";
    public static final String COL_TRILHA_VEL_MAXIMA      = "velocidade_maxima";
    public static final String COL_TRILHA_DISTANCIA_TOTAL = "distancia_total";

    /**
     * SQL de criação da tabela trilha.
     *
     * Campos:
     *   id                INTEGER PRIMARY KEY AUTOINCREMENT
     *   nome              TEXT NOT NULL              — obrigatório
     *   data_inicio       TEXT                       — ISO 8601
     *   data_fim          TEXT                       — ISO 8601
     *   velocidade_media  REAL DEFAULT 0             — km/h
     *   velocidade_maxima REAL DEFAULT 0             — km/h
     *   distancia_total   REAL DEFAULT 0             — km
     */
    private static final String SQL_CREATE_TABLE_TRILHA =
            "CREATE TABLE " + TABLE_TRILHA + " (" +
            COL_TRILHA_ID              + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_TRILHA_NOME            + " TEXT NOT NULL, " +
            COL_TRILHA_DATA_INICIO     + " TEXT, " +
            COL_TRILHA_DATA_FIM        + " TEXT, " +
            COL_TRILHA_VEL_MEDIA       + " REAL DEFAULT 0, " +
            COL_TRILHA_VEL_MAXIMA      + " REAL DEFAULT 0, " +
            COL_TRILHA_DISTANCIA_TOTAL + " REAL DEFAULT 0" +
            ")";

    // =========================================================================
    // Tabela: ponto_trilha
    // =========================================================================

    /** Nome da tabela de pontos geográficos. */
    public static final String TABLE_PONTO_TRILHA = "ponto_trilha";

    // Nomes das colunas
    public static final String COL_PONTO_ID        = "id";
    public static final String COL_PONTO_TRILHA_ID = "trilha_id";   // FK → trilha.id
    public static final String COL_PONTO_LATITUDE  = "latitude";
    public static final String COL_PONTO_LONGITUDE = "longitude";
    public static final String COL_PONTO_TIMESTAMP = "timestamp";

    /**
     * SQL de criação da tabela ponto_trilha.
     *
     * Campos:
     *   id         INTEGER PRIMARY KEY AUTOINCREMENT
     *   trilha_id  INTEGER NOT NULL  — chave estrangeira para trilha(id)
     *   latitude   REAL NOT NULL     — graus decimais (ex: -12.9714)
     *   longitude  REAL NOT NULL     — graus decimais (ex: -38.5014)
     *   timestamp  TEXT              — ISO 8601
     *
     * Relacionamento:
     *   FOREIGN KEY (trilha_id) REFERENCES trilha(id) ON DELETE CASCADE
     *   → ao deletar uma trilha, todos os seus pontos são removidos automaticamente
     */
    private static final String SQL_CREATE_TABLE_PONTO_TRILHA =
            "CREATE TABLE " + TABLE_PONTO_TRILHA + " (" +
            COL_PONTO_ID        + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_PONTO_TRILHA_ID + " INTEGER NOT NULL, " +
            COL_PONTO_LATITUDE  + " REAL NOT NULL, " +
            COL_PONTO_LONGITUDE + " REAL NOT NULL, " +
            COL_PONTO_TIMESTAMP + " TEXT, " +
            "FOREIGN KEY (" + COL_PONTO_TRILHA_ID + ") " +
            "REFERENCES " + TABLE_TRILHA + "(" + COL_TRILHA_ID + ") " +
            "ON DELETE CASCADE" +
            ")";

    // =========================================================================
    // Construtor
    // =========================================================================

    /**
     * Inicializa o DatabaseHelper.
     *
     * @param context Contexto da aplicação (use getApplicationContext() quando possível)
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // =========================================================================
    // Ciclo de vida do banco
    // =========================================================================

    /**
     * Chamado pelo Android na PRIMEIRA vez que o banco é criado no dispositivo.
     * Aqui executamos os CREATEs de todas as tabelas.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Criando banco de dados versão " + DATABASE_VERSION);

        // Habilitar suporte a chaves estrangeiras no SQLite
        // (desabilitado por padrão no Android por compatibilidade)
        db.execSQL("PRAGMA foreign_keys = ON");

        // Criar tabela de trilhas
        db.execSQL(SQL_CREATE_TABLE_TRILHA);
        Log.d(TAG, "Tabela '" + TABLE_TRILHA + "' criada.");

        // Criar tabela de pontos (depende da tabela trilha já existir)
        db.execSQL(SQL_CREATE_TABLE_PONTO_TRILHA);
        Log.d(TAG, "Tabela '" + TABLE_PONTO_TRILHA + "' criada.");

        // Criar índice na FK trilha_id para acelerar consultas de pontos por trilha
        db.execSQL("CREATE INDEX idx_ponto_trilha_id ON " +
                TABLE_PONTO_TRILHA + "(" + COL_PONTO_TRILHA_ID + ")");
        Log.d(TAG, "Índice 'idx_ponto_trilha_id' criado.");
    }

    /**
     * Chamado quando DATABASE_VERSION é incrementado.
     * Estratégia atual: drop e recria (adequado para desenvolvimento).
     * Em produção, realizar migração incremental para preservar dados.
     *
     * @param db         Banco existente
     * @param oldVersion Versão anterior
     * @param newVersion Nova versão
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Atualizando banco de versão " + oldVersion + " para " + newVersion);

        // Remove tabelas existentes (atenção: perde dados)
        // A ordem importa: remover a tabela filha antes da pai (por causa da FK)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PONTO_TRILHA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRILHA);

        // Recria tudo com o esquema mais novo
        onCreate(db);
    }

    /**
     * Chamado toda vez que uma conexão com o banco é aberta.
     * Garante que as foreign keys sejam respeitadas a cada conexão.
     */
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Habilitar foreign keys (precisa ser feito em cada conexão)
            db.execSQL("PRAGMA foreign_keys = ON");
        }
    }
}
