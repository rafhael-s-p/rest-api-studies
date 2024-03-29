package com.studies.foodorders.domain.services.security;

import com.studies.foodorders.domain.exceptions.BusinessException;
import com.studies.foodorders.domain.exceptions.UserNotFoundException;
import com.studies.foodorders.domain.models.security.Group;
import com.studies.foodorders.domain.models.security.Users;
import com.studies.foodorders.domain.repositories.security.UsersRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsersService {

    private final UsersRepository usersRepository;

    private final GroupService groupService;

    private final PasswordEncoder passwordEncoder;

    public UsersService(UsersRepository usersRepository, GroupService groupService, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.groupService = groupService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Users> list() {
        return usersRepository.findAll();
    }

    public Optional<Users> find(Long id) {
        Optional<Users> user = usersRepository.findById(id);
        return user;
    }

    @Transactional
    public Users save(Users users) {
        usersRepository.detach(users);
        Optional<Users> existingUser = usersRepository.findByEmail(users.getEmail());

        if (existingUser.isPresent() && !existingUser.get().equals(users)) {
            throw new BusinessException(String.format("There is already a user registered with the email %s", users.getEmail()));
        }

        if (users.isNew()) {
            users.setPassword(passwordEncoder.encode(users.getPassword()));
        }

        return usersRepository.save(users);
    }

    @Transactional
    public void updatePassword(Long id, String currentPassword, String newPassword) {
        Users users = findIfExists(id);

        if (!passwordEncoder.matches(currentPassword, users.getPassword())) {
            throw new BusinessException("Current password entered does not match the user's password.");
        }

        users.setPassword(passwordEncoder.encode(newPassword));
    }

    @Transactional
    public void groupAssociate(Long userId, Long groupId) {
        Users users = findIfExists(userId);
        Group group = groupService.findIfExists(groupId);

        users.addGroup(group);
    }

    @Transactional
    public void groupDisassociate(Long userId, Long groupId) {
        Users users = findIfExists(userId);
        Group group = groupService.findIfExists(groupId);

        users.deleteGroup(group);
    }

    public Users findIfExists(Long id) {
        return usersRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

}
