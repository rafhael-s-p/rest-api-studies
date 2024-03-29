package com.studies.foodorders.api.v1.openapi.models;

import com.studies.foodorders.api.v1.models.localization.city.CityModel;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springframework.hateoas.Links;

import java.util.List;

@ApiModel("CitiesModel")
@Data
public class CitiesModelOpenApi {

	private CityEmbeddedModelOpenApi _embedded;
	private Links _links;

	@ApiModel("CitiesEmbeddedModel")
	@Data
	public class CityEmbeddedModelOpenApi {

		private List<CityModel> cities;

	}

}
