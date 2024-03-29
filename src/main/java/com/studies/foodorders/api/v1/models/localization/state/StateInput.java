package com.studies.foodorders.api.v1.models.localization.state;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class StateInput {

    @ApiModelProperty(example = "California")
    @NotBlank
    private String name;

}
