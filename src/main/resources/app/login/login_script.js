document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');

    loginForm.addEventListener('submit', function(event) {
        event.preventDefault();

        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value.trim();
        const rememberMe = document.getElementById('rememberMe').checked;

        hashSHA256(password).then(hashPW => {
            fetch(`http://${ADDRESS}/api/market/user/login`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Basic " + btoa(email + ":" + hashPW)
                }
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error("Login failed");
                    }
                    return response.json();
                })
                .then(json => {
                    console.log(json);
                    if (json.success) {
                        setCookie("token", json.token, 30);
                        window.location.href = '/';
                    } else {
                        console.error("Login nicht erfolgreich:", json.message || "Unbekannter Fehler");
                    }
                })
                .catch(error => {
                    console.error("Fehler:", error);
                });
        });

    });
});
async function hashSHA256(text) {
    const encoder = new TextEncoder();
    const data = encoder.encode(text);
    const hashBuffer = await crypto.subtle.digest("SHA-256", data);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
    return hashHex;
}