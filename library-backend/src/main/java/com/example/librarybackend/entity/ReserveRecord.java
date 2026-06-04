package com.example.librarybackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预约记录实体
 */
@Data
@TableName("reserve_record")
public class ReserveRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long bookId;
    private LocalDateTime reserveTime;
    private String status;      // WAITING, READY, CANCELLED, FULFILLED
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String bookTitle;

    @TableField(exist = false)
    private Integer stock;
}
