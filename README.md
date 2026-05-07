# FinTrack — Servicio de Autenticación

Este repositorio contiene el microservicio de autenticación del proyecto FinTrack. Provee registro e inicio de sesión, emisión y renovación de tokens JWT (access + refresh), gestión básica de usuarios, roles y permisos, y control de sesión/refresh tokens.

## Tabla de contenidos

- **Resumen**
- **Arquitectura**
- **Tecnologías**
- **Funcionamiento**
- **Modelo de datos (resumen)**
- **Endpoints**
- **Configuración**
- **Ejecución local / Docker**

## Resumen

Microservicio responsable de la autenticación y autorización de usuarios para FinTrack. Implementa:

- Registro de usuarios (email + contraseña).
- Inicio de sesión que devuelve `accessToken` (JWT) y `refreshToken`.
- Renovación de `accessToken` mediante `refreshToken`.
- Logout que invalida el `refreshToken`.
- Gestión de roles y permisos (entidades y relaciones básicas).

## Arquitectura

Arquitectura en capas (Layered Architecture):

- Capa de Presentación / API: controladores REST que exponen los endpoints. Carpeta principal: `controller` (ej. `AuthController`).
- Capa de Servicio / Lógica de negocio: servicios que contienen la lógica de autenticación, registro, renovación y logout. Carpeta: `service`.
- Capa de Persistencia / Repositorios: acceso a datos con JPA/Hibernate (`repository`), responsables de operaciones CRUD sobre `User`, `Rol`, `Permiso`, `RefreshToken`, etc.
- Capa de Dominio / Modelo: entidades JPA que representan el modelo de negocio (`domain`).
- Capa de DTOs y Mappers: objetos de transferencia (`dto`) y mapeadores (`mapper`) para separar las entidades de la API pública.
- Capa de Configuración y Seguridad: configuración de Spring Security, JWT, CORS y beans globales en `config` (ej. `SecurityConfig`, `JwtProperties`).
- Infraestructura transveral: filtros de autenticación (`security.JwtAuthenticationFilter`), manejo global de excepciones (`exception`), y utilidades (`util`).

Patrón y propiedades claves:

- Separación clara de responsabilidades: controllers → services → repositories.
- Stateless para requests protegidos usando `accessToken` JWT; `refreshToken` se persiste para permitir revocación.
- Hash de contraseñas con `PasswordEncoder` (BCrypt) en `SecurityConfig`.
- Validaciones en DTOs (`jakarta.validation`) y manejo centralizado de errores.

Archivo principal: [src/main/java/com/example/webapi/fintrackautentication/FintrackautenticationApplication.java](src/main/java/com/example/webapi/fintrackautentication/FintrackautenticationApplication.java#L1)

## Estructura del proyecto y clases clave

Carpetas principales:

- `controller` — Controladores REST que exponen la API (ej. `AuthController`).
- `service` — Interfaces y clases con la lógica de negocio (ej. `AutenticacionService`, `UserService`, `TokenService`).
- `repository` — Repositorios JPA (ej. `UserRepository`, `RefreshTokenRepository`, `RolRepository`).
- `domain` — Entidades JPA (`User`, `Rol`, `Permiso`, `RefreshToken`, `UserRol`, `RolPermiso`).
- `dto` — Objetos de transferencia (request/response) y `mapper` para convertir entre entidades y DTOs.
- `security` — Filtros y servicios relacionados con JWT y Spring Security (`JwtAuthenticationFilter`, `JwtService`, `CustomUserDetailsService`).
- `config` — Beans y configuración (ej. `SecurityConfig`, `JwtProperties`, `SecurityProperties`).

Clases y componentes relevantes:

- `AuthController` — Endpoints de autenticación en [src/main/java/com/example/webapi/fintrackautentication/controller/AuthController.java](src/main/java/com/example/webapi/fintrackautentication/controller/AuthController.java#L1).
- `JwtService` — Generación y validación de JWT en [src/main/java/com/example/webapi/fintrackautentication/security/JwtService.java](src/main/java/com/example/webapi/fintrackautentication/security/JwtService.java#L1).
- `JwtAuthenticationFilter` — Filtra requests y carga la autenticación desde el JWT en [src/main/java/com/example/webapi/fintrackautentication/security/JwtAuthenticationFilter.java](src/main/java/com/example/webapi/fintrackautentication/security/JwtAuthenticationFilter.java#L1).
- `CustomUserDetailsService` — Carga `UserDetails` desde la BD, incluyendo roles, en [src/main/java/com/example/webapi/fintrackautentication/security/CustomUserDetailsService.java](src/main/java/com/example/webapi/fintrackautentication/security/CustomUserDetailsService.java#L1).
- `SecurityConfig` — Configuración de seguridad, CORS y beans (ej. `PasswordEncoder`) en [src/main/java/com/example/webapi/fintrackautentication/config/SecurityConfig.java](src/main/java/com/example/webapi/fintrackautentication/config/SecurityConfig.java#L1).

## Valores por defecto y detalles de JWT

Valores por defecto definidos en código (`JwtService`):

- `jwt.access.expiration-ms` por defecto: `900000` ms (15 minutos).
- `jwt.refresh.expiration-ms` por defecto: `604800000` ms (7 días).

Estos valores pueden ser sobrescritos en `application.properties` o variables de entorno.


## Tecnologías

- Java 17+ y Spring Boot
- Spring Security
- JPA / Hibernate
- Lombok
- OpenAPI / Swagger annotations
- H2 / Postgres / MySQL (según configuración de datasource en `application.properties`)

## Funcionamiento

- El cliente se registra en `/api/auth/register` con `email` y `password`.
- Para iniciar sesión, el cliente manda credenciales a `/api/auth/login` y recibe `accessToken` (Bearer JWT) y `refreshToken`.
- Las peticiones a recursos protegidos deben incluir el header `Authorization: Bearer <accessToken>`.
- Cuando el `accessToken` expira, el cliente solicita un nuevo `accessToken` enviando el `refreshToken` a `/api/auth/refresh`.
- El `logout` (`/api/auth/logout`) revoca el `refreshToken` en el servidor.

El filtro de seguridad está definido en [src/main/java/com/example/webapi/fintrackautentication/config/SecurityConfig.java](src/main/java/com/example/webapi/fintrackautentication/config/SecurityConfig.java#L1) y permite libre acceso a `/api/auth/**`, documentación y actuator.

## Modelo de datos (resumen)

- `User` (tabla `usuarios`): id, email, passwordHash, estado, cuentaBloqueada, intentosFallidos, timestamps, relaciones con roles.
- `Rol` (tabla `roles`): id, nombre, timestamps. Relación muchos-a-muchos con `User` mediante `UserRol`.
- `Permiso` (tabla `permisos`): id, nombre, timestamps. Relación con `Rol` mediante `RolPermiso`.
- `RefreshToken` (tabla `refresh_tokens`): token, referencia a `User`, expiresAt, revoked, timestamps.

Entidades principales se encuentran en `src/main/java/com/example/webapi/fintrackautentication/domain/`.

## Endpoints

Prefijo base: `/api/auth`

- `POST /api/auth/register` — Registrar usuario
  - Request: `RegisterRequestDTO`
    - `email` (string, email válido)
    - `password` (string, mínimo 8 caracteres, al menos una mayúscula, una minúscula y un número)
  - Response: `UserResponseDTO` (201 Created)

- `POST /api/auth/login` — Iniciar sesión
  - Request: `LoginRequestDTO`
    - `email` (string)
    - `password` (string)
  - Response: `AuthenticationResponseDTO` (200 OK)
    - `accessToken` (string)
    - `refreshToken` (string)
    - `tokenType` (por defecto "Bearer")
    - `expiresIn` (ms o segundos según implementación)

- `POST /api/auth/refresh` — Renovar tokens
  - Request: `RefreshTokenRequestDTO` { `refreshToken` }
  - Response: `AuthenticationResponseDTO` (200 OK)

- `POST /api/auth/logout` — Cerrar sesión / invalidar refresh token
  - Request: `RefreshTokenRequestDTO` { `refreshToken` }
  - Response: 204 No Content

Los DTOs están en `src/main/java/com/example/webapi/fintrackautentication/dto/`.

Ejemplos de payloads

Registro:

```json
{
  "email": "usuario@ejemplo.com",
  "password": "Password123"
}
```

Login:

```json
{
  "email": "usuario@ejemplo.com",
  "password": "Password123"
}
```

Refresh / Logout:

```json
{
  "refreshToken": "eyJhbGciOi..."
}
```

## Configuración

Variables/propiedades importantes (en `application.properties` o variables de entorno):

- `jwt.secret` — Secreto para firmar los JWT.
- `jwt.access.expirationMs` — Duración del access token (ms).
- `jwt.refresh.expirationMs` — Duración del refresh token (ms).
- `security.maxIntentosFallidos` — Número máximo de intentos fallidos antes de bloquear la cuenta (por defecto 5).

Propiedades mapeadas en: [src/main/java/com/example/webapi/fintrackautentication/config/JwtProperties.java](src/main/java/com/example/webapi/fintrackautentication/config/JwtProperties.java#L1) y [src/main/java/com/example/webapi/fintrackautentication/config/SecurityProperties.java](src/main/java/com/example/webapi/fintrackautentication/config/SecurityProperties.java#L1).

## Seguridad

- El microservicio usa `PasswordEncoder` (BCrypt) para almacenar hashes de contraseña (`SecurityConfig.passwordEncoder`).
- `JwtAuthenticationFilter` valida `accessToken` en cada petición protegida.
- Endpoints públicos: `/api/auth/**`, documentación y `actuator`.
- CORS restringido a `https://fintrack-frontend-rho.vercel.app` (configurable en `SecurityConfig`).

## Ejecutar localmente

Requisitos: JDK 17+, Maven

Ejecutar con Maven:

```bash
mvn clean package
mvn spring-boot:run
```

O ejecutar el JAR generado:

```bash
java -jar target/*.jar
```

## Docker

Construir imagen (si hay Dockerfile en la raíz):

```bash
docker build -t fintrack-auth:latest .
docker run -p 8080:8080 --env-file .env fintrack-auth:latest
```

## Contacto

Para dudas sobre la autenticación, hablar con el equipo backend de FinTrack o abrir un issue en este repo.
