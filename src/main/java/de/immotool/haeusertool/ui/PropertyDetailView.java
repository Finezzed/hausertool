package de.immotool.haeusertool.ui;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import de.immotool.haeusertool.service.PropertyService;
import jakarta.annotation.security.PermitAll;

@Route(value = "properties/:id", layout = MainLayout.class)
@PageTitle("Objekt")
@PermitAll
public class PropertyDetailView extends VerticalLayout implements BeforeEnterObserver {

    private final PropertyService service;

    public PropertyDetailView(PropertyService service) {
        this.service = service;
        setSizeFull();     // weiße leere Seite, ready zum Befüllen
        setPadding(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var idOpt = event.getRouteParameters().getLong("id");
        if (idOpt.isEmpty()) {
            event.forwardTo(PropertyView.class);
            return;
        }
        removeAll();
        add(new H2("Objekt #" + idOpt.get()));
    }
}

