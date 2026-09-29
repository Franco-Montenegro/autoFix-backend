# autoFix-backend

API REST del proyecto autoFix. Este repo es solo el backend; el frontend vive en otro repositorio.

## Stack
- Java (versión definida en pom.xml) + Spring Boot
- Spring Data JPA + Hibernate
- MySQL (driver oficial)
- Lombok
- Bean Validation
- Maven (usar el wrapper: ./mvnw)

## Reglas de negocio
El enunciado completo está en ../docs/enunciado.md y el plan de trabajo en ../docs/plan.md.
No los leas completos en cada tarea: lee solo la sección relevante para lo que se pide.

## Alcance actual
- Dentro de alcance: desarrollo y ejecución local del backend.
- Fuera de alcance por ahora: testing y despliegue. Están descritos en el enunciado,
  pero se harán en fases posteriores. No los implementes ni configures todavía.
- Escribe el código de forma que esas etapas no se dificulten: configuración por
  variables de entorno y capas desacopladas.

## Arquitectura
Paquetes por capa dentro del paquete base:
- controller: endpoints REST, sin lógica de negocio
- service: lógica de negocio
- repository: interfaces Spring Data JPA
- entity: entidades JPA
- dto: objetos de request/response (nunca exponer entidades en la API)
- exception: excepciones propias y manejador global (@RestControllerAdvice)
- config: configuración de Spring

## Convenciones de código
- Lombok para boilerplate (@Getter, @Setter, @Builder, @RequiredArgsConstructor).
  Evitar @Data en entidades JPA.
- Inyección de dependencias por constructor, no por @Autowired en campos.
- Validaciones con anotaciones (@NotNull, @Size, etc.) en los DTOs de entrada.
- Errores devueltos con códigos HTTP correctos y mensajes claros.
- Nombres en inglés para clases, métodos y variables; mensajes de error al usuario en español.
- Cambios pequeños y enfocados: una funcionalidad por vez.

## Configuración y seguridad
- Configuración en archivos .properties (no YAML): application.properties.
- Perfil "local": application-local.properties (ignorado por git).
- Credenciales de BD solo por variables de entorno (DB_HOST, DB_USERNAME, DB_PASSWORD).
- Nunca escribir credenciales en código, en archivos versionados ni en el chat.

## Cómo trabajar conmigo
- Para tareas grandes, propón primero un plan y espera mi aprobación antes de escribir código.
- Trabaja solo la fase que te pida; no avances a la siguiente por tu cuenta.
- Antes de modificar entidades o el esquema de BD, explícame el cambio.
- Al terminar, indícame cómo probar lo que hiciste (comando y resultado esperado).
- No ejecutes git commit ni git push; los hago yo.