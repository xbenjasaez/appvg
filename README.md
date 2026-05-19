# Docente Calma

Aplicación Android nativa de bienestar socioemocional para **docentes part-time** del **Instituto Profesional Virginio Gómez** (en la app: **IP Virginio Gómez**). Permite registrar estados emocionales, recibir recomendaciones inmediatas, consultar orientación para situaciones de aula, acceder a ejercicios breves de regulación y conversar con un chat de apoyo asistido por IA.

> **Uso personal, privado y offline-first.** Sin login, sin backend propio, sin panel administrativo. Todos los datos viven en el dispositivo.

---

## Tabla de contenidos

1. [Propósito](#propósito)
2. [Stack tecnológico](#stack-tecnológico)
3. [Arquitectura](#arquitectura)
4. [Estructura de carpetas](#estructura-de-carpetas)
5. [Módulos principales](#módulos-principales)
6. [Configuración de la Gemini API](#configuración-de-la-gemini-api)
7. [Cómo ejecutar](#cómo-ejecutar)
8. [Testing](#testing)
9. [Privacidad](#privacidad)
10. [Limitaciones del MVP](#limitaciones-del-mvp)
11. [Mejoras futuras](#mejoras-futuras)

---

## Propósito

La app está pensada como herramienta **personal** para docentes a honorarios que trabajan en distintas sedes, con horarios parciales y bajo acompañamiento institucional continuo. Busca ser:

- **Rápida**: chequeo emocional en menos de 15 segundos.
- **Privada**: no exporta datos ni requiere cuenta.
- **Práctica**: respuestas concretas y breves, no diagnósticos.
- **Responsable**: ante señales de crisis, deriva a ayuda profesional.

No reemplaza atención psicológica profesional. Es un complemento de autoobservación y apoyo inmediato.

---

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Kotlin `2.0.21` |
| UI | Jetpack Compose (BOM `2024.10.01`), Material 3 |
| Navegación | Navigation Compose `2.8.3` |
| DI | Hilt `2.52` |
| Persistencia | Room `2.6.1` + KSP `2.0.21-1.0.28` |
| Concurrencia | Kotlin Coroutines `1.9.0`, `StateFlow`, `Channel` |
| IA | Google Generative AI SDK `0.9.0` (Gemini) |
| Build | AGP `8.6.1`, JDK 17 |
| Testing | JUnit 4, `kotlinx-coroutines-test` |
| `compileSdk` / `targetSdk` | 35 |
| `minSdk` | 26 (Android 8.0) |

---

## Arquitectura

**MVVM + Repository + Domain rules**, offline-first, con la capa `ai/` completamente desacoplada.

```
┌───────────────────────── UI (Compose) ─────────────────────────┐
│  Screens ──► ViewModels  (uiState: StateFlow, events, effects) │
└───────────────────┬────────────────────────────────────────────┘
                    │
┌───────────────────▼───────────── Domain ──────────────────────┐
│  models (Emotion, EmotionalCheckIn, Recommendation, …)        │
│  mappers (entity ↔ domain)                                    │
│  rules (RecommendationEngine, ProgressCalculator,             │
│         ClassroomScenarioCatalog, QuickExerciseCatalog,       │
│         EmotionRuleCatalog)                                   │
└───────────────────┬──────────────────────────┬────────────────┘
                    │                          │
┌───────────────────▼──────────┐  ┌────────────▼──────────────┐
│  Data                         │  │  AI (decoupled module)     │
│   local (Room: DAO + Entity)  │  │   SupportChatAi interface  │
│   repository                  │  │   Gemini + Resilient +     │
│                               │  │   Fallback + RiskClassifier│
└───────────────────────────────┘  └────────────────────────────┘
```

### Reglas aplicadas

- **Una fuente de verdad por ViewModel**: `MutableStateFlow<UiState>` interno, expuesto como `StateFlow` de solo lectura.
- **Eventos del usuario**: `sealed interface ...Event` con `data object` / `data class`.
- **Efectos one-shot** (navegación, snackbar): `Channel<Effect>.receiveAsFlow()`.
- **Sin acceso directo a Room desde Composables**. Toda lectura/escritura pasa por un repositorio.
- **Sin lógica de negocio en Composables**. Agregados y reglas viven en `domain/rules/`.
- **La UI nunca conoce la capa `ai/`**. El ViewModel traduce `ChatMessage` ↔ `SupportChatTurn`.
- **Entities separadas de modelos de dominio**: sin anotaciones Room en `UiState`.

---

## Estructura de carpetas

```
app/src/main/java/cl/ipvg/docentecalma/
├── DocenteCalmaApplication.kt           @HiltAndroidApp
├── MainActivity.kt                      Activity única (edge-to-edge)
│
├── ui/
│   ├── DocenteCalmaApp.kt               Composable raíz (theme + NavHost)
│   ├── theme/                           Theme, Color, Type
│   ├── components/
│   │   ├── DocenteCalmaScaffold.kt      Scaffold con TopAppBar + back
│   │   └── PlaceholderContent.kt
│   └── screens/
│       ├── home/                        HomeScreen + VM + UiState + NavActions
│       ├── emotionalcheckin/
│       ├── recommendations/
│       ├── classroomguidance/
│       ├── quickexercises/
│       ├── supportchat/
│       ├── history/
│       ├── progress/
│       └── privacy/
│
├── navigation/
│   ├── Routes.kt                        sealed class con pattern + arguments
│   ├── NavActions.kt                    @Stable, encapsula NavController
│   └── DocenteCalmaNavHost.kt
│
├── data/
│   ├── di/DatabaseModule.kt             Hilt provisions
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── dao/                         EmotionalCheckInDao, RecommendationHistoryDao,
│   │   │                                ChatMessageDao
│   │   └── entity/                      *Entity.kt
│   └── repository/                      EmotionalRepository, ChatRepository,
│                                        ClassroomGuidanceRepository,
│                                        RecommendationHistoryRepository
│
├── domain/
│   ├── model/                           Emotion, EmotionCategory, EmotionalCheckIn,
│   │                                    ChatMessage, ChatRole, Recommendation,
│   │                                    RecommendationHistory, RecommendationType,
│   │                                    ClassroomScenario, QuickExercise,
│   │                                    SeverityFlag, ChatSessionSummary
│   ├── mapper/                          *Mapper.kt + EmotionLabels
│   └── rules/                           RecommendationEngine, ProgressCalculator,
│                                        EmotionRuleCatalog, QuickExerciseCatalog,
│                                        ClassroomScenarioCatalog
│
├── ai/
│   ├── AiConfig.kt                      model name, tokens, apiKey (BuildConfig)
│   ├── AiModule.kt                      Hilt bindings
│   ├── AiResult.kt                      sealed Success / Error (kind, fromFallback)
│   ├── SupportChatAi.kt                 interfaz única
│   ├── SupportChatTurn.kt               tipo propio, sin dependencia de domain
│   ├── GeminiSupportChatAi.kt           implementación Google Generative AI
│   ├── FallbackSupportChatAi.kt         responder local por reglas
│   ├── ResilientSupportChatAi.kt        decora Gemini con fallback automático
│   ├── RiskCategory.kt
│   └── RiskClassifier.kt                pre-AI safety (autolesión, terceros, abuso…)
│
└── util/
    └── DateTimeFormatters.kt
```

Los tests viven en `app/src/test/java/…` reflejando la misma estructura, con un paquete extra `testing/` para fakes (`FakeEmotionalCheckInDao`, `FakeChatMessageDao`, `FakeSupportChatAi`).

---

## Módulos principales

### 1. Emotional Check-In (`ui/screens/emotionalcheckin/`)

Registro rápido de un estado emocional.

- **Emociones**: `STRESS`, `ANXIETY`, `ANGUST`, `ANGER`, `SADNESS`, `FRUSTRATION`, `FATIGUE`, `CALM`, `HAPPY`.
- **Categorías** (`EmotionCategory`):
  - `DIFFICULT_HIGH_ACTIVATION` (stress, anxiety, angust, anger)
  - `DIFFICULT_LOW_ENERGY` (sadness, frustration, fatigue)
  - `REGULATED_POSITIVE` (calm, happy)
- **Intensidad**: escala 1–5.
- **Nota opcional** con límite de caracteres (ver `EmotionalCheckInUiState.NOTE_MAX_LENGTH`).
- Persiste en `EmotionalCheckInEntity` y emite un efecto `Saved(checkInId)` que permite navegar a `Recommendations`.

### 2. Recommendation Engine (`domain/rules/RecommendationEngine.kt`)

Motor **local y determinístico** (sin IA). Recibe emoción + intensidad + categoría y produce una `Recommendation`:

- `title`
- `shortMessage`
- `immediateAction`
- `breathingSuggestion`
- `whatToAvoid`
- `optionalPedagogicalTip`

Cada recomendación mostrada se registra en `RecommendationHistoryEntity` vía `RecommendationsViewModel`.

### 3. Classroom Guidance (`ui/screens/classroomguidance/`)

Lista curada de escenarios de aula frecuentes (indisciplina, conflicto entre estudiantes, padres tensos, etc.) con orientación práctica. Catálogo estático en `ClassroomScenarioCatalog`.

### 4. Quick Exercises (`ui/screens/quickexercises/`)

Ejercicios breves con pasos numerados, leídos desde `QuickExerciseCatalog`:

- Respiración 4-7-8
- Grounding 5-4-3-2-1
- Pausa activa
- Reencuadre cognitivo
- Micro descanso

### 5. Support Chat (`ui/screens/supportchat/` + `ai/`)

Chat de apoyo socioemocional **no clínico**. Flujo:

1. `RiskClassifier` intercepta el mensaje del usuario antes de llegar al modelo.
2. Si se detecta autolesión, daño a terceros, abuso o crisis aguda → se responde con un mensaje de contención seguro y la línea de emergencias **(llamada 4141 o 600 360 7777 Salud Responde)**, sin pasar por Gemini.
3. En el resto de casos, `ResilientSupportChatAi` llama a Gemini; si falla (red, sin API key, vacío, safety-blocked), degrada a `FallbackSupportChatAi` (reglas locales) y marca `fromFallback = true` para que la UI lo indique.
4. Cada turno (USER/MODEL) se persiste en `ChatMessageEntity` con `sessionId`.

### 6. History (`ui/screens/history/`)

Combina tres fuentes reactivas:

- `EmotionalRepository.observeAll()` → lista de chequeos.
- `RecommendationHistoryRepository.observeAll()` → recomendaciones vistas.
- `ChatRepository.observeSessionSummaries()` → resumen por sesión de chat.

### 7. Progress (`ui/screens/progress/` + `ProgressCalculator`)

Métricas agregadas **calculadas en la capa domain**, no en Composables:

- Emoción más frecuente
- Intensidad promedio
- Total de registros
- Proporción difíciles vs. positivas/reguladas
- Resumen semanal (últimos 7 días)

---

## Configuración de la Gemini API

La app **no funciona con una API key hardcodeada**. La clave se inyecta vía `BuildConfig` desde `local.properties`.

### Paso a paso

1. Obtén una API key en [Google AI Studio](https://aistudio.google.com/app/apikey).
2. En la raíz del proyecto, copia `local.properties.example` como `local.properties`:

    ```properties
    GEMINI_API_KEY=tu_api_key_aqui
    ```

3. `local.properties` está en `.gitignore`: **nunca lo commitees**.
4. `app/build.gradle.kts` lee la propiedad y expone `BuildConfig.GEMINI_API_KEY`:

    ```kotlin
    val geminiApiKey: String = localProps.getProperty("GEMINI_API_KEY", "")
    buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
    ```

5. `AiConfig` la consume y `AiModule` decide qué implementación inyectar:
    - Con key válida → `GeminiSupportChatAi` envuelto en `ResilientSupportChatAi`.
    - Sin key → directamente `FallbackSupportChatAi`.

### Modelo y parámetros

Definidos en `ai/AiConfig.kt`. Modelo por defecto `gemini-1.5-flash-latest`. El system prompt está en `GeminiSupportChatAi` y refuerza:

- Tono empático, breve, no clínico.
- Prohibición de diagnósticos.
- Derivación explícita a profesional en casos graves.
- Respuestas en español neutro de Chile.

---

## Cómo ejecutar

### Requisitos

- **Android Studio Ladybug (2024.2.1)** o superior.
- **JDK 17**.
- SDK Android 35 instalado.
- Dispositivo físico o emulador con **API 26+**.

### Pasos

```bash
git clone <repo-url>
cd "IPVG- S. Castro"
cp local.properties.example local.properties   # y edita GEMINI_API_KEY si tienes
```

En Android Studio:

1. **File → Open** y selecciona la carpeta raíz.
2. Esperar a que Gradle sincronice (descarga AGP 8.6.1, Kotlin 2.0.21, KSP, Compose BOM).
3. **Run ▶** con configuración `app`.

Desde terminal:

```bash
./gradlew assembleDebug        # build debug
./gradlew installDebug          # instalar en dispositivo conectado
./gradlew test                  # tests unitarios JVM
./gradlew lint                  # análisis estático
```

---

## Testing

Tests unitarios en `app/src/test/java/…`:

| Categoría | Archivos |
|---|---|
| Mappers | `EmotionLabelsTest`, `EmotionalCheckInMapperTest`, `RecommendationHistoryMapperTest`, `ChatMessageMapperTest` |
| Reglas / dominio | `RecommendationEngineTest`, `ProgressCalculatorTest` |
| Seguridad IA | `RiskClassifierTest` |
| Repositorios | `EmotionalRepositoryTest`, `ChatRepositoryTest` (con fakes DAO) |
| ViewModels | `EmotionalCheckInViewModelTest`, `SupportChatViewModelTest` |

Herramientas clave:

- `MainDispatcherRule` (en `app/src/test/…/MainDispatcherRule.kt`) — reemplaza `Dispatchers.Main` por un `UnconfinedTestDispatcher`.
- `FakeEmotionalCheckInDao`, `FakeChatMessageDao` — implementaciones en memoria con `MutableStateFlow`.
- `FakeSupportChatAi` — programable vía `nextResult`.

Ejecutar: `./gradlew test` o desde Android Studio clic derecho sobre la carpeta `test`.

---

## Privacidad

Diseño pensado para **uso personal**:

- **Sin autenticación, sin cuentas, sin sincronización en la nube.**
- **Sin backend propio.** No existe servidor al que se envíen datos personales.
- **Almacenamiento 100% local** en la base Room del dispositivo (`AppDatabase`).
- **Sin analytics ni crash reporting externos** en el MVP.
- **Gemini API**: los mensajes del chat se envían a Google Generative AI **solo cuando el usuario escribe en el chat**. No se envían los chequeos emocionales ni el historial de recomendaciones. Revisar los [términos de Google AI](https://ai.google.dev/gemini-api/terms) para el tratamiento de datos.
- **Mensaje de privacidad visible** al usuario en `PrivacyScreen` desde Home.
- **Sin permisos sensibles** declarados en `AndroidManifest.xml`. Solo `INTERNET` para el chat IA.
- **Safety layer**: `RiskClassifier` se ejecuta **antes** de enviar el texto a Gemini. En crisis, el mensaje no sale del dispositivo.

Si más adelante se agrega exportación o sync, debe tratarse como cambio de alcance con aviso explícito al usuario.

---

## Limitaciones del MVP

- **Sin backup / export.** Si el usuario desinstala, pierde su historial. No hay export a CSV/JSON.
- **Una sola sesión de chat activa a la vez** (`sessionId` generado en cada apertura). El historial por sesión se persiste pero no se navega entre sesiones archivadas.
- **Sin notificaciones ni recordatorios.**
- **Sin gráficos complejos** en Progress — solo barras de proporción con `LinearProgressIndicator` y listas.
- **Idioma único**: español de Chile hardcodeado en los labels.
- **Sin tests de UI ni de integración instrumentada**. Solo tests JVM.
- **Gemini model fijo** (`gemini-1.5-flash-latest`). No hay selector de modelo.
- **No hay rotación/expiración automática** de datos antiguos en la base Room.
- **Sin modo accesibilidad avanzado** (TalkBack funciona pero no hay tamaños dinámicos extensos ni alto contraste explícito).

---

## Mejoras futuras

Sugerencias priorizadas por valor / esfuerzo, listas para retomar:

### Alto valor

1. **Export / import cifrado** del historial (JSON con passphrase) para que el docente no pierda datos al cambiar de dispositivo.
2. **Recordatorios diarios opcionales** con `WorkManager` para sugerir un chequeo al comenzar/terminar la jornada.
3. **Navegación entre sesiones de chat archivadas**; ya existen `ChatSessionSummary` y la query en `ChatMessageDao`.
4. **Gráfico semanal/mensual de emociones** (considerar `vico-charts` o dibujo con Canvas Compose, manteniendo simplicidad).
5. **Auto-purga configurable** (ej. "borrar chequeos > 12 meses") con una política en `domain/rules/`.

### Calidad

6. **Tests instrumentados** (Compose UI Test) para `HomeScreen`, `EmotionalCheckInScreen` y `SupportChatScreen`.
7. **Migración a KMP** del módulo `domain/` si se plantea una versión iOS.
8. **`proguard-rules.pro` endurecido** y `isMinifyEnabled = true` en release.
9. **Crash reporting opt-in** (ej. Sentry con consentimiento explícito).
10. **Migraciones Room** explícitas; hoy se confía en `fallbackToDestructiveMigration` durante desarrollo — revisar antes de producción.

### Experiencia

11. **Modo oscuro dinámico** (ya hay color scheme, validar contraste WCAG AA en todos los estados).
12. **Strings extraídos a `strings.xml`** (hoy muchos están inline para agilidad del MVP) para facilitar i18n.
13. **Selector de modelo Gemini** o soporte multi-proveedor (OpenAI, Claude) vía el mismo `SupportChatAi`.
14. **Audio en ejercicios de respiración** (guía hablada para 4-7-8, grounding).
15. **Widget Home Screen** con botón rápido "Chequeo emocional".

### Seguridad IA

16. **Ampliar `RiskClassifier`** con heurísticas adicionales (detección de patrones temporales, intensidad creciente).
17. **Logging local opt-in** de respuestas marcadas como `fromFallback` para auditoría de calidad.
18. **Revisión periódica del system prompt** según feedback de usuarios y guías de salud mental.

---

## Licencia y contacto

Proyecto académico/institucional. Consultar con el responsable del Instituto Profesional Virginio Gómez (IP Virginio Gómez) antes de redistribuir.
#   a p p v g  
 