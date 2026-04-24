package com.productapi.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getProducts_shouldReturn401_whenAnonymousUser() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProductById_shouldReturn401_whenAnonymousUser() throws Exception {
        mockMvc.perform(get("/products/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createProduct_shouldReturn401_whenAnonymousUser() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Laptop",
                                    "description": "Opis",
                                    "price": 1999.99,
                                    "category": "Elektronika"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateProduct_shouldReturn401_whenAnonymousUser() throws Exception {
        mockMvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Nowa nazwa"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteProduct_shouldReturn401_whenAnonymousUser() throws Exception {
        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getProducts_shouldReturn200_whenUserRole() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getProductById_shouldReturn200_whenUserRole() throws Exception {
        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createProduct_shouldReturn403_whenUserRole() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Laptop",
                                    "description": "Opis",
                                    "price": 1999.99,
                                    "category": "Elektronika"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateProduct_shouldReturn403_whenUserRole() throws Exception {
        mockMvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Nowa nazwa"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteProduct_shouldReturn403_whenUserRole() throws Exception {
        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getProducts_shouldReturn200_whenAdminRole() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getProductById_shouldReturn200_whenAdminRole() throws Exception {
        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_shouldReturn201_whenAdminRole() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Laptop",
                                    "description": "Opis",
                                    "price": 1999.99,
                                    "category": "Elektronika"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProduct_shouldReturn200_whenAdminRole() throws Exception {
        mockMvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Nowa nazwa"
                            }
                            """))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_shouldReturn204_whenAdminRole() throws Exception {
        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isNoContent());
    }
}