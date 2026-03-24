package cn.gp.smartparking.mapper;

import cn.gp.smartparking.model.entity.User;
import cn.gp.smartparking.model.vo.UserVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

/**
* @author HeGuoping
* @description 针对表【user(系统用户表)】的数据库操作Mapper
* @createDate 2026-03-18 22:44:40
* @Entity cn.gp.smartparking.domain.entity.User
*/
public interface UserMapper extends BaseMapper<User> {

    @Insert("insert into user(username, password) values(#{username}, #{password})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int userRegister(User user);

    @Select("select count(*) from user where username = #{username}")
    int selectByUsername(@Param("username") String username);

//    @Select("select id, username, nickname, avatar from user where username = #{username} and password = #{password}")
    UserVO selectByUsernameAndPassword(@Param("username") String username,
                                       @Param("password") String password);
}




