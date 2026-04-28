<p align="center">
  <table>
    <tr>
      <td align="center" bgcolor="#202B45" style="padding:32px 48px; border-radius:12px;">
        <img src="src/main/resources/images/logoTico.png" alt="TICO" width="200" />
      </td>
    </tr>
  </table>
</p>

<h1 align="center">TICO — Backend</h1>

<p align="center">
  Sistema de gestión de tickets de soporte para CoHispania
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Security-JWT-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white" />
  <img src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/JavaMailSender-Email-EA4335?style=for-the-badge&logo=gmail&logoColor=white" />
</p>

---

## 📖 Sobre el proyecto

**TICO** (Tickets CoHispania) es una API REST para un sistema interno de gestión de tickets de soporte IT. Desarrollada como proyecto final de bootcamp, reemplaza Zendesk para el departamento de TI de CoHispania. Existen dos roles: **EMPLOYEE** (crea y realiza seguimiento de sus tickets) y **ADMIN** (gestiona, asigna, prioriza y cierra tickets).

Una funcionalidad clave es el **email threading**: cada ticket genera automáticamente un asunto de email fijo `[TICO-{id}] {título}` mediante `@PostPersist`, lo que garantiza que todas las respuestas y notificaciones aterricen en el mismo hilo de Gmail.

El proyecto fue desarrollado siguiendo un flujo de trabajo profesional con **Spring Boot 3.5**, **Spring Security + JWT**, arquitectura en capas **(Controller → Service → Repository)**, mappers **MapStruct** y pruebas unitarias con **JUnit + Mockito**.

La API es consumida por un frontend React ([ver README Frontend](../tico_frontEnd/README.md)).

### 🎯 Objetivos del proyecto

- Construir una API REST completa con Spring Boot 3
- Implementar autenticación stateless con Spring Security + JWT
- Gestionar dos roles de usuario (ADMIN / EMPLOYEE) con control de acceso por método (`@PreAuthorize`)
- Implementar el ciclo de vida completo de un ticket (OPEN → IN_PROGRESS → CLOSED / reapertura)
- Enviar notificaciones por email con hilo de conversación mediante JavaMailSender
- Aplicar arquitectura en capas con interfaces, MapStruct y manejo global de excepciones
- Escribir pruebas unitarias con JUnit y Mockito

---

## ✨ Funcionalidades principales

- 🔐 **Autenticación JWT** — login stateless, token devuelto en cabecera `Authorization`
- 🔑 **Control de acceso por rol** — rutas protegidas por `ADMIN` / `EMPLOYEE` con `@PreAuthorize`
- ✉️ **Activación de cuenta por email** — el ADMIN crea usuarios; el empleado activa su cuenta con un código enviado por email
- 🔒 **Reseteo de contraseña** — flujo completo de recuperación por email con token temporal
- 🎫 **CRUD de tickets** — creación, consulta, asignación, cambio de estado y prioridad, cierre con mensaje y reapertura
- 💬 **Mensajería interna** — hilo de mensajes por ticket entre empleado y admin
- 🏷️ **Etiquetas** — creación, edición, activación/desactivación y asignación a tickets
- 👥 **Gestión de usuarios** — alta, edición, activación/desactivación y reasignación de tickets (ADMIN)
- 🔔 **Notificaciones** — sistema de notificaciones por evento (asignación, mensajes, cambios de estado)
- 📧 **Email threading** — asunto fijo `[TICO-{id}] {título}` para mantener el hilo en Gmail
- ⚠️ **Manejo global de excepciones** — respuestas de error consistentes en toda la API

---

## 🛠️ Tecnologías

| Tecnología        | Uso                                          |
| ----------------- | -------------------------------------------- |
| Java 21           | Lenguaje principal                           |
| Spring Boot 3.5   | Framework backend                            |
| Spring Security   | Autenticación y autorización                 |
| Spring Data JPA   | ORM con Hibernate                            |
| PostgreSQL        | Base de datos relacional (producción)        |
| H2                | Base de datos en memoria (tests)             |
| Maven             | Gestión de dependencias y build              |
| Lombok            | Reducción de boilerplate                     |
| MapStruct 1.6     | Mapeo entidad ↔ DTO                          |
| java-jwt 4.5      | Creación y validación de tokens JWT          |
| JavaMailSender    | Envío de emails (activación, notificaciones) |
| springdoc-openapi | Documentación Swagger UI                     |
| dotenv-java       | Variables de entorno desde `.env`            |
| JUnit + Mockito   | Pruebas unitarias                            |

---

## 📁 Estructura del proyecto

<details>
<summary><strong>Haz clic para expandir la estructura completa</strong></summary>

```
src/main/java/com/femcoders/tico/
│
├── config/
│   ├── CorsConfig.java                   → CORS (permite localhost:5173)
│   └── OpenApiConfig.java                → Configuración Swagger
│
├── controller/
│   ├── ActivationController.java          → /api/activation
│   ├── AuthController.java                → /api/auth
│   ├── LabelController.java               → /api/labels
│   ├── NotificationController.java        → /api/notifications
│   ├── TicketController.java              → /api/tickets
│   ├── TicketMessageController.java       → /api/tickets/{id}/messages
│   └── UserController.java                → /api/users
│
├── dto/
│   ├── request/
│   │   ├── ActivationRequest.java
│   │   ├── AdminCreateUserRequest.java
│   │   ├── AssignAdminRequest.java
│   │   ├── ChangePriorityRequest.java
│   │   ├── ChangeStatusRequest.java
│   │   ├── CloseTicketRequest.java
│   │   ├── LabelRequest.java
│   │   ├── LoginRequest.java
│   │   ├── ResendCodeRequest.java
│   │   ├── ResetPasswordConfirmRequest.java
│   │   ├── ResetPasswordRequest.java
│   │   ├── TicketCreateRequest.java
│   │   ├── TicketMessageRequest.java
│   │   └── UpdateUserRequest.java
│   └── response/
│       ├── AuthResponse.java
│       ├── LabelResponse.java
│       ├── LabelSummary.java
│       ├── LabelTicketCountsResponse.java
│       ├── NotificationResponse.java
│       ├── NotificationSummaryResponse.java
│       ├── TicketMessageResponse.java
│       ├── TicketResponse.java
│       └── UserResponse.java
│
├── entity/
│   ├── ActivationToken.java
│   ├── Label.java
│   ├── Ticket.java
│   ├── TicketMessage.java
│   └── User.java
│
├── enums/
│   ├── TicketPriority.java    → LOW, MEDIUM, HIGH, CRITICAL
│   ├── TicketStatus.java      → OPEN, IN_PROGRESS, CLOSED
│   ├── TokenType.java
│   └── UserRole.java          → ADMIN, EMPLOYEE
│
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── BadRequestException.java          → 400
│   ├── ConflictException.java            → 409
│   ├── InternalServerException.java      → 500
│   ├── InvalidIdException.java
│   ├── InvalidValueException.java
│   ├── MethodNotAllowedException.java    → 405
│   └── ExceptionResponseBuilder.java
│
├── mapper/
│   ├── LabelMapper.java
│   ├── TicketMapper.java
│   ├── TicketMessageMapper.java
│   └── UserMapper.java
│
├── repository/
│   ├── ActivationTokenRepository.java
│   ├── LabelRepository.java
│   ├── TicketMessageRepository.java
│   ├── TicketRepository.java
│   └── UserRepository.java
│
├── security/
│   ├── SecurityConfig.java               → Reglas de protección de rutas
│   └── filter/
│       ├── JWTAuthenticationFilter.java  → POST /login, emite JWT
│       └── JWTAuthorizationFilter.java   → Valida Bearer token en cada request
│
└── service/
    ├── ActivationService.java + ActivationServiceImpl.java
    ├── AuthService.java + AuthServiceImpl.java
    ├── EmailService.java
    ├── LabelService.java + LabelServiceImpl.java
    ├── NotificationService.java + NotificationServiceImpl.java
    ├── RateLimiterService.java
    ├── TicketMessageService.java + TicketMessageServiceImpl.java
    ├── TicketService.java + TicketServiceImpl.java
    ├── UserService.java + UserServiceImpl.java
    └── event/
        ├── TicketCreatedEvent.java
        └── TicketEmailEvent.java
```

> La arquitectura sigue el patrón **Controller → Service → Repository** con interfaces para desacoplar la lógica de negocio de la implementación.

</details>

---

## 🚀 Instalación y configuración

### Requisitos previos

- **Java 21** o superior
- **Maven 3**
- **PostgreSQL** corriendo en el puerto `5432`
- Cuenta de email configurada para JavaMailSender (Gmail con contraseña de aplicación recomendado)

### Pasos

1. **Clonar el repositorio**

```bash
git clone <url-del-repositorio>
cd tico_backEnd
```

2. **Crear la base de datos**

```sql
CREATE DATABASE tico;
```

3. **Configurar las variables de entorno**

```bash
cp .env.example .env
```

```env
DB_URL=jdbc:postgresql://localhost:5432/tico
DB_USERNAME=tu_usuario_postgres
DB_PASSWORD=tu_contraseña_postgres
JWT_SECRET=una_clave_aleatoria_de_al_menos_32_caracteres
MAIL_USERNAME=tu_email@gmail.com
MAIL_PASSWORD=tu_contraseña_de_aplicacion_gmail
```

4. **Iniciar la aplicación**

```bash
./mvnw spring-boot:run
```

Las tablas se crean automáticamente al iniciar mediante Hibernate (`ddl-auto=update`).
La API estará disponible en `http://localhost:8080`.
La documentación Swagger estará en `http://localhost:8080/swagger-ui.html`.

### Ejecutar los tests

```bash
./mvnw test
```

---

## 🗄️ Modelo de datos

<details>
<summary><strong>Haz clic para expandir todas las tablas</strong></summary>

### users

| Campo        | Tipo      | Descripción                        |
| ------------ | --------- | ---------------------------------- |
| id           | BIGINT PK | Auto-generado                      |
| name         | VARCHAR   | Nombre completo                    |
| email        | VARCHAR   | Email único (login)                |
| passwordHash | VARCHAR   | BCrypt                             |
| isActive     | BOOLEAN   | `false` hasta activación por email |
| createdAt    | TIMESTAMP | Auto-generado                      |
| updatedAt    | TIMESTAMP | Auto-actualizado                   |

### user_roles (tabla de unión — `@ElementCollection` sobre `User`)

| Campo   | Tipo      | Descripción      |
| ------- | --------- | ---------------- |
| user_id | BIGINT FK | → users          |
| role    | VARCHAR   | ADMIN o EMPLOYEE |

### tickets

| Campo          | Tipo      | Descripción                                          |
| -------------- | --------- | ---------------------------------------------------- |
| id             | BIGINT PK | Auto-generado                                        |
| title          | VARCHAR   | Título del ticket                                    |
| description    | TEXT      | Descripción del problema                             |
| status         | ENUM      | OPEN, IN_PROGRESS, CLOSED                            |
| priority       | ENUM      | LOW, MEDIUM, HIGH, CRITICAL                          |
| created_by     | FK        | → users (empleado creador)                           |
| assigned_to    | FK        | → users (admin asignado, nullable)                   |
| emailSubject   | VARCHAR   | `[TICO-{id}] {título}` — generado por `@PostPersist` |
| closingMessage | TEXT      | Mensaje de cierre del admin                          |
| createdAt      | TIMESTAMP | Auto-generado                                        |
| updatedAt      | TIMESTAMP | Auto-actualizado                                     |
| closedAt       | TIMESTAMP | Fecha de cierre                                      |

### ticket_labels (tabla de unión M2M)

| Campo     | Tipo      | Descripción |
| --------- | --------- | ----------- |
| ticket_id | BIGINT FK | → tickets   |
| label_id  | BIGINT FK | → labels    |

### ticket_message

| Campo        | Tipo      | Descripción                     |
| ------------ | --------- | ------------------------------- |
| id           | BIGINT PK | Auto-generado                   |
| ticket_id    | BIGINT FK | → tickets                       |
| author_id    | FK        | → users                         |
| recipient_id | BIGINT    | ID del destinatario del mensaje |
| content      | TEXT      | Contenido del mensaje           |
| isRead       | BOOLEAN   | `false` por defecto             |
| createdAt    | TIMESTAMP | Auto-generado (`@PrePersist`)   |

### labels

| Campo     | Tipo       | Descripción           |
| --------- | ---------- | --------------------- |
| id        | BIGINT PK  | Auto-generado         |
| name      | VARCHAR    | Nombre único          |
| color     | VARCHAR(7) | Color hex (`#RRGGBB`) |
| isActive  | BOOLEAN    | `true` por defecto    |
| createdAt | TIMESTAMP  | Auto-generado         |
| updatedAt | TIMESTAMP  | Actualización manual  |

</details>

---

## 🔗 Endpoints de la API

<details>
<summary><strong>Haz clic para expandir todos los endpoints</strong></summary>

### 🔓 Autenticación — `/login` y `/api/auth` (Público)

| Método | Endpoint                     | Descripción                                      |
| ------ | ---------------------------- | ------------------------------------------------ |
| POST   | `/login`                     | Login — devuelve JWT en cabecera `Authorization` |
| POST   | `/api/auth/password/request` | Solicitar reseteo de contraseña por email        |
| POST   | `/api/auth/password/confirm` | Confirmar reseteo con token recibido por email   |

### ✉️ Activación de cuenta — `/api/activation` (Público)

| Método | Endpoint                   | Descripción                                  |
| ------ | -------------------------- | -------------------------------------------- |
| POST   | `/api/activation/activate` | Activar cuenta con código recibido por email |
| POST   | `/api/activation/resend`   | Reenviar código de activación                |

### 🎫 Tickets — `/api/tickets` (Autenticado)

| Método | Endpoint                             | Acceso      | Descripción                            |
| ------ | ------------------------------------ | ----------- | -------------------------------------- |
| POST   | `/api/tickets`                       | EMPLOYEE+   | Crear un nuevo ticket                  |
| GET    | `/api/tickets`                       | ADMIN       | Listar todos los tickets (paginado)    |
| GET    | `/api/tickets/my-tickets`            | EMPLOYEE    | Mis tickets (paginado)                 |
| GET    | `/api/tickets/assigned`              | ADMIN       | Tickets asignados al admin autenticado |
| GET    | `/api/tickets/{id}/detail`           | Autenticado | Obtener detalle de un ticket           |
| PATCH  | `/api/tickets/{id}/assign-admin`     | ADMIN       | Asignar admin a un ticket              |
| PATCH  | `/api/tickets/{id}/priority`         | ADMIN       | Cambiar prioridad                      |
| PATCH  | `/api/tickets/{id}/status`           | ADMIN       | Cambiar estado                         |
| PATCH  | `/api/tickets/{id}/close`            | ADMIN       | Cerrar ticket con mensaje              |
| PATCH  | `/api/tickets/{id}/reopen`           | Autenticado | Reabrir ticket cerrado                 |
| POST   | `/api/tickets/{id}/labels/{labelId}` | ADMIN       | Asignar etiqueta a ticket              |
| DELETE | `/api/tickets/{id}/labels/{labelId}` | ADMIN       | Quitar etiqueta de ticket              |

### 💬 Mensajes — `/api/tickets/{ticketId}/messages` (Autenticado)

| Método | Endpoint                           | Descripción                              |
| ------ | ---------------------------------- | ---------------------------------------- |
| GET    | `/api/tickets/{ticketId}/messages` | Obtener mensajes de un ticket (paginado) |
| POST   | `/api/tickets/{ticketId}/messages` | Enviar un mensaje en el ticket           |

### 🏷️ Etiquetas — `/api/labels`

| Método | Endpoint                      | Acceso      | Descripción                           |
| ------ | ----------------------------- | ----------- | ------------------------------------- |
| POST   | `/api/labels`                 | ADMIN       | Crear etiqueta                        |
| GET    | `/api/labels`                 | Autenticado | Listar todas las etiquetas (paginado) |
| GET    | `/api/labels/active`          | Autenticado | Listar etiquetas activas              |
| GET    | `/api/labels/filter?name=...` | Autenticado | Buscar etiquetas por nombre           |
| PUT    | `/api/labels/{id}`            | ADMIN       | Editar etiqueta                       |
| PATCH  | `/api/labels/{id}/deactivate` | ADMIN       | Desactivar etiqueta                   |
| PATCH  | `/api/labels/{id}/activate`   | ADMIN       | Activar etiqueta                      |

### 👥 Usuarios — `/api/users` (ADMIN)

| Método | Endpoint                 | Descripción                                                         |
| ------ | ------------------------ | ------------------------------------------------------------------- |
| POST   | `/api/users`             | Crear usuario (empleado)                                            |
| GET    | `/api/users`             | Listar todos los usuarios (paginado)                                |
| GET    | `/api/users/admins`      | Listar todos los administradores                                    |
| GET    | `/api/users/{id}`        | Obtener usuario por ID                                              |
| PUT    | `/api/users/{id}`        | Editar usuario                                                      |
| PATCH  | `/api/users/{id}/active` | Activar / desactivar usuario (con reasignación opcional de tickets) |

### 🔔 Notificaciones — `/api/notifications` (Autenticado)

| Método | Endpoint                       | Descripción                                  |
| ------ | ------------------------------ | -------------------------------------------- |
| GET    | `/api/notifications`           | Obtener notificaciones paginadas con resumen |
| GET    | `/api/notifications/unread`    | Obtener notificaciones no leídas             |
| PUT    | `/api/notifications/{id}/read` | Marcar notificación como leída               |
| PUT    | `/api/notifications/read-all`  | Marcar todas como leídas                     |

</details>

---

## 👥 Equipo

| Rol           | Nombre                  | GitHub                                                   | LinkedIn                                                         |
| ------------- | ----------------------- | -------------------------------------------------------- | ---------------------------------------------------------------- |
| Product Owner | Leonela Rivas           | [@Leonela88](https://github.com/Leonela88)               | [LinkedIn](https://www.linkedin.com/in/leonela-rivas-28a706246/) |
| Scrum Master  | Ingrid Lopez            | [@Nuclea88](https://github.com/Nuclea88)                 | [LinkedIn](https://www.linkedin.com/in/ingrid-lopez-poveda/)     |
| Developer     | Anna Costa              | [@annahico](https://github.com/annahico)                 | [LinkedIn](https://www.linkedin.com/in/annahico/)                |
| Developer     | Maria Jose Ozta         | [@majoz-t](https://github.com/majoz-t)                   | [LinkedIn](https://www.linkedin.com/in/maria-jose-ozta)          |
| Developer     | Sukaina Hadani          | [@sukisu91-alt](https://github.com/sukisu91-alt)         | [LinkedIn](https://www.linkedin.com/in/sukaina-hadani-97161b394) |
| Developer     | Marie-Charlotte Doulcet | [@Charlottedoulcet](https://github.com/Charlottedoulcet) | [LinkedIn](https://www.linkedin.com/in/marie-charlottedoulcet/)  |

> 💙 Proyecto desarrollado durante el **FemCoders Bootcamp — 7 al 29 de abril de 2026**

---

## 🌱 Posibles mejoras futuras

- 📎 **Adjuntos en tickets** — permitir adjuntar archivos o capturas de pantalla al crear un ticket
- 📊 **Panel de estadísticas avanzado** — métricas de tickets por estado, prioridad y tiempo de resolución para el ADMIN
