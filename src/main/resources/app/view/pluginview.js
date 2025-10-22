const url = new URLSearchParams(window.location.search);
let plName = url.get("plugin");

document.getElementById('pl-home').href = document.getElementById('pl-home').href + '?plugin=' + plName;
document.getElementById('pl-discuss').href = document.getElementById('pl-discuss').href + '?plugin=' + plName;
document.getElementById('pl-versions').href = document.getElementById('pl-versions').href + '?plugin=' + plName;
document.getElementById('pl-changelogs').href = document.getElementById('pl-changelogs').href + '?plugin=' + plName;
document.getElementById('pl-gallery').href = document.getElementById('pl-gallery').href + '?plugin=' + plName;