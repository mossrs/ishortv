package com.mossflower.user_service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mossflower.user_service.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author z's'b
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
