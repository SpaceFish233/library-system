package com.example.librarybackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 借阅记录实体
 */
@Data
@TableName("borrow_record")
public class BorrowRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long bookId;
    private LocalDate borrowDate;
    private LocalDateTime dueDate;      // 应还日期
    private LocalDate returnDate;       // 实际归还日期，null表示未还
    private String status;              // BORROWED, RETURNED, OVERDUE
    private BigDecimal fineAmount;      // 罚金
    private Integer overdueDays;        // 逾期天数
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
