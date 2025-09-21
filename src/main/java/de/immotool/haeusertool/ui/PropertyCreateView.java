package de.immotool.haeusertool.ui;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import de.immotool.haeusertool.model.Property;
import de.immotool.haeusertool.service.PropertyService;
import jakarta.annotation.security.PermitAll;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Locale;

@Route(value = "properties/new", layout = MainLayout.class)
@PageTitle("Objekt anlegen")
@PermitAll
@JsModule("./cropper.js") // stellt window.openCropper(...) bereit
public class PropertyCreateView extends VerticalLayout {

    private final PropertyService service;

    // Upload / Bild
    private final FileBuffer imgBuffer = new FileBuffer();
    private final Upload imageUpload = new Upload(imgBuffer);
    private final Image preview = new Image();

    // temporär zu speichernde Bilddatei + Name (wird beim Speichern ins Storage verschoben)
    private Path pendingImageTemp;
    private String pendingImageName;

    public PropertyCreateView(PropertyService service) {
        this.service = service;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Objekt anlegen");

        // --- Felder ---
        TextField street = new TextField("Straße");
        street.setRequiredIndicatorVisible(true);
        street.setWidthFull();

        TextField hausnr = new TextField("Haus-Nr.");
        hausnr.setRequiredIndicatorVisible(true);
        hausnr.setWidth("200px");

        TextField postleitzahl = new TextField("Postleitzahl");
        postleitzahl.setRequiredIndicatorVisible(true);
        postleitzahl.setWidth("200px");

        TextField ort = new TextField("Ort");
        ort.setRequiredIndicatorVisible(true);
        ort.setWidthFull();

        NumberField area = new NumberField("Fläche (m²)");
        area.setStep(0.1);
        area.setMin(0);
        area.setWidth("200px");

        DatePicker kaufdatum = new DatePicker("Kaufdatum");
        kaufdatum.setPlaceholder("TT.MM.JJJJ");
        kaufdatum.setClearButtonVisible(true);
        kaufdatum.setLocale(Locale.GERMANY);

        // --- Bild-Upload + Vorschau ---
        imageUpload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
        imageUpload.setMaxFiles(1);
        imageUpload.setDropAllowed(false);
        imageUpload.setUploadButton(new Button("Titelbild auswählen"));

        preview.setAlt("Vorschau");
        preview.setMaxWidth("320px");
        preview.getStyle().set("border-radius", "8px");

        imageUpload.addSucceededListener(ev -> {
            // 1) Temp-Datei & Name merken
            pendingImageTemp = imgBuffer.getFileData().getFile().toPath();
            pendingImageName = ev.getFileName();

            // 2) Vorschau aus Temp-Datei
            if (pendingImageTemp != null && Files.exists(pendingImageTemp)) {
                StreamResource res = new StreamResource(pendingImageName, () -> {
                    try { return Files.newInputStream(pendingImageTemp); }
                    catch (Exception e) { return InputStream.nullInputStream(); }
                });
                preview.setSrc(res);

                // 3) Cropper-Dialog öffnen (JS) — beachte: host = diese View!
                UI.getCurrent().getPage().executeJs(
                        "window.openCropper($0, $1, $2)",
                        getElement(),              // host (View mit @ClientCallable)
                        preview.getElement(),      // Bildquelle/Vorschau
                        pendingImageName           // Original-Dateiname
                );
            }
        });

        // --- Binder ---
        Binder<Property> binder = new Binder<>(Property.class);
        binder.forField(street).asRequired("Adresse ist erforderlich")
                .bind(Property::getStreet, Property::setStreet);
        binder.forField(postleitzahl).asRequired("Postleitzahl ist erforderlich")
                .bind(Property::getPostleitzahl, Property::setPostleitzahl);
        binder.forField(hausnr).asRequired("Haus-Nr. ist erforderlich")
                .bind(Property::getHausnr, Property::setHausnr);
        binder.forField(ort).asRequired("Ort ist erforderlich")
                .bind(Property::getOrt, Property::setOrt);
        binder.forField(area)
                .bind(Property::getAreaM2, Property::setAreaM2);
        binder.forField(kaufdatum)
                .bind(Property::getPurchaseDate, Property::setPurchaseDate);

        // --- Buttons ---
        Button save = new Button("Speichern", e -> {
            try {
                Property p = new Property();
                binder.writeBean(p);

                // Bild dauerhaft speichern und Pfad ins Property setzen
                if (pendingImageTemp != null && Files.exists(pendingImageTemp)) {
                    String rel = service.storeImage(pendingImageTemp, pendingImageName);
                    p.setImagePath(rel);
                }

                service.create(p);
                UI.getCurrent().navigate("properties");
            } catch (ValidationException ve) {
                Notification.show("Bitte Felder prüfen.");
            } catch (Exception ex) {
                Notification.show("Fehler: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Abbrechen", e -> UI.getCurrent().navigate("properties"));

        // --- Layout ---
        FormLayout form = new FormLayout(
                street, hausnr,
                postleitzahl, ort,
                area, kaufdatum,
                imageUpload, preview
        );
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("700px", 2)
        );

        form.setColspan(street, 1);
        form.setColspan(hausnr, 1);
        form.setColspan(postleitzahl, 1);
        form.setColspan(ort, 1);
        form.setColspan(imageUpload, 1);
        form.setColspan(preview, 1);

        HorizontalLayout actions = new HorizontalLayout(save, cancel);

        add(title, form, actions);
    }

    /**
     * Wird von frontend/cropper.js nach Klick auf "Übernehmen" aufgerufen.
     * @param dataUrl Base64-Data-URL (z. B. "data:image/jpeg;base64,...")
     * @param originalFileName ursprünglicher Dateiname
     */
    @ClientCallable
    private void saveCropped(String dataUrl, String originalFileName) {
        try {
            int comma = dataUrl.indexOf(',');
            if (comma < 0) throw new IllegalArgumentException("Ungültige Data-URL");
            String header = dataUrl.substring(0, comma); // z.B. data:image/jpeg;base64
            String base64 = dataUrl.substring(comma + 1);
            byte[] bytes = Base64.getDecoder().decode(base64);

            // Endung aus MIME
            String ext = extensionFromDataUrlHeader(header); // ".jpg" / ".png" / ".webp"

            // neue Temp-Datei schreiben
            Path tmp = Files.createTempFile("hausertool-cropped-", ext);
            Files.write(tmp, bytes);

            // alte pending-Datei ersetzen
            if (pendingImageTemp != null && Files.exists(pendingImageTemp)) {
                try { Files.deleteIfExists(pendingImageTemp); } catch (IOException ignore) {}
            }
            pendingImageTemp = tmp;
            pendingImageName = deriveCroppedName(originalFileName, ext);

            // Vorschau aktualisieren
            StreamResource res = new StreamResource(pendingImageName, () -> new ByteArrayInputStream(bytes));
            preview.setSrc(res);

            Notification.show("Ausschnitt übernommen.", 2000, Notification.Position.BOTTOM_CENTER);
        } catch (Exception ex) {
            Notification.show("Zuschneiden fehlgeschlagen: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private static String extensionFromDataUrlHeader(String header) {
        if (header.contains("image/png")) return ".png";
        if (header.contains("image/webp")) return ".webp";
        return ".jpg"; // Default
    }

    private static String deriveCroppedName(String original, String newExt) {
        if (original == null || original.isBlank()) return "bild" + newExt;
        int dot = original.lastIndexOf('.');
        String base = (dot > 0) ? original.substring(0, dot) : original;
        return base + "-cropped" + newExt;
    }
}





