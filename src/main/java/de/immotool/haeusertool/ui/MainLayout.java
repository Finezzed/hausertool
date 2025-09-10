package de.immotool.haeusertool.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;

public class MainLayout extends AppLayout {
    public MainLayout() {
        var title = new H1("Häusertool");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "0");

        var logout = new Button("Logout", e -> getUI().ifPresent(ui -> ui.getPage().setLocation("/logout")));

        var header = new HorizontalLayout(title, logout);
        header.setWidthFull();
        header.expand(title);
        header.setAlignItems(HorizontalLayout.Alignment.CENTER);

        addToNavbar(header);

        // Drawer / Seitenmenü
        addToDrawer(new RouterLink("Objekte", PropertyView.class)); // PropertyView kommt im nächsten Schritt
        addToDrawer(new Anchor("/steuer", "Steuer"));
        addToDrawer(new Anchor("/allgemeine-rechnungen", "Allgemeine Rechnungen"));
        addToDrawer(new Anchor("/rechner-und-kalkulationen", "Rechner & Kalkulationen"));
        addToDrawer(new Anchor("/vorlagen", "Vorlagen"));
        addToDrawer(new Anchor("/banken", "Banken"));
        addToDrawer(new Anchor("/allgemeine-infos", "Allgemeine Infos"));
        addToDrawer(new Anchor("/vereine-und-firmen", "Vereine und Firmen"));
        addToDrawer(new Anchor("/behoerden", "Behörden"));
        addToDrawer(new Anchor("/it-und-passwoerter", "IT & Passwörter"));

    }
}

