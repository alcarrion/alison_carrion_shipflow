# 📦 ShipFlow – Gestión de Envíos

**ShipFlow** es un microservicio REST que permite registrar, actualizar y consultar el estado de envíos de paquetes.

---

## ✅ Funcionalidades principales

- Registro de nuevos envíos
- Generación automática de `tracking_id` único (secuencial)
- Cambio de estado del envío según reglas de negocio
- Historial de eventos (cambios de estado)
- Validaciones de negocio: descripción ≤ 50 caracteres, ciudad origen ≠ destino

---

## 🚀 ¿Cómo correr el proyecto?

1. Clona el repositorio:
   ```bash
   git clone https://github.com/alcarrion/alison_carrion_shipflow.git
   cd alison_carrion_shipflow
   ```

2. Abre el proyecto en **IntelliJ IDEA** o cualquier IDE compatible con Spring Boot.
   ```

## ⚙️ Cómo correr el proyecto

1. Asegúrate de tener Docker instalado y corriendo.
2. Abre una terminal en el directorio raíz del proyecto.
3. Ejecuta:

```bash
docker-compose up
```

Esto levantará el contenedor de PostgreSQL necesario para el backend.

4. Abre otra terminal y corre el backend desde IntelliJ o usando:

```bash
./gradlew bootRun
```

---

## 🛠️ Conexión en DBeaver

Si deseas conectarte a tu base de datos PostgreSQL desde DBeaver para revisar, insertar o borrar datos directamente, sigue estos pasos:

### 🔧 Configuración recomendada:

1. Abre **DBeaver**.
2. Haz clic en **Archivo > Nueva conexión**.
3. Selecciona **PostgreSQL**.
4. Completa los siguientes parámetros:

| Parámetro         | Valor          |
|-------------------|----------------|
| **Host**          | `localhost`    |
| **Puerto**        | `6969`         |
| **Base de datos** | `shipflowdb`   |
| **Usuario**       | `admin`        |
| **Contraseña**    | `admin`        |


5. El backend estará disponible en:  
   👉 `http://localhost:8080/api/shipflow`

---

## 📂 Estructura del proyecto

```
src/
└── main/kotlin/com/pucetec/alison_carrion_shipflow/
    ├── controllers/         # Controladores REST
    ├── services/            # Lógica de negocio
    ├── mappers/             # Conversores entre entidades y DTOs
    ├── models/
    │   ├── entities/        # Entidades JPA
    │   ├── requests/        # DTOs de entrada
    │   └── responses/       # DTOs de salida
    ├── repositories/        # Interfaces JPA
    ├── exceptions/          # Excepciones personalizadas
    └── routes/              # Rutas centralizadas
```

---

## 🔗 Endpoints principales

| Método | Ruta                                               | Descripción                              |
|--------|----------------------------------------------------|------------------------------------------|
| POST   | /api/shipflow/shipments                            | Crear un nuevo envío                     |
| GET    | /api/shipflow/shipments                            | Obtener todos los envíos                 |
| GET    | /api/shipflow/shipments/{trackingId}               | Obtener un envío por tracking ID         |
| PUT    | /api/shipflow/shipments/{trackingId}/status        | Actualizar el estado del envío           |
| GET    | /api/shipflow/shipments/{trackingId}/events        | Ver historial de eventos del envío       |

### 📦 Crear envío

`POST /api/shipflow/shipments`

```json
{
  "type": "DOCUMENT",
  "weight": 0.8,
  "description": "Envío urgente",
  "city_from": "Quito",
  "city_to": "Cuenca"
}
```

---

### 🔁 Cambiar estado

`PUT /api/shipflow/shipments/{trackingId}/status`

```json
{
  "status": "IN_TRANSIT",
  "comment": "Recogido en agencia"
}
```
### 🔁Cambiar a DELIVERED

`PUT /shipments/{trackingId}/status`

```json
{
  "status": "DELIVERED"
}
```
---

### 📜 Ver historial de eventos

`GET /api/shipflow/shipments/{trackingId}/events`

---

### 📄 Listar todos los envíos

`GET /api/shipflow/shipments`

---


### ❌ Casos de Error Esperados

### 🛑 Origen y destino iguales
`POST /api/shipflow/shipments`

```json
{
  "type": "SMALL_BOX",
  "weight": 1.2,
  "description": "Error de ciudad",
  "city_from": "Cuenca",
  "city_to": "Cuenca"
}
```

---

### 🛑 Descripción muy larga
`POST /api/shipflow/shipments`

```json
{
  "type": "FRAGILE",
  "weight": 1.0,
  "description": "Este texto es demasiado largo y excede los cincuenta caracteres permitidos por el sistema.",
  "city_from": "Loja",
  "city_to": "Quito"
}
```

---

### 🛑 Estado no permitido
`PUT /api/shipflow/shipments/{trackingId}/status`

```json
{
  "status": "LOST",
  "comment": "Se perdió"
}
```


## 🧪 Pruebas y ejemplos

Puedes probar el sistema usando [Postman](https://www.postman.com/):

- Enviar un `POST` para crear un envío
- Copiar el `tracking_id` de la respuesta
- Enviar `PUT` para cambiar estado usando `/status`
- Consultar eventos con `/events`

📁 También se incluye una colección Postman exportada (`ShipFlow.postman_collection.json`) dentro del proyecto.

---

## 👨‍💻 Tecnologías usadas

- Kotlin
- Spring Boot
- Gradle
- JPA / Hibernate
- RESTful API

---

## 📌 Notas

- Se cumplen todas las reglas del flujo: `PENDING → IN_TRANSIT → DELIVERED`
- Se registran los cambios de estado en el historial
- Excepciones personalizadas para validaciones

---


 
