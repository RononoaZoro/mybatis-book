package com.luo.example.mapper;


import com.luo.example.entity.UserEntity;
import com.luo.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {

    List<UserEntity> listAllUser();

//    @Select("select * from user where id=#{userId,jdbcType=INTEGER}")
//    UserEntity getUserById(@Param("userId") String userId);

    List<UserEntity> getUserByEntity(UserEntity user);

    UserEntity getUserByPhone(@Param("phone") String phone);

}