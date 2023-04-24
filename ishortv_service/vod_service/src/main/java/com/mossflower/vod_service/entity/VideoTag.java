package com.mossflower.vod_service.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.sql.Date;

/**
 * @author z's'b
 */
@Data
@Table(name = "t_video_tag", comment = "视频标签表")
public class VideoTag implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(isKey = true, isAutoIncrement = true, comment = "主键")
    private Long id;

    @Column(comment = "视频标签名称", length = 16)
    private String name;

    @Column(comment = "视频id")
    private Long videoId;

    @Column(comment = "是否删除(0删1无)", defaultValue = "0")
    private Boolean isDelete;

    @TableField(fill = FieldFill.INSERT)
    @Column(comment = "创建时间")
    private Date createTime;

    @TableField(fill = FieldFill.UPDATE)
    @Column(comment = "更新时间")
    private Date updateTime;

}
