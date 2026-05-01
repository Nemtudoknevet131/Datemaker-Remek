package com.example.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

@RestController
@RequestMapping("/api/users")
public class PasswordResetPageController {

  @GetMapping(value = "/reset-password/page", produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<String> resetPasswordPage(@RequestParam("token") String token) {

    System.out.println("[RESET_PAGE] GET /api/users/reset-password/page token=" + token);

    String escapedToken = HtmlUtils.htmlEscape(token);

    String html = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <title>Reset your DateMaker password</title>
          <style>
            body {
              font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
              background: linear-gradient(135deg,#10002b,#3c096c,#9d4edd);
              color: #f8f0ff;
              display:flex;
              align-items:center;
              justify-content:center;
              min-height:100vh;
              margin:0;
            }
            .card {
              background: rgba(16,0,43,0.92);
              padding: 32px 28px;
              border-radius: 18px;
              box-shadow: 0 22px 60px rgba(0,0,0,0.55);
              max-width: 420px;
              width: 100%;
            }
            h1 {
              margin-top:0;
              margin-bottom:8px;
              font-size:24px;
            }
            p {
              margin-top:4px;
              margin-bottom:12px;
              font-size:14px;
              color:#e0c3ff;
              line-height:1.5;
            }
            label {
              display:block;
              margin-top:18px;
              margin-bottom:6px;
              font-size:13px;
            }
            input[type=password] {
              width:100%;
              padding:10px 12px;
              border-radius:10px;
              border:1px solid #c77dff;
              background:#1b0738;
              color:#ffffff;
              font-size:14px;
              outline:none;
            }
            input[type=password]:focus {
              border-color:#e0b0ff;
              box-shadow:0 0 0 1px #e0b0ff55;
            }
            button {
              margin-top:22px;
              width:100%;
              padding:11px 12px;
              border-radius:999px;
              border:none;
              background:#c77dff;
              color:#2b0035;
              font-weight:600;
              font-size:15px;
              cursor:pointer;
              transition:transform .12s ease, box-shadow .12s ease, background .12s ease;
            }
            button:hover {
              transform:translateY(-1px);
              box-shadow:0 10px 24px rgba(0,0,0,0.35);
              background:#e0b0ff;
            }
            .badge {
              display:inline-block;
              padding:4px 10px;
              border-radius:999px;
              background:rgba(157,78,221,0.22);
              color:#f8f0ff;
              font-size:11px;
              text-transform:uppercase;
              letter-spacing:.04em;
              margin-bottom:14px;
            }
          </style>
        </head>
        <body>
          <div class="card">
            <div class="badge">DateMaker</div>
            <h1>Choose a new password</h1>
            <p>Type your new password below and confirm to finish resetting your account.</p>
            <form method="POST" action="/api/users/reset-password/confirm">
              <input type="hidden" name="token" value="{TOKEN}" />
              <label for="newPassword">New password</label>
              <input id="newPassword" name="newPassword" type="password" minlength="6" required />
              <button type="submit">Update password</button>
            </form>
          </div>
        </body>
        </html>
        """;

    html = html.replace("{TOKEN}", escapedToken);

    return ResponseEntity.ok(html);
  }
}