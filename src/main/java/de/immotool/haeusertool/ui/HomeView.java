package de.immotool.haeusertool.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("home") // Startseite
@PageTitle("Haeusertool")
public class HomeView extends VerticalLayout {

    public HomeView() {
        setSpacing(true);
        setPadding(true);

        Button helloButton = new Button("Klick mich!",
                e -> Notification.show("Häusertool läuft 🚀"));

        add(helloButton);
    }
}
