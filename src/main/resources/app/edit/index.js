const urlParams = new URLSearchParams(window.location.search);

window.addEventListener("beforeunload", function(e){
    if (!document.getElementById('newVersionForm').classList.contains('hidden')) {e.preventDefault();}
}, false);


fetch(`http://${ADDRESS}/api/market/plugin?name=` + urlParams.get("plugin"))
    .then(response => {
        if (!response.ok) {
            console.log(response.url);
            throw new Error('Netzwerkantwort war nicht ok: ' + response.statusText);
        }
        return response.json();
    })
    .then(data => {
        document.getElementById("pluginName").value = data.plugin.name;
        document.getElementById("shortDesc").value = data.plugin.description;
        document.getElementById("longDesc").value = data.plugin.descriptionLong.replace('\n\r', '<br>');
        document.getElementById('licenseSelect').value = data.plugin.license;
        document.getElementById('githubLink').value = data.plugin.sourceCode;
        document.getElementById('sponsorLink').value = data.plugin.sponsorLink;
        document.getElementById('wikiLink').value = data.plugin.wikiLink;

        if (data.plugin.state === "REQUESTED") {
            document.getElementById('popup-text').textContent = "Your plugin hasn't been approved by the " +
                "moderator team, yet. Until then, it's not publicly available on the market place.";
            document.getElementById('popup-title').textContent = "Information!";
        } else if (data.plugin.state === "APPROVED") {
            document.getElementById('popup-text').textContent = "You plugin has been approved by the moderation" +
                " team. It's now visible to everyone.";
            document.getElementById('popup-title').textContent = "Congrats!";
            document.getElementById('popup').classList.remove("bg-yellow-500");
            document.getElementById('popup').classList.add("bg-green-500");
        } else if (data.plugin.state === "REJECTED") {
            document.getElementById('popup-text').innerHTML = "Your plugin request has been denied by our " +
                "moderation team. They gave the following reason(s):<br>"; //TODO: Reasons
            document.getElementById('popup-title').textContent = "Information!";
            document.getElementById('popup').classList.remove("bg-yellow-500");
            document.getElementById('popup').classList.add("bg-red-500");
        }


        document.getElementById("pl-logo").src = `http://${ADDRESS}/api/image/` + data.plugin.uniqueId + "?type=plogo";
        if ("screenshots" in data.plugin) {
            for (let i in data.plugin.screenshots) {
                let e = `
                            <div class="hidden duration-700 ease-in-out" data-carousel-item>
                                <img src="http://${ADDRESS}/api/image/${data.plugin.screenshots[i]}?type=screenshot" 
                                class="absolute block max-w-full h-auto -translate-x-1/2 -translate-y-1/2 top-1/2 left-1/2" alt="Screenshot">
                            </div>`
                let el = document.createElement('div');
                el.innerHTML = e;
                document.getElementById('screenshots-gal').appendChild(el);
            }
        }
    })
    .catch(error => {
        console.error('Fehler bei der Fetch-Anfrage:', error);
    });



function showSection(id) {
    document.querySelectorAll('section').forEach(s => s.classList.add('hidden'));
    document.getElementById(id).classList.remove('hidden');
    document.getElementById(id + "-button").classList.remove('hover:bg-gray-600');
    document.getElementById(id + "-button").classList.remove('bg-gray-700');
    document.getElementById(id + "-button").classList.add('hover:bg-blue-600');
    document.getElementById(id + "-button").classList.add('bg-blue-700');
    let otherSec = ((id === 'plugin-section') ? 'versions' : "plugin") + "-section";
    document.getElementById(otherSec + "-button").classList.remove('hover:bg-blue-600');
    document.getElementById(otherSec + "-button").classList.remove('bg-blue-700');
    document.getElementById(otherSec + "-button").classList.add('hover:bg-gray-600');
    document.getElementById(otherSec + "-button").classList.add('bg-gray-700');
}

function toggleNewVersionForm() {
    const form = document.getElementById('newVersionForm');
    form.classList.toggle('hidden');
}