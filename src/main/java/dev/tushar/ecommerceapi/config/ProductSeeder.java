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
        Business sellerOneBusiness = businessRepository.findByUserId(2L).orElseThrow(() -> new RuntimeException("Seller 1 business not found."));
        Business sellerTwoBusiness = businessRepository.findByUserId(3L).orElseThrow(() -> new RuntimeException("Seller 2 business not found."));

        // Fetch clothing categories
        Category mensTShirtCategory = categoryRepository.findByName("Men's T-Shirts").orElseThrow();
        Category mensJeansCategory = categoryRepository.findByName("Men's Jeans").orElseThrow();

        // --- Create a new category for electronics for Seller 2 ---
        Category electronicsCategory = createCategoryIfNotExists("Electronics", 0, null);
        Category phonesCategory = createCategoryIfNotExists("Smartphones", 1, electronicsCategory);
        Category laptopsCategory = createCategoryIfNotExists("Laptops", 1, electronicsCategory);


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

        // --- Products for Seller 2: Digital Haven ---
        createProduct(
                "Pixel Pro 10",
                "The latest flagship smartphone with a stunning display and pro-grade camera system.",
                sellerTwoBusiness, phonesCategory,
                List.of(
                        createVariant(new BigDecimal("79999.00"), 50, Map.of("Color", "#E0E0E0", "Storage", "128GB")),
                        createVariant(new BigDecimal("89999.00"), 30, Map.of("Color", "#333333", "Storage", "256GB"))
                )
        );

        createProduct(
                "UltraBook X1",
                "A lightweight and powerful laptop for professionals on the go. Features a 14-inch display.",
                sellerTwoBusiness, laptopsCategory,
                List.of(
                        createVariant(new BigDecimal("95000.00"), 25, Map.of("RAM", "16GB", "Storage", "512GB SSD")),
                        createVariant(new BigDecimal("115000.00"), 15, Map.of("RAM", "32GB", "Storage", "1TB SSD"))
                )
        );

        createProduct(
                "Gaming Beast G9",
                "Dominate the competition with this high-performance gaming laptop.",
                sellerTwoBusiness, laptopsCategory,
                List.of(
                        createVariant(new BigDecimal("145000.00"), 20, Map.of("RAM", "16GB", "GPU", "RTX 4070"))
                )
        );
    }

    // Helper to create categories if they don't exist
    private Category createCategoryIfNotExists(String name, int level, Category parent) {
        return categoryRepository.findByName(name).orElseGet(() -> {
            Category category = Category.builder().name(name).level(level).build();
            Category savedCategory = categoryRepository.save(category);
            String path = (parent != null) ? parent.getPath() + savedCategory.getId() + "/" : savedCategory.getId() + "/";
            savedCategory.setPath(path);
            return categoryRepository.save(savedCategory);
        });
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