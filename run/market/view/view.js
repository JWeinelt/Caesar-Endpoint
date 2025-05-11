 document.getElementById("installBtn").addEventListener("click", function() {
            document.getElementById("progressContainer").classList.remove("hidden");
            document.getElementById("installBtn").classList.remove("bg-blue-500");
            document.getElementById("installBtn").classList.remove("hover:bg-blue-400");
            document.getElementById("installBtn").classList.add("bg-gray-500");
            document.getElementById("installBtn").classList.add("hover:bg-gray-500");
            startWebSocket();
        });

function startWebSocket() {
    const socket = new WebSocket("ws://localhost:41824");

    socket.onmessage = function(event) {
        const progress = parseInt(event.data, 10);
        updateProgressBar(progress);
    };

    socket.onerror = function() {
        console.error("WebSocket-Verbindung fehlgeschlagen");
        document.getElementById("progressContainer").classList.add("hidden");
        document.getElementById("installBtn").classList.add("bg-blue-500");
        document.getElementById("installBtn").classList.add("hover:bg-blue-400");
        document.getElementById("installBtn").classList.remove("bg-gray-500");
        document.getElementById("installBtn").classList.remove("hover:bg-gray-500");
        showErrorPopup('Caesar not detected.');
    };
}

function updateProgressBar(value) {
    const progressBar = document.getElementById("progressBar");
    const progressText = document.getElementById("progressText");
    progressBar.style.width = value + "%";
    progressText.textContent = value + "%";

    if (value >= 100) {
        setTimeout(() => {
            document.getElementById("progressContainer").classList.add("hidden");
            alert("Installation abgeschlossen!");
            document.getElementById("installBtn").classList.add("bg-blue-500");
            document.getElementById("installBtn").classList.add("hover:bg-blue-400");
            document.getElementById("installBtn").classList.remove("bg-gray-500");
            document.getElementById("installBtn").classList.remove("hover:bg-gray-500");
        }, 500);
    }
}
function showInfoPopup(message) {
            const popup = document.getElementById("infoPopup");
            document.getElementById("infoText").textContent = message;
            popup.classList.remove("hidden", "opacity-0");
            popup.classList.add("opacity-100");
        }

function closeInfoPopup() {
    const popup = document.getElementById("infoPopup");
    popup.classList.add("opacity-0");
    setTimeout(() => popup.classList.add("hidden"), 500);
}
function showErrorPopup(message) {
            const popup = document.getElementById("errorPopup");
            document.getElementById("errorText").textContent = message;
            popup.classList.remove("hidden", "opacity-0");
            popup.classList.add("opacity-100");
        }

function closeErrorPopup() {
    const popup = document.getElementById("errorPopup");
    popup.classList.add("opacity-0");
    setTimeout(() => popup.classList.add("hidden"), 500);
}