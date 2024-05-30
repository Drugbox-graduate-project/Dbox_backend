package com.drugbox.common.config;

import com.drugbox.common.jwt.JwtAccessDeniedHandler;
import com.drugbox.common.jwt.JwtAuthenticationEntryPoint;
import com.drugbox.common.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final CorsConfig corsConfig;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // h2 database 테스트가 원활하도록 관련 API 들은 전부 무시
    private static final String[] WHITE_LIST = {
            "/h2-console/**", "/favicon.ico",
            /* -- Swagger UI v2 */
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            /* -- Swagger UI v3 (OpenAPI) */
            "/v3/api-docs/**",
            "/swagger-ui/**",
            /* Auth */
            "/auth/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // CSRF 설정 Disable
            .csrf().disable()
            .sessionManagement() // 시큐리티는 기본적으로 세션을 사용
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 여기서는 세션을 사용하지 않기 때문에 세션 설정을 Stateless 로 설정

            .and()
            // exception handling 할 때 우리가 만든 클래스를 추가
            .exceptionHandling()
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .accessDeniedHandler(jwtAccessDeniedHandler)

            .and()
            .headers()
            .frameOptions()
            .sameOrigin()

            // 로그인, 회원가입 API 는 토큰이 없는 상태에서 요청이 들어오기 때문에 permitAll 설정
            .and()
            .addFilter(corsConfig.corsFilter())
            .authorizeRequests()
            .antMatchers(WHITE_LIST).permitAll()
            .requestMatchers(forPort(9292)).anonymous() // actuator
            .anyRequest().authenticated()   // 나머지 API 는 전부 인증 필요

            // JwtFilter 를 addFilterBefore 로 등록했던 JwtSecurityConfig 클래스를 적용
            .and()
            .apply(new JwtSecurityConfig(tokenProvider));

        return http.build();
    }

    private RequestMatcher forPort(final int port) {
        return (HttpServletRequest request) -> { return port == request.getLocalPort(); };
    }
}
