[![Project Status: Unmaintained](https://www.repostatus.org/badges/latest/unmaintained.svg)](https://www.repostatus.org/#unmaintained)

# 🌐 Azeroth Legends

> ⚠️ **Nota:** Este proyecto fue desarrollado como parte del módulo de Desarrollo de Aplicaciones Web y actualmente **no se encuentra en desarrollo activo ni recibe mantenimiento**. El código fuente se conserva únicamente de forma académica y como referencia.

Es una plataforma web centrada en mostrar información de personajes de *World of Warcraft* utilizando las APIs oficiales de Blizzard y facilitando la comunicación entre usuarios.

🔗 **URL del proyecto:** [http://azerothlegends.sytes.net](http://azerothlegends.sytes.net) *(Servidor de demostración desactivado / sin garantía de disponibilidad)*

---

## 🚀 Tecnologías utilizadas

- **Backend:** Java 17 con Spring Boot
- **Frontend:** HTML5, CSS3 (basado en [QuestLog](https://github.com/BrettMCoding/QuestLog)), JavaScript
- **Base de datos:** MySQL
- **Autenticación e Integraciones:** OAuth2 (Battle.net), PHPMailer (2FA por correo)

---

## 📋 Estado de funcionalidades

### Implementadas
- [x] Guardado de personajes de WoW desde las APIs oficiales.
- [x] Almacenamiento de estadísticas, talentos y equipo de personajes.
- [x] Inicio de sesión con Battle.net mediante OAuth2.
- [x] Sistema de usuarios (web y Battle.net) con verificación 2FA por email.
- [x] Página de detalle para cada personaje.
- [x] Visualización de personajes en la web (estático o 3D).

### Pendientes / No desarrolladas
- [ ] Sistema de "Me Gusta" para personajes.
- [ ] Chat global anónimo.
- [ ] Sistema de mensajería entre usuarios registrados.
- [ ] Página con listado de personajes y filtrado de resultados.
- [ ] Gestión de errores y mensajes amigables al usuario.
- [ ] Sección de administración (moderación de mensajes/reportes).
- [ ] Optimización del rendimiento (cacheo de respuestas API, paginación, etc.).
