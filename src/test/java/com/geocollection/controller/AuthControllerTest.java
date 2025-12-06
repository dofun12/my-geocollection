package com.geocollection.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geocollection.dto.LoginRequest;
import com.geocollection.dto.RegisterRequest;
import com.geocollection.entity.User;
import com.geocollection.security.JwtUtil;
import com.geocollection.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(com.geocollection.config.TestSecurityConfig.class)
@DisplayName("Auth Controller Tests")
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private UserService userService;

        @MockBean
        private JwtUtil jwtUtil;

        @MockBean
        private AuthenticationManager authenticationManager;

        @MockBean
        private com.geocollection.security.JwtAuthenticationFilter jwtAuthenticationFilter;

        @MockBean
        private com.geocollection.security.CustomUserDetailsService userDetailsService;

        private RegisterRequest validRegisterRequest;
        private LoginRequest validLoginRequest;
        private User validUser;

        @BeforeEach
        void setUp() {
                validRegisterRequest = new RegisterRequest();
                validRegisterRequest.setUsername("testuser");
                validRegisterRequest.setEmail("test@example.com");
                validRegisterRequest.setPassword("Test@123");

                validLoginRequest = new LoginRequest();
                validLoginRequest.setUsernameOrEmail("testuser");
                validLoginRequest.setPassword("Test@123");

                validUser = new User();
                validUser.setId(1L);
                validUser.setUsername("testuser");
                validUser.setEmail("test@example.com");
                validUser.setPassword("encodedPassword");
        }

        @Test
        @DisplayName("POST /api/auth/register - Should register user successfully")
        void testRegister_Success() throws Exception {
                when(userService.registerUser(any(RegisterRequest.class))).thenReturn(validUser);
                when(jwtUtil.generateToken("testuser")).thenReturn("fake-jwt-token");

                mockMvc.perform(post("/api/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                                .andExpect(jsonPath("$.username").value("testuser"));

                verify(userService, times(1)).registerUser(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("POST /api/auth/login - Should login successfully")
        void testLogin_Success() throws Exception {
                when(userService.findByUsernameOrEmail("testuser")).thenReturn(validUser);
                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                                .thenReturn(mock(Authentication.class));
                when(jwtUtil.generateToken("testuser")).thenReturn("fake-jwt-token");

                mockMvc.perform(post("/api/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validLoginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").value("fake-jwt-token"));

                verify(userService, times(1)).findByUsernameOrEmail("testuser");
        }

        @Test
        @DisplayName("POST /api/auth/login - Should return 401 when credentials are invalid")
        void testLogin_InvalidCredentials() throws Exception {
                when(userService.findByUsernameOrEmail("testuser")).thenReturn(validUser);
                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                                .thenThrow(new BadCredentialsException("Invalid credentials"));

                mockMvc.perform(post("/api/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validLoginRequest)))
                                .andExpect(status().isUnauthorized());

                verify(jwtUtil, never()).generateToken(anyString());
        }
}
