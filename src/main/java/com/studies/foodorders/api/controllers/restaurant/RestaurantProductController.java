package com.studies.foodorders.api.controllers.restaurant;

import com.studies.foodorders.api.converter.product.ProductModelConverter;
import com.studies.foodorders.api.model.product.ProductInput;
import com.studies.foodorders.api.model.product.ProductModel;
import com.studies.foodorders.api.openapi.controllers.RestaurantProductControllerOpenApi;
import com.studies.foodorders.domain.models.product.Product;
import com.studies.foodorders.domain.models.restaurant.Restaurant;
import com.studies.foodorders.domain.repositories.product.ProductRepository;
import com.studies.foodorders.domain.services.product.ProductService;
import com.studies.foodorders.domain.services.restaurant.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/restaurants/{restaurantId}/products")
public class RestaurantProductController implements RestaurantProductControllerOpenApi {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private ProductModelConverter productModelConverter;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductModel> list(@PathVariable Long restaurantId,
                                   @RequestParam(required = false) boolean includeInactives) {
        Restaurant restaurant = restaurantService.findIfExists(restaurantId);

        if (includeInactives) {
            return productModelConverter.toCollectionModel(productRepository.findAllByRestaurant(restaurant));
        }

        return productModelConverter.toCollectionModel(productRepository.findActivesByRestaurant(restaurant));
    }

    @GetMapping(path = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductModel find(@PathVariable Long restaurantId, @PathVariable Long productId) {
        Product product = productService.findIfExists(restaurantId, productId);

        return productModelConverter.toModel(product);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ProductModel save(@PathVariable Long restaurantId,
                                  @RequestBody @Valid ProductInput productInput) {
        Restaurant restaurant = restaurantService.findIfExists(restaurantId);

        Product product = productModelConverter.toDomainObject(productInput);
        product.setRestaurant(restaurant);

        product = productService.save(product);

        return productModelConverter.toModel(product);
    }

    @PutMapping(path = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductModel update(@PathVariable Long restaurantId, @PathVariable Long productId,
                                  @RequestBody @Valid ProductInput productInput) {
        Product currentProduct = productService.findIfExists(restaurantId, productId);

        productModelConverter.copyToDomainObject(productInput, currentProduct);

        currentProduct = productService.save(currentProduct);

        return productModelConverter.toModel(currentProduct);
    }

}
