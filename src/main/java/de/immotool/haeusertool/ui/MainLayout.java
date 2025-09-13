package de.immotool.haeusertool.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;

import java.util.HashMap;
import java.util.Map;

public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private final Tabs tabs = new Tabs();
    private final Map<String, Tab> pathToTab = new HashMap<>();

    public MainLayout() {
        // wir nutzen nur die Navbar
        setPrimarySection(Section.NAVBAR);

        var title = new H1("Häusertool");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "0");

        // Tabs als "angrenzende Kästen"
        tabs.addThemeVariants(TabsVariant.LUMO_SMALL);
        tabs.setAutoselect(false); // keine Auto-Selektion beim Klick auf Tab selbst

        tabs.add(
                createTab("Objekte", "/properties"),
                createTab("Todos/Aufgaben", "/todos"),
                createTab("Steuer", "/steuer"),
                createTab("Allgemeine Rechnungen", "/allgemeine-rechnungen"),
                createTab("Rechner & Kalkulationen", "/rechner-und-kalkulationen"),
                createTab("Vorlagen", "/vorlagen"),
                createTab("Banken", "/banken"),
                createTab("Allgemeine Infos", "/allgemeine-infos"),
                createTab("Vereine und Firmen", "/vereine-und-firmen"),
                createTab("Behörden", "/behoerden"),
                createTab("IT & Passwörter", "/it-und-passwoerter")

        );

        var logout = new Button("Logout", e ->
                getUI().ifPresent(ui -> ui.getPage().setLocation("/logout")));
        logout.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // Header: Titel links, Tabs direkt daneben, Logout am Rand
        var header = new HorizontalLayout(title, tabs, logout);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.expand(tabs); // Tabs nehmen den Platz zwischen Titel und Logout ein (wirkt "rechts vom Titel")

        addToNavbar(header);
    }

    private Tab createTab(String text, String href) {
        // Anchor nutzt den Vaadin-Router (KEIN router-ignore hier, das sind Vaadin-Routen)
        Anchor a = new Anchor(href, text);
        a.getElement().setAttribute("highlight", "true"); // aktiver Link wird optisch hervorgehoben
        var tab = new Tab(a);
        pathToTab.put(normalize(href), tab);
        return tab;
    }

    private static String normalize(String href) {
        return href.startsWith("/") ? href.substring(1) : href;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        var path = event.getLocation().getPath(); // z. B. "steuer"
        var t = pathToTab.get(path);
        if (t != null) {
            tabs.setSelectedTab(t);
        } else {
            tabs.setSelectedTab(null); // nichts aktiv (z. B. Login)
        }
    }
}



