package com.studies.foodorders.api.v1.models.localization.city;

import com.studies.foodorders.api.v1.models.localization.state.StateIdInput;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Setter
@Getter
public class CityInput {

    @ApiModelProperty(example = "San Francisco", required = true)
    @NotBlank
    private String name;

    @Valid
    @NotNull
    private StateIdInput state;

}
