package com.example.trilhasapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.trilhasapp.model.PontoTrilha;
import com.example.trilhasapp.model.Trilha;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper para criação e gerenciamento do banco de dados SQLite.
 * Contém todos os métodos CRUD para as tabelas 'trilha' e 'ponto_trilha'.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    public static final String DATABASE_NAME    = "trilhas.db";
    public static final int    DATABASE_VERSION = 1;

    // ── Tabela trilha ────────────────────────────────────────────────────────
    public static final String TABLE_TRILHA              = "trilha";
    public static final String COL_TRILHA_ID             = "id";
    public static final String COL_TRILHA_NOME           = "nome";
    public static final String COL_TRILHA_DATA_INICIO    = "data_inicio";
    public static final String COL_TRILHA_DATA_FIM       = "data_fim";
    public static final String COL_TRILHA_VEL_MEDIA      = "velocidade_media";
    public static final String COL_TRILHA_VEL_MAXIMA     = "velocidade_maxima";
    public static final String COL_TRILHA_DISTANCIA_TOTAL= "distancia_total";

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

    // ── Tabela ponto_trilha ──────────────────────────────────────────────────
    public static final String TABLE_PONTO_TRILHA  = "ponto_trilha";
    public static final String COL_PONTO_ID        = "id";
    public static final String COL_PONTO_TRILHA_ID = "trilha_id";
    public static final String COL_PONTO_LATITUDE  = "latitude";
    public static final String COL_PONTO_LONGITUDE = "longitude";
    public static final String COL_PONTO_TIMESTAMP = "timestamp";

    private static final String SQL_CREATE_TABLE_PONTO_TRILHA =
            "CREATE TABLE " + TABLE_PONTO_TRILHA + " (" +
            COL_PONTO_ID        + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_PONTO_TRILHA_ID + " INTEGER NOT NULL, " +
            COL_PONTO_LATITUDE  + " REAL NOT NULL, " +
            COL_PONTO_LONGITUDE + " REAL NOT NULL, " +
            COL_PONTO_TIMESTAMP + " TEXT, " +
            "FOREIGN KEY (" + COL_PONTO_TRILHA_ID + ") " +
            "REFERENCES " + TABLE_TRILHA + "(" + COL_TRILHA_ID + ") ON DELETE CASCADE" +
            ")";

    // ── Construtor ───────────────────────────────────────────────────────────

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON");
        db.execSQL(SQL_CREATE_TABLE_TRILHA);
        db.execSQL(SQL_CREATE_TABLE_PONTO_TRILHA);
        db.execSQL("CREATE INDEX idx_ponto_trilha_id ON " +
                TABLE_PONTO_TRILHA + "(" + COL_PONTO_TRILHA_ID + ")");
        Log.d(TAG, "Banco criado.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PONTO_TRILHA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRILHA);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys = ON");
        }
    }

    // =========================================================================
    // CRUD – Trilha
    // =========================================================================

    /** Insere uma nova trilha e retorna o id gerado, ou -1 em caso de erro. */
    public long inserirTrilha(Trilha trilha) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TRILHA_NOME,            trilha.getNome());
        cv.put(COL_TRILHA_DATA_INICIO,     trilha.getDataInicio());
        cv.put(COL_TRILHA_DATA_FIM,        trilha.getDataFim());
        cv.put(COL_TRILHA_VEL_MEDIA,       trilha.getVelocidadeMedia());
        cv.put(COL_TRILHA_VEL_MAXIMA,      trilha.getVelocidadeMaxima());
        cv.put(COL_TRILHA_DISTANCIA_TOTAL, trilha.getDistanciaTotal());
        return db.insert(TABLE_TRILHA, null, cv);
    }

    /** Retorna todas as trilhas ordenadas da mais recente para a mais antiga. */
    public List<Trilha> listarTodas() {
        List<Trilha> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_TRILHA, null, null, null, null, null,
                COL_TRILHA_DATA_INICIO + " DESC");
        if (c != null) {
            while (c.moveToNext()) {
                lista.add(cursorParaTrilha(c));
            }
            c.close();
        }
        return lista;
    }

    /** Busca uma trilha pelo id. Retorna null se não encontrada. */
    public Trilha buscarTrilhaPorId(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_TRILHA, null,
                COL_TRILHA_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null);
        Trilha trilha = null;
        if (c != null) {
            if (c.moveToFirst()) {
                trilha = cursorParaTrilha(c);
            }
            c.close();
        }
        return trilha;
    }

    /** Atualiza apenas o nome de uma trilha. */
    public int atualizarNomeTrilha(long id, String novoNome) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TRILHA_NOME, novoNome);
        return db.update(TABLE_TRILHA, cv,
                COL_TRILHA_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    /** Atualiza o resumo final de uma trilha (chamado ao finalizar o registro). */
    public int atualizarResumoFinal(long id, String dataFim,
                                    double velocidadeMedia, double velocidadeMaxima,
                                    double distanciaTotal) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TRILHA_DATA_FIM,        dataFim);
        cv.put(COL_TRILHA_VEL_MEDIA,       velocidadeMedia);
        cv.put(COL_TRILHA_VEL_MAXIMA,      velocidadeMaxima);
        cv.put(COL_TRILHA_DISTANCIA_TOTAL, distanciaTotal);
        return db.update(TABLE_TRILHA, cv,
                COL_TRILHA_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    /** Remove uma trilha e seus pontos (via CASCADE). */
    public int deletarTrilha(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_TRILHA,
                COL_TRILHA_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    /**
     * Remove trilhas cujo data_inicio esteja entre dataInicio e dataFim (ISO 8601).
     * Retorna o número de linhas removidas.
     */
    public int deletarTrilhasPorIntervalo(String dataInicio, String dataFim) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_TRILHA,
                COL_TRILHA_DATA_INICIO + " >= ? AND " + COL_TRILHA_DATA_INICIO + " <= ?",
                new String[]{dataInicio, dataFim});
    }

    /** Remove todas as trilhas (e seus pontos via CASCADE). */
    public int deletarTodasTrilhas() {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_TRILHA, null, null);
    }

    // =========================================================================
    // CRUD – PontoTrilha
    // =========================================================================

    /** Insere um ponto de localização associado a uma trilha. */
    public long inserirPonto(PontoTrilha ponto) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PONTO_TRILHA_ID, ponto.getTrilhaId());
        cv.put(COL_PONTO_LATITUDE,  ponto.getLatitude());
        cv.put(COL_PONTO_LONGITUDE, ponto.getLongitude());
        cv.put(COL_PONTO_TIMESTAMP, ponto.getTimestamp());
        return db.insert(TABLE_PONTO_TRILHA, null, cv);
    }

    /** Retorna todos os pontos de uma trilha ordenados pelo timestamp. */
    public List<PontoTrilha> listarPontos(long trilhaId) {
        List<PontoTrilha> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PONTO_TRILHA, null,
                COL_PONTO_TRILHA_ID + " = ?",
                new String[]{String.valueOf(trilhaId)},
                null, null, COL_PONTO_TIMESTAMP + " ASC");
        if (c != null) {
            while (c.moveToNext()) {
                lista.add(cursorParaPonto(c));
            }
            c.close();
        }
        return lista;
    }

    // =========================================================================
    // Helpers privados
    // =========================================================================

    private Trilha cursorParaTrilha(Cursor c) {
        return new Trilha(
                c.getLong(c.getColumnIndexOrThrow(COL_TRILHA_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_TRILHA_NOME)),
                c.getString(c.getColumnIndexOrThrow(COL_TRILHA_DATA_INICIO)),
                c.getString(c.getColumnIndexOrThrow(COL_TRILHA_DATA_FIM)),
                c.getDouble(c.getColumnIndexOrThrow(COL_TRILHA_VEL_MEDIA)),
                c.getDouble(c.getColumnIndexOrThrow(COL_TRILHA_VEL_MAXIMA)),
                c.getDouble(c.getColumnIndexOrThrow(COL_TRILHA_DISTANCIA_TOTAL))
        );
    }

    private PontoTrilha cursorParaPonto(Cursor c) {
        return new PontoTrilha(
                c.getLong(c.getColumnIndexOrThrow(COL_PONTO_ID)),
                c.getLong(c.getColumnIndexOrThrow(COL_PONTO_TRILHA_ID)),
                c.getDouble(c.getColumnIndexOrThrow(COL_PONTO_LATITUDE)),
                c.getDouble(c.getColumnIndexOrThrow(COL_PONTO_LONGITUDE)),
                c.getString(c.getColumnIndexOrThrow(COL_PONTO_TIMESTAMP))
        );
    }
}
