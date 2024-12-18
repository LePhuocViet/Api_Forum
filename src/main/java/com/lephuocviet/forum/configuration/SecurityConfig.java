package com.lephuocviet.forum.configuration;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@EnableWebSecurity
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig   {

    @Value("${CROSS.URL_LOCAL}")
    String urlLocal;

    @Value("${CROSS.URL_SERVER}")
    String urlServer;

    final JwtDecoderCustom jwtDecoderCustom;
    String GET_ENDPOINT = "*";
    String POST_ENDPOINT = "*";
    String GET_USER_ENDPOINT = "*";
    @Bean
    SecurityFilterChain securityFilterChain (HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeRequests(requests ->
                requests
//                        .requestMatchers(HttpMethod.GET, "/users/my-infor").hasAnyRole("USER", "ADMIN")
//                        .requestMatchers(HttpMethod.GET, "/posts/**").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/comments").hasAnyRole("USER", "MODERATOR")
//                        .requestMatchers(HttpMethod.GET, "/notices/**").hasRole("USER")
//                        .requestMatchers(HttpMethod.GET, "/vnpay/submitOrder").authenticated()
//
//                        // Phân quyền POST
//                        .requestMatchers(HttpMethod.POST, "/auth/**").permitAll() // Auth không cần xác thực
//                        .requestMatchers(HttpMethod.POST, "/mail/**").permitAll()
//                        .requestMatchers(HttpMethod.POST, "/users").permitAll()
//                        .requestMatchers(HttpMethod.POST, "/posts").hasRole("USER")
//                        .requestMatchers(HttpMethod.POST, "/comments").hasRole("USER")
//                        .requestMatchers(HttpMethod.POST, "/likes").hasRole("USER")
//                        .requestMatchers(HttpMethod.POST, "/accounts/check").authenticated()
//
//                        // Phân quyền PUT
//                        .requestMatchers(HttpMethod.PUT, "/users").hasRole("USER")
//                        .requestMatchers(HttpMethod.PUT, "/notices/**").hasRole("ADMIN")
//
//                        // Phân quyền DELETE
//                        .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.DELETE, "/posts/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.DELETE, "/upload/**").hasRole("USER")

                        // Mặc định các endpoint còn lại
                        .anyRequest().permitAll());
        httpSecurity.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer ->
                                jwtConfigurer.decoder(jwtDecoderCustom)
                                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(new JwtEntryPoint()));
        httpSecurity.csrf(http -> http.disable());
        httpSecurity.headers(headers ->
                headers.contentSecurityPolicy(csp ->
                        csp.policyDirectives("frame-ancestors 'self' " + urlLocal + " " + urlServer)
                )
        );
        return httpSecurity.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("ROLE_");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;

    }


    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        // Chỉ định các origin cụ thể, không sử dụng '*' hoặc patterns khi allowCredentials là true
                        .allowedOrigins(urlLocal,urlServer)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Các phương thức HTTP được phép
                        .allowedHeaders("*") // Cho phép tất cả headers
                        .allowCredentials(true); // Cho phép gửi cookie nếu cần
            }
        };
    }

}
