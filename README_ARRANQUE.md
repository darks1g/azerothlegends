# Arranque rápido (modo local)

Requisitos: Java 17 o superior. No hace falta MySQL, PHP ni Apache.

1. Define tus credenciales de Blizzard (https://develop.battle.net/access/clients):

   Linux / macOS:
       export BLIZZARD_CLIENT_ID=tu_client_id
       export BLIZZARD_CLIENT_SECRET=tu_client_secret

   Windows (PowerShell):
       $env:BLIZZARD_CLIENT_ID="tu_client_id"
       $env:BLIZZARD_CLIENT_SECRET="tu_client_secret"

2. Arranca desde la carpeta backend/:

       cd backend
       ./gradlew bootRun        (Windows: gradlew.bat bootRun)

3. Abre http://localhost:8080 y busca un personaje (o explora los ya guardados en /personajes).

Notas:
- La base de datos H2 se crea sola en backend/data/.
- En el primer arranque se descargan los reinos de Blizzard (tarda unos segundos).
- El login con Battle.net y el 2FA por correo (PHP) no funcionan en este modo.
- Para MySQL: SPRING_PROFILES_ACTIVE=prod con DB_URL, DB_USER y DB_PASSWORD.
- Los datos de un personaje se refrescan desde Blizzard como mucho cada 5 minutos
  (se puede cambiar con la propiedad app.refresco-minutos).
- Si cambias de versión de la base de datos o quieres empezar de cero, para la aplicación y borra
  la carpeta backend/data/ (los reinos se vuelven a descargar solos).
- Para ver qué falla en Classic, mira la consola de Spring: cuando no puede leer los talentos,
  escribe un resumen de la respuesta de Blizzard.
