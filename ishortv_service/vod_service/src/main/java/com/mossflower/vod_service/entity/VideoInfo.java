package com.mossflower.vod_service.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.Table;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.sql.Date;

/**
 * @author z's'b
 * @date 2023/3/2 星期四 20:18
 * @description
 */
@Table(name = "t_video_info", comment = "视频信息表")
@Data
@Validated
public class VideoInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(isKey = true, isAutoIncrement = true, comment = "主键")
    private Long id;

    @Column(comment = "时长", length = 16)
    private String duration;

    @Column(comment = "宽度")
    private Integer width;

    @Column(comment = "高度")
    private Integer height;

    @Column(comment = "帧率")
    private Float frameRate;

    @Column(comment = "码率")
    private Integer bitRate;

    @Column(comment = "编码格式")
    private String decoder;

    @Column(comment = "格式")
    private String format;

    @Column(comment = "视频主体id")
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
