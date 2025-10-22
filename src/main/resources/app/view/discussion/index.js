const urlParams = new URLSearchParams(window.location.search);

function displayComment(cData) {
    let id = cData.RecordID;
    let el = `
                <div class="bg-gray-700 p-4 rounded-lg mb-4">
                    <p class="text-sm text-gray-400">Posted by <strong>${cData.AuthorName}</strong> on <strong>${cData.Created}</strong></p>
                    <p class="text-gray-300 mt-2">${cData.Content}</p>
                    <hr style="margin: 3px">
                    <div class="md:flex space-x-3 justify-begin">
                        <a id="thumbup-${id}" href="javascript:thumbUp('${id}');"><i class="fa-solid fa-thumbs-up"></i>${cData.ThumbsUp}</a>
                        <a id="thumbdown-${id}"  href="javascript:thumbDown('${id}');"><i class="fa-solid fa-thumbs-down"></i>${cData.ThumbsDown}</a>
                        <a></a>
                        <a></a>
                        <a></a>
                        <a href="javascript:answer('${id}');"><i class="fa-solid fa-quote-right"></i></a>
                        <a href="javascript:shareLink('${id}');"><i class="fa-solid fa-share"></i></a>
                        <a></a>
                        <a></a>
                        <a></a>
                        <a></a>
                        <a></a>
                        <a></a>
                        <a></a>
                        <a></a>
                        <a href="javascript:report('${id}');"><i class="fa-solid fa-flag"></i></a>
                    </div>
                </div>
`
    let e = document.createElement("div");
    e.innerHTML = el;
    document.getElementById("comments").appendChild(e);
}

function thumbUp(id) {
    let amount = parseInt(document.getElementById('thumbup-' + id).innerText);
    amount++;
    document.getElementById('thumbup-' + id).innerHTML = `<i class="fa-solid fa-thumbs-up"></i>` + amount;
}

function thumbDown(id) {
    let amount = parseInt(document.getElementById('thumbdown-' + id).innerText);
    amount++;
    document.getElementById('thumbdown-' + id).innerHTML = `<i class="fa-solid fa-thumbs-down"></i>` + amount;
}

function postComment() {
    let bd = JSON.stringify({
        "pluginName": urlParams.get("plugin"),
        "content": document.getElementById("comment-box").value,
    });
    console.log(bd);

    fetch(`http://${ADDRESS}/api/plugin/comment`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + getCookie("token")
        },
        body: bd
    })
        .then(response => {
            if (!response.ok) {
                console.error("Register failed")
            }
            return response.json();
        })
        .then(json => {
            console.log(json);
            if (json.success) {
                getData();
            }
        }).catch(error => {
        console.error("Fehler:", error);
    });
}

function getData() {
    document.getElementById('plugin-name').textContent = urlParams.get("plugin") + ": Discussion";
    clearComments();
    fetch(`http://${ADDRESS}/api/market/plugin/comment?pluginName=` + urlParams.get("plugin"))
        .then(response => {
            if (!response.ok) {
                console.log(response.url);
                throw new Error('Netzwerkantwort war nicht ok: ' + response.statusText);
            }
            return response.json();
        })
        .then(data => {
            for (let i in data) {
                displayComment(data[i]);
            }
        })
        .catch(error => {
            console.error('Fehler bei der Fetch-Anfrage:', error);
        });
}

function clearComments() {
    document.getElementById("comments").innerHTML = `<div class="mt-8" id="comments">
                <h2 class="text-3xl font-semibold text-blue-500 mb-4">Comments on this resource</h2>

        </div>`;
}

getData();