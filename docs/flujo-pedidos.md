# Flujo de Pedidos — OrderManager

## Arquitectura general

```
ViewModel (OrderViewModel)
  │
  ├── OrderUiState (StateFlow)
  │     ├── orders: List<PedidoEntity>              ← Pedidos pendientes
  │     ├── pendingConfirmOrders: Map<String, PedidoEntity>  ← En countdown 3s
  │     ├── confirmTimers: Map<String, Int>          ← Segundos restantes
  │     ├── completedOrders: List<PedidoEntity>     ← Historial
  │     └── newOrderAlert: PedidoEntity?            ← Toast de nuevo pedido
  │
  ├── iniciarSimulacion()           ← Loop 10s que genera PedidoEntity mock
  ├── startSendConfirmation(id)     ← Inicia countdown 3s
  ├── cancelSend(id)                ← Cancela countdown, vuelve a orders
  └── (auto) confirmSend(id)        ← Tras 3s mueve a completedOrders
```

Cada `PedidoEntity` viaja **intacto** de una lista a otra. Nunca se modifica ni se copia parcialmente.

---

## 1. Generación de datos mock

### Origen
`OrderViewModel.kt:103` — `generarPedidoMock()`

### Frecuencia
Cada 10 segundos, vía `iniciarSimulacion()` en un `while(true)` con `delay(10000)`.

### Menú base
Productos desde los que se seleccionan 1-3 aleatoriamente:

| Producto | Cantidad por defecto |
|---|---|
| Hamburguesa Clásica | 2 |
| Papas Fritas Grandes | 1 |
| Pizza Pepperoni | 1 |
| Tacos al Pastor | 4 |
| Ensalada César | 1 |
| Refresco de Cola | 2 |
| Agua Natural | 1 |
| Flan Napolitano | 2 |
| Quesadillas | 3 |
| Burrito Supreme | 1 |

### JSON generado por pedido (campo `productos`)
```json
[
  {
    "nombre": "Hamburguesa Clásica",
    "cantidad": 2,
    "precio": 160,
    "imagen": "Hamburguesa Clásica"
  },
  {
    "nombre": "Papas Fritas Grandes",
    "cantidad": 1,
    "precio": 80,
    "imagen": "Papas Fritas Grandes"
  }
]
```

El campo `"imagen"` se usa como flag de existencia (no como ruta). La resolución real del drawable se hace por nombre.

### PedidoEntity generado
```kotlin
PedidoEntity(
    id = "ORD-0001",
    cliente = "María García",          // aleatorio de lista de 8
    direccion = "Av. Reforma 123...",  // aleatorio de lista de 6
    productos = "... JSON ...",         // string con el JSON de 1-3 items
    total = 240.0,                      // suma de precios
    tiempoEstimado = 18,                // random 10..30
    notas = "Sin cebolla, por favor",   // 50% probabilidad
    timestamp = System.currentTimeMillis()
)
```

---

## 2. Card de pedido pendiente — OrderCard

### Llamada desde PendingOrdersScreen
`PendingOrdersScreen.kt:71`:
```kotlin
OrderCard(
    order = order,
    isPendingConfirm = isPending,
    confirmTimer = confirmTimers[order.id] ?: 0,
    onMarcarEnviado = { orderViewModel.startSendConfirmation(it) },
    onCancelSend = { orderViewModel.cancelSend(it) }
)
```

### Parámetros que recibe el composable
```kotlin
@Composable
fun OrderCard(
    order: PedidoEntity,       // Entidad completa con todos los datos
    isPendingConfirm: Boolean, // true si está en countdown de 3s
    confirmTimer: Int,         // segundos restantes (3,2,1,0)
    onMarcarEnviado: (String) -> Unit,  // callback: id del pedido
    onCancelSend: (String) -> Unit      // callback: id del pedido
)
```

### Resolución de la imagen
```kotlin
val imageResId = remember(order.productos) {
    try {
        val arr = JSONArray(order.productos)
        if (arr.length() > 0) {
            val obj = arr.getJSONObject(0)
            val nombre = obj.optString("nombre", "")
            MenuImages.getImageResId(nombre)
        } else {
            R.drawable.img_placeholder
        }
    } catch (e: Exception) {
        Log.e("OrderCard", "Error parseando JSON: ${e.message}", e)
        R.drawable.img_placeholder
    }
}
```

Flujo:
1. Parsea el JSON del primer producto
2. Extrae el nombre (`optString` — tolerante a null)
3. Busca el `R.drawable.*` en `MenuImages.getImageResId(nombre)`
4. Si falla cualquier cosa → log + placeholder

### Elementos visuales (orden de arriba a abajo)
```
┌─────────────────────────────────────┐
│ 👤 María García        Hace 2 min  │  ← HeaderSection
├─────────────────────────────────────┤
│                                     │
│          ┌─────────────┐            │
│          │   IMAGEN    │  180dp     │  ← painterResource + ContentScale.Crop
│          │   (full)    │            │
│          └─────────────┘            │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │   Marcar como Enviado           │ │  ← o "Cancelar (3)" si countdown
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

### Qué datos del PedidoEntity NO se muestran pero viajan dentro
| Campo | Uso en card | ¿Viaja al historial? |
|---|---|---|
| `id` | Solo para callbacks | Sí |
| `cliente` | Mostrado | Sí |
| `direccion` | No se muestra | Sí |
| `productos` | Solo 1er item para imagen | Sí (JSON completo) |
| `total` | No se muestra | Sí |
| `tiempoEstimado` | No se muestra | Sí |
| `notas` | No se muestra | Sí |
| `timestamp` | Calcula "Hace X min" | Sí |

---

## 3. Countdown de 3 segundos con Cancelar

### Inicio: `startSendConfirmation(orderId)`
```kotlin
fun startSendConfirmation(orderId: String) {
    // 1. Mueve el PedidoEntity de orders → pendingConfirmOrders
    _uiState.update { state ->
        val order = state.orders.find { it.id == orderId } ?: return@update state
        state.copy(
            orders = state.orders.filter { it.id != orderId },
            pendingConfirmOrders = state.pendingConfirmOrders + (orderId to order),
            confirmTimers = state.confirmTimers + (orderId to 3)
        )
    }

    // 2. Lanza corrutina con countdown
    val job = viewModelScope.launch {
        for (remaining in 2 downTo 0) {
            delay(1000)
            _uiState.update { state ->
                // Si cancelaron, confirmTimers ya no tiene esta key
                if (!state.confirmTimers.containsKey(orderId)) return@update state
                state.copy(confirmTimers = state.confirmTimers + (orderId to remaining))
            }
        }
        // 3. Auto-confirmar tras 3s
        confirmSend(orderId)
    }
    sendJobs[orderId] = job  ← guarda referencia para cancelar
}
```

### Cancelación: `cancelSend(orderId)`
```kotlin
fun cancelSend(orderId: String) {
    sendJobs[orderId]?.cancel()    // Cancela la corrutina pendiente
    sendJobs.remove(orderId)
    _uiState.update { state ->
        val order = state.pendingConfirmOrders[orderId] ?: return@update state
        state.copy(
            pendingConfirmOrders = state.pendingConfirmOrders - orderId,
            confirmTimers = state.confirmTimers - orderId,
            orders = state.orders + order  ← Devuelve a pendientes
        )
    }
}
```

### Confirmación automática: `confirmSend(orderId)`
```kotlin
private fun confirmSend(orderId: String) {
    _uiState.update { state ->
        val order = state.pendingConfirmOrders[orderId] ?: return@update state
        state.copy(
            pendingConfirmOrders = state.pendingConfirmOrders - orderId,
            confirmTimers = state.confirmTimers - orderId,
            completedOrders = listOf(order) + state.completedOrders  ← Mueve a historial
        )
    }
}
```

### Mapa de estados
```
          startSendConfirmation(id)
orders ──────────────────────────────► pendingConfirmOrders
  ▲                                          │
  │        cancelSend(id)                    │ delay(3000)
  └──────────────────────────────────────────┤
                                             ▼
                                      completedOrders
```

---

## 4. Historial de pedidos — HistorialScreen

### Card colapsada (vista inicial)
```
┌─────────────────────────────────────┐
│ ORD-0001                            │
│ María García                        │
│ Total: $240.00                    ···│  ← "···" expande
└─────────────────────────────────────┘
```

### Card expandida (`AnimatedVisibility`)
Al tocar "···" se muestra:
```
┌─────────────────────────────────────┐
│ ORD-0001                            │
│ María García                        │
│ Total: $240.00                    ✕│
├─────────────────────────────────────┤
│ Dirección de entrega                │
│ Av. Reforma 123, Col. Centro        │
│                                     │
│ Detalle del pedido                  │
│ ┌─────────────────────────────────┐ │
│ │ [img] 2x Hamburguesa Clásica    │ │
│ │                          $160.00│ │
│ │─────────────────────────────────│ │
│ │ [img] 1x Papas Fritas Grandes   │ │
│ │                           $80.00│ │
│ └─────────────────────────────────┘ │
│                                     │
│ Subtotal                    $240.00 │
│ Total                      $240.00  │
└─────────────────────────────────────┘
```

### Parseo de productos en HistoryCard
```kotlin
val productos = remember(order.productos) {
    try {
        val arr = JSONArray(order.productos)
        (0 until arr.length()).mapNotNull { i ->
            try {
                val obj = arr.getJSONObject(i)
                ProductDetail(
                    nombre = obj.optString("nombre", ""),
                    cantidad = obj.optInt("cantidad", 1),
                    precio = obj.optDouble("precio", 0.0),
                    imagenResId = MenuImages.getImageResId(nombre)
                )
            } catch (e: Exception) {
                Log.e("Historial", "Error item $i: ${e.message}", e)
                null  ← skip este item, no tumbar el resto
            }
        }
    } catch (e: Exception) {
        Log.e("Historial", "Error JSON: ${e.message}", e)
        emptyList()
    }
}
```

Diferencias clave con OrderCard:
- Itera **todos** los items del array (`arr.length()`)
- Usa `mapNotNull` + try/catch por item para que un error no tumbe todo el listado
- Muestra precio unitario y calcula subtotal (`productos.sumOf { it.precio }`)

---

## 5. Mapeo de imágenes — MenuImages

### Archivo
`ui/menu/MenuImages.kt`

### Estructura
```kotlin
object MenuImages {
    val productImageMap: Map<String, Int> = mapOf(
        "Hamburguesa Clásica" to R.drawable.hamburguesa,
        "Papas Fritas Grandes" to R.drawable.papasfritas,
        "Pizza Pepperoni" to R.drawable.pizza,
        "Tacos al Pastor" to R.drawable.tacos,
        "Ensalada César" to R.drawable.ensalada,
        "Refresco de Cola" to R.drawable.refresco,
        "Agua Natural" to R.drawable.agua,
        "Flan Napolitano" to R.drawable.flan,
        "Quesadillas" to R.drawable.quesadilla,
        "Burrito Supreme" to R.drawable.burrito
    )

    fun getImageResId(productName: String): Int =
        productImageMap[productName] ?: R.drawable.img_placeholder
}
```

### Carpeta de recursos
`res/drawable/` contiene los PNG reales:

| Archivo | Tamaño |
|---|---|
| `hamburguesa.png` | 44 KB |
| `papasfritas.png` | 34 KB |
| `pizza.png` | 1.4 MB |
| `tacos.png` | 420 KB |
| `ensalada.png` | 23 KB |
| `refresco.png` | 26 KB |
| `agua.png` | 15 KB |
| `flan.png` | 26 KB |
| `quesadilla.png` | 43 KB |
| `burrito.png` | 39 KB |
| `img_placeholder.xml` | Placeholder vectorial |

### Método de carga
```kotlin
Image(
    painter = painterResource(id = imageResId),
    contentDescription = null,
    modifier = Modifier.fillMaxSize(),
    contentScale = ContentScale.Crop
)
```

Se usa `painterResource` directo (no Coil) porque son recursos locales. `ContentScale.Crop` asegura que la imagen llene el contenedor sin deformarse.

---

## 6. Manejo de errores

### Logging
Todos los catch registran el error con su mensaje:

```kotlin
catch (e: Exception) {
    Log.e("OrderCard", "Error parseando JSON: ${e.message}", e)
}
```

Filtros recomendados en Logcat:
- `"OrderCard"` — errores en card pendiente
- `"Historial"` — errores en detalle expandido

### Tolerancia a datos inconsistentes
- `optString("nombre", "")` — si falta nombre, string vacío
- `optInt("cantidad", 1)` — si falta cantidad, asume 1
- `optDouble("precio", 0.0)` — si falta precio, asume 0.0
- `mapNotNull` por item — un producto malformed no tumba los demás

### Fallback visual
- Si no hay productos en el JSON → `R.drawable.img_placeholder`
- Si el nombre no matchea en el map → `R.drawable.img_placeholder`
- Si todo el parseo falla → placeholder en la card, lista vacía en historial

---

## 7. Auto-scroll en pantalla de pendientes

```kotlin
val listState = rememberLazyListState()
val previousCount = remember { mutableIntStateOf(0) }

LaunchedEffect(orders.size) {
    if (orders.size > previousCount.intValue) {
        listState.animateScrollToItem(orders.size - 1)
    }
    previousCount.intValue = orders.size
}
```

Cuando llega un nuevo pedido (aumenta `orders.size`), el `LazyColumn` hace scroll animado automáticamente al último elemento.

### Badge flotante de conteo
```kotlin
Box(
    modifier = Modifier
        .align(Alignment.TopCenter)
        .shadow(6.dp, CircleShape)
        .background(MaterialTheme.colorScheme.primary, CircleShape)
        .padding(horizontal = 20.dp, vertical = 10.dp)
) {
    Text("${totalCount} pedido(s) en espera")
}
```

Se posiciona sobre el `LazyColumn` vía `Box` contenedor. Muestra la suma de `orders.size + pendingConfirmOrders.size`.

---

## Resumen del ciclo de vida de un PedidoEntity

```
1. NACE
   generarPedidoMock() → PedidoEntity
   
2. PENDIENTE (orders)
   └── OrderCard muestra: imagen, cliente, timestamp
       └── Usuario click "Marcar como Enviado"
       
3. COUNTDOWN (pendingConfirmOrders + confirmTimers)
   └── OrderCard muestra: imagen, cliente, timestamp, botón rojo "Cancelar (N)"
       ├── Usuario click "Cancelar" → vuelve a step 2 (orders)
       └── Pasan 3 segundos → va a step 4 (completedOrders)

4. COMPLETADO (completedOrders)
   └── HistoryCard colapsada: ID, cliente, total
       └── Usuario click "···" → expande:
           imágenes miniatura, items, precios, dirección, subtotal, total
```

El `PedidoEntity` **nunca se modifica** durante todo el ciclo. Solo se mueve entre colecciones dentro del `OrderUiState`. Todos los datos originales (productos JSON, total, dirección, tiempo estimado, notas) están disponibles en el historial.
