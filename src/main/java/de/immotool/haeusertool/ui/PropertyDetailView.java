package de.immotool.haeusertool.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.*;
import de.immotool.haeusertool.service.PropertyService;
import jakarta.annotation.security.PermitAll;

import java.util.List;
import java.util.Map;

@Route(value = "properties/:id", layout = MainLayout.class)
@PageTitle("Objekt")
@PermitAll
public class PropertyDetailView extends VerticalLayout implements BeforeEnterObserver {

    private final PropertyService service;
    private Long propertyId;

    private final SideNav nav = new SideNav();
    private final Div content = new Div();

    public PropertyDetailView(PropertyService service) {
        this.service = service;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        nav.setWidth("280px");
        content.setSizeFull();
        content.getStyle().set("padding", "var(--lumo-space-m)");

        HorizontalLayout shell = new HorizontalLayout(nav, content);
        shell.setSizeFull();
        shell.setFlexGrow(1, content);
        add(shell);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var idOpt = event.getRouteParameters().getLong("id");
        if (idOpt.isEmpty()) {
            event.forwardTo(PropertyView.class);
            return;
        }
        propertyId = idOpt.get();

        // SideNav nach Kenntnis der ID aufbauen (Links enthalten die ID + tab)
        buildNav();

        // aktiven Tab aus Query wählen (Default: objekt)
        var qp = event.getLocation().getQueryParameters().getParameters();
        var tab = qp.getOrDefault("tab", List.of("objekt")).get(0);
        show(tab);
    }

    private void buildNav() {
        nav.removeAll();

        String base = "properties/" + propertyId;

        // Obenliegende Punkte
        SideNavItem iObjekt   = new SideNavItem("Objektdaten", base + "?tab=objekt");
        SideNavItem iEinheiten= new SideNavItem("Einheiten",   base + "?tab=einheiten");

        // Gruppe: Mieter
        SideNavItem gMieter   = new SideNavItem("Mieter"); // Überpunkt
        SideNavItem mAktuell  = new SideNavItem("Aktuell", base + "?tab=mieter-aktuell");
        SideNavItem mArchiv   = new SideNavItem("Archiv",  base + "?tab=mieter-archiv");
        gMieter.addItem(mAktuell, mArchiv);

        // Gruppe: Nebenkostenabrechnungen
        SideNavItem gNka      = new SideNavItem("Nebenkostenabrechnungen"); // Überpunkt
        SideNavItem nkaAkt    = new SideNavItem("Aktuell", base + "?tab=nka-aktuell");
        SideNavItem nkaArchiv = new SideNavItem("Archiv",  base + "?tab=nka-archiv");
        gNka.addItem(nkaAkt, nkaArchiv);

        // Reihenfolge im Menü
        nav.addItem(iObjekt, gMieter, iEinheiten, gNka);
    }

    private void show(String key) {
        content.removeAll();

        Map<String, Component> pages = Map.of(
                "objekt",          page("Objektdaten"),
                "mieter-aktuell",  page("Mieter – Aktuell"),
                "mieter-archiv",   page("Mieter – Archiv"),
                "einheiten",       page("Einheiten"),
                "nka-aktuell",     page("Nebenkostenabrechnungen – Aktuell"),
                "nka-archiv",      page("Nebenkostenabrechnungen – Archiv")
        );

        content.add(pages.getOrDefault(key, page("Objektdaten")));
    }

    private Component page(String title) {
        Div d = new Div();
        d.add(new H2(title));
        d.add(new Paragraph("Inhalt folgt …"));
        return d;
    }
}



