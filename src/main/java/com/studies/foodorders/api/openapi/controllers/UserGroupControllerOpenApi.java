package com.studies.foodorders.api.openapi.controllers;

import com.studies.foodorders.api.exceptionhandler.ApiError;
import com.studies.foodorders.api.model.security.group.GroupModel;
import io.swagger.annotations.*;

import java.util.List;

@Api(tags = "Users")
public interface UserGroupControllerOpenApi {

    @ApiOperation("List the groups associated with a user")
    @ApiResponses({
            @ApiResponse(code = 404, message = "User not found", response = ApiError.class)
    })
    List<GroupModel> list(@ApiParam(value = "User id", example = "1", required = true) Long userId);

    @ApiOperation("Association with user group")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Association done successfully"),
            @ApiResponse(code = 404, message = "User or group not found", response = ApiError.class)
    })
    void associate(@ApiParam(value = "User id", example = "1", required = true) Long userId,
                   @ApiParam(value = "Group id", example = "1", required = true) Long groupId);

    @ApiOperation("Disassociation with user group")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Disassociation done successfully"),
            @ApiResponse(code = 404, message = "User or group not found", response = ApiError.class)
    })
    void desassociar(@ApiParam(value = "User id", example = "1", required = true) Long userId,
                     @ApiParam(value = "Group id", example = "1", required = true) Long groupId);

}