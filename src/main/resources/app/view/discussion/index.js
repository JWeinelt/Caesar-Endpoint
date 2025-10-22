const urlParams = new URLSearchParams(window.location.search);

function displayComment(cData) {
    let id = cData.id;
    let el = `
                <div class="bg-gray-700 p-4 rounded-lg mb-4">
                    <p class="text-sm text-gray-400">Posted by <strong>User1</strong> on <strong>April 1, 2025 at 1:00 PM</strong></p>
                    <p class="text-gray-300 mt-2">This is a comment on the resource. It can contain any user input.</p>
                    <hr style="margin: 3px">
                    <div class="md:flex space-x-3 justify-begin">
                        <a id="thumbup-${id}" href="javascript:thumbUp(${id});"><i class="fa-solid fa-thumbs-up"></i>2</a>
                        <a id="thumbdown-${id}"  href="javascript:thumbDown(${id});"><i class="fa-solid fa-thumbs-down"></i>5</a>
                        <a></a>
                        <a></a>
                        <a></a>
                        <a href="javascript:answer(${id});"><i class="fa-solid fa-quote-right"></i></a>
                        <a href="javascript:shareLink(${id});"><i class="fa-solid fa-share"></i></a>
                        <a></a>
                        <a></a>
                        <a></a>
                        <a></a>
                        <a></a>
                        <a></a>
                        <a></a>
                        <a></a>
                        <a href="javascript:report(${id});"><i class="fa-solid fa-flag"></i></a>
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

}

displayComment({"id": "1"})