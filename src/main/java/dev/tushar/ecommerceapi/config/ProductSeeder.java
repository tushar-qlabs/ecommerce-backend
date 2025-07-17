package dev.tushar.ecommerceapi.config;

import dev.tushar.ecommerceapi.entity.Business;
import dev.tushar.ecommerceapi.entity.Category;
import dev.tushar.ecommerceapi.entity.Product;
import dev.tushar.ecommerceapi.entity.ProductVariant;
import dev.tushar.ecommerceapi.repository.BusinessRepository;
import dev.tushar.ecommerceapi.repository.CategoryRepository;
import dev.tushar.ecommerceapi.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@Order(3) // Runs third
@Transactional
@RequiredArgsConstructor
public class ProductSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final BusinessRepository businessRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        if (productRepository.count() == 0) {
            seedProducts();
        }
    }

    private void seedProducts() {
        // --- Fetch prerequisite data created in other seeders ---
        Business sellerOneBusiness = businessRepository.findByUserId(2L).orElseThrow(() -> new RuntimeException("Business not found."));
        Business sellerTwoBusiness = businessRepository.findByUserId(3L).orElseThrow(() -> new RuntimeException("Business not found."));

        // Fetch clothing categories
        Category mensTShirtCategory = categoryRepository.findByName("Men's T-Shirts").orElseThrow();
        Category mensJeansCategory = categoryRepository.findByName("Men's Jeans").orElseThrow();
        Category womensTopsCategory = categoryRepository.findByName("Women's Tops & T-Shirts").orElseThrow();
        Category womensDressesCategory = categoryRepository.findByName("Women's Dresses").orElseThrow();


        // --- Products for Seller 1: Fashion Fusion ---
        createProduct(
                "Classic Cotton T-Shirt",
                "A comfortable and stylish T-Shirt made from 100% premium cotton.",
                sellerOneBusiness, mensTShirtCategory,
                List.of(
                        createVariant(new BigDecimal("499.00"), 100, Map.of("Color", "#000000", "Material", "Cotton", "Size", "M", "Stretchable", true, "Style", "Casual")),
                        createVariant(new BigDecimal("549.00"), 80, Map.of("Color", "#FFFFFF", "Material", "Cotton", "Size", "L", "Stretchable", true, "Style", "Casual"))
                )
        );

        createProduct(
                "Slim-Fit Denim Jeans",
                "Modern slim-fit jeans made from stretchable denim for maximum comfort.",
                sellerOneBusiness, mensJeansCategory,
                List.of(
                        createVariant(new BigDecimal("1499.00"), 60, Map.of("Color", "#00008B", "Material", "Denim", "Size", "M", "Stretchable", true, "Style", "Casual"))
                )
        );

        // --- Products for Seller 2: Urban Weave ---
        createProduct(
                "Women's Floral Print Top",
                "A vibrant and stylish top with a beautiful floral print, perfect for a sunny day out.",
                sellerTwoBusiness, womensTopsCategory,
                List.of(
                        createVariant(new BigDecimal("799.00"), 50, Map.of("Color", "#FFC0CB", "Material", "Polyester", "Size", "S", "Stretchable", false, "Style", "Casual")),
                        createVariant(new BigDecimal("849.00"), 30, Map.of("Color", "#ADD8E6", "Material", "Polyester", "Size", "M", "Stretchable", false, "Style", "Casual"))
                )
        );

        createProduct(
                "Elegant Evening Dress",
                "A stunning and elegant dress perfect for evening parties and formal events.",
                sellerTwoBusiness, womensDressesCategory,
                List.of(
                        createVariant(new BigDecimal("2499.00"), 25, Map.of("Color", "#000000", "Material", "Silk", "Size", "S", "Stretchable", false, "Style", "Party")),
                        createVariant(new BigDecimal("2599.00"), 15, Map.of("Color", "#800000", "Material", "Silk", "Size", "M", "Stretchable", false, "Style", "Party"))
                )
        );
    }

    private void createProduct(String name, String description, Business business, Category category, List<ProductVariant> variants) {
        Product product = Product.builder()
                .name(name)
                .description(description)
                .business(business)
                .category(category)
                .build();

        variants.forEach(variant -> variant.setProduct(product));
        product.setVariants(variants);

        if (!variants.isEmpty()) {
            product.setPrimaryVariant(variants.get(0));
        }

        productRepository.save(product);
    }

    private ProductVariant createVariant(BigDecimal price, int stock, Map<String, Object> attributes) {
        return ProductVariant.builder()
                .price(price)
                .stockQuantity(stock)
                .attributes(attributes)
                .build();
    }
}