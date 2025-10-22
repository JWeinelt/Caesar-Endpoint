var plugin_data = {}

document.addEventListener('DOMContentLoaded', function() {
    if (getCookie("token") != null && getCookie("token") !== '') {
        document.getElementById("login-button").textContent = "Your Account";
        document.getElementById("login-button").href = "/dev-profile?user=me";
        document.getElementById("submit-resource").classList.remove("hidden");
    }
});

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
            displayPluginMostDownloads(data[i]);
            displayPluginNewest(data[i]);
            console.log(data[i]);
        }
    })
    .catch(error => {
        console.error('Fehler:', error);
    });


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
    const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
    return hashHex;
}

function gotoForum() {
    window.location.href = "https://forum.caesarnet.cloud";
}