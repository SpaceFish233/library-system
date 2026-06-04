package com.example.librarybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.librarybackend.entity.Book;
import com.example.librarybackend.entity.BorrowRecord;
import com.example.librarybackend.entity.ReserveRecord;
import com.example.librarybackend.entity.User;
import com.example.librarybackend.mapper.ReserveRecordMapper;
import com.example.librarybackend.service.BookService;
import com.example.librarybackend.service.BorrowService;
import com.example.librarybackend.service.NotificationService;
import com.example.librarybackend.service.ReserveService;
import com.example.librarybackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReserveServiceImpl extends ServiceImpl<ReserveRecordMapper, ReserveRecord> implements ReserveService {

    private final BookService bookService;
    private final NotificationService notificationService;
    @Lazy
    @Autowired
    private BorrowService borrowService;
    @Lazy
    @Autowired
    private UserService userService;

    @Override
    public void reserveBook(Long userId, Long bookId) {
        // 检查是否已预约
        LambdaQueryWrapper<ReserveRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReserveRecord::getUserId, userId)
                .eq(ReserveRecord::getBookId, bookId)
                .eq(ReserveRecord::getStatus, "WAITING");
        if (count(wrapper) > 0) {
            throw new RuntimeException("您已预约此书，请等待");
        }

        ReserveRecord record = new ReserveRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setReserveTime(LocalDateTime.now());
        record.setStatus("WAITING");
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        save(record);
    }

    @Override
    public List<ReserveRecord> getMyReservations(Long userId) {
        LambdaQueryWrapper<ReserveRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReserveRecord::getUserId, userId)
                .in(ReserveRecord::getStatus, "WAITING", "READY")
                .orderByDesc(ReserveRecord::getReserveTime);
        List<ReserveRecord> list = list(wrapper);
        for (ReserveRecord record : list) {
            Book book = bookService.getById(record.getBookId());
            if (book != null) {
                record.setBookTitle(book.getTitle());
                record.setStock(book.getStock());
            }
        }
        return list;
    }

    @Override
    public List<ReserveRecord> getReadyReservations(Long userId) {
        LambdaQueryWrapper<ReserveRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReserveRecord::getUserId, userId)
                .eq(ReserveRecord::getStatus, "READY")
                .orderByDesc(ReserveRecord::getReserveTime);
        return list(wrapper);
    }

    @Override
    @Transactional
    public BorrowRecord claimReservation(Long userId, Long reserveId, Integer duration, String unit) {
        ReserveRecord record = getById(reserveId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new RuntimeException("预约记录不存在");
        }
        if (!"READY".equals(record.getStatus())) {
            throw new RuntimeException("该预约状态无法领取");
        }

        // 预约领取时直接完成借阅，跳过库存校验（因为已预留）
        BorrowRecord borrowRecord = borrowService.borrowForReservation(userId, record.getBookId(), duration, unit);

        // 更新预约状态为已领取
        record.setStatus("FULFILLED");
        record.setUpdateTime(LocalDateTime.now());
        updateById(record);

        return borrowRecord;
    }

    @Override
    public void cancelReservation(Long userId, Long reserveId) {
        ReserveRecord record = getById(reserveId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new RuntimeException("预约记录不存在");
        }
        if (!"WAITING".equals(record.getStatus())) {
            throw new RuntimeException("只能取消等待中的预约");
        }
        record.setStatus("CANCELLED");
        record.setUpdateTime(LocalDateTime.now());
        updateById(record);
    }

    @Override
    public int getWaitingCount(Long bookId) {
        LambdaQueryWrapper<ReserveRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReserveRecord::getBookId, bookId)
                .eq(ReserveRecord::getStatus, "WAITING");
        return (int) count(wrapper);
    }

    /**
     * 还书或补货时处理预约：将最早的等待预约改为READY，并发送通知
     */
    @Override
    @Transactional
    public void processReturnReservation(Long bookId) {
        LambdaQueryWrapper<ReserveRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReserveRecord::getBookId, bookId)
                .eq(ReserveRecord::getStatus, "WAITING")
                .orderByAsc(ReserveRecord::getReserveTime)
                .last("LIMIT 1");
        ReserveRecord record = getOne(wrapper);
        if (record != null) {
            record.setStatus("READY");
            record.setUpdateTime(LocalDateTime.now());
            updateById(record);

            // 发送通知
            Book book = bookService.getById(bookId);
            String bookName = book != null ? book.getTitle() : "图书";
            int stock = book != null ? book.getStock() : 0;
            String content = "您预约的《" + bookName + "》目前库存为" + stock + "，请尽快领取";
            notificationService.sendNotification(record.getUserId(), content, "RESERVE_READY", record.getId());
        }
    }

    @Override
    public List<ReserveRecord> getAllReservations() {
        LambdaQueryWrapper<ReserveRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ReserveRecord::getCreateTime);
        return list(wrapper);
    }
}
