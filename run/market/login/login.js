document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');

    loginForm.addEventListener('submit', function(event) {
        event.preventDefault();

        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value.trim();
        const rememberMe = document.getElementById('rememberMe').checked;
        
        fetch("http://localhost:4040/auth", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Basic " + btoa(email + ":" + password)
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error("Login fehlgeschlagen");
            }
            return response.json();
        })
        .then(data => {
            console.log("Token erhalten:", data.token);
            setCookie("atoken", data.token, 30);
        }).catch(error => {
            console.error("Fehler:", error);
        });

        if (email && password) {
            // Hier würde die API-Anfrage zur Authentifizierung gesendet werden
            console.log('E-Mail:', email);
            console.log('Passwort:', password);
            console.log('Angemeldet bleiben:', rememberMe);

            // Beispiel: Umleiten nach erfolgreichem Login
            // In einer echten Anwendung erfolgt hier eine Serveranfrage:
            window.location.href = '/dashboard';
            
            alert("Anmeldung erfolgreich!"); // Platzhalter für die Antwort des Servers
        } else {
            alert("Bitte füllen Sie alle Felder aus.");
        }
    });
});
