package com.mossflower.user_service.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author z's'b
 */
@Data
public class ResUserInfoVo  implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String nickname;

    private String email;

    private String avatar;

}
