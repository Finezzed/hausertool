package de.immotool.haeusertool.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
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
import com.vaadin.flow.router.NotFoundException;



@Route(value = "properties/:id", layout = MainLayout.class)
@PageTitle("Objekt")
@PermitAll
public class PropertyDetailView extends VerticalLayout implements BeforeEnterObserver {

    private final PropertyService service;
    private Long propertyId;
    private de.immotool.haeusertool.model.Property property;


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

        var opt = service.findById(propertyId);
        if(opt.isEmpty()) {
            event.rerouteToError(NotFoundException.class, "Objekt" + propertyId + " nicht gefunden");
            return;
        }
        property = opt.get();


        var qp = event.getLocation().getQueryParameters().getParameters();
        var tab = qp.getOrDefault("tab", List.of("objekt")).get(0);

        buildNav();
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

        if ("objekt".equals(key)) {
            content.add(buildObjektdatenHeader(), buildObjektdatenBody());
            return;
        }
        if ("mieter-aktuell".equals(key)) {
            content.add(page("Mieter – Aktuell"));
            return;
        }
        if ("mieter-archiv".equals(key)) {
            content.add(page("Mieter – Archiv"));
            return;
        }
        if ("einheiten".equals(key)) {
            content.add(page("Einheiten"));
            return;
        }
        if ("nka-aktuell".equals(key)) {
            content.add(page("Nebenkostenabrechnungen – Aktuell"));
            return;
        }
        if ("nka-archiv".equals(key)) {
            content.add(page("Nebenkostenabrechnungen – Archiv"));
            return;
        }
        content.add(page("Objektdaten"));
    }


    private Component page(String title) {
        Div d = new Div();
        d.add(new H2(title));
        d.add(new Paragraph("Inhalt folgt …"));
        return d;
    }
    private Component buildObjektdatenHeader() {
        var title = new H2("Objektdaten");
        var edit  = new com.vaadin.flow.component.button.Button("Bearbeiten",
                e -> UI.getCurrent().navigate("properties/" + propertyId + "/edit"));
        edit.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);

        var header = new com.vaadin.flow.component.orderedlayout.HorizontalLayout(title, edit);
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.expand(title);
        return header;
    }

    private Component buildObjektdatenBody() {
        var box = new com.vaadin.flow.component.orderedlayout.VerticalLayout();
        box.setPadding(false);
        box.setSpacing(false);
        box.getStyle().set("gap", "var(--lumo-space-m)");

        // Kurzer Adressblock
        var addr = String.join(" ",
                nz(property.getStreet()),
                nz(property.getHausnr())).trim();
        var city = (nz(property.getPostleitzahl()) + " " + nz(property.getOrt())).trim();

        var p1 = new Paragraph(addr.isBlank() ? "—" : addr);
        var p2 = new Paragraph(city.isBlank() ? "—" : city);
        p1.getStyle().set("margin", "0");
        p2.getStyle().set("margin", "0");

        // Daten-Tabelle (FormLayout)
        var form = new com.vaadin.flow.component.formlayout.FormLayout();
        form.setResponsiveSteps(
                new com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep("0", 1),
                new com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep("600px", 2)
        );

        form.addFormItem(new com.vaadin.flow.component.html.Span(nzNum(property.getAreaM2(), " m²")),
                "Fläche");
        var df = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String kauf = property.getPurchaseDate() != null ? property.getPurchaseDate().format(df) : "—";
        form.addFormItem(new com.vaadin.flow.component.html.Span(kauf), "Kaufdatum");

        box.add(new H2("Adresse"), p1, p2, new H2("Stammdaten"), form);
        return box;
    }

    private String nz(String s) { return s == null ? "" : s; }
    private String nzNum(Number n, String unit) {
        if (n == null) return "—";
        var dec = java.text.NumberFormat.getNumberInstance(new java.util.Locale("de", "DE"));
        dec.setMaximumFractionDigits(2);
        return dec.format(n) + unit;
    }

}



