<?php
session_start();

require_once __DIR__ . '/php/funciones_envio.php';

// Si es la primera vez, guarda datos en la sesión desde POST
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['email'], $_POST['origen'])) {
    $email = $_POST['email'];
    $origen = $_POST['origen'];

    // Validar datos mínimos
    if (empty($email)) {
        header('Location: /index');
        exit;
    }

    if ($origen === 'registro') {
        if (!isset($_POST['nombre_usuario'], $_POST['password'])) {
            header('Location: /index');
            exit;
        }

        $_SESSION['usuario_pendiente'] = [
            'email' => $email,
            'nombre_usuario' => $_POST['nombre_usuario'],
            'password_hash' => password_hash($_POST['password'], PASSWORD_BCRYPT),
            'tipo' => 'web',
            'es_verificado' => false
        ];
    } elseif ($origen === 'login') {
        $_SESSION['usuario_pendiente'] = [ 'email' => $email ];
    }

    $_SESSION['origen'] = $origen;
    $_SESSION['verificacion_email'] = $email;

    // Enviar el código
    enviarCodigoDeVerificacion($email);
}

// Si no hay sesión, redirige
if (empty($_SESSION['usuario_pendiente']) || empty($_SESSION['verificacion_email']) || empty($_SESSION['origen'])) {
    header('Location: /index');
    exit;
}
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