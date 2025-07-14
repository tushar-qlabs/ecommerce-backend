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
@Order(3) // Runs third, after security and catalog data is ready
@RequiredArgsConstructor
public class ProductSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final BusinessRepository businessRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Only seed products if the repository is empty to avoid duplicates
        if (productRepository.count() == 0) {
            seedProducts();
        }
    }

    private void seedProducts() {
        // --- Fetch prerequisite data created in other seeders ---
        Business sellerBusiness = businessRepository.findByUserId(2L).orElseThrow(() -> new RuntimeException("Seller's business not found. Ensure SecuritySeeder ran correctly."));

        // Fetch the correct, more specific categories
        Category mensTShirtCategory = categoryRepository.findByName("Men's T-Shirts").orElseThrow(() -> new RuntimeException("Men's T-Shirts category not found."));
        Category mensShirtsCategory = categoryRepository.findByName("Men's Shirts").orElseThrow(() -> new RuntimeException("Men's Shirts category not found."));
        Category mensJeansCategory = categoryRepository.findByName("Men's Jeans").orElseThrow(() -> new RuntimeException("Men's Jeans category not found."));
        Category mensSweatersCategory = categoryRepository.findByName("Men's Sweaters").orElseThrow(() -> new RuntimeException("Men's Sweaters category not found."));

        // --- Product 1: Classic Cotton T-Shirt ---
        createProduct(
                "Classic Cotton T-Shirt",
                "A comfortable and stylish T-Shirt made from 100% premium cotton. Perfect for everyday wear.",
                sellerBusiness, mensTShirtCategory,
                List.of(
                        createVariant(new BigDecimal("499.00"), 100, Map.of("Color", "#000000", "Material", "Cotton", "Size", "M", "Stretchable", true, "Style", "Casual")),
                        createVariant(new BigDecimal("549.00"), 80, Map.of("Color", "#FFFFFF", "Material", "Cotton", "Size", "L", "Stretchable", true, "Style", "Casual"))
                )
        );

        // --- Product 2: Sporty Polo T-Shirt ---
        createProduct(
                "Sporty Polo T-Shirt",
                "A breathable polo t-shirt made with a polyester blend, ideal for sports or casual outings.",
                sellerBusiness, mensTShirtCategory,
                List.of(
                        createVariant(new BigDecimal("799.00"), 120, Map.of("Color", "#0000FF", "Material", "Polyester", "Size", "M", "Stretchable", false, "Style", "Sporty")),
                        createVariant(new BigDecimal("799.00"), 95, Map.of("Color", "#FF0000", "Material", "Polyester", "Size", "L", "Stretchable", false, "Style", "Sporty"))
                )
        );

        // --- Product 3: Slim-Fit Denim Jeans ---
        createProduct(
                "Slim-Fit Denim Jeans",
                "Modern slim-fit jeans made from stretchable denim for maximum comfort and style.",
                sellerBusiness, mensJeansCategory,
                List.of(
                        createVariant(new BigDecimal("1499.00"), 60, Map.of("Color", "#00008B", "Material", "Denim", "Size", "M", "Stretchable", true, "Style", "Casual")),
                        createVariant(new BigDecimal("1499.00"), 50, Map.of("Color", "#00008B", "Material", "Denim", "Size", "L", "Stretchable", true, "Style", "Casual"))
                )
        );

        // --- Product 4: Formal Business Shirt ---
        createProduct(
                "Formal Business Shirt",
                "A classic formal shirt, non-stretch, perfect for the office or business meetings. Made from pure cotton.",
                sellerBusiness, mensShirtsCategory,
                List.of(
                        createVariant(new BigDecimal("1299.00"), 70, Map.of("Color", "#ADD8E6", "Material", "Cotton", "Size", "M", "Style", "Business")),
                        createVariant(new BigDecimal("1349.00"), 45, Map.of("Color", "#FFFFFF", "Material", "Cotton", "Size", "XL", "Style", "Business"))
                )
        );

        // --- Product 5: Woolen Winter Sweater ---
        createProduct(
                "Woolen Winter Sweater",
                "A warm and cozy sweater crafted from the finest wool. A winter wardrobe essential.",
                sellerBusiness, mensSweatersCategory,
                List.of(
                        createVariant(new BigDecimal("1999.00"), 40, Map.of("Color", "#808080", "Material", "Wool", "Size", "L", "Style", "Casual")),
                        createVariant(new BigDecimal("1999.00"), 30, Map.of("Color", "#A52A2A", "Material", "Wool", "Size", "XL", "Style", "Casual"))
                )
        );

        // --- Product 6: Luxury Silk Party Shirt ---
        createProduct(
                "Luxury Silk Party Shirt",
                "An elegant and shiny shirt made from pure silk, perfect for parties and special occasions.",
                sellerBusiness, mensShirtsCategory,
                List.of(
                        createVariant(new BigDecimal("2499.00"), 25, Map.of("Color", "#FFD700", "Material", "Silk", "Size", "M", "Style", "Party")),
                        createVariant(new BigDecimal("2599.00"), 20, Map.of("Color", "#C0C0C0", "Material", "Silk", "Size", "L", "Style", "Party"))
                )
        );

        // --- Product 7: V-Neck T-Shirt ---
        createProduct(
                "V-Neck Cotton T-Shirt",
                "A stylish V-neck t-shirt that offers a different look from the classic round-neck.",
                sellerBusiness, mensTShirtCategory,
                List.of(
                        createVariant(new BigDecimal("599.00"), 110, Map.of("Color", "#006400", "Material", "Cotton", "Size", "S", "Stretchable", true, "Style", "Casual")),
                        createVariant(new BigDecimal("599.00"), 90, Map.of("Color", "#800080", "Material", "Cotton", "Size", "M", "Stretchable", true, "Style", "Casual"))
                )
        );

        // --- Product 8: Ripped Denim Jeans ---
        createProduct(
                "Ripped Denim Jeans",
                "Fashion-forward ripped jeans for a modern, casual style. Made with comfortable stretch denim.",
                sellerBusiness, mensJeansCategory,
                List.of(
                        createVariant(new BigDecimal("1799.00"), 55, Map.of("Color", "#4682B4", "Material", "Denim", "Size", "M", "Stretchable", true, "Style", "Casual")),
                        createVariant(new BigDecimal("1799.00"), 40, Map.of("Color", "#000000", "Material", "Denim", "Size", "L", "Stretchable", true, "Style", "Casual"))
                )
        );

        // --- Product 9: Linen Summer Shirt ---
        createProduct(
                "Linen Summer Shirt",
                "A lightweight and breathable shirt made from linen, perfect for staying cool in the summer heat.",
                sellerBusiness, mensShirtsCategory,
                List.of(
                        createVariant(new BigDecimal("1199.00"), 80, Map.of("Color", "#F5F5DC", "Material", "Linen", "Size", "L", "Style", "Casual")),
                        createVariant(new BigDecimal("1199.00"), 65, Map.of("Color", "#E0FFFF", "Material", "Linen", "Size", "XL", "Style", "Casual"))
                )
        );

        // --- Product 10: Graphic Print T-Shirt ---
        createProduct(
                "Graphic Print T-Shirt",
                "Express yourself with this cool graphic print t-shirt. Comfortable, stylish, and stretchable.",
                sellerBusiness, mensTShirtCategory,
                List.of(
                        createVariant(new BigDecimal("899.00"), 150, Map.of("Color", "#36454F", "Material", "Cotton", "Size", "M", "Stretchable", true, "Style", "Casual")),
                        createVariant(new BigDecimal("949.00"), 130, Map.of("Color", "#FFFFFF", "Material", "Cotton", "Size", "L", "Stretchable", true, "Style", "Casual"))
                )
        );
    }

    /**
     * Helper method to create a Product and its Variants more cleanly.
     */
    private void createProduct(String name, String description, Business business, Category category, List<ProductVariant> variants) {
        Product product = Product.builder()
                .name(name)
                .description(description)
                .business(business)
                .category(category)
                .build();

        // Link each variant back to the main product
        variants.forEach(variant -> variant.setProduct(product));
        product.setVariants(variants);

        productRepository.save(product);
    }

    /**
     * Helper method to build a ProductVariant.
     */
    private ProductVariant createVariant(BigDecimal price, int stock, Map<String, Object> attributes) {
        return ProductVariant.builder()
                .price(price)
                .stockQuantity(stock)
                .attributes(attributes)
                .build();
    }
}