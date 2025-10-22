const urlParams = new URLSearchParams(window.location.search);

document.addEventListener('DOMContentLoaded', function() {

    console.log(urlParams.get("plugin"));
    console.log("Fetching...")

    fetch(`http://${ADDRESS}/api/market/plugin?name=` + urlParams.get("plugin"))
        .then(response => {
            if (!response.ok) {
                console.log(response.url);
                throw new Error('Netzwerkantwort war nicht ok: ' + response.statusText);
            }
            return response.json();
        })
        .then(data => {
            console.log('Antwort erhalten:', data);
            document.getElementById("plugin-version").textContent = "Version " + data.version;
            //if ("compatibleVersions" in data) document.getElementById("compatible-versions").textContent = "Caesar " + data.compatibleVersions[0];
            document.getElementById("plugin-name").textContent = data.name;
            document.getElementById("desc-short").textContent = data.description;
            document.getElementById("desc-long").textContent = data.descriptionLong;
            document.getElementById("author").textContent = data.author;
            if ("license" in data) {
                document.getElementById("license").textContent = data.license;
            } else {
                document.getElementById("license-line").classList.add("hidden");
            }

            if ("sourceCode" in data) {
                document.getElementById("source-code-link").textContent = data.sourceCode;
                document.getElementById("source-code-link").href = data.sourceCode;
            } else {
                document.getElementById("source-code").classList.add("hidden");
            }

            if ("sponsorLink" in data) {
                document.getElementById("sponsor-link").href = data.sponsorLink;
                document.getElementById("sponsor-link").textContent = data.sponsorLink;
            } else {
                document.getElementById("sponsor").classList.add("hidden");
            }

            if ("wikiLink" in data) {
                document.getElementById("wiki-link").href = data.wikiLink;
                document.getElementById("wiki-link").textContent = data.wikiLink;
            } else {
                document.getElementById("wiki").classList.add("hidden");
            }
            if ("screenshots" in data) {
                document.getElementById("screen1").src = `http://${ADDRESS}/api/image/` + data.screenshots[0] + "?type=screenshot";
                document.getElementById("screen2").src = `http://${ADDRESS}/api/image/` + data.screenshots[1] + "?type=screenshot";
            }
        })
        .catch(error => {
            console.error('Fehler bei der Fetch-Anfrage:', error);
        });

    document.getElementById("installBtn").addEventListener("click", function() {
        document.getElementById("progressContainer").classList.remove("hidden");
        document.getElementById("installBtn").classList.remove("bg-blue-500");
        document.getElementById("installBtn").classList.remove("hover:bg-blue-400");
        document.getElementById("installBtn").classList.add("bg-gray-500");
        document.getElementById("installBtn").classList.add("hover:bg-gray-500");
        startWebSocket();
    });
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
            showInfoPopup("Installation as successful!")
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