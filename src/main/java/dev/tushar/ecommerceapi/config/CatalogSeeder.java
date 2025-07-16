package dev.tushar.ecommerceapi.config;

import dev.tushar.ecommerceapi.entity.Attribute;
import dev.tushar.ecommerceapi.entity.Category;
import dev.tushar.ecommerceapi.entity.CategoryAttribute;
import dev.tushar.ecommerceapi.entity.OptionSet;
import dev.tushar.ecommerceapi.model.AttributeType;
import dev.tushar.ecommerceapi.repository.AttributeRepository;
import dev.tushar.ecommerceapi.repository.CategoryRepository;
import dev.tushar.ecommerceapi.repository.OptionSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component
@Order(2) // Runs second
@Transactional
@RequiredArgsConstructor
public class CatalogSeeder implements CommandLineRunner {

    private final AttributeRepository attributeRepository;
    private final OptionSetRepository optionSetRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        // --- Create Common Attributes and Option Sets if they don't exist ---
        if (attributeRepository.count() == 0) {
            createCommonAttributesAndOptionSets();
        }

        // --- Create Seed Categories if they don't exist ---
        if (categoryRepository.count() == 0) {
            createSeedCategories();
        }
    }

    private void createSeedCategories() {
        // --- Get Seeded Attributes and Option Sets for Clothing ---
        Attribute colorAttr = attributeRepository.findByName("Color").orElseThrow();
        Attribute materialAttr = attributeRepository.findByName("Material").orElseThrow();
        Attribute sizeAttr = attributeRepository.findByName("Size").orElseThrow();
        Attribute stretchableAttr = attributeRepository.findByName("Stretchable").orElseThrow();
        Attribute styleAttr = attributeRepository.findByName("Style").orElseThrow();
        OptionSet materialOptions = optionSetRepository.findByName("Clothing Materials").orElseThrow();
        OptionSet sizeOptions = optionSetRepository.findByName("Apparel Sizes").orElseThrow();
        OptionSet styleOptions = optionSetRepository.findByName("Clothing Styles").orElseThrow();

        // --- Create "Clothing" Root Category ---
        Category clothing = createCategory("Clothing", 0, null, Set.of(
                CategoryAttribute.builder().attribute(colorAttr).attributeType(AttributeType.TEXT).build(),
                CategoryAttribute.builder().attribute(materialAttr).attributeType(AttributeType.ENUM).optionSet(materialOptions).build(),
                CategoryAttribute.builder().attribute(styleAttr).attributeType(AttributeType.ENUM).optionSet(styleOptions).build()
        ));

        // --- Create "Men's Clothing" Branch ---
        Category mens = createCategory("Men's Clothing", 1, clothing, Set.of());
        createCategory("Men's T-Shirts", 2, mens, Set.of(
                CategoryAttribute.builder().attribute(sizeAttr).attributeType(AttributeType.ENUM).optionSet(sizeOptions).build(),
                CategoryAttribute.builder().attribute(stretchableAttr).attributeType(AttributeType.BOOLEAN).build()
        ));
        createCategory("Men's Shirts", 2, mens, Set.of(
                CategoryAttribute.builder().attribute(sizeAttr).attributeType(AttributeType.ENUM).optionSet(sizeOptions).build()
        ));
        createCategory("Men's Jeans", 2, mens, Set.of(
                CategoryAttribute.builder().attribute(sizeAttr).attributeType(AttributeType.ENUM).optionSet(sizeOptions).build(),
                CategoryAttribute.builder().attribute(stretchableAttr).attributeType(AttributeType.BOOLEAN).build()
        ));
        createCategory("Men's Sweaters", 2, mens, Set.of(
                CategoryAttribute.builder().attribute(sizeAttr).attributeType(AttributeType.ENUM).optionSet(sizeOptions).build()
        ));

        // --- Create "Women's Clothing" Branch ---
        Category womens = createCategory("Women's Clothing", 1, clothing, Set.of());
        createCategory("Women's Tops & T-Shirts", 2, womens, Set.of(
                CategoryAttribute.builder().attribute(sizeAttr).attributeType(AttributeType.ENUM).optionSet(sizeOptions).build(),
                CategoryAttribute.builder().attribute(stretchableAttr).attributeType(AttributeType.BOOLEAN).build()
        ));
        createCategory("Women's Dresses", 2, womens, Set.of(
                CategoryAttribute.builder().attribute(sizeAttr).attributeType(AttributeType.ENUM).optionSet(sizeOptions).build()
        ));
        createCategory("Women's Jeans", 2, womens, Set.of(
                CategoryAttribute.builder().attribute(sizeAttr).attributeType(AttributeType.ENUM).optionSet(sizeOptions).build(),
                CategoryAttribute.builder().attribute(stretchableAttr).attributeType(AttributeType.BOOLEAN).build()
        ));
    }

    private void createCommonAttributesAndOptionSets() {
        // --- Core Attributes ---
        createAttributeIfNotFound("Color");
        createAttributeIfNotFound("Size");
        createAttributeIfNotFound("Material");
        createAttributeIfNotFound("Style");
        createAttributeIfNotFound("Stretchable");

        // --- New, More Detailed Attributes ---
        createAttributeIfNotFound("Sleeve Length");
        createAttributeIfNotFound("Neckline");
        createAttributeIfNotFound("Fit");
        createAttributeIfNotFound("Pattern");
        createAttributeIfNotFound("Occasion");

        // --- Core Option Sets ---
        createOptionSetIfNotFound("Apparel Sizes", List.of("XS", "S", "M", "L", "XL", "XXL"));
        createOptionSetIfNotFound("Clothing Materials", List.of("Cotton", "Polyester", "Wool", "Silk", "Denim", "Linen", "Rayon"));
        createOptionSetIfNotFound("Clothing Styles", List.of("Casual", "Formal", "Sporty", "Party", "Business", "Ethnic"));

        // --- New, More Detailed Option Sets ---
        createOptionSetIfNotFound("Sleeve Lengths", List.of("Full Sleeve", "Half Sleeve", "Sleeveless", "3/4 Sleeve", "Roll-up Sleeve"));
        createOptionSetIfNotFound("Neckline Types", List.of("Round Neck", "V-Neck", "Polo Neck", "Henley Neck", "Turtleneck", "Scoop Neck"));
        createOptionSetIfNotFound("Fit Types", List.of("Slim Fit", "Regular Fit", "Loose Fit", "Skinny Fit", "Tailored Fit"));
        createOptionSetIfNotFound("Pattern Types", List.of("Solid", "Printed", "Striped", "Checked", "Floral", "Polka Dot", "Abstract"));
        createOptionSetIfNotFound("Occasion Types", List.of("Casual Wear", "Formal Wear", "Party Wear", "Festive Wear", "Workwear", "Sportswear"));    }

    private Category createCategory(String name, int level, Category parent, Set<CategoryAttribute> attributes) {
        Category category = Category.builder().name(name).level(level).build();
        attributes.forEach(attr -> attr.setCategory(category));
        category.getCategoryAttributes().addAll(attributes);
        Category savedCategory = categoryRepository.save(category);
        String path = (parent != null) ? parent.getPath() + savedCategory.getId() + "/" : savedCategory.getId() + "/";
        savedCategory.setPath(path);
        return categoryRepository.save(savedCategory);
    }

    private void createAttributeIfNotFound(String name) {
        attributeRepository.findByName(name)
                .orElseGet(() -> attributeRepository.save(Attribute.builder().name(name).build()));
    }

    private void createOptionSetIfNotFound(String name, List<String> options) {
        optionSetRepository.findByName(name)
                .orElseGet(() -> optionSetRepository.save(OptionSet.builder().name(name).options(options).build()));
    }
}