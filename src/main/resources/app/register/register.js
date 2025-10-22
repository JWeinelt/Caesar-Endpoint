document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('registerForm');

    loginForm.addEventListener('submit', function(event) {
        event.preventDefault();

        const email = document.getElementById('email').value.trim();
        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value.trim();
        const confirmPW = document.getElementById('confirm_password').value.trim();
        if (password.value === confirmPW.value) {

        }

        console.log("Sending to http://localhost/api/market/user/register")
        hashSHA256(password).then(hashPW => {
            fetch(`http://${ADDRESS}/api/market/user/register`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    "email": email,
                    "username": username,
                    "password": hashPW,
                })
            })
                .then(response => {
                    if (!response.ok) {
                        console.error("Register failed")
                    }
                    return response;
                })
                .then(data => {
                    var json = data.json();
                    console.log(json);
                    if (json.success) {
                        setCookie("token", json.token, 30);
                        window.location.href = '/';
                    } else console.log("No success??")
                }).catch(error => {
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
    return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
}