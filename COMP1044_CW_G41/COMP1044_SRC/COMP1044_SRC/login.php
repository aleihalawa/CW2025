<?php
session_start();
require 'db_connect.php';

$error = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $email = trim($_POST['email']);
    $password = $_POST['password'];

    $stmt = $conn->prepare("SELECT * FROM users WHERE email=?");
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($row = $result->fetch_assoc()) {
        if ($password == $row['password']) { 
            $_SESSION['user_id'] = $row['id'];
            $_SESSION['user_name'] = $row['username']; 
            $_SESSION['role'] = $row['role'];
            header("Location: dashboard.php");
            exit();
        } else {
            $error = "Incorrect password. Please try again.";
        }
    } else {
        $error = "No account found with that email.";
    }
    $stmt->close();
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Login - CRM System</title>
  <link href="https://fonts.googleapis.com/css?family=Nunito:400,600,700" rel="stylesheet">
  <style>
    * { box-sizing: border-box; }
    body {
      font-family: "Nunito", sans-serif;
      color: #26375a;
      margin: 0;
      padding: 0;
      background: #f6f9fc;
    }
    .container {
      height: 200vh;
      background-image: url('img/background.png'); /* or .jpg if that's your file */
      background-size: 100%;
      background-position: center;
      background-repeat: no-repeat;
    }
    .modal {
      position: fixed;
      left: 0;
      bottom: 0;
      width: 100%;
      height: 60px;
      background: rgba(38, 55, 90, 0.35);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      transition: 0.4s;
    }
    .modal-container {
      display: flex;
      max-width: 720px;
      width: 100%;
      border-radius: 12px;
      overflow: hidden;
      position: absolute;
      opacity: 0;
      pointer-events: none;
      transition-duration: 0.3s;
      background: #fff;
      box-shadow: 0 8px 48px rgba(38, 55, 90, 0.12);
      transform: translateY(100px) scale(0.4);
    }
    .modal-title {
      font-size: 26px;
      margin: 0 0 10px 0;
      font-weight: 700;
      color: #2068b8;
    }
    .modal-desc {
      margin: 6px 0 30px 0;
      color: #26375a;
      opacity: 0.85;
    }
    .modal-left {
      padding: 48px 32px 24px;
      background: #fff;
      flex: 1.5;
      transition-duration: 0.5s;
      transform: translateY(80px);
      opacity: 0;
      display: flex;
      flex-direction: column;
      justify-content: center;
      min-width: 340px;
    }
    .modal-button {
      color: #2068b8;
      font-family: "Nunito", sans-serif;
      font-size: 18px;
      cursor: pointer;
      border: 0;
      outline: 0;
      padding: 10px 40px;
      border-radius: 30px;
      background: #fff;
      box-shadow: 0 10px 40px rgba(38, 55, 90, 0.12);
      transition: 0.3s;
      margin-top: 12px;
      font-weight: 600;
      letter-spacing: 0.5px;
    }
    .modal-button:hover {
      background: #2068b8;
      color: #fff;
    }
    .modal-right {
      flex: 2;
      font-size: 0;
      transition: 0.3s;
      overflow: hidden;
      min-width: 220px;
    }
    .modal-right img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition-duration: 1.2s;
      filter: grayscale(10%) brightness(1.15);
    }
    .modal.is-open { height: 100%; background: rgba(38,55,90,0.7);}
    .modal.is-open .modal-button { opacity: 0; }
    .modal.is-open .modal-container {
      opacity: 1; transition-duration: 0.6s; pointer-events: auto; transform: translateY(0) scale(1);
    }
    .modal.is-open .modal-right img { transform: scale(1); }
    .modal.is-open .modal-left { transform: translateY(0); opacity: 1; transition-delay: 0.1s; }
    .modal-buttons {
      display: flex; justify-content: space-between; align-items: center;
      margin-bottom: 10px;
    }
    .modal-buttons a {
      color: #2068b8;
      font-size: 14px;
      text-decoration: none;
      font-weight: 500;
    }
    .modal-buttons a:hover {
      text-decoration: underline;
    }
    .sign-up {
      margin: 28px 0 0; font-size: 14px; text-align: center;
    }
    .sign-up a { color: #2068b8; text-decoration: none; font-weight: 600;}
    .sign-up a:hover { text-decoration: underline;}
    .input-button {
      padding: 8px 24px;
      outline: none;
      border: 0;
      color: #fff;
      border-radius: 4px;
      background: #2068b8;
      font-family: "Nunito", sans-serif;
      transition: 0.3s;
      cursor: pointer;
      margin-left: 10px;
      font-size: 16px;
      font-weight: 600;
      letter-spacing: 0.4px;
      box-shadow: 0 4px 18px rgba(38, 55, 90, 0.07);
    }
    .input-button:hover { background: #114077;}
    .input-label {
      font-size: 12px; text-transform: uppercase; font-family: "Nunito", sans-serif;
      font-weight: 700; letter-spacing: 0.7px; color: #2068b8; transition: 0.3s;
      margin-bottom: 2px;
    }
    .input-block {
      display: flex;
      flex-direction: column;
      padding: 10px 10px 8px;
      border: 1px solid #dde4ee;
      border-radius: 5px;
      margin-bottom: 18px;
      transition: 0.3s;
      background: #fafdff;
    }
    .input-block input {
      outline: 0;
      border: 0;
      padding: 4px 0 0;
      font-size: 15px;
      font-family: "Nunito", sans-serif;
      background: transparent;
      color: #26375a;
    }
    .input-block input::placeholder { color: #aac2e3; opacity: 1; }
    .input-block:focus-within { border-color: #2068b8; background: #f0f6fd;}
    .input-block:focus-within .input-label { color: #114077;}
    .icon-button {
      outline: 0;
      position: absolute;
      right: 10px;
      top: 12px;
      width: 32px;
      height: 32px;
      border: 0;
      background: 0;
      padding: 0;
      cursor: pointer;
    }
    .scroll-down {
      position: fixed;
      top: 50%;
      left: 50%;
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
      color: #2068b8;
      font-size: 32px;
      font-weight: 800;
      transform: translate(-50%, -50%);
      z-index: 2;
    }
    .scroll-down svg { margin-top: 16px; width: 52px; fill: currentColor; }
    @media(max-width: 750px) {
      .modal-container { width: 94%; }
      .modal-right { display: none; }
    }
    .error-msg {
      color: #d40000;
      background: #eaf2fb;
      padding: 10px 16px;
      border-radius: 6px;
      margin-bottom: 18px;
      font-size: 15px;
      font-weight: 600;
      text-align: center;
      border: 1px solid #aad1f3;
    }
  </style>
</head>
<body>
  <div class="scroll-down">SCROLL DOWN
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32">
      <path d="M16 3C8.832031 3 3 8.832031 3 16s5.832031 13 13 13 13-5.832031 13-13S23.167969 3 16 3zm0 2c6.085938 0 11 4.914063 11 11 0 6.085938-4.914062 11-11 11-6.085937 0-11-4.914062-11-11C5 9.914063 9.914063 5 16 5zm-1 4v10.28125l-4-4-1.40625 1.4375L16 23.125l6.40625-6.40625L21 15.28125l-4 4V9z"/> 
    </svg>
  </div>
  <div class="container"></div>
  <div class="modal">
    <div class="modal-container">
      <div class="modal-left">
        <h1 class="modal-title">Welcome!</h1>
        <p class="modal-desc">Log in to your ABB account below.</p>
        <?php if ($error): ?>
          <div class="error-msg"><?= htmlspecialchars($error) ?></div>
        <?php endif; ?>
        <form method="POST" autocomplete="off">
          <div class="input-block">
            <label for="email" class="input-label">Email</label>
            <input type="email" name="email" id="email" placeholder="Email" required>
          </div>
          <div class="input-block">
            <label for="password" class="input-label">Password</label>
            <input type="password" name="password" id="password" placeholder="Password" required>
          </div>
          <div class="modal-buttons">
            <a href="#">Forgot your password?</a>
            <button class="input-button" type="submit">Login</button>
          </div>
        </form>
        <p class="sign-up">Don't have an account? <a href="#">Sign up now</a></p>
      </div>
      <div class="modal-right">
        <img src="https://images.unsplash.com/photo-1512486130939-2c4f79935e4f?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=dfd2ec5a01006fd8c4d7592a381d3776&auto=format&fit=crop&w=1000&q=80" alt="">
      </div>
      <button class="icon-button close-button">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 50 50">
          <path d="M 25 3 C 12.86158 3 3 12.86158 3 25 C 3 37.13842 12.86158 47 25 47 C 37.13842 47 47 37.13842 47 25 C 47 12.86158 37.13842 3 25 3 z M 25 5 C 36.05754 5 45 13.94246 45 25 C 45 36.05754 36.05754 45 25 45 C 13.94246 45 5 36.05754 5 25 C 5 13.94246 13.94246 5 25 5 z M 16.990234 15.990234 A 1.0001 1.0001 0 0 0 16.292969 17.707031 L 23.585938 25 L 16.292969 32.292969 A 1.0001 1.0001 0 1 0 17.707031 33.707031 L 25 26.414062 L 32.292969 33.707031 A 1.0001 1.0001 0 1 0 33.707031 32.292969 L 26.414062 25 L 33.707031 17.707031 A 1.0001 1.0001 0 0 0 32.980469 15.990234 A 1.0001 1.0001 0 0 0 32.292969 16.292969 L 25 23.585938 L 17.707031 16.292969 A 1.0001 1.0001 0 0 0 16.990234 15.990234 z"></path>
        </svg>
      </button>
    </div>
    <button class="modal-button">Click here to login</button>
  </div>
  <script>
    const body = document.querySelector("body");
    const modal = document.querySelector(".modal");
    const modalButton = document.querySelector(".modal-button");
    const closeButton = document.querySelector(".close-button");
    const scrollDown = document.querySelector(".scroll-down");
    let isOpened = false;

    const openModal = () => {
      modal.classList.add("is-open");
      body.style.overflow = "hidden";
    };
    const closeModal = () => {
      modal.classList.remove("is-open");
      body.style.overflow = "initial";
    };
    window.addEventListener("scroll", () => {
      if (window.scrollY > window.innerHeight / 3 && !isOpened) {
        isOpened = true;
        scrollDown.style.display = "none";
        openModal();
      }
    });
    modalButton.addEventListener("click", openModal);
    closeButton.addEventListener("click", closeModal);
    document.onkeydown = evt => {
      evt = evt || window.event;
      evt.keyCode === 27 ? closeModal() : false;
    };
  </script>
</body>
</html>
