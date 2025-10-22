document.addEventListener('DOMContentLoaded', function() {

    
});


async function uploadProfilePicture(file, userId = null) {
    const formData = new FormData();
    formData.append('file', file);

    const url = userId
        ? `http://${ADDRESS}/api/image/user-profile?user=${encodeURIComponent(userId)}`
        : `http://${ADDRESS}/api/image/user-profile`;

    try {
        const response = await fetch(url, {
            method: 'POST',
            headers: new Headers({ 'Authorization': 'Bearer ' + getCookie('token'), }),
            body: formData
        });

        if (!response.ok) {
            const errorData = await response.json();
            console.error('Fehler beim Hochladen:', errorData);
        } else {
            console.log('Bild erfolgreich hochgeladen!');
        }
    } catch (error) {
        console.error('Netzwerkfehler:', error);
    }
}


function formatUnixTimestamp(unixTimestamp, locale = 'de-DE', options = {}) {
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
