package com.mossflower.vod_service.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.Table;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.sql.Date;

/**
 * @author z's'b
 */
@Data
@Table(name = "t_video", comment = "视频表")
@Validated
public class Video implements Serializable {

    private static final long serialVersionUID = 1L;
    @Column(isKey = true, isAutoIncrement = true, comment = "主键")
    private Long id;

    @Column(comment = "视频标题", length = 16)
    private String title;

    @Column(comment = "视频地址", length = 512)
    private String videoUrl;

    @Column(comment = "视频封面地址", length = 512)
    private String coverUrl;

    @Column(comment = "分类path")
    private String categoryPath;

    @Column(comment = "视频所属用户")
    private Long userId;

    @Column(comment = "是否删除(0删1无)", defaultValue = "0")
    private Boolean isDelete;

    @TableField(fill = FieldFill.INSERT)
    @Column(comment = "创建时间")
    private Date createTime;

    @TableField(fill = FieldFill.UPDATE)
    @Column(comment = "更新时间")
    private Date updateTime;

}
