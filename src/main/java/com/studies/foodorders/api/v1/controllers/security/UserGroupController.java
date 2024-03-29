package com.studies.foodorders.api.v1.controllers.security;

import com.studies.foodorders.api.v1.assemblers.security.GroupModelAssembler;
import com.studies.foodorders.api.v1.links.UserLinks;
import com.studies.foodorders.api.v1.models.security.group.GroupModel;
import com.studies.foodorders.api.v1.openapi.controllers.UserGroupControllerOpenApi;
import com.studies.foodorders.core.security.ApiSecurity;
import com.studies.foodorders.core.security.CheckSecurity;
import com.studies.foodorders.domain.models.security.Users;
import com.studies.foodorders.domain.services.security.UsersService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/v1/users/{userId}/groups")
public class UserGroupController implements UserGroupControllerOpenApi {

    private UsersService usersService;

    private GroupModelAssembler groupModelAssembler;

    private UserLinks userLinks;

    private ApiSecurity apiSecurity;

    public UserGroupController(UsersService usersService, GroupModelAssembler groupModelAssembler, UserLinks userLinks, ApiSecurity apiSecurity) {
        this.usersService = usersService;
        this.groupModelAssembler = groupModelAssembler;
        this.userLinks = userLinks;
        this.apiSecurity = apiSecurity;
    }

    @CheckSecurity.UsersGroupsPermissions.AllowToSearch
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CollectionModel<GroupModel> list(@PathVariable Long userId) {
        Users users = usersService.findIfExists(userId);

        CollectionModel<GroupModel> groupsModel = groupModelAssembler.toCollectionModel(users.getGroups())
                .removeLinks();

        if (apiSecurity.isAllowedToUpdateUsersGroupsPermissions()) {
            groupsModel.add(userLinks.linkToUserGroupAssociate(userId, "associate"));

            groupsModel.getContent().forEach(groupModel -> {
                groupModel.add(userLinks.linkToUserGroupDisassociate(
                        userId, groupModel.getId(), "disassociate"));
            });
        }

        return groupsModel;
    }

    @CheckSecurity.UsersGroupsPermissions.AllowToUpdate
    @PutMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> associate(@PathVariable Long userId, @PathVariable Long groupId) {
        usersService.groupAssociate(userId, groupId);

        return ResponseEntity.noContent().build();
    }

    @CheckSecurity.UsersGroupsPermissions.AllowToUpdate
    @DeleteMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> disassociate(@PathVariable Long userId, @PathVariable Long groupId) {
        usersService.groupDisassociate(userId, groupId);

        return ResponseEntity.noContent().build();
    }

}
