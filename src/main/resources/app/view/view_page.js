const urlParams = new URLSearchParams(window.location.search);

let fetchedData = {}

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
            fetchedData = data.plugin;

            document.getElementById("plugin-version").textContent = "Version " + fetchedData.version;
            document.getElementById('downloads').textContent = fetchedData.downloads + "";
            document.getElementById("last-update").textContent = fetchedData.lastUpdated.substr(0, 12);
            document.getElementById("creationDate").textContent = fetchedData.dateCreated.substr(0, 12);
            //if ("compatibleVersions" in data) document.getElementById("compatible-versions").textContent = "Caesar " + data.compatibleVersions[0];
            document.getElementById("plugin-name").textContent = fetchedData.name;
            document.getElementById("desc-short").textContent = fetchedData.description;
            document.getElementById("desc-long").innerHTML = fetchedData.descriptionLong.replace('\n\r', '<br>');
            document.getElementById("author").textContent = fetchedData.author;
            document.getElementById("author").href = "/dev-profile?user=" + fetchedData.author;
            if (fetchedData.author === getCookie("username")) {
                document.getElementById('editBTN').classList.remove("hidden");
            }
            if ("license" in fetchedData) {
                document.getElementById("license").textContent = fetchedData.license;
            } else {
                document.getElementById("license-line").classList.add("hidden");
            }

            if ("sourceCode" in fetchedData && fetchedData.sourceCode !== "") {
                document.getElementById("source-code-link").innerHTML = `<i class="fa-brands fa-github"></i> GitHub`;
                document.getElementById("source-code-link").href = fetchedData.sourceCode;
            } else {
                document.getElementById("source-code").classList.add("hidden");
            }

            if ("sponsorLink" in fetchedData && fetchedData.sponsorLink !== "") {
                document.getElementById("sponsor-link").href = fetchedData.sponsorLink;
                document.getElementById("sponsor-link").textContent = fetchedData.sponsorLink;
            } else {
                document.getElementById("sponsor").classList.add("hidden");
            }

            if ("wikiLink" in fetchedData && fetchedData.wikiLink !== "") {
                document.getElementById("wiki-link").href = fetchedData.wikiLink;
                document.getElementById("wiki-link").textContent = fetchedData.wikiLink;
            } else {
                document.getElementById("wiki").classList.add("hidden");
            }
            document.getElementById("pl-logo").src = `http://${ADDRESS}/api/image/` + fetchedData.uniqueId + "?type=plogo";
            if ("screenshots" in fetchedData) {
                document.getElementById("screen1").src = `http://${ADDRESS}/api/image/` + fetchedData.screenshots[0] + "?type=screenshot";
                if (fetchedData.screenshots.length >= 2) {
                    document.getElementById("screen2").src = `http://${ADDRESS}/api/image/` + fetchedData.screenshots[1] + "?type=screenshot";
                } else document.getElementById("screen2").classList.add("hidden");
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


function editPage() {
    window.location.href = "/edit?plugin=" + fetchedData.uniqueId;
}

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