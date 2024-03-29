package com.studies.foodorders.api.v1.openapi.models;

import com.studies.foodorders.api.v1.models.product.ProductModel;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springframework.hateoas.Links;

import java.util.List;

@ApiModel("ProductsModel")
@Data
public class ProductsModelOpenApi {

	private ProductsEmbeddedModelOpenApi _embedded;
	private Links _links;

	@ApiModel("ProductsEmbeddedModel")
	@Data
	public class ProductsEmbeddedModelOpenApi {

		private List<ProductModel> products;

	}

}
