package de.immotool.haeusertool.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
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

import java.nio.file.Files;
import java.nio.file.Path;

@Route(value = "properties/new", layout = MainLayout.class)
@PageTitle("Objekt anlegen")
@PermitAll
public class PropertyCreateView extends VerticalLayout {

    // temporär gemerkte Upload-Datei (wird beim Speichern verschoben)
    private Path pendingImageTemp;
    private String pendingImageName;

    public PropertyCreateView(PropertyService service) {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Objekt anlegen");

        // --- Felder ---

        TextField street = new TextField("Straße");
        street.setRequiredIndicatorVisible(true);
        street.setWidthFull();

        TextField postleitzahl = new TextField("Postleitzahl");
        postleitzahl.setRequiredIndicatorVisible(true);
        postleitzahl.setWidthFull();

        TextField hausnr = new TextField("Haus-Nr.");
        hausnr.setRequiredIndicatorVisible(true);
        hausnr.setWidthFull();

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
        kaufdatum.setLocale(java.util.Locale.GERMANY);

        // --- Bild-Upload + Vorschau ---
        FileBuffer imgBuffer = new FileBuffer();
        Upload imageUpload = new Upload(imgBuffer);
        imageUpload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
        imageUpload.setMaxFiles(1);
        imageUpload.setDropAllowed(false);
        imageUpload.setUploadButton(new Button("Titelbild auswählen"));

        Image preview = new Image();
        preview.setAlt("Vorschau");
        preview.setMaxWidth("320px");
        preview.getStyle().set("border-radius", "8px");

        imageUpload.addSucceededListener(ev -> {
            pendingImageTemp = imgBuffer.getFileData().getFile().toPath();
            pendingImageName = ev.getFileName();

            // kleine Vorschau aus Temp-Datei
            if (pendingImageTemp != null && Files.exists(pendingImageTemp)) {
                StreamResource res = new StreamResource(pendingImageName, () -> {
                    try {
                        return Files.newInputStream(pendingImageTemp);
                    } catch (Exception e) {
                        return null;
                    }
                });
                preview.setSrc(res);
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
                if (pendingImageTemp != null) {
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

        Button cancel = new Button("Abbrechen",
                e -> UI.getCurrent().navigate("properties"));

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
}



