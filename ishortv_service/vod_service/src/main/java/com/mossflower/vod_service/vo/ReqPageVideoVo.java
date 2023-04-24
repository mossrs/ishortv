package com.mossflower.vod_service.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author z's'b
 * @date 2023/4/1 星期六 17:22
 * @description
 */
@Data
public class ReqPageVideoVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long page;

    private Long size;

    private List<String> categories;
}
