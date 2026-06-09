package com.example.trilhasapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    public static final String DATABASE_NAME = "trilhas.db";

    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_TRILHA = "trilha";

    public static final String COL_TRILHA_ID              = "id";
    public static final String COL_TRILHA_NOME            = "nome";
    public static final String COL_TRILHA_DATA_INICIO     = "data_inicio";
    public static final String COL_TRILHA_DATA_FIM        = "data_fim";
    public static final String COL_TRILHA_VEL_MEDIA       = "velocidade_media";
    public static final String COL_TRILHA_VEL_MAXIMA      = "velocidade_maxima";
    public static final String COL_TRILHA_DISTANCIA_TOTAL = "distancia_total";

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

    public static final String TABLE_PONTO_TRILHA = "ponto_trilha";

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
            "REFERENCES " + TABLE_TRILHA + "(" + COL_TRILHA_ID + ") " +
            "ON DELETE CASCADE" +
            ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Criando banco de dados versão " + DATABASE_VERSION);

        db.execSQL("PRAGMA foreign_keys = ON");

        db.execSQL(SQL_CREATE_TABLE_TRILHA);
        Log.d(TAG, "Tabela '" + TABLE_TRILHA + "' criada.");

        db.execSQL(SQL_CREATE_TABLE_PONTO_TRILHA);
        Log.d(TAG, "Tabela '" + TABLE_PONTO_TRILHA + "' criada.");

        db.execSQL("CREATE INDEX idx_ponto_trilha_id ON " +
                TABLE_PONTO_TRILHA + "(" + COL_PONTO_TRILHA_ID + ")");
        Log.d(TAG, "Índice 'idx_ponto_trilha_id' criado.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Atualizando banco de versão " + oldVersion + " para " + newVersion);

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
}
