<?php
session_start();
header('Content-Type: text/html; charset=utf-8');

if (!isset($_SESSION['usuario'])) {
  echo '
    <ul class="perfil-menu">
      <li><a href="/login">Iniciar sesión</a></li>
      <li><a href="/registro">Registrarse</a></li>
    </ul>';
} else {
  echo '
    <ul class="perfil-menu">
      <li><a href="/zona">Mi zona</a></li>
      <li><a href="/chats">Chats</a></li>
      <li><a href="/logout">Cerrar sesión</a></li>
    </ul>';
}
