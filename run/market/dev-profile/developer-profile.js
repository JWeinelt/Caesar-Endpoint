document.addEventListener('DOMContentLoaded', function() {
    const verificationStatus = document.getElementById('verificationStatus');

    fetch('https://api.caesarnet.cloud/forum/profile/dev/example')
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
            "pdesc2": "",
            "avatar": "https://randomuser.me/api/portraits/men/2.jpg",
            "verified": true
        }
        display(data);
    });
    
});

function check_verify(isVerified) {
    if (isVerified) {
            verificationStatus.textContent = "Verified";
            verificationStatus.classList.remove('text-blue-500');
            verificationStatus.classList.add('text-green-500');
    }
}

function display(data) {
    
        check_verify(data.verified);
        document.getElementById('pname').textContent = data.pname;
        document.getElementById('ptitle').textContent = data.ptitle;
        document.getElementById('pplfo').textContent = data.pplfo;
        document.getElementById('memsince').textContent = 'Member since ' + data.memsince;
        document.getElementById('last-activity').textContent = data.lastac;
        document.getElementById('pdesc1').textContent = data.pdesc1;
        document.getElementById('pdesc2').textContent = data.pdesc2;
        document.getElementById('avatar').src = data.avatar;
    //pl-hist
}