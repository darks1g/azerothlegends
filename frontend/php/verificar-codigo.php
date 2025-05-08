<?php
require_once __DIR__ . '/credenciales.php';
session_start();
header('Content-Type: application/json');

// Verifica sesión y código recibido
if (
    !isset($_POST['codigo']) ||
    empty($_SESSION['verificacion']) ||
    empty($_SESSION['usuario_pendiente']) ||
    empty($_SESSION['origen'])
) {
    http_response_code(400);
    echo json_encode(['error' => 'Sesión de verificación inválida.']);
    exit;
}

$codigoIngresado = trim($_POST['codigo']);
$verificacion = $_SESSION['verificacion'];
$usuarioPendiente = $_SESSION['usuario_pendiente'];
$origen = $_SESSION['origen'];

// Validaciones del código
if (time() > $verificacion['expira']) {
    http_response_code(403);
    echo json_encode(['error' => 'El código ha expirado.']);
    exit;
}

if ($codigoIngresado !== strval($verificacion['codigo'])) {
    http_response_code(403);
    echo json_encode(['error' => 'Código incorrecto.']);
    exit;
}

if ($verificacion['email'] !== $usuarioPendiente['email']) {
    http_response_code(403);
    echo json_encode(['error' => 'El correo no coincide.']);
    exit;
}

// Inserta en base de datos si es registro
if ($origen === 'registro') {
    $conn = new mysqli($db_host, $db_user, $db_pass, $db_name);
    if ($conn->connect_error) {
        http_response_code(500);
        echo json_encode(['error' => 'Error de conexión a la base de datos.']);
        exit;
    }

    // Comprueba que no esté ya registrado (prevención de doble verificación)
    $check = $conn->prepare("SELECT id FROM usuarios WHERE email = ?");
    $check->bind_param("s", $usuarioPendiente['email']);
    $check->execute();
    $check->store_result();
    if ($check->num_rows > 0) {
        http_response_code(409);
        echo json_encode(['error' => 'El correo ya está registrado.']);
        $check->close();
        $conn->close();
        exit;
    }
    $check->close();

    // Inserta usuario
    $stmt = $conn->prepare("INSERT INTO usuarios (email, nombre_usuario, password_hash, tipo, es_verificado) VALUES (?, ?, ?, ?, ?)");
    $esVerificado = 1;
    $stmt->bind_param(
        "ssssi",
        $usuarioPendiente['email'],
        $usuarioPendiente['nombre_usuario'],
        $usuarioPendiente['password_hash'],
        $usuarioPendiente['tipo'],
        $esVerificado
    );
    $stmt->execute();
    $stmt->close();
    $conn->close();
}

// Devuelve email y contraseña original (solo si está disponible)
$email = $usuarioPendiente['email'];
$password = $_SESSION['usuario_pendiente_password_plain'] ?? null;

// Limpieza de sesión
unset($_SESSION['usuario_pendiente']);
unset($_SESSION['usuario_pendiente_password_plain']);
unset($_SESSION['verificacion']);
unset($_SESSION['origen']);

// Devuelve respuesta para login.js
echo json_encode([
    'success' => true,
    'email' => $email,
    'password' => $password
]);
