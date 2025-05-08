<?php
require_once __DIR__ . '/credenciales.php';

session_start();
header('Content-Type: application/json');

// Verificaciones de sesión y POST
if (!isset($_POST['codigo']) || empty($_SESSION['verificacion']) || empty($_SESSION['usuario_pendiente']) || empty($_SESSION['origen'])) {
    http_response_code(400);
    echo json_encode(['error' => 'Sesión de verificación inválida.']);
    exit;
}

$codigoIngresado = trim($_POST['codigo']);
$verificacion = $_SESSION['verificacion'];
$usuarioPendiente = $_SESSION['usuario_pendiente'];
$origen = $_SESSION['origen'];

// Comprobaciones de código
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

$email = $verificacion['email'];

if ($email !== $usuarioPendiente['email']) {
    http_response_code(403);
    echo json_encode(['error' => 'El correo no coincide.']);
    exit;
}

// Conexión a la base de datos solo si es registro
if ($origen === 'registro') {
    $conn = new mysqli($db_host, $db_user, $db_pass, $db_name);
    if ($conn->connect_error) {
        http_response_code(500);
        echo json_encode(['error' => 'Error de conexión a la base de datos.']);
        exit;
    }

    // Verificar si ya existe el correo
    $check = $conn->prepare("SELECT id FROM usuarios WHERE email = ?");
    $check->bind_param("s", $email);
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

    // Insertar usuario nuevo
    $stmt = $conn->prepare("INSERT INTO usuarios (email, nombre_usuario, password_hash, tipo, es_verificado) VALUES (?, ?, ?, ?, ?)");
    if (!$stmt) {
        http_response_code(500);
        echo json_encode(['error' => 'Error al preparar la consulta.']);
        exit;
    }

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

    // Recuperar el usuario recién insertado
    $stmt2 = $conn->prepare("SELECT id, email, nombre_usuario, tipo, es_verificado FROM usuarios WHERE email = ?");
    $stmt2->bind_param("s", $usuarioPendiente['email']);
    $stmt2->execute();
    $resultado = $stmt2->get_result();
    $usuario = $resultado->fetch_assoc();
    $stmt2->close();
    $conn->close();

    $_SESSION['usuario'] = $usuario;
} else {
// En login, el usuario ya fue validado por Java, recuperamos datos completos
$conn = new mysqli($db_host, $db_user, $db_pass, $db_name);
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(['error' => 'Error al conectar para obtener el usuario.']);
    exit;
}

$stmt = $conn->prepare("SELECT id, email, nombre_usuario, tipo, es_verificado FROM usuarios WHERE email = ?");
$stmt->bind_param("s", $usuarioPendiente['email']);
$stmt->execute();
$resultado = $stmt->get_result();
$usuario = $resultado->fetch_assoc();
$stmt->close();
$conn->close();

$_SESSION['usuario'] = $usuario;
}

// Limpieza
unset($_SESSION['usuario_pendiente']);
unset($_SESSION['verificacion']);
unset($_SESSION['origen']);

// Iniciar sesión en Java
if ($origen === 'registro') {
    $email = $usuarioPendiente['email'];
    $password = $_SESSION['usuario_pendiente']['original_password'] ?? null;

    if ($password) {
        $ch = curl_init('http://localhost:8080/api/login');
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode([
            'email' => $email,
            'password' => $password
        ]));
        curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_COOKIEFILE, ''); // usa cookies actuales
        curl_setopt($ch, CURLOPT_COOKIEJAR, '');  // guarda cookies nuevas
        curl_exec($ch);
        curl_close($ch);
    }
}


echo json_encode(['success' => true]);
