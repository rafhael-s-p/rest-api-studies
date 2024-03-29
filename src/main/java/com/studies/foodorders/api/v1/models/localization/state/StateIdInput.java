package com.studies.foodorders.api.v1.models.localization.state;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Setter
@Getter
public class StateIdInput {

    @ApiModelProperty(example = "1", required = true)
    @NotNull
    private Long id;

}
