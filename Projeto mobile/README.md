# TrilhasApp — Parte 1

Sistema de gerenciamento de trilhas para Android.
Trabalho acadêmico de Programação Mobile.

---

## Estrutura de pacotes

```
app/src/main/
├── AndroidManifest.xml
├── java/com/example/trilhasapp/
│   ├── activities/
│   │   ├── MainActivity.java          ← Tela principal (menu)
│   │   └── ConfiguracaoActivity.java  ← Tela de configurações
│   ├── database/
│   │   └── DatabaseHelper.java        ← SQLiteOpenHelper (2 tabelas)
│   ├── model/
│   │   ├── Trilha.java                ← Modelo da trilha
│   │   └── PontoTrilha.java           ← Modelo de ponto GPS
│   └── utils/
│       └── PreferencesManager.java    ← Wrapper para SharedPreferences
└── res/
    ├── layout/
    │   ├── activity_main.xml
    │   └── activity_configuracao.xml
    ├── values/
    │   ├── strings.xml
    │   ├── colors.xml
    │   └── styles.xml
    └── drawable/
        ├── ic_trilha_logo.xml
        ├── ic_add_location.xml
        ├── ic_list_trilhas.xml
        ├── ic_settings.xml
        ├── ic_map.xml
        └── ic_navigation.xml
```

---

## Como importar no Android Studio

1. Abra o Android Studio → **File > Open**
2. Selecione a pasta `TrilhasApp/`
3. Aguarde o Gradle sync completar
4. Execute em um dispositivo ou emulador com API 17+

---

## Dependências (app/build.gradle)

| Biblioteca              | Versão   | Uso                            |
|-------------------------|----------|-------------------------------|
| appcompat               | 1.6.1    | Compatibilidade API 17+        |
| material                | 1.11.0   | Componentes Material Design    |
| constraintlayout        | 2.1.4    | Layouts responsivos            |
| cardview                | 1.0.0    | Cards nas configurações        |

---

## Banco de dados SQLite

### Tabela `trilha`
| Coluna            | Tipo    | Descrição                |
|-------------------|---------|--------------------------|
| id                | INTEGER | PK auto-increment        |
| nome              | TEXT    | Nome da trilha           |
| data_inicio       | TEXT    | ISO 8601                 |
| data_fim          | TEXT    | ISO 8601                 |
| velocidade_media  | REAL    | km/h                     |
| velocidade_maxima | REAL    | km/h                     |
| distancia_total   | REAL    | km                       |

### Tabela `ponto_trilha`
| Coluna     | Tipo    | Descrição                     |
|------------|---------|-------------------------------|
| id         | INTEGER | PK auto-increment             |
| trilha_id  | INTEGER | FK → trilha(id) CASCADE DELETE |
| latitude   | REAL    | Graus decimais                |
| longitude  | REAL    | Graus decimais                |
| timestamp  | TEXT    | ISO 8601                      |

---

## SharedPreferences — PreferencesManager

| Método                      | Chave              | Padrão      |
|-----------------------------|--------------------|-------------|
| `saveMapType(String)`       | `map_type`         | `"vector"`  |
| `getMapType()`              | `map_type`         | `"vector"`  |
| `saveNavigationMode(String)`| `navigation_mode`  | `"north_up"`|
| `getNavigationMode()`       | `navigation_mode`  | `"north_up"`|

---

## O que será implementado na Parte 2

- `RegistrarTrilhaActivity` — rastreamento GPS em tempo real
- `ConsultarTrilhasActivity` — listagem e detalhes das trilhas
- DAOs para `Trilha` e `PontoTrilha`
- Serviço de localização em background

---

## Implementado nesta versão — Registrar Trilha

Arquivos adicionados/alterados:

- `activities/RegistrarTrilhaActivity.java`
  - Exibe mapa em tempo real;
  - Solicita permissão de localização;
  - Mostra velocidade atual, velocidade máxima, cronômetro e distância;
  - Desenha marcador personalizado, círculo de acurácia e linha do percurso;
  - Salva trilha e pontos no SQLite.

- `dao/TrilhaDao.java`
  - Insere trilha;
  - Insere pontos da trilha;
  - Atualiza resumo final com data de fim, velocidade média, velocidade máxima e distância total.

- `res/layout/activity_registrar_trilha.xml`
  - Layout da tela de registro com `SupportMapFragment` e painel sobreposto.

- `AndroidManifest.xml`
  - Ativou `RegistrarTrilhaActivity`;
  - Adicionou metadado da chave do Google Maps.

- `app/build.gradle`
  - Adicionou dependências de Google Maps e Fused Location.

### Atenção: chave do Google Maps

Antes de executar em aparelho real/emulador, troque o valor abaixo em `res/values/strings.xml`:

```xml
<string name="google_maps_key">COLE_SUA_CHAVE_AQUI</string>
```

Sem a chave da API, a tela abre, mas o mapa do Google pode não carregar corretamente.
