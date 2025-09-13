package de.immotool.haeusertool.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import de.immotool.haeusertool.model.Property;
import de.immotool.haeusertool.repo.PropertyRepository;


@Route(value = "properties", layout = MainLayout.class)
@jakarta.annotation.security.PermitAll // erlaubt allen angemeldeten Nutzern
public class PropertyView extends VerticalLayout {

    private final PropertyRepository repo;

    private final Grid<Property> grid = new Grid<>(Property.class, false);
    private final TextField name = new TextField("Name");
    private final TextField address = new TextField("Adresse");
    private final NumberField areaM2 = new NumberField("Fläche (m²)");
    private final Button save = new Button("Speichern");
    private final Button neu  = new Button("Neu");
    private final Button del  = new Button("Löschen");

    private final Binder<Property> binder = new Binder<>(Property.class);
    private Property current = new Property();

    public PropertyView(PropertyRepository repo) {
        this.repo = repo;

        grid.addColumn(Property::getId).setHeader("ID").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Property::getName).setHeader("Name").setAutoWidth(true);
        grid.addColumn(p -> p.getAddress() == null ? "" : p.getAddress()).setHeader("Adresse").setAutoWidth(true);
        grid.addColumn(p -> p.getAreaM2() == null ? "" : p.getAreaM2()).setHeader("m²").setAutoWidth(true);
        grid.setItems(repo.findAll());
        grid.setHeight("55vh");
        grid.asSingleSelect().addValueChangeListener(ev -> {
            var sel = ev.getValue();
            if (sel != null) { current = sel; binder.readBean(current); del.setEnabled(current.getId()!=null); }
        });

        binder.forField(name).asRequired("Name ist erforderlich")
                .bind(Property::getName, Property::setName);
        binder.forField(address).bind(Property::getAddress, Property::setAddress);
        binder.forField(areaM2).withConverter(v -> v==null?null:v.doubleValue(), d->d)
                .bind(Property::getAreaM2, Property::setAreaM2);

        neu.addClickListener(e -> { current = new Property(); binder.readBean(current); grid.deselectAll(); del.setEnabled(false); });
        save.addClickListener(e -> {
            try {
                if (!binder.writeBeanIfValid(current)) return;
                var saved = repo.save(current);
                refresh(); grid.select(saved);
                del.setEnabled(true);
                Notification.show("Gespeichert");
            } catch (Exception ex) {
                Notification.show("Fehler: "+ex.getMessage());
            }
        });
        del.addClickListener(e -> {
            if (current.getId()!=null) { repo.deleteById(current.getId()); refresh(); neu.click(); Notification.show("Gelöscht"); }
        });
        del.setEnabled(false);

        var form = new HorizontalLayout(name, address, areaM2, save, neu, del);
        name.setWidth("240px"); address.setWidth("300px"); areaM2.setWidth("140px");

        add(form, grid);
        setPadding(true); setSpacing(true); setSizeFull();
    }

    private void refresh() { grid.setItems(repo.findAll()); }
}


