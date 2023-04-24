package com.mossflower.user_service.entity;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.Table;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDateTime;

/**
 * @author z's'b
 */
@Data
@Table(name = "t_user", comment = "用户表")
@Validated
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(comment = "主键", isKey = true, isAutoIncrement = true)
    private Long id;


    @NotBlank(message = "昵称不能为空")
    @Length(min = 6, max = 16, message = "昵称长度必须在4-16之间")
    @Column(comment = "昵称", length = 16)
    private String nickname;


    @NotBlank(message = "用户名不能为空")
    @Length(min = 6, max = 16, message = "用户名长度必须在4-16之间")
    @Column(comment = "用户名", length = 16, isNull = false)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Length(min = 6, max = 16, message = "密码长度必须在6-16之间")
    @Column(comment = "密码", length = 512, isNull = false)
    private String password;

    @Email
    @NotBlank
    @Column(comment = "邮箱")
    private String email;

    @Column(comment = "头像", length = 512)
    private String avatar;

    @Column(comment = "是否是管理员", defaultValue = "0")
    private Boolean isAdmin;

    @Column(comment = "是否删除(0删1无)", defaultValue = "0")
    private Boolean isDelete;

    @TableField(fill = FieldFill.INSERT)
    @Column(comment = "创建时间")
    private Date createTime;

    @TableField(fill = FieldFill.UPDATE)
    @Column(comment = "更新时间")
    private Date updateTime;

}
