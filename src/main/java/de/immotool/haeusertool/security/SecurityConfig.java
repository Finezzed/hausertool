package de.immotool.haeusertool.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import de.immotool.haeusertool.ui.LoginView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class SecurityConfig extends VaadinWebSecurity {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Vaadin kümmert sich um das Meiste (Static Resources etc.)
        super.configure(http);
        // unsere Login-View
        setLoginView(http, LoginView.class);
    }

    // Zwei Demo-User für den Start (später DB/Rollen)
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        var seb   = User.withUsername("seb").password(encoder.encode("test123")).roles("USER").build();
        var marco = User.withUsername("marco").password(encoder.encode("test123")).roles("USER").build();
        return new InMemoryUserDetailsManager(seb, marco);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

