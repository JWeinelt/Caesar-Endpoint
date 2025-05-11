let plugins = [
    {
        "pluginName": "BetterShops",
        "author": "PixelCoder",
        "verified": true,
        "rating": 4.8,
        "downloads": 3000,
        "description": "Brings new mechanics to your world.",
        "tags": ["util", "tools"]
    },
    {
        "pluginName": "XPManager",
        "author": "ScriptKing",
        "verified": true,
        "rating": 3.7,
        "downloads": 5000,
        "description": "A must-have plugin for every admin.",
        "tags": ["admin", "security"]
    },
    {
        "pluginName": "UltraProtection",
        "author": "GameModder",
        "verified": true,
        "rating": 4.9,
        "downloads": 75000,
        "description": "Brings new mechanics to your world.",
        "tags": ["teleport", "movement"]
    },
    {
        "pluginName": "FastTP",
        "author": "ScriptKing",
        "verified": true,
        "rating": 4.5,
        "downloads": 9000,
        "description": "Brings new mechanics to your world.",
        "tags": ["chat", "communication"]
    },
    {
        "pluginName": "EasyClaims",
        "author": "CodeNinja",
        "verified": true,
        "rating": 3.6,
        "downloads": 330,
        "description": "Simplifies complex server tasks.",
        "tags": ["teleport", "movement"]
    },
    {
        "pluginName": "WeatherControl",
        "author": "ScriptKing",
        "verified": true,
        "rating": 3.9,
        "downloads": 500,
        "description": "Simplifies complex server tasks.",
        "tags": ["teleport", "movement"]
    },
    {
        "pluginName": "ChatFilter",
        "author": "PluginWizard",
        "verified": false,
        "rating": 3.8,
        "downloads": 20,
        "description": "Enhance your server with advanced features.",
        "tags": ["gameplay", "enhancement"]
    },
    {
        "pluginName": "SuperStats",
        "author": "DevMaster",
        "verified": true,
        "rating": 4.3,
        "downloads": 3000,
        "description": "A lightweight and powerful solution.",
        "tags": ["economy", "shops"]
    },
    {
        "pluginName": "AdvancedLogger",
        "author": "TechGuru",
        "verified": false,
        "rating": 3.8,
        "description": "Adds creative functionalities.",
        "tags": ["util", "tools"]
    },
    {
        "pluginName": "AutoBackup",
        "author": "TechGuru",
        "rating": 3.9,
        "downloads": 412,
        "description": "A must-have plugin for every admin.",
        "tags": ["util", "tools"]
    },
    {
        "pluginName": "MobController",
        "author": "PluginWizard",
        "verified": false,
        "rating": 4.8,
        "downloads": 361,
        "description": "Brings new mechanics to your world.",
        "tags": ["util", "tools"]
    },
    {
        "pluginName": "VoteRewards",
        "author": "DevMaster",
        "verified": true,
        "rating": 4.4,
        "downloads": 831,
        "description": "Optimized for performance and security.",
        "tags": ["admin", "security"]
    },
    {
        "pluginName": "AdvancedLogger",
        "author": "CyberGenius",
        "verified": false,
        "rating": 4.1,
        "downloads": 4537,
        "description": "Simplifies complex server tasks.",
        "tags": ["util", "tools"]
    },
    {
        "pluginName": "AdvancedLogger",
        "author": "PixelCoder",
        "verified": true,
        "rating": 3.6,
        "downloads": 73877,
        "description": "Enhance your server with advanced features.",
        "tags": ["world", "editing"]
    },
    {
        "pluginName": "SuperStats",
        "author": "CodeNinja",
        "verified": false,
        "rating": 4.4,
        "downloads": 5791,
        "description": "Optimized for performance and security.",
        "tags": ["teleport", "movement"]
    },
    {
        "pluginName": "SuperStats",
        "author": "TechGuru",
        "verified": false,
        "rating": 3.9,
        "downloads": 12494,
        "description": "Enhance your server with advanced features.",
        "tags": ["teleport", "movement"]
    },
    {
        "pluginName": "BetterShops",
        "author": "TechGuru",
        "verified": false,
        "rating": 4.9,
        "downloads": 74,
        "description": "Simplifies complex server tasks.",
        "tags": ["moderation", "rules"]
    },
    {
        "pluginName": "ChatFilter",
        "author": "CyberGenius",
        "rating": 4.8,
        "downloads": 4576,
        "description": "Enhance your server with advanced features.",
        "tags": ["chat", "communication"]
    },
    {
        "pluginName": "WeatherControl",
        "author": "CyberGenius",
        "verified": false,
        "rating": 4.6,
        "downloads": 550,
        "description": "A must-have plugin for every admin.",
        "tags": ["teleport", "movement"]
    },
    {
        "pluginName": "VoteRewards",
        "author": "CodeNinja",
        "verified": true,
        "rating": 4.4,
        "downloads": 656,
        "description": "Empowers users with more control.",
        "tags": ["gameplay", "enhancement"]
    }
]
;
// #########################################################

const urlParams = new URLSearchParams(window.location.search);
let currentPage = 1;
if (urlParams.get('page') != null) currentPage = urlParams.get('page');

// ##########################################################

document.getElementById("search").addEventListener("input", function() {
    filterByName(this.value);
    document.title = this.value + " - Search - CPM";
});

document.getElementById("category").addEventListener("change", function() {
    filterByName(document.getElementById('search').value);
});

document.addEventListener("DOMContentLoaded", filterByName(''));

// ##############################################################

document.getElementById('search').value = urlParams.get('query');
document.getElementById('category').value = urlParams.get('category');
filterByName(document.getElementById('search').value)

// ###########################################################

function filterByName(searchText) {
    const container = document.getElementById("plugin-list");
    let filterTag = document.getElementById("category").value;
    container.innerHTML = "";
    searchText = searchText.toLowerCase();
    let i = 0;
    for (plugin of plugins) {
        let fTags = plugin.tags.concat(['all']);
        if (!plugin.pluginName.toLowerCase().includes(searchText) || !fTags.includes(filterTag)) continue;
        i++;
        
        if (i>currentPage*10) continue;
        if (i<(currentPage-1)*10+1) continue;
        
        let developerVerified = '<img class="tooltip" src="../img/verified.png">';
        if (!plugin.verified) developerVerified = '';

        const pluginElement = document.createElement("div");
        pluginElement.className = "plugin-item bg-gray-800 p-4 rounded-lg flex justify-between items-center";
        
        let downloads = formatNumberWithCommas(plugin.downloads);

        pluginElement.innerHTML = `
            <div>
                <div class="md:flex space-x-1">
                    <a class="text-xl font-bold cursor-pointer" onclick="openPlugin('${plugin.pluginName}')">${plugin.pluginName} <span class="text-gray-400">by</span><span class="text-blue-400"> ${plugin.author}</span></a>
                    <div class="tooltip">${developerVerified}
                        <span class="tooltiptext">Verified developer</span>
                    </div>
                </div>
                <p class="text-gray-400">${plugin.description}</p>
                <p><span class="text-blue-500 text-sm">Tags: </span><span class="text-gray-500 text-sm">${plugin.tags.join(", ")}</span></p>
            </div>

            <div class="flex items-center space-x-4">
                <span class="text-yellow-400">⭐ ${plugin.rating}</span>
                <div class="tooltip"><span class="text-gray-200">⬇ ${downloads}</span><span class="tooltiptext">${downloads} Downloads</span></div>
                <!--<a href="/view/${plugin.pluginName}" class="bg-blue-600 hover:bg-blue-500 text-white py-2 px-4 rounded">View</a>-->
            </div>
        `;

        container.appendChild(pluginElement);
    };
    document.getElementById('search').placeholder = "Search for " + i + " plugins...";
    createPageButtons(i);
    if (Math.ceil(i / 10) < currentPage) {
        let h = document.createElement('h2');
        h.innerHTML = `<h2 class="text-blue-400 text-4xl">Wow, that's too far!</h2>`
        container.appendChild(h);
        let txt = document.createElement('p');
        txt.innerHTML = `
            <p class="text-gray-200">It looks like you wanted to see more than we have.</p>
`
        container.appendChild(txt);
    }
    if (i == 0) {
        let txt = document.createElement('p');
        txt.innerHTML = `
            <p class="text-gray-200">Your search got no results.</p>
`
        container.appendChild(txt);
    }
}

function createPageButtons(entryAmount) {
    const cont = document.getElementById('page-container');
    cont.innerHTML = '';
    let txt = document.createElement('p');
    txt.textContent = 'Pages:';
    cont.appendChild(txt);
    let pages = Math.ceil(entryAmount / 10);
    for (let i = 0; i < pages; i++) {
        let a = document.createElement('a');
        let pg = i+1;
        a.innerHTML = `<a class="cursor-pointer" onclick="openPage(${pg})">${pg}</a>`
        cont.appendChild(a);
    }
}

function openPlugin(plugin) {
    window.location.href = "/view/index.html?plugin=" + plugin;
}

function openPage(page) {
    let path = '?page=' + page;
    if (document.getElementById('search').value != null) path += '&query=' + document.getElementById('search').value;
    if (document.getElementById('category').value != null) path += '&category=' + document.getElementById('category').value;
    window.location.href = path;
}

function formatNumberWithCommas(number) {
    return number.toLocaleString('de-DE'); // 'de-DE' für deutsche Tausendertrennzeichen
}