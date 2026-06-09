package com.example.trilhasapp.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.trilhasapp.database.DatabaseHelper;
import com.example.trilhasapp.model.PontoTrilha;
import com.example.trilhasapp.model.Trilha;

/**
 * DAO responsável por gravar trilhas e pontos GPS no SQLite.
 * A parte de consulta/edição/remoção pode reutilizar esta classe depois.
 */
public class TrilhaDao {

    private final DatabaseHelper databaseHelper;

    public TrilhaDao(Context context) {
        databaseHelper = new DatabaseHelper(context.getApplicationContext());
    }

    /**
     * Insere uma trilha ainda em andamento.
     * data_fim, velocidade média, velocidade máxima e distância são atualizadas ao finalizar.
     */
    public long inserirTrilha(Trilha trilha) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_TRILHA_NOME, trilha.getNome());
        values.put(DatabaseHelper.COL_TRILHA_DATA_INICIO, trilha.getDataInicio());
        values.put(DatabaseHelper.COL_TRILHA_DATA_FIM, trilha.getDataFim());
        values.put(DatabaseHelper.COL_TRILHA_VEL_MEDIA, trilha.getVelocidadeMedia());
        values.put(DatabaseHelper.COL_TRILHA_VEL_MAXIMA, trilha.getVelocidadeMaxima());
        values.put(DatabaseHelper.COL_TRILHA_DISTANCIA_TOTAL, trilha.getDistanciaTotal());

        return db.insert(DatabaseHelper.TABLE_TRILHA, null, values);
    }

    /**
     * Insere um ponto geográfico associado à trilha atual.
     */
    public long inserirPonto(PontoTrilha ponto) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_PONTO_TRILHA_ID, ponto.getTrilhaId());
        values.put(DatabaseHelper.COL_PONTO_LATITUDE, ponto.getLatitude());
        values.put(DatabaseHelper.COL_PONTO_LONGITUDE, ponto.getLongitude());
        values.put(DatabaseHelper.COL_PONTO_TIMESTAMP, ponto.getTimestamp());

        return db.insert(DatabaseHelper.TABLE_PONTO_TRILHA, null, values);
    }

    /**
     * Atualiza as estatísticas finais da trilha quando o usuário para o monitoramento.
     */
    public int atualizarResumoFinal(long trilhaId, String dataFim,
                                    double velocidadeMedia, double velocidadeMaxima,
                                    double distanciaTotal) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_TRILHA_DATA_FIM, dataFim);
        values.put(DatabaseHelper.COL_TRILHA_VEL_MEDIA, velocidadeMedia);
        values.put(DatabaseHelper.COL_TRILHA_VEL_MAXIMA, velocidadeMaxima);
        values.put(DatabaseHelper.COL_TRILHA_DISTANCIA_TOTAL, distanciaTotal);

        return db.update(
                DatabaseHelper.TABLE_TRILHA,
                values,
                DatabaseHelper.COL_TRILHA_ID + " = ?",
                new String[]{String.valueOf(trilhaId)}
        );
    }

    public void fechar() {
        databaseHelper.close();
    }
}
