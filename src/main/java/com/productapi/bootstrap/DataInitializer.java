package com.productapi.bootstrap;

import com.productapi.product.ProductEntity;
import com.productapi.product.ProductRepository;
import com.productapi.user.UserEntity;
import com.productapi.user.UserRepository;
import com.productapi.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        seedUsers();
        seedProducts();
    }

    private void seedUsers() {
        createUserIfNotExists("admin", "admin123", UserRole.ADMIN);
        createUserIfNotExists("user", "user123", UserRole.USER);
    }

    private void createUserIfNotExists(String username, String password, UserRole role) {
        if (userRepository.findByUsername(username).isPresent()) {
            log.debug("User '{}' already exists, skipping", username);
            return;
        }

        UserEntity user = UserEntity.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();

        userRepository.save(user);
        log.info("Created default {} user: '{}'", role, username);
    }

    private void seedProducts() {
        if (productRepository.count() > 0) {
            log.debug("Products already exist, skipping seed");
            return;
        }

        List<ProductEntity> defaultProducts = List.of(
                ProductEntity.builder()
                        .name("Laptop Dell XPS 15")
                        .description("Laptop biznesowy z ekranem 4K")
                        .category("Electronics")
                        .price(new BigDecimal("4500.00"))
                        .build(),
                ProductEntity.builder()
                        .name("Logitech MX Master 3")
                        .description("Bezprzewodowa mysz ergonomiczna")
                        .category("Electronics")
                        .price(new BigDecimal("450.00"))
                        .build(),
                ProductEntity.builder()
                        .name("Effective Java")
                        .description("Klasyczna książka Joshua Blocha o dobrych praktykach w Javie")
                        .category("Books")
                        .price(new BigDecimal("159.00"))
                        .build()
        );

        productRepository.saveAll(defaultProducts);
        log.info("Seeded {} default products", defaultProducts.size());
    }
}
