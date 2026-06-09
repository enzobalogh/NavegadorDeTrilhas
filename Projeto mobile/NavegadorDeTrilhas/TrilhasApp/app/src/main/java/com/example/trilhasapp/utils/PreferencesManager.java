package com.example.trilhasapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Gerenciador centralizado de preferências do aplicativo.
 *
 * Encapsula toda a lógica de leitura e escrita no SharedPreferences,
 * evitando que as Activities manipulem as chaves diretamente.
 *
 * Uso:
 *   PreferencesManager prefs = new PreferencesManager(context);
 *   prefs.saveMapType(PreferencesManager.MAP_TYPE_SATELLITE);
 *   String tipo = prefs.getMapType(); // "satellite"
 */
public class PreferencesManager {

    // -------------------------------------------------------------------------
    // Constantes — nome do arquivo de preferências e chaves
    // -------------------------------------------------------------------------

    /** Nome do arquivo SharedPreferences armazenado no dispositivo. */
    private static final String PREFS_NAME = "trilhas_app_prefs";

    /** Chave para o tipo de mapa selecionado. */
    private static final String KEY_MAP_TYPE = "map_type";

    /** Chave para o modo de navegação selecionado. */
    private static final String KEY_NAVIGATION_MODE = "navigation_mode";

    // -------------------------------------------------------------------------
    // Valores possíveis — tipo de mapa
    // -------------------------------------------------------------------------

    /** Mapa vetorial (padrão). */
    public static final String MAP_TYPE_VECTOR = "vector";

    /** Mapa satélite. */
    public static final String MAP_TYPE_SATELLITE = "satellite";

    // -------------------------------------------------------------------------
    // Valores possíveis — modo de navegação
    // -------------------------------------------------------------------------

    /** North Up: o norte permanece sempre no topo do mapa (padrão). */
    public static final String NAVIGATION_NORTH_UP = "north_up";

    /** Course Up: o mapa gira conforme a direção do movimento. */
    public static final String NAVIGATION_COURSE_UP = "course_up";

    // -------------------------------------------------------------------------
    // Instância do SharedPreferences
    // -------------------------------------------------------------------------

    private final SharedPreferences sharedPreferences;

    // -------------------------------------------------------------------------
    // Construtor
    // -------------------------------------------------------------------------

    /**
     * Inicializa o PreferencesManager obtendo o SharedPreferences do contexto.
     *
     * @param context Contexto da Activity ou Application
     */
    public PreferencesManager(Context context) {
        // MODE_PRIVATE: somente este app pode ler/escrever essas preferências
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // -------------------------------------------------------------------------
    // Tipo de Mapa
    // -------------------------------------------------------------------------

    /**
     * Salva o tipo de mapa escolhido pelo usuário.
     *
     * @param mapType {@link #MAP_TYPE_VECTOR} ou {@link #MAP_TYPE_SATELLITE}
     */
    public void saveMapType(String mapType) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_MAP_TYPE, mapType);
        editor.apply(); // apply() é assíncrono e não bloqueia a UI thread
    }

    /**
     * Recupera o tipo de mapa salvo.
     * Retorna {@link #MAP_TYPE_VECTOR} como padrão caso nenhum valor tenha sido salvo.
     *
     * @return String com o tipo de mapa
     */
    public String getMapType() {
        return sharedPreferences.getString(KEY_MAP_TYPE, MAP_TYPE_VECTOR);
    }

    // -------------------------------------------------------------------------
    // Modo de Navegação
    // -------------------------------------------------------------------------

    /**
     * Salva o modo de navegação escolhido pelo usuário.
     *
     * @param navigationMode {@link #NAVIGATION_NORTH_UP} ou {@link #NAVIGATION_COURSE_UP}
     */
    public void saveNavigationMode(String navigationMode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_NAVIGATION_MODE, navigationMode);
        editor.apply();
    }

    /**
     * Recupera o modo de navegação salvo.
     * Retorna {@link #NAVIGATION_NORTH_UP} como padrão caso nenhum valor tenha sido salvo.
     *
     * @return String com o modo de navegação
     */
    public String getNavigationMode() {
        return sharedPreferences.getString(KEY_NAVIGATION_MODE, NAVIGATION_NORTH_UP);
    }

    // -------------------------------------------------------------------------
    // Utilitários
    // -------------------------------------------------------------------------

    /**
     * Remove todas as preferências salvas (útil para reset ou logout).
     */
    public void clearAll() {
        sharedPreferences.edit().clear().apply();
    }

    /**
     * Verifica se o tipo de mapa atual é Satélite.
     *
     * @return true se for satélite, false caso contrário
     */
    public boolean isSatelliteMap() {
        return MAP_TYPE_SATELLITE.equals(getMapType());
    }

    /**
     * Verifica se o modo de navegação atual é Course Up.
     *
     * @return true se for Course Up, false caso contrário
     */
    public boolean isCourseUp() {
        return NAVIGATION_COURSE_UP.equals(getNavigationMode());
    }
}
