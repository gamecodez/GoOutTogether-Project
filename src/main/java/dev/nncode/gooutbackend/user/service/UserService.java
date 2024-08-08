package dev.nncode.gooutbackend.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import dev.nncode.gooutbackend.user.dto.UserCreationDto;
import dev.nncode.gooutbackend.user.dto.UserInfoDto;
import dev.nncode.gooutbackend.user.dto.UserUpdateDto;
import dev.nncode.gooutbackend.user.model.User;

public interface UserService {
    User getUserById(int id);

    UserInfoDto getUserDtoById(int id);

    UserInfoDto createUser(UserCreationDto body);

    UserInfoDto updateUser(int id, UserUpdateDto body);

    boolean deleteUserById(int id);

    Page<User> findUserByFirstName(String keyword, Pageable pageable);

}
