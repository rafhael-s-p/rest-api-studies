package com.studies.foodorders.api.v1.models.kitchen;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Setter
@Getter
public class KitchenIdInput {

    @ApiModelProperty(example = "1", required = true)
    @NotNull
    private Long id;

}
