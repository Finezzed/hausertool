// src/main/java/de/immotool/haeusertool/ui/PropertyView.java
package de.immotool.haeusertool.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.immotool.haeusertool.model.Property;
import de.immotool.haeusertool.service.PropertyService;
import jakarta.annotation.security.PermitAll;

@Route(value = "properties", layout = MainLayout.class)
@PageTitle("Objekte")
@PermitAll
@CssImport("./styles/property-cards.css")   // ⬅️ CSS unten
public class PropertyView extends VerticalLayout {

    private final PropertyService service;
    private final Div grid = new Div();

    public PropertyView(PropertyService service) {
        this.service = service;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Objekte");
        Button newBtn = new Button("Objekt anlegen", e -> UI.getCurrent().navigate("properties/new"));
        newBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout header = new HorizontalLayout(title, newBtn);
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.expand(title);

        grid.addClassName("property-grid"); // CSS-Grid
        grid.setSizeFull();

        add(header, grid);
        reload();
    }

    private void reload() {
        grid.removeAll();
        for (Property p : service.listAll()) {
            grid.add(buildCard(p));
        }
    }

    private Div buildCard(Property p) {
        Div card = new Div();
        card.addClassName("property-card");

        // Bild
        Image img = createImage(p);
        img.addClassName("thumb");
        card.add(img);

        // Inhalt
        Div content = new Div();
        content.addClassName("content");
        H4 name = new H4(p.getName() == null ? "(Unbenannt)" : p.getName());
        Paragraph addr = new Paragraph(p.getStreet() == null ? "" : p.getStreet());
        addr.addClassName("subtitle");
        content.add(name, addr);
        card.add(content);

        card.getElement().addEventListener("click", ev -> {
            // TODO: später Detail-/Edit-View: "properties/{id}/edit"
            // UI.getCurrent().navigate("properties/" + p.getId());
        });
        card.getStyle().set("cursor", "pointer");

        return card;
    }

    private Image createImage(Property p) {
        if (p.getImagePath() != null) {
            StreamResource res = new StreamResource(p.getId() + "-img", () -> service.openImage(p.getImagePath()));
            return new Image(res, "Bild");
        }
        // Platzhalter aus /static/images/placeholder.png
        return new Image("images/placeholder.png", "Kein Bild");
    }
}




