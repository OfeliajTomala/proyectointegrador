# OXXXO — Sistema de Gestión de Inventario

Aplicación Android nativa para gestión de inventario de tiendas de conveniencia. Construida con **Jetpack Compose**, **Firebase** y **Supabase Storage**, con sistema de roles y acceso diferenciado por tipo de usuario.

---

## Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| UI | Jetpack Compose + Material 3 |
| Autenticación | Firebase Authentication (Email/Password + Google Sign-In) |
| Base de datos | Cloud Firestore |
| Almacenamiento | Supabase Storage |
| Arquitectura | MVVM + Repository Pattern |
| Navegación | Jetpack Navigation Compose |
| Ciclo de vida | `repeatOnLifecycle` + `StateFlow` |
| Imágenes async | Coil |
| Animaciones | Lottie |

---

## Arquitectura del proyecto

```
app/
├── data/
│   ├── model/
│   │   ├── User.kt              # uid, email, name, role, photoUrl
│   │   ├── Product.kt           # id, name, codigo, price, stock, category, imageUrl, isDeleted + auditoría
│   │   ├── Movement.kt          # productId, quantity, type, date, userId, isDeleted + auditoría
│   │   ├── ProductCategory.kt   # enum: BEBIDAS, LACTEOS, PANADERIA … (15 categorías)
│   │   ├── MovementType.kt      # enum: ENTRADA | SALIDA
│   │   └── UserRole.kt          # enum: ADMIN | ENCARGADO | CAJERO
│   ├── repository/
│   │   ├── AuthRepository.kt    # Firebase Auth + Firestore users + Supabase profile images
│   │   ├── ProductRepository.kt # Firestore products + movements (soft delete)
│   │   └── ImageRepository.kt   # Supabase Storage (product/profile images)
│   └── util/
│       └── UiState.kt           # sealed class: Idle | Loading | Success<T> | Error
│
├── viewmodel/
│   ├── AuthViewModel.kt         # Sesión, perfil, imagen. Usa Application para ContentResolver
│   ├── ProductViewModel.kt      # Productos, movimientos, búsqueda, filtros, fechas
│   ├── DashboardViewModel.kt    # Estadísticas en paralelo con coroutineScope + async
│   └── UserViewModel.kt         # Gestión de usuarios (solo ADMIN)
│
├── ui/
│   ├── auth/
│   │   ├── navigation/AuthNavGraph.kt
│   │   └── screens/             # SplashScreen, LoginScreen, RegisterScreen
│   ├── admin/
│   │   ├── AdminActivity.kt
│   │   └── navigation/AdminNavGraph.kt
│   ├── encargado/
│   │   ├── EncargadoActivity.kt
│   │   └── navigation/EncargadoNavGraph.kt
│   ├── cajero/
│   │   ├── CajeroActivity.kt
│   │   └── navigation/CajeroNavGraph.kt
│   ├── screens/                 # Pantallas compartidas entre roles
│   │   ├── DashboardScreen.kt
│   │   ├── ProductListScreen.kt
│   │   ├── AddProductScreen.kt
│   │   ├── EditProductScreen.kt
│   │   ├── MovementsScreen.kt
│   │   ├── AddMovementScreen.kt # Chips de categoría + grid de productos + panel de registro
│   │   ├── MovementDetailScreen.kt
│   │   ├── AlertsScreen.kt
│   │   ├── ProfileScreen.kt
│   │   └── …
│   ├── components/              # Componentes reutilizables
│   │   ├── AppTextField.kt
│   │   ├── PasswordTextField.kt # Con indicador de fortaleza y toggle de visibilidad
│   │   ├── AppButton.kt
│   │   ├── AppCard.kt
│   │   ├── ProductCard.kt
│   │   ├── MovementCard.kt
│   │   ├── StockAlertChip.kt
│   │   └── ImagePickerSection.kt
│   └── theme/                   # Material 3 con 5 paletas de color + modo oscuro
│
└── MainActivity.kt              # Entry point: Google Sign-In + redirección por rol
```

---

## Roles y permisos

| Funcionalidad | ADMIN | ENCARGADO | CAJERO |
|--------------|:-----:|:---------:|:------:|
| Ver productos | ✅ | ✅ | ✅ |
| Agregar producto | ✅ | ✅ | ❌ |
| Editar producto | ✅ | ✅ | ❌ |
| Eliminar producto | ✅ | ✅ | ❌ |
| Restaurar producto | ✅ | ✅ | ❌ |
| Registrar movimientos | ✅ | ✅ | ✅ |
| Eliminar movimientos | ✅ | ✅ | ✅ |
| Ver alertas de stock | ✅ | ✅ | ✅ |
| Ver dashboard completo | ✅ | ✅ | ✅ (resumen) |
| Gestionar usuarios | ✅ | ❌ | ❌ |
| Cambiar roles | ✅ | ❌ | ❌ |

> El rol se valida desde Firestore en cada Activity al iniciar, no solo desde el Intent extra.

---

## Flujo de navegación

```
MainActivity (Auth)
├── SplashScreen   → si hay sesión activa, redirige por rol
├── LoginScreen    → Email/Password o Google Sign-In
└── RegisterScreen → rol por defecto: CAJERO
         ↓ onLoginSuccess(role)
AdminActivity      → AdminNavGraph
                     Dashboard · Productos · Movimientos · Agregar Movimiento
                     Alertas · Usuarios · Perfil
EncargadoActivity  → EncargadoNavGraph
                     Dashboard · Productos · Movimientos · Agregar Movimiento
                     Alertas · Perfil
CajeroActivity     → CajeroNavGraph
                     Productos · Movimientos · Agregar Movimiento
                     Alertas · Resumen · Perfil
```

---

## Configuración

### Prerrequisitos

- Android Studio Hedgehog o superior
- JDK 17+
- Proyecto Firebase con Firestore y Authentication habilitados
- Proyecto Supabase con buckets de Storage configurados

### Firebase

1. Crea un proyecto en [Firebase Console](https://console.firebase.google.com)
2. Habilita **Authentication** → proveedores: Email/Password y Google
3. Habilita **Firestore** y crea las colecciones: `users`, `products`, `movements`
4. Descarga `google-services.json` y colócalo en `/app`
5. Configura las reglas de seguridad (ver sección más abajo)

### Supabase

1. Crea un proyecto en [Supabase](https://supabase.com)
2. Ve a **Storage** y crea dos buckets con acceso público de lectura:
   - `product-images`
   - `profile-images`
3. Copia la URL y la anon key del proyecto

### Variables de configuración

En `local.properties`:
```properties
SUPABASE_URL=https://tu-proyecto.supabase.co
SUPABASE_KEY=tu-anon-key
```

En `res/values/strings.xml`:
```xml
<string name="web_client_id">TU_WEB_CLIENT_ID_DE_FIREBASE</string>
```

---

## Reglas de seguridad en Firestore

Las validaciones de rol en el cliente son solo una capa de UX. Para protección real, configura estas reglas en **Firebase Console → Firestore → Reglas**:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null
        && get(/databases/$(database)/documents/users/$(request.auth.uid))
               .data.role == 'ADMIN';
    }

    match /products/{productId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null
        && get(/databases/$(database)/documents/users/$(request.auth.uid))
               .data.role in ['ADMIN', 'ENCARGADO'];
    }

    match /movements/{movementId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
  }
}
```

---

## Modelos de datos

### User
```kotlin
data class User(
    val uid: String,
    val email: String,
    val name: String,
    val role: UserRole,     // ADMIN | ENCARGADO | CAJERO
    val photoUrl: String
)
```

### Product
```kotlin
data class Product(
    val id: String,
    val name: String,
    val codigo: String,
    val price: Double,
    val stock: Int,
    val imageUrl: String,
    val category: ProductCategory,
    val isDeleted: Boolean,
    val createdBy: String,
    val createdById: String,
    val createdAt: Timestamp,
    val updatedBy: String,
    val updatedById: String,
    val updatedAt: Timestamp?,
    val deletedBy: String,
    val deletedById: String,
    val deletedAt: Timestamp?
)
```

### Movement
```kotlin
data class Movement(
    val id: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val type: MovementType,     // ENTRADA | SALIDA
    val date: Timestamp,
    val userId: String,
    val userName: String,
    val isDeleted: Boolean,
    val deletedBy: String,
    val deletedById: String,
    val deletedAt: Timestamp?
)
```

---

## Funcionalidades principales

### Productos
- CRUD completo con **soft delete** — los productos eliminados se pueden restaurar
- Imagen opcional subida a Supabase Storage (la imagen anterior se elimina automáticamente al reemplazar)
- Validación de nombre y código únicos en Firestore antes de guardar
- Búsqueda en tiempo real por nombre o código
- Filtro por categoría (15 categorías disponibles)
- Al crear un producto con stock > 0, se genera automáticamente un movimiento de ENTRADA inicial

### Movimientos
- Registro de entradas y salidas con recalculo automático de stock
- **Pantalla de agregar movimiento** accesible desde el FAB `+` en `MovementsScreen`:
  - Chips horizontales para filtrar por categoría (15 categorías + todos)
  - Grid de 2 columnas con productos seleccionables
  - Cada card muestra nombre, categoría y stock actual con indicador de color (🔴 sin stock / 🟠 stock bajo / 🟢 stock normal)
  - Panel inferior animado al seleccionar producto con selector ENTRADA/SALIDA, campo de cantidad y validación de stock insuficiente en salidas
- Historial filtrable por tipo (ENTRADA/SALIDA), rango de fechas y nombre de producto
- Soft delete con registro de auditoría (quién y cuándo eliminó)
- Vista de movimientos por producto específico y vista global

### Dashboard
- Carga paralela de estadísticas con `coroutineScope + async` (4 queries simultáneas)
- Se refresca automáticamente en cada `RESUME` del ciclo de vida con `repeatOnLifecycle`
- Muestra: total productos, stock total, valor del inventario, productos con stock bajo, últimos 5 productos agregados ordenados por `createdAt`, contadores de movimientos

### Alertas
- Lista de productos sin stock (0 unidades) y con stock bajo (1–5 unidades)
- Ordenados de menor a mayor stock para priorizar los más urgentes

### Perfil de usuario
- Cambio de nombre, correo y contraseña con reautenticación
- Foto de perfil con upload a Supabase y limpieza automática de la imagen anterior
- Soporte para cuentas Google con opción de agregar contraseña
- Selector de tema (5 paletas de color) y modo oscuro

---

## Categorías de productos

`BEBIDAS` · `LACTEOS` · `PANADERIA` · `CARNES` · `FRUTAS_VERDURAS` · `ABARROTES` · `SNACKS` · `LIMPIEZA` · `HIGIENE` · `CONGELADOS` · `LICORES` · `CIGARRILLOS` · `MASCOTAS` · `BAZAR` · `OTROS`

---

## Paletas de color

| Nombre | Primario | Secundario |
|--------|----------|-----------|
| Rojo & Amarillo | `#D32F2F` | `#FBC02D` |
| Azul & Verde Agua | `#1565C0` | `#00897B` |
| Morado & Naranja | `#6A1B9A` | `#E65100` |
| Verde & Ámbar | `#2E7D32` | `#FF6F00` |
| Índigo & Rosa | `#283593` | `#C2185B` |

---

## Seguridad

- Autenticación con Firebase Auth (tokens JWT gestionados automáticamente)
- Validación de rol en cada Activity al iniciar vía `isAuthReady.first()` + `syncUser()`
- Cambios de email requieren verificación en la nueva dirección antes de aplicarse
- Cambios de contraseña requieren reautenticación con la contraseña actual
- `AuthViewModel` usa `Application` en lugar de `Context` para evitar memory leaks
- Reglas de Firestore en el servidor como única capa de seguridad real (el cliente es complementario)

---

## Requisitos mínimos

- **minSdk**: 26 (Android 8.0)
- **targetSdk**: 35
- **Kotlin**: 1.9+
- **Compose BOM**: 2024.x
