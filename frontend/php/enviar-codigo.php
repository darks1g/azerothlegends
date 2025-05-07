<?php
session_start();
require_once __DIR__ . '/PHPMailer/src/Exception.php';
require_once __DIR__ . '/PHPMailer/src/PHPMailer.php';
require_once __DIR__ . '/PHPMailer/src/SMTP.php';

use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

header('Content-Type: application/json');

// Verifica si se ha enviado un email
if (!isset($_POST['email'])) {
    http_response_code(400);
    echo json_encode(['error' => 'Falta el campo de email.']);
    exit;
}

$email = trim($_POST['email']);

// Validar que sea un correo válido
if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    http_response_code(400);
    echo json_encode(['error' => 'El correo no es válido.']);
    exit;
}

// Generar código aleatorio de 6 dígitos
$codigo = random_int(100000, 999999);

// Guardar en sesión
$_SESSION['verificacion'] = [
    'email' => $email,
    'codigo' => $codigo,
    'expira' => time() + 300 // 5 minutos de validez
];

// Configurar PHPMailer
$mail = new PHPMailer(true);

try {
    // Configuración del servidor SMTP (ajusta con tu servidor real)
    $mail->isSMTP();
    $mail->Host       = 'smtp.gmail.com'; // ej: smtp.gmail.com
    $mail->SMTPAuth   = true;
    $mail->Username   = 'soporte.azerothlegends@gmail.com';
    $mail->Password   = 'cfjz snlz djnm wohp';
    $mail->SMTPSecure = PHPMailer::ENCRYPTION_STARTTLS;
    $mail->Port       = 587;

    // Remitente y destinatario
    $mail->setFrom('soporte.azerothlegends@gmail.com', 'Azeroth Legends');
    $mail->addAddress($email);

    // Contenido
    $mail->isHTML(true);
    $mail->CharSet = "UTF-8";
    $mail->Subject = 'Tu código de verificación';
    $mail->Body    = "<p>Tu código de verificación es:</p><h2 style='color: #8b5e3c;'>$codigo</h2><p>Este código expira en 5 minutos.</p>";

    $mail->send();

    // Solo imprime JSON si es una petición directa al archivo
    if (basename($_SERVER['SCRIPT_NAME']) === 'enviar-codigo.php') {
        echo json_encode(['ok' => true]);
    }

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['error' => 'No se pudo enviar el correo. Detalles: ' . $mail->ErrorInfo]);
}
