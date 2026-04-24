package com.productapi.product;

import com.productapi.auth.dto.AuthResponse;
import com.productapi.auth.dto.LoginUserRequest;
import com.productapi.product.dto.ProductResponse;
import com.productapi.user.UserEntity;
import com.productapi.user.UserRepository;
import com.productapi.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ProductIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        userRepository.deleteAll();

        userRepository.save(UserEntity.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .role(UserRole.ADMIN)
                .build());

        userRepository.save(UserEntity.builder()
                .username("user")
                .password(passwordEncoder.encode("user123"))
                .role(UserRole.USER)
                .build());

        adminToken = login("admin", "admin123");
        userToken = login("user", "user123");
    }

    private String login(String username, String password) {
        AuthResponse response = webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginUserRequest(username, password))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        return response.token();
    }

    private Long createProduct() {
        ProductResponse response = webTestClient.post()
                .uri("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.setBearerAuth(adminToken))
                .bodyValue("""
                        {
                            "name": "Laptop Dell",
                            "description": "Laptop biznesowy",
                            "price": 4500.00,
                            "category": "Elektronika"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        return response.id();
    }

    @Test
    void createProduct_shouldReturn201AndSaveToDatabase_whenAdminRole() {
        // given
        String requestBody = """
                {
                    "name": "Laptop Dell",
                    "description": "Laptop biznesowy",
                    "price": 4500.00,
                    "category": "Elektronika"
                }
                """;

        // when
        ProductResponse response = webTestClient.post()
                .uri("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.setBearerAuth(adminToken))
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductResponse.class)
                .returnResult()
                .getResponseBody();

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("Laptop Dell");
        assertThat(response.price()).isEqualByComparingTo("4500.00");
        assertThat(response.category()).isEqualTo("Elektronika");

        // db
        ProductEntity saved = productRepository.findById(response.id()).orElseThrow();
        assertThat(saved.getName()).isEqualTo("Laptop Dell");
        assertThat(saved.getPrice()).isEqualByComparingTo("4500.00");
        assertThat(saved.getCategory()).isEqualTo("Elektronika");
    }

    @Test
    void getProductById_shouldReturn200AndCorrectData_whenProductExists() {
        // given
        Long id = createProduct();

        // when
        ProductResponse response = webTestClient.get()
                .uri("/products/" + id)
                .headers(h -> h.setBearerAuth(userToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponse.class)
                .returnResult()
                .getResponseBody();

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.name()).isEqualTo("Laptop Dell");

        // db
        assertThat(productRepository.findById(id)).isPresent();
    }

    @Test
    void getProducts_shouldReturn200AndPagedResults_whenProductsExist() {
        // given
        createProduct();

        // when
        String response = webTestClient.get()
                .uri("/products")
                .headers(h -> h.setBearerAuth(userToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        // then
        assertThat(response).isNotNull();
        assertThat(response).contains("Laptop Dell");

        // db
        assertThat(productRepository.count()).isEqualTo(1);
    }

    @Test
    void getProductsByCategory_shouldReturn200AndFilteredResults_whenCategoryExists() {
        // given
        createProduct();

        // when
        ProductResponse[] response = webTestClient.get()
                .uri("/products/category/Elektronika")
                .headers(h -> h.setBearerAuth(userToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponse[].class)
                .returnResult()
                .getResponseBody();

        // then
        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response[0].category()).isEqualTo("Elektronika");

        // db
        assertThat(productRepository.findByCategory("Elektronika")).hasSize(1);
    }

    @Test
    void updateProduct_shouldReturn200AndUpdatedData_whenAdminRole() {
        // given
        Long id = createProduct();
        String requestBody = """
                {
                    "price": 3999.99
                }
                """;

        // when
        ProductResponse response = webTestClient.put()
                .uri("/products/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.setBearerAuth(adminToken))
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponse.class)
                .returnResult()
                .getResponseBody();

        // then
        assertThat(response).isNotNull();
        assertThat(response.price()).isEqualByComparingTo("3999.99");
        assertThat(response.name()).isEqualTo("Laptop Dell");

        // db
        ProductEntity updated = productRepository.findById(id).orElseThrow();
        assertThat(updated.getPrice()).isEqualByComparingTo("3999.99");
        assertThat(updated.getName()).isEqualTo("Laptop Dell");
    }

    @Test
    void deleteProduct_shouldReturn204AndRemoveFromDatabase_whenAdminRole() {
        // given
        Long id = createProduct();

        // when
        webTestClient.delete()
                .uri("/products/" + id)
                .headers(h -> h.setBearerAuth(adminToken))
                .exchange()
                .expectStatus().isNoContent();

        // then
        assertThat(productRepository.findById(id)).isEmpty();
    }

    @Test
    void getProductById_shouldReturn404_whenProductNotExists() {
        // given

        // when
        webTestClient.get()
                .uri("/products/9999")
                .headers(h -> h.setBearerAuth(userToken))
                .exchange()
                .expectStatus().isNotFound();

        // then
        assertThat(productRepository.findById(9999L)).isEmpty();
    }

    @Test
    void createProduct_shouldReturn400_whenInvalidRequest() {
        // given
        String requestBody = """
                {
                    "name": "",
                    "price": -1
                }
                """;
        long countBefore = productRepository.count();

        // when
        webTestClient.post()
                .uri("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.setBearerAuth(adminToken))
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isBadRequest();

        // then
        assertThat(productRepository.count()).isEqualTo(countBefore);
    }

    @Test
    void deleteProduct_shouldReturn404_whenProductNotExists() {
        // given
        long countBefore = productRepository.count();

        // when
        webTestClient.delete()
                .uri("/products/9999")
                .headers(h -> h.setBearerAuth(adminToken))
                .exchange()
                .expectStatus().isNotFound();

        // then
        assertThat(productRepository.count()).isEqualTo(countBefore);
    }
}