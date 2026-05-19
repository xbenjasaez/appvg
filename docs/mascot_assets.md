# Mascota Docente Calma — Brief de producción

Documento de referencia para producir las variantes visuales del personaje
(zorrito mascota oficial del IP Virginio Gómez) que se integran en la app
Docente Calma. Es la única fuente de verdad sobre qué archivos hay que
crear, cómo deben verse y dónde se usan.

El sistema de mascota ya está implementado en código. Las 14 poses
estáticas del brief están entregadas y enlazadas; sólo los estados de
**ejercicios breves** (`Breathing`, `Grounding`, etc.) siguen usando
`mascot_base.png` hasta que existan ilustraciones específicas por
ejercicio.

> **Estrategia de animación**: por simplicidad de producción, no se
> harán animaciones Lottie. En su lugar, el componente `Mascot` aplica
> automáticamente una animación sutil por código (escala, rotación,
> rebote vertical) sobre cada ilustración estática. Cada estado tiene
> su propio movimiento — ver
> `[MascotMotionFactory](../app/src/main/java/cl/ipvg/docentecalma/ui/mascot/MascotMotion.kt)`.
> Las hojas Lottie de la sección 4 quedan como referencia opcional por
> si en el futuro se quiere subir el nivel.

---

## 1. Identidad visual canónica

### Personaje

- Especie: zorrito antropomorfo, mascota oficial del IPVG.
- Edad visual: amigable, no infantilizado.
- Vestuario fijo:
  - Polera amarilla de manga corta con el escudo IPVG sobre el pecho.
  - Short azul.
  - Sin calzado visible (mantener consistencia con la imagen base).
- Rasgos clave que NO deben variar entre poses:
  - Cuerpo anaranjado, pecho y hocico blancos, puntas de orejas y cola
    en café/marrón oscuro.
  - Nariz negra redonda; ojos grandes y expresivos.
  - Cola tupida visible.
  - Escudo IPVG en la polera, legible.

### Paleta del personaje

Mantener consistencia con los tokens definidos en
`[app/src/main/java/cl/ipvg/docentecalma/ui/theme/Color.kt](../app/src/main/java/cl/ipvg/docentecalma/ui/theme/Color.kt)`:

| Elemento                | Color           | Token sugerido    |
|-------------------------|-----------------|-------------------|
| Cuerpo (naranja)        | tonos de zorro  | n/a (propio)      |
| Polera                  | amarillo        | `IpvgYellow`      |
| Short                   | azul marino     | `IpvgBluePrimary` |
| Detalle escudo (verde)  | verde IPVG      | `IpvgGreen`       |
| Pecho/hocico            | blanco          | `#FFFFFF`         |
| Puntas orejas/cola      | café oscuro     | n/a               |

### Voz visual

- Cercana, calma, profesional. Nunca paternalista ni "cuchi" exagerado.
- Sin gestos extremos: nada de ojos en forma de corazón, nada de
  expresiones de manga muy estilizadas, nada de poses heroicas.
- Las emociones se expresan con sutilezas: posición de orejas, cejas,
  inclinación de cabeza, postura corporal.

---

## 2. Especificaciones técnicas universales

Aplicar a TODOS los archivos salvo que la hoja del estado indique algo
distinto.

### Estáticos (PNG)

- **Formato**: PNG con canal alfa (fondo transparente).
- **Tamaño**: mínimo 1024×1024 px. Ideal: vector original (`.svg` / `.ai`)
  exportado a PNG en al menos esa resolución.
- **Lienzo**: cuadrado.
- **Área segura**: el personaje ocupa máximo el 80 % del lienzo, dejando
  al menos 10 % de margen transparente en cada borde. Esto evita
  recortes cuando el componente `Mascot(sizeDp = 28.dp)` se renderiza en
  contenedores chicos.
- **Sin fondos decorativos**: nada de fondo turquesa, círculos, rayos o
  marcos. El fondo siempre debe ser transparente.
- **Sin texto integrado**: ninguna palabra ni número dibujado en la
  imagen.
- **Sin sombras proyectadas** sobre el "suelo" (asume fondos variables).
- **Limpieza**: la imagen base actual contiene un icono de Google Lens
  en la esquina superior izquierda. Eliminar cualquier overlay de UI,
  marca de agua, o elemento ajeno al personaje.
- **Carpeta destino**: `app/src/main/res/drawable-nodpi/`.

### Animaciones — estrategia actual (sin Lottie)

Por defecto NO se producen Lottie. El componente `Mascot` aplica una
animación sutil por código a cada PNG estático, con parámetros definidos
por estado en
`[MascotMotionFactory](../app/src/main/java/cl/ipvg/docentecalma/ui/mascot/MascotMotion.kt)`:

- Presencia (`Idle`, `Listening`): respiración suave (escala ±2 %, ~3.6 s).
- Saludo (`Greeting`): leve oscilación lateral (rotación ±3°).
- Pensando (`Thinking`): rebote corto (translateY ±4 %).
- Sin conexión / Error: oscilación lenta o vibración corta.
- Ejercicios: cadencia coherente (Breathing escala lenta, Grounding gira,
  Stretching rebota, Reframing pulsa, Resting respira).
- Espejo emocional: cada emoción tiene su propio movimiento
  (Anxious vibra rápido, Sad casi quieto, Happy bota, Tired apenas oscila).

Para ajustar la animación de un estado, editar
`MascotMotionFactory.forState(...)`. El componente respeta el preview
(no anima en `@Preview`) y permite forzar `animate = false` desde el
call site para tests/snapshots.

### Animaciones Lottie (opcional, futura)

Si en el futuro se decide subir el nivel a Lottie, los archivos van
en `app/src/main/res/raw/` con formato JSON Lottie v5.x, peso <100 KB,
lienzo cuadrado, 30 fps, loop infinito sin salto, sin assets externos.
Al colocar el archivo y registrarlo en `MascotResources.lottieFor(state)`,
el Lottie reemplaza automáticamente al estático + motion. Ver el guion
de cada animación en la sección 4.

### Naming

- Minúsculas, separadas por guion bajo, prefijo `mascot_`.
- Mismo nombre exacto que aparece en cada hoja de este documento.
- Android `R.drawable.*` no acepta mayúsculas ni guiones medios.

### Tamaño de uso en pantalla

Cada hoja indica el `sizeDp` con que el componente `Mascot` renderiza el
estado. La ilustración debe leerse bien en ese tamaño. Si el `sizeDp` es
chico (24–28 dp), simplificar detalles (escudo plano, sin sombreados
finos) y exagerar levemente los rasgos faciales.

---

## 3. Hojas por estado — estáticos

A continuación una hoja por archivo PNG. Cada hoja indica:

- **Archivo**: nombre exacto, en minúsculas.
- **Estado en código**: `MascotState.<X>` (definido en `MascotState.kt`).
- **Pantalla(s)**: dónde aparece.
- **Tamaño usado**: `sizeDp` con que se renderiza.
- **Pose**: descripción corporal.
- **Expresión**: cara y mirada.
- **Frase asociada**: texto de copy que acompaña al personaje en pantalla
  (definido en `MascotPersona.phraseFor`). El ilustrador no la dibuja,
  pero le ayuda a entender la intención.
- **Transmite**: qué debe sentir el usuario.
- **Evitar**: errores frecuentes a no cometer.

---

### `mascot_greeting.png` — entregado

- **Estado**: `MascotState.Greeting`.
- **Pantalla**: `SplashScreen` (primera pantalla al abrir la app).
- **Tamaño usado**: 180 dp.
- **Pose**: de frente, una pata levantada saludando a la altura de la
  cabeza, la otra al costado del cuerpo. Ambos pies bien plantados.
- **Expresión**: sonrisa amable, ojos abiertos y enfocados al frente.
- **Frase asociada**: "Hola. Soy Virgi. Tomemos esto con calma."
- **Transmite**: bienvenida tranquila, no efusiva.
- **Evitar**: salto en el aire, brazos abiertos en V, gritos de
  emoción, líneas de movimiento exageradas.
- **Estado del archivo**: disponible en
  `app/src/main/res/drawable-nodpi/mascot_greeting.png` y enlazado en
  `MascotResources.drawableFor`. Animación por código: oscilación lateral
  leve (rotación ±3°).

---

### `mascot_idle.png` — entregado

- **Estado**: `MascotState.Idle`.
- **Pantallas**: `BrandingHeader` (cabecera del Home), avatar de cada
  burbuja del asistente en `SupportChatScreen`, `EmptyLatestCard` del
  Home. También se usa como fallback cuando no hay emoción seleccionada
  en el espejo emocional.
- **Tamaño usado**: 28 dp / 64 dp / 72 dp (debe leerse en chico).
- **Pose**: de pie, postura neutra, mirando levemente hacia el
  espectador.
- **Expresión**: sonrisa pequeña y serena. Sin gestos.
- **Frase asociada**: ninguna (decorativo).
- **Transmite**: presencia tranquila, "estoy acá".
- **Evitar**: pose dinámica, gestos llamativos. Esta es la pose canónica
  del personaje y la que más veces se ve.
- **Estado del archivo**: disponible en
  `app/src/main/res/drawable-nodpi/mascot_idle.png` y enlazado en
  `MascotResources.drawableFor`. Animación por código: respiración suave
  (escala ±2 %).

---

### `mascot_cheering.png` — entregado

- **Estado**: `MascotState.Cheering`.
- **Pantallas**: reservado para mostrar progreso o racha positiva
  (todavía no enganchado a una pantalla concreta).
- **Tamaño usado**: ~80 dp.
- **Pose**: pulgar arriba con la pata sobre el pecho, postura abierta y
  estable en el suelo.
- **Expresión**: sonrisa amplia pero no efusiva.
- **Frase asociada**: "Bien. Un paso a la vez es suficiente."
- **Transmite**: reconocimiento de avance pequeño y sostenido.
- **Evitar**: gritos, fuegos artificiales, expresión de "victoria
  final", coaching motivacional visual.
- **Estado del archivo**: disponible en
  `app/src/main/res/drawable-nodpi/mascot_cheering.png` y enlazado en
  `MascotResources.drawableFor`. Animación por código: rebote moderado
  con leve rotación.

---

### `mascot_listening.png` — entregado

- **Estado**: `MascotState.Listening`.
- **Pantalla**: `EmptyState` del `SupportChatScreen` cuando no hay
  mensajes aún.
- **Tamaño usado**: 120 dp.
- **Pose**: ligeramente inclinado hacia adelante, una oreja un poco más
  girada hacia el frente, brazos relajados al costado.
- **Expresión**: sonrisa pequeña, ojos atentos pero suaves.
- **Frase asociada**: "Cuando quieras, te leo."
- **Transmite**: disponibilidad sin presión, escucha activa.
- **Evitar**: postura ansiosa, mano en el oído estilo cómic, ceja
  levantada.
- **Estado del archivo**: disponible en
  `app/src/main/res/drawable-nodpi/mascot_listening.png` y enlazado en
  `MascotResources.drawableFor`. Animación por código: respiración suave.

---

### `mascot_thinking.png` — entregado

- **Estado**: `MascotState.Thinking`.
- **Pantalla**: indicador "Pensando contigo…" del chat mientras la IA
  genera la respuesta.
- **Tamaño usado**: 24 dp (¡muy chico! debe leerse a esa escala).
- **Pose**: pata en la barbilla, cabeza levemente inclinada, mirada
  hacia arriba.
- **Expresión**: reflexiva, no preocupada. Cejas neutras.
- **Frase asociada**: "Pensando contigo…"
- **Transmite**: trabajo silencioso, presencia que aún no responde.
- **Evitar**: signos de interrogación flotando, bombillo encendido,
  expresión de duda angustiada. Mantener detalles mínimos para que
  legible a 24 dp.
- **Estado del archivo**: disponible en
  `app/src/main/res/drawable-nodpi/mascot_thinking.png` y enlazado en
  `MascotResources.drawableFor`. Animación por código: rebote vertical
  corto (efecto "typing").

---

### `mascot_empathic.png` — entregado

- **Estado**: `MascotState.Empathic`.
- **Pantalla**: reservado para mensajes con validación emocional alta
  (todavía no enganchado a un componente fijo; previsto para futuras
  iteraciones del chat).
- **Tamaño usado**: variable.
- **Pose**: pata abierta sobre el pecho, postura serena.
- **Expresión**: ojos suaves, cejas levemente bajadas, sin sonrisa
  amplia (calidez, no alegría).
- **Frase asociada**: "Tiene sentido lo que sientes."
- **Transmite**: escucha respetuosa, sin minimizar ni dramatizar.
- **Evitar**: cara llorosa, gestos de pena ostensible, "ay pobrecito".
- **Estado del archivo**: disponible en
  `app/src/main/res/drawable-nodpi/mascot_empathic.png` y enlazado en
  `MascotResources.drawableFor`. Animación por código: pulso muy lento
  (escala ±1.5 %).

---

### `mascot_offline_sad.png` — entregado

- **Estado**: `MascotState.OfflineSad`.
- **Pantalla**: `FallbackNotice` del chat (cuando se cae la IA y se usa
  el responder local).
- **Tamaño usado**: 28 dp.
- **Pose**: ligeramente encogido, una pata atrás de la nuca, postura
  apenas más cerrada que la `Idle`.
- **Expresión**: media sonrisa de "ups", una ceja levantada. NO triste.
- **Frase asociada**: "Sin conexión. Sigo contigo en modo local."
- **Transmite**: situación temporal manejada con humor sutil.
- **Evitar**: lágrimas, expresión de derrota, signos de wifi cortados
  flotando alrededor.
- **Estado del archivo**: disponible en
  `app/src/main/res/drawable-nodpi/mascot_offline_sad.png` y enlazado en
  `MascotResources.drawableFor`. Animación por código: oscilación lenta
  y resignada (rotación ±1.5°).

---

### `mascot_error.png` — entregado

- **Estado**: `MascotState.ErrorState`.
- **Pantalla**: `ErrorBar` del chat (cuando ocurre un error).
- **Tamaño usado**: 28 dp.
- **Pose**: ambas patas levantadas en gesto de "esperen", postura
  estable.
- **Expresión**: cejas un poco subidas, boca pequeña abierta.
- **Frase asociada**: "Algo no funcionó. Podemos volver a intentarlo."
- **Transmite**: contratiempo manejable, no alarma.
- **Evitar**: cara de pánico, signos de exclamación rojos, gotas de
  sudor estilo manga.
- **Estado del archivo**: disponible en
  `app/src/main/res/drawable-nodpi/mascot_error.png` y enlazado en
  `MascotResources.drawableFor`. Animación por código: vibración corta
  de "atención" (rotación ±2°, ~0.35 s).

---

### `mascot_emotion_calm.png` — entregado

- **Estado**: `MascotState.EmotionCalm`.
- **Pantalla**: `EmotionalMirror` del `EmotionalCheckInScreen` cuando la
  persona selecciona la emoción `Calma`.
- **Tamaño usado**: 96 dp.
- **Pose**: postura abierta, una pata sobre el pecho.
- **Expresión**: párpados a media asta, sonrisa serena.
- **Frase asociada**: "Una calma así también vale registrarla."
- **Transmite**: presencia plena, suavidad.
- **Evitar**: pose de meditación oriental cliché (loto, manos en mudra),
  aura de luz alrededor.
- **Estado del archivo**: disponible en
  `app/src/main/res/drawable-nodpi/mascot_emotion_calm.png` y enlazado
  en `MascotResources.drawableFor`. Animación por código: pulso muy
  lento (escala ±2.5 %, ~2.6 s).

---

### `mascot_emotion_anxious.png` — entregado

- **Estado**: `MascotState.EmotionAnxious`.
- **Pantalla**: `EmotionalMirror` para `Estrés` y `Ansiedad`.
- **Tamaño usado**: 96 dp.
- **Pose**: ambas patas cerca del pecho, postura algo encogida.
- **Expresión**: ojos un poco más abiertos de lo normal, mirada alerta,
  cejas levemente subidas, boca apretada.
- **Frase asociada**: "Si la activación es alta, podemos respirar."
- **Transmite**: tensión reconocible, no caótica.
- **Evitar**: cara de pánico, ojos vibrantes con líneas de
  nerviosismo, signos de exclamación.
- **Estado del archivo**: disponible en
  `app/src/main/res/drawable-nodpi/mascot_emotion_anxious.png` y
  enlazado en `MascotResources.drawableFor`. Animación por código:
  vibración rápida y leve (rotación ±0.8°, ~0.18 s).

---

### `mascot_emotion_frustrated.png` — entregado

- **Estado**: `MascotState.EmotionFrustrated`.
- **Pantalla**: `EmotionalMirror` para `Enojo` y `Frustración`.
- **Tamaño usado**: 96 dp.
- **Pose**: brazos cruzados sobre el pecho, piernas separadas y
  firmes en el suelo.
- **Expresión**: cejas bajas y fruncidas, mirada directa, comisuras
  tensas con dientes apretados al costado (queja contenida, no
  agresividad).
- **Frase asociada**: "La frustración avisa. La escuchamos sin reaccionar."
- **Transmite**: enojo contenido, autocontrol activo.
- **Evitar**: humo saliendo por las orejas, cara roja, puño cerrado
  amenazante hacia adelante.
- **Estado del archivo**: disponible en
  `app/src/main/res/drawable-nodpi/mascot_emotion_frustrated.png` y
  enlazado en `MascotResources.drawableFor`. Animación por código:
  tensión leve (rotación ±1.2°, ~0.32 s).

---

### `mascot_emotion_sad.png` — entregado

- **Estado**: `MascotState.EmotionSad`.
- **Pantalla**: `EmotionalMirror` para `Tristeza` y `Angustia`.
- **Tamaño usado**: 96 dp.
- **Pose**: de pie, brazos cruzados sobre el pecho, piernas levemente
  separadas.
- **Expresión**: párpados pesados, cejas hacia arriba en el centro
  (preocupación suave), boca pequeña hacia abajo.
- **Frase asociada**: "Estar así también es parte. No tienes que arreglarlo ya."
- **Transmite**: tristeza acompañable, no desesperación.
- **Evitar**: lágrimas grandes brotando, cara muy triste estilo
  caricatura, charco de lágrimas.
- **Estado del archivo**: disponible en
  `app/src/main/res/drawable-nodpi/mascot_emotion_sad.png` y enlazado
  en `MascotResources.drawableFor`. Animación por código: traslación
  mínima vertical (~±1.5 % del lado, ciclo lento).

---

### `mascot_emotion_happy.png` — entregado

- **Estado**: `MascotState.EmotionHappy`.
- **Pantalla**: `EmotionalMirror` para `Feliz`.
- **Tamaño usado**: 96 dp.
- **Pose**: brazos abiertos a los lados, postura acogedora.
- **Expresión**: sonrisa amplia con dientes visibles, ojos vivos y
  cejas arqueadas.
- **Frase asociada**: "Buen momento para anclar lo que está funcionando."
- **Transmite**: alegría tranquila, no euforia.
- **Evitar**: salto en el aire, ojos en forma de corazón o estrella,
  brillos exagerados.
- **Estado del archivo**: disponible en
  `app/src/main/res/drawable-nodpi/mascot_emotion_happy.png` y enlazado
  en `MascotResources.drawableFor`. Animación por código: rebote
  vertical moderado (~±5 % del lado, ciclo corto).

---

### `mascot_emotion_tired.png` — entregado

- **Estado**: `MascotState.EmotionTired`.
- **Pantalla**: `EmotionalMirror` para `Cansancio`.
- **Tamaño usado**: 96 dp.
- **Pose**: de pie, brazos a los costados, postura neutra.
- **Expresión**: párpados a media asta, cejas levemente bajas, sonrisa
  pequeña y cerrada.
- **Frase asociada**: "Cansancio registrado. Quizá un micro descanso ayude."
- **Transmite**: agotamiento honesto, sin dramatismo.
- **Evitar**: bostezo enorme con boca abierta gigante, gota de sudor en
  la frente, "Z" flotando dormida.
- **Estado del archivo**: disponible en
  `app/src/main/res/drawable-nodpi/mascot_emotion_tired.png` y enlazado
  en `MascotResources.drawableFor`. Animación por código: oscilación
  muy lenta (traslación vertical ~±1.2 %, ciclo largo).

---

## 4. Hojas por animación — Lottie

Cada hoja indica:

- **Archivo**: nombre exacto en minúsculas.
- **Estado en código**: `MascotState.<X>`.
- **Ejercicio (id)**: id en
  `[QuickExerciseCatalog](../app/src/main/java/cl/ipvg/docentecalma/domain/rules/QuickExerciseCatalog.kt)`
  con que se enlaza vía `MascotResources.stateForExerciseId`.
- **Duración / loop**: duración total y si rebobina.
- **Acción cuadro a cuadro**: qué se mueve y cuándo.
- **Notas**: claves para que la animación se sienta bien acompañando
  el ejercicio.

---

### `mascot_breathing.json`

- **Estado**: `MascotState.Breathing`.
- **Ejercicio (id)**: `breathing_478` (Respiración 4-7-8).
- **Duración**: 19 s, loop infinito.
- **Acción**:
  - 0 s – 4 s: tórax y abdomen se expanden lentamente. Hombros suben un
    poco. Ojos entreabiertos.
  - 4 s – 11 s: pausa. El cuerpo queda quieto, salvo un movimiento
    mínimo de la cola o las orejas para que no se vea congelado.
  - 11 s – 19 s: tórax desciende suavemente, hombros bajan, leve
    descarga al exhalar.
- **Notas**: la cadencia debe coincidir con la cuenta 4‑7‑8. La animación
  es la guía visual del usuario, no decorativa.

---

### `mascot_grounding.json`

- **Estado**: `MascotState.Grounding`.
- **Ejercicio (id)**: `grounding_54321` (5‑4‑3‑2‑1).
- **Duración**: ~10 s, loop.
- **Acción**: gira la cabeza progresivamente hacia 5 puntos distintos
  del entorno (arriba‑izquierda, derecha, abajo, izquierda, frente),
  ~2 s entre punto y punto. Cuerpo quieto; sólo se mueven cabeza y
  orejas. Mirada curiosa.
- **Notas**: no exagerar las rotaciones (máx 30 grados). Es atención,
  no susto.

---

### `mascot_stretching.json`

- **Estado**: `MascotState.Stretching`.
- **Ejercicio (id)**: `active_pause` (Pausa activa).
- **Duración**: ~5 s, loop.
- **Acción**:
  - 0 s – 1 s: estira ambos brazos hacia arriba.
  - 1 s – 2 s: rota hombros hacia atrás (rueda pequeña).
  - 2 s – 3 s: inclina el torso a la izquierda.
  - 3 s – 4 s: inclina el torso a la derecha.
  - 4 s – 5 s: regresa al centro respirando.
- **Notas**: movimientos amplios pero controlados. Sirve también para
  el header de la pantalla de ejercicios (`Header()` en
  `QuickExercisesScreen`).

---

### `mascot_reframing.json`

- **Estado**: `MascotState.Reframing`.
- **Ejercicio (id)**: `cognitive_reframe` (Reencuadre cognitivo).
- **Duración**: ~6 s, loop.
- **Acción**: una nube/burbuja de pensamiento sobre la cabeza del
  zorrito. Inicialmente la nube tiene un tono más oscuro o un símbolo
  de tachado dentro. A los 3 s la nube se aclara y muestra una versión
  más amable (por ejemplo, una marca de visto). El zorrito mantiene
  postura neutra y mirada hacia la nube.
- **Notas**: sin texto dentro de la nube. Sólo formas y colores. El
  cambio debe leerse como "antes / después" de la idea.

---

### `mascot_resting.json`

- **Estado**: `MascotState.Resting`.
- **Ejercicio (id)**: `micro_rest` (Micro descanso).
- **Duración**: ~4 s, loop.
- **Acción**: zorrito sentado o apoyado. Cierra los párpados durante
  1 s, los mantiene cerrados 2 s con una respiración suave (tórax
  apenas oscila), y los abre 1 s al final.
- **Notas**: ritmo lento. No debe parecer que se durmió: hay descanso
  consciente, no siesta.

---

### `mascot_thinking_loop.json` (opcional)

- **Estado**: `MascotState.Thinking`.
- **Pantalla**: indicador "Pensando contigo…" del chat. Reemplaza
  o complementa al `mascot_thinking.png` estático.
- **Tamaño usado**: 24 dp.
- **Duración**: ~1.5 s, loop.
- **Acción**: tres puntos pequeños rebotando uno tras otro sobre la
  cabeza del zorrito (efecto "typing"). El zorrito en pose `thinking`
  estática.
- **Notas**: priorizar legibilidad a 24 dp; los puntos deben tener
  buen contraste.

---

## 5. Flujo de incorporación

Cuando un archivo nuevo esté listo:

1. Copiar al directorio correspondiente:
   - PNG estático → `app/src/main/res/drawable-nodpi/<nombre>.png`.
   - Lottie → `app/src/main/res/raw/<nombre>.json`.
2. Editar `[MascotResources.kt](../app/src/main/java/cl/ipvg/docentecalma/ui/mascot/MascotResources.kt)`:
   - Reemplazar el `R.drawable.mascot_base` correspondiente al estado
     por `R.drawable.<nombre>` en `drawableFor`.
   - Si es Lottie, reemplazar el `null` del estado en `lottieFor` por
     `R.raw.<nombre>`.
3. No tocar pantallas: el componente
   `[Mascot](../app/src/main/java/cl/ipvg/docentecalma/ui/mascot/Mascot.kt)`
   ya consume los recursos vía `MascotResources` y hace fallback a
   estático si la animación falla.
4. Recompilar y verificar visualmente en cada pantalla listada en la
   sección 3 / 4.

---

## 6. Estado actual

Disponibles en `app/src/main/res/drawable-nodpi/`:

- `mascot_base.png` — placeholder genérico para los estados sin pose
  propia todavía.
- `mascot_listening.png` — pose "a la escucha", enlazada al estado
  `MascotState.Listening`.
- `mascot_greeting.png` — pose "saludando", enlazada al estado
  `MascotState.Greeting`.
- `mascot_idle.png` — pose neutra, enlazada al estado
  `MascotState.Idle`.
- `mascot_cheering.png` — pulgar arriba, enlazada al estado
  `MascotState.Cheering`.
- `mascot_thinking.png` — pata en la barbilla, enlazada al estado
  `MascotState.Thinking`.
- `mascot_empathic.png` — pata sobre el pecho, enlazada al estado
  `MascotState.Empathic`.
- `mascot_offline_sad.png` — pata tras la nuca, enlazada al estado
  `MascotState.OfflineSad`.
- `mascot_error.png` — patas levantadas, enlazada al estado
  `MascotState.ErrorState`.
- `mascot_emotion_calm.png` — pata sobre el pecho con párpados a
  media asta, enlazada al estado `MascotState.EmotionCalm`.
- `mascot_emotion_anxious.png` — patas cerca del pecho con mirada
  alerta, enlazada al estado `MascotState.EmotionAnxious`.
- `mascot_emotion_frustrated.png` — brazos cruzados con ceño fruncido,
  enlazada al estado `MascotState.EmotionFrustrated`.
- `mascot_emotion_sad.png` — brazos cruzados con mirada melancólica,
  enlazada al estado `MascotState.EmotionSad`.
- `mascot_emotion_happy.png` — brazos abiertos y sonrisa amplia,
  enlazada al estado `MascotState.EmotionHappy`.
- `mascot_emotion_tired.png` — de pie con párpados pesados, enlazada al
  estado `MascotState.EmotionTired`.

**Set de poses estáticas**: completo (14 variantes + `mascot_base` como
respaldo para los estados de ejercicio breve que aún comparten el
placeholder).

Animaciones Lottie: descartadas como entrega obligatoria. Sustituidas
por motion por código en
`[MascotMotionFactory](../app/src/main/java/cl/ipvg/docentecalma/ui/mascot/MascotMotion.kt)`.
Quedan en la sección 4 como referencia por si se decide producirlas
más adelante.

## 7. Derechos

Mascota oficial del IP Virginio Gómez, con uso autorizado para
Docente Calma. Todas las variantes derivadas deben respetar la guía de
identidad de la sección 1 y mantener el escudo IPVG en la polera.
