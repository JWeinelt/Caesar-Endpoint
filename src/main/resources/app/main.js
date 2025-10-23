var plugin_data = {}

const intervalID = setInterval(() => {
    console.log("%cSTOP!", "color: red; font-size: 60px; font-weight: bold; text-shadow: 2px 2px 8px black;");
    console.log("%cThis console is intended for developers only.\nIf someone told you to copy and paste something here," +
        " STOP immediately.\nIt may give attackers access to your account or your data.", "color: white; background-color:" +
        " red; font-size: 16px; border-radius: 4px;");
}, 1000);
setTimeout(() => clearInterval(intervalID), 5000);

document.addEventListener('DOMContentLoaded', function() {
    if (getCookie("token") != null && getCookie("token") !== '') {
        document.getElementById("login-button").textContent = "Your Profile";
        document.getElementById("login-button").href = "/dev-profile?user=me";
        document.getElementById("submit-resource").classList.remove("hidden");
    }
});

document.querySelectorAll("header").forEach(e => {
    e.classList.remove("bg-blue-900");
    e.classList.add("bg-gray-800");
});

document.querySelectorAll("footer").forEach(e => {
    e.classList.remove("bg-blue-900");
    e.classList.add("bg-gray-800");
});


function getData() {
    fetch(`http://${ADDRESS}/api/market/plugin`, {
        method: "GET",

        headers: {
            'Authorization': 'Bearer ' + getCookie('token'),
            'Content-Type': 'application/json',
        }
    })
        .then(response => response.json())
        .then(data => {
            plugin_data = data;
            for (let i in data) {
                if (data[i].state !== "APPROVED") continue;
                displayPluginMostDownloads(data[i]);
                displayPluginNewest(data[i]);
                console.log(data[i]);
            }
        })
        .catch(error => {
            console.error('Fehler:', error);
        });
}


function displayPluginMostDownloads(pdata) {
    let pluginName = pdata.name;
    let el = `
                <div class="bg-gray-800 rounded-lg shadow-lg overflow-hidden">
                    <img src="http://${ADDRESS}/api/image/${pdata.uniqueId}?type=plogo" alt="${pluginName}" class="w-full h-48 object-cover">
                    <div class="p-4">
                        <h3 class="text-xl font-semibold"><a href="/view?plugin=${pluginName}">${pluginName}</a></h3>
                        <p class="text-sm text-gray-400 mb-1">${pdata.description}</p>
                        <p class="mb-5 text-m font-semibold">by <a class="devname" href="#">${pdata.author}</a></p>
                        <button class="bg-blue-500 hover:bg-blue-400 text-white py-2 px-4 rounded-full">Install</button>
                    </div>
                </div>
`
    let e = document.createElement('div');
    e.innerHTML = el;
    document.getElementById("most-downloads").appendChild(e);

}
function displayPluginNewest(pdata) {
    let pluginName = pdata.name;
    let el = `
                <div class="bg-gray-800 rounded-lg shadow-lg overflow-hidden">
                    <img src="http://${ADDRESS}/api/image/${pdata.uniqueId}?type=plogo" alt="${pluginName}" class="w-full h-48 object-cover">
                    <div class="p-4">
                        <h3 class="text-xl font-semibold"><a href="/view?plugin=${pluginName}">${pluginName}</a></h3>
                        <p class="text-sm text-gray-400 mb-1">${pdata.description}</p>
                        <p class="mb-5 text-m font-semibold">by <a class="devname" href="#">${pdata.author}</a></p>
                        <button class="bg-blue-500 hover:bg-blue-400 text-white py-2 px-4 rounded-full">Install</button>
                    </div>
                </div>
`
    let e = document.createElement('div');
    e.innerHTML = el;
    document.getElementById("new-resources").appendChild(e);

}


async function hashSHA256(text) {
    const encoder = new TextEncoder();
    const data = encoder.encode(text);
    const hashBuffer = await crypto.subtle.digest("SHA-256", data);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
}

function gotoForum() {
    window.location.href = "https://forum.caesarnet.cloud";
}