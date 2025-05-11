function translate() {
    var urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('lang') === 'de') {
        document.getElementById('header').textContent = 'Ein Ziel. Eine Software.';
        document.getElementById('hb-pricing').textContent = 'Preise';
        document.getElementById('desc1').textContent = 'Serververwaltung schnell und einfach.';
        document.getElementById('desc2').textContent = 'Vollständig Open Source.';
        document.getElementById('learn-more').textContent = 'Mehr erfahren';
        document.getElementById('key-features').textContent = 'Hauptfunktionen';
        document.getElementById('f1t').textContent = 'Einfache Verwaltung';
        document.getElementById('f2t').textContent = 'Integrierbar';
        document.getElementById('f3t').textContent = 'Entwicklerfreundlich';
        document.getElementById('f1d').textContent = 'Verwalte deine Server einfach und überall.';
        document.getElementById('f2d').textContent = 'Funktioniert mit CloudNET oder mit einzelnen Servern. Für maximale Flexibilität.';
        document.getElementById('f3d').textContent = 'Eine riesige gut dokumentierte API für deine Individuallösung.';
        document.getElementById('in-action').textContent = 'Cäsar in Aktion';
        document.getElementById('action-d').textContent = 'Schau dir Cäsar in Aktion an, wie es dein Servermanagement für immer erleichtert.';
        document.getElementById('pricing-plans').textContent = 'Preisgestaltung';
        document.getElementById('p-basic-t').textContent = 'Basis';
        document.getElementById('p-basic-price').textContent = 'Kostenlos';
        document.getElementById('p-basic-d1').textContent = 'Voller Zugriff auf alle Funktionen';
        document.getElementById('p-basic-d2').textContent = 'Zugriff auf den Marktplatz';
        document.getElementById('p-basic-d3').textContent = '🥉 Bronze Support';
        
        document.getElementById('p-premium-t').textContent = 'Premium';
        document.getElementById('p-premium-price').textContent = '5€ pro Monat';
        document.getElementById('p-premium-d1').textContent = 'Alles aus Basis';
        document.getElementById('p-premium-d2').textContent = 'Exklusive Betafunktionen';
        document.getElementById('p-premium-d3').textContent = '🥈 Silver Support';
        
        document.getElementById('p-enterprise-t').textContent = 'Enterprise';
        document.getElementById('p-enterprise-price').textContent = '15€ pro Monat';
        document.getElementById('p-enterprise-d1').textContent = 'Alles aus Premium';
        document.getElementById('').textContent = '';
        document.getElementById('').textContent = '';
        document.getElementById('').textContent = '';
        document.getElementById('').textContent = '';
        document.getElementById('').textContent = '';
        document.getElementById('').textContent = '';
        document.getElementById('').textContent = '';
        document.getElementById('').textContent = '';
    }
}

translate();