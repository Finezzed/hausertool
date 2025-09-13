package de.immotool.haeusertool.ui;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "", layout = MainLayout.class)  // Startseite nach Login
@PageTitle("Home")
@PermitAll
public class HomeDashboardView extends VerticalLayout {
    public HomeDashboardView() {
        setSizeFull();
        // bewusst leer/weiß lassen – später befüllen
    }
}
