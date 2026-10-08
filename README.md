[![Project Status: Active – The project has reached a stable, usable state and is being actively developed.](https://www.repostatus.org/badges/latest/active.svg)](https://www.repostatus.org/#active)

# 🌐 Azeroth Legends

> ℹ️ **Nota:** Este proyecto nació como trabajo del módulo de Desarrollo de Aplicaciones Web y se ha retomado como proyecto personal. Para ejecutarlo en local de forma sencilla, mira [README_ARRANQUE.md](README_ARRANQUE.md).

Es una plataforma web centrada en mostrar información de personajes de *World of Warcraft* utilizando las APIs oficiales de Blizzard y facilitando la comunicación entre usuarios.

🔗 **URL del proyecto:** [http://azerothlegends.sytes.net](http://azerothlegends.sytes.net) *(Servidor de demostración desactivado / sin garantía de disponibilidad)*

---

## 🚀 Tecnologías utilizadas

- **Backend:** Java 17 con Spring Boot
- **Frontend:** HTML5, CSS3 (basado en [QuestLog](https://github.com/BrettMCoding/QuestLog)), JavaScript
- **Base de datos:** H2 en local (perfil `dev`) o MySQL (perfil `prod`)
- **Autenticación e Integraciones:** OAuth2 (Battle.net), PHPMailer (2FA por correo)

---

## 📋 Estado de funcionalidades

### Implementadas
- [x] Guardado de personajes de WoW desde las APIs oficiales (Retail, Classic Era y Classic Progresión).
- [x] Almacenamiento de estadísticas, talentos y equipo de personajes.
- [x] Ficha del personaje: equipo con colores de calidad y tooltips de Wowhead, estadísticas, talentos e imagen.
- [x] Listado de personajes con filtros (nombre, clase, versión) y paginación.
- [x] Gestión de errores con mensajes claros (personaje no encontrado, Blizzard no disponible…).
- [x] Optimización: token de Blizzard renovado automáticamente, caché de iconos y reinos descargados una sola vez.
- [x] Inicio de sesión con Battle.net mediante OAuth2.
- [x] Sistema de usuarios (web y Battle.net) con verificación 2FA por email.

### Pendientes / No desarrolladas
- [ ] Probar y pulir Classic con más personajes reales (el formato de talentos de Blizzard es el menos documentado).
- [ ] Sistema de "Me Gusta" para personajes.
- [ ] Chat global anónimo.
- [ ] Sistema de mensajería entre usuarios registrados.
- [ ] Sección de administración (moderación de mensajes/reportes).
- [ ] Visor 3D del personaje (por ahora se usa la imagen que facilita Blizzard).
- [ ] Soporte para los reinos Anniversary de Classic.
