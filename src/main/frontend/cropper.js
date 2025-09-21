// frontend/cropper.js

// Cropper.js dynamisch laden (JS + CSS), damit kein zusätzliches Build-Setup nötig ist
if (!window.__cropperCssLoaded) {
  const link = document.createElement('link');
  link.rel = 'stylesheet';
  link.href = 'https://unpkg.com/cropperjs@1.6.2/dist/cropper.min.css';
  document.head.appendChild(link);
  window.__cropperCssLoaded = true;
}
if (!window.Cropper) {
  const script = document.createElement('script');
  script.src = 'https://unpkg.com/cropperjs@1.6.2/dist/cropper.min.js';
  document.head.appendChild(script);
}

/**
 * Öffnet einen simplen Crop-Dialog und sendet das Ergebnis zurück an die Vaadin-View.
 * @param {Element} host - Vaadin Server-Komponente (die View) -> host.$server.saveCropped(...)
 * @param {Element} vaadinImgEl - <vaadin-image> (bzw. img in Shadow) Quelle zur Anzeige
 * @param {string} fileName - ursprünglicher Dateiname
 */
window.openCropper = async (host, vaadinImgEl, fileName) => {
  // warte, bis Cropper geladen ist
  if (!window.Cropper) {
    await new Promise(res => {
      const id = setInterval(() => { if (window.Cropper) { clearInterval(id); res(); } }, 30);
    });
  }

  // rudimentärer "Dialog"
  const overlay = document.createElement('div');
  Object.assign(overlay.style, {
    position: 'fixed', inset: 0, background: 'rgba(0,0,0,.35)', zIndex: 10000,
    display: 'flex', alignItems: 'center', justifyContent: 'center'
  });

  const dialog = document.createElement('div');
  Object.assign(dialog.style, {
    background: 'white', padding: '12px', borderRadius: '10px',
    boxShadow: '0 10px 30px rgba(0,0,0,.2)', maxWidth: '90vw', maxHeight: '90vh',
    display: 'flex', flexDirection: 'column'
  });
  overlay.appendChild(dialog);
  document.body.appendChild(overlay);

  const img = document.createElement('img');
  img.src = vaadinImgEl.getAttribute('src');
  img.style.maxWidth = '80vw';
  img.style.maxHeight = '70vh';
  img.style.objectFit = 'contain';
  dialog.appendChild(img);

  const btnRow = document.createElement('div');
  btnRow.style.marginTop = '12px';
  btnRow.style.display = 'flex';
  btnRow.style.gap = '8px';
  btnRow.style.justifyContent = 'flex-end';

  const ok = document.createElement('button');
  ok.textContent = 'Übernehmen';
  const cancel = document.createElement('button');
  cancel.textContent = 'Abbrechen';
  btnRow.append(ok, cancel);
  dialog.appendChild(btnRow);

  const cropper = new window.Cropper(img, {
    aspectRatio: 4 / 3,      // TODO: an dein Kachel-Format anpassen (z. B. 16/9)
    viewMode: 1,
    autoCropArea: 1,
    movable: true,
    zoomable: true,
    background: false
  });

  ok.onclick = () => {
    // Zielgröße passend zu deiner Anzeige wählen
    const canvas = cropper.getCroppedCanvas({ width: 800, height: 600 }); // TODO: anpassen
    const dataUrl = canvas.toDataURL('image/jpeg', 0.92);
    host.$server.saveCropped(dataUrl, fileName);
    document.body.removeChild(overlay);
  };
  cancel.onclick = () => {
    document.body.removeChild(overlay);
  };
};

