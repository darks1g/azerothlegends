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

// Firma una prueba temporal para que Spring autentique solo después del segundo factor.
$verificationSecret = getenv('VERIFICATION_SHARED_SECRET');
if ($verificationSecret === false || $verificationSecret === '') {
    http_response_code(503);
    echo json_encode(['error' => 'La verificación no está configurada en el servidor.']);
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

$email = $usuarioPendiente['email'];
$timestamp = (string) time();
$nonce = bin2hex(random_bytes(16));
$mensaje = $email . "\n" . $timestamp . "\n" . $nonce;
$signature = hash_hmac('sha256', $mensaje, $verificationSecret);

// Limpieza de sesión
unset($_SESSION['usuario_pendiente']);
unset($_SESSION['verificacion']);
unset($_SESSION['origen']);

echo json_encode([
    'success' => true,
    'email' => $email,
    'timestamp' => $timestamp,
    'nonce' => $nonce,
    'signature' => $signature
]);
