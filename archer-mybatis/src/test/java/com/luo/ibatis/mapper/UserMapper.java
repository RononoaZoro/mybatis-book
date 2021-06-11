package com.luo.ibatis.mapper;

import com.luo.ibatis.entity.UserEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {

    List<UserEntity> listAllUser();

//    @Select("select * from user where id=#{userId,jdbcType=INTEGER}")
    UserEntity getUserById(@Param("userId") String userId);

    List<UserEntity> getUserByEntity(UserEntity user);

    UserEntity getUserByPhone(@Param("phone") String phone);

}