package com.example.librarybackend.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.librarybackend.entity.BorrowRecord;
import com.example.librarybackend.service.BorrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OverdueScheduler {

    private final BorrowService borrowService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void checkOverdue() {
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowRecord::getStatus, "BORROWED")
                .le(BorrowRecord::getDueDate, LocalDate.now());
        List<BorrowRecord> overdueList = borrowService.list(wrapper);
        for (BorrowRecord record : overdueList) {
            record.setStatus("OVERDUE");
            record.setUpdateTime(LocalDateTime.now());
            borrowService.updateById(record);
            log.info("借阅记录 {} 已标记为逾期", record.getId());
        }
    }
}
