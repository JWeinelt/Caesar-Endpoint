const urlParams = new URLSearchParams(window.location.search);

document.getElementById("manage-acc").addEventListener('click', () => {
    window.location.href = '/account'
})

document.addEventListener('DOMContentLoaded', function() {
    const verificationStatus = document.getElementById('verificationStatus');

    if (urlParams.get("user") === "me") {
        document.getElementById("manage-acc").classList.remove("hidden");
        document.getElementById("follow").classList.add("hidden");
    }

    fetch(`http://${ADDRESS}/api/market/profile/${urlParams.get("user")}`, {
        method: "GET",

        headers: {
            'Authorization': 'Bearer ' + getCookie('token'),
            'Content-Type': 'application/json',
        }
    })
    .then(response => response.json())
    .then(data => {
        display(data);
    })
    .catch(error => {
        console.error('Fehler:', error);
        let data = {
            "pname": "Julian Weinelt",
            "ptitle": "Developer",
            "pplfo": "12 Plugins • 50.317 Followers",
            "memsince": "April 2025",
            "lastac": "4 minutes ago",
            "pdesc1": "Julian is the leading developer of Caesar and all of its platforms.",
            "avatar": "https://randomuser.me/api/portraits/men/2.jpg",
            "verified": true
        }
        display(data);
    });
    
});

function check_verify(isVerified) {
    if (isVerified) {
            verificationStatus.innerHTML = `<i class="fa-solid fa-circle-check"></i> Verified`;
            verificationStatus.classList.remove('text-blue-500');
            verificationStatus.classList.add('text-green-500');
    }
}

function display(data) {
        check_verify(data.user.verified);
        document.getElementById('pname').textContent = data.user.userName;
        document.getElementById('ptitle').textContent = "Developer";
        document.getElementById('pplfo').textContent = `${data.plugins.length} Plugins • ${data.followers} Followers`;
        document.getElementById('memsince').textContent = 'Member since ' + formatUnixTimestamp(data.user.created);
        document.getElementById('last-activity').textContent = 'Last Activity ' + formatUnixTimestamp(data.user.lastOnline);
        document.getElementById('pdesc1').textContent = data.user.description;
        document.getElementById('avatar').src = `http://${ADDRESS}/api/image/` + data.user.uniqueID;
    //pl-hist
}

function formatUnixTimestamp(unixTimestamp, locale = 'en-US', options = {}) {
    if (unixTimestamp.toString().length === 10) {
        unixTimestamp *= 1000;
    }

    const date = new Date(unixTimestamp);

    const defaultOptions = {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    };

    return date.toLocaleString(locale, { ...defaultOptions, ...options });
}
