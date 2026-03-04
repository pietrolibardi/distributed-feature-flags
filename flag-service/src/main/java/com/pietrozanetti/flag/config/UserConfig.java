package com.pietrozanetti.flag.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class UserConfig {

    /**
     * Returns a UserDetails for any username (JWT subject = email).
     * Flag-service has no user DB; the JWT is already validated by the filter.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            UserDetails user = User.builder()
                    .username(username)
                    .password("")
                    .roles("USER")
                    .build();
            return user;
        };
    }
}
