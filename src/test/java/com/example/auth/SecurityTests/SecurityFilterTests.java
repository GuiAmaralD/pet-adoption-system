package com.example.auth.SecurityTests;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.auth.infra.security.SecurityFilter;
import com.example.auth.infra.security.TokenService;
import com.example.auth.user.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Security Filter Tests")
class SecurityFilterTests {

    @Mock
    private HandlerExceptionResolver handlerExceptionResolver;
    @Mock
    private TokenService tokenService;
    @Mock
    private UserDetailsServiceImpl userDetailsService;
    @Mock
    private FilterChain filterChain;
    @InjectMocks
    private SecurityFilter securityFilter;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("doFilterInternal should pass through when no Authorization header")
    void doFilterInternal_shouldPassThrough_whenNoAuthorizationHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        securityFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal should resolve exception when token is invalid")
    void doFilterInternal_shouldResolveException_whenTokenIsInvalid() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer bad-token");

        when(tokenService.validateToken("bad-token"))
                .thenThrow(new JWTVerificationException("invalid"));

        securityFilter.doFilter(request, response, filterChain);

        verify(handlerExceptionResolver).resolveException(eq(request), eq(response), isNull(), any(JWTVerificationException.class));
        verify(filterChain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal should set authentication when token is valid")
    void doFilterInternal_shouldSetAuthentication_whenTokenIsValid() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer good-token");

        when(tokenService.validateToken("good-token")).thenReturn(true);
        when(tokenService.getUsernameFromToken("good-token")).thenReturn("user@test.com");
        UserDetails userDetails = User.withUsername("user@test.com").password("pw").authorities("ROLE_USER").build();
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);

        securityFilter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user@test.com", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }
}
