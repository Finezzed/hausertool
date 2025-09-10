package de.immotool.haeusertool.ui;

import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Login")
@AnonymousAllowed   // darf man ohne Anmeldung sehen
public class LoginView extends VerticalLayout {
    public LoginView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        var form = new LoginForm();
        form.setAction("login"); // Spring Security Default-Endpoint

        add(form);
    }
}

