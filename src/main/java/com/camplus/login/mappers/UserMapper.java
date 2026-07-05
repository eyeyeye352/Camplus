package com.camplus.login.mappers;

import com.camplus.login.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {

    int insertUser(User user);

    User selectByUsername(String username);
    User selectByEmail(String email);

    int updateUsername(@Param("userId") Long userId, @Param("username") String username);
    int updateEmail(@Param("userId") Long userId, @Param("email") String email);
    int updatePassword(@Param("userId") Long userId, @Param("passwordHash") String passwordHash);

    User selectById(@Param("userId") Long userId);

    java.util.List<User> selectByRole(@Param("role") Integer role);

    int updateRole(@Param("userId") Long userId, @Param("role") Integer role);

    int deleteUser(@Param("userId") Long userId);
}
