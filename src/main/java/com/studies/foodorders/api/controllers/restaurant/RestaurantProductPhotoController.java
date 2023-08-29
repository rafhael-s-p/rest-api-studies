package com.studies.foodorders.api.controllers.restaurant;

import com.studies.foodorders.api.converter.product.ProductPhotoModelConverter;
import com.studies.foodorders.api.model.product.ProductPhotoInput;
import com.studies.foodorders.api.model.product.ProductPhotoModel;
import com.studies.foodorders.domain.models.product.Product;
import com.studies.foodorders.domain.models.product.ProductPhoto;
import com.studies.foodorders.domain.services.product.ProductPhotoService;
import com.studies.foodorders.domain.services.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("/restaurants/{restaurantId}/products/{productId}/photo")
public class RestaurantProductPhotoController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductPhotoService productPhotoService;

    @Autowired
    private ProductPhotoModelConverter converter;

    @GetMapping
    public ProductPhotoModel find(@PathVariable Long restaurantId,
                                   @PathVariable Long productId) {
        ProductPhoto productPhoto = productPhotoService.findIfExists(restaurantId, productId);

        return converter.toModel(productPhoto);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductPhotoModel updatePhoto(@PathVariable Long restaurantId,
                                         @PathVariable Long productId,
                                         @Valid ProductPhotoInput productPhotoInput) throws IOException {

        Product product = productService.findIfExists(restaurantId, productId);

        MultipartFile file = productPhotoInput.getFile();

        ProductPhoto productPhoto = new ProductPhoto();
        productPhoto.setProduct(product);
        productPhoto.setDescription(productPhotoInput.getDescription());
        productPhoto.setContentType(file.getContentType());
        productPhoto.setFileSize(file.getSize());
        productPhoto.setFileName(file.getOriginalFilename());

        ProductPhoto savedProductPhoto = productPhotoService.save(productPhoto, file.getInputStream());

        return converter.toModel(savedProductPhoto);

    }

}
