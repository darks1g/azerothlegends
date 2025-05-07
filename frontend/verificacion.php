<?php
session_start();

if (!isset($_POST['email']) || empty($_POST['email'])) {
    header('Location: /index');
    exit;
}

require_once __DIR__ . '/php/funciones_envio.php';

// Solo si es registro, crear usuario pendiente en la sesión PHP
if ($_POST['origen'] === 'registro') {
    if (!isset($_SESSION['usuario_pendiente'])) {
        // Crear un usuario básico en sesión solo con email
        $_SESSION['usuario_pendiente'] = [
            'email' => $_POST['email']
        ];
    }
} elseif ($_POST['origen'] === 'login') {
    // Copiar el usuario desde Java a PHP no es posible directamente.
    // Así que solo guardamos el email, y asumimos que Spring validó
    $_SESSION['usuario_pendiente'] = [
        'email' => $_POST['email']
    ];
}

$_SESSION['origen'] = $_POST['origen'];
$_SESSION['verificacion_email'] = $_POST['email'];

// Enviar código
enviarCodigoDeVerificacion($_POST['email']);
?>

<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Verificación en dos pasos</title>
    <link rel="stylesheet" href="/css/questlog.css" />
    <link rel="stylesheet" href="/css/login.css" />
</head>

<body class="bg-parchment font-fantasy text-dark">
    <header class="header">
        <div class="header-center">
            <a href="/index">
                <h1 class="title">Azeroth Legends</h1>
            </a>
        </div>
    </header>

    <div class="login-container">
        <h1>Verificación en dos pasos</h1>
        <p>Hemos enviado un código a tu correo electrónico. Por favor, introdúcelo para continuar.</p>

        <p id="verificationError" class="error" style="display: none;"></p>

        <form id="verificacionForm" class="login-form">
            <label for="codigo">Código de verificación</label>
            <input type="text" id="codigo" name="codigo" required maxlength="6" pattern="\d{6}" placeholder="123456" />
            <button type="submit" class="button">Verificar</button>
        </form>

        <p class="register-link"><a href="/login">Volver al inicio de sesión</a></p>
    </div>

    <footer class="footer">
        <p>© 2025 Azeroth Legends. Proyecto educativo.</p>
    </footer>

    <script src="/js/verificacion.js"></script>
</body>

</html>
