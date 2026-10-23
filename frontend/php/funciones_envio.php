<?php


use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

require_once __DIR__ . '/PHPMailer/src/Exception.php';
require_once __DIR__ . '/PHPMailer/src/PHPMailer.php';
require_once __DIR__ . '/PHPMailer/src/SMTP.php';

function enviarCodigoDeVerificacion($email) {
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        throw new RuntimeException('La dirección de correo no es válida.');
    }

    $username = getenv('SMTP_USERNAME');
    $password = getenv('SMTP_PASSWORD');
    if ($username === false || $username === '' || $password === false || $password === '') {
        throw new RuntimeException('Falta configurar las credenciales SMTP.');
    }

    $codigo = random_int(100000, 999999);
    $_SESSION['verificacion'] = [
        'email' => $email,
        'codigo' => $codigo,
        'expira' => time() + 300
    ];

    $mail = new PHPMailer(true);
    try {
        $mail->isSMTP();
        $mail->Host       = 'smtp.gmail.com';
        $mail->SMTPAuth   = true;
        $mail->Username   = $username;
        $mail->Password   = $password;
        $mail->SMTPSecure = PHPMailer::ENCRYPTION_STARTTLS;
        $mail->Port       = 587;

        $mail->setFrom(getenv('SMTP_FROM_EMAIL') ?: $username, 'Azeroth Legends');
        $mail->addAddress($email);
        $mail->isHTML(true);
        $mail->CharSet = "UTF-8";
        $mail->Subject = 'Tu código de verificación';
        $mail->Body    = "<p>Tu código de verificación es:</p><h2 style='color: #8b5e3c;'>$codigo</h2><p>Este código expira en 5 minutos.</p>";
        $mail->send();
    } catch (Exception $e) {
        error_log("Error enviando email: " . $mail->ErrorInfo);
        throw new RuntimeException('No se pudo enviar el código de verificación.', 0, $e);
    }
}
