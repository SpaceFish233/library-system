package com.example.librarybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.librarybackend.entity.Book;
import com.example.librarybackend.entity.BorrowRecord;
import com.example.librarybackend.entity.ReserveRecord;
import com.example.librarybackend.entity.User;
import com.example.librarybackend.mapper.BorrowRecordMapper;
import com.example.librarybackend.service.BookService;
import com.example.librarybackend.service.BorrowService;
import com.example.librarybackend.service.ReserveService;
import com.example.librarybackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class BorrowServiceImpl extends ServiceImpl<BorrowRecordMapper, BorrowRecord> implements BorrowService {

    private final BookService bookService;
    private final UserService userService;
    private final ReserveService reserveService;

    @Value("${borrow.credit-threshold}")
    private int creditThreshold;

    @Value("${borrow.borrow-days}")
    private int borrowDays;

    @Value("${borrow.fine-per-day}")
    private double finePerDay;

    @Value("${borrow.credit-deduct-per-day}")
    private int creditDeductPerDay;

    @Override
    @Transactional
    public BorrowRecord borrowBook(Long userId, Long bookId, Integer duration, String unit) {
        // 1. 检查信用分
        User user = userService.getById(userId);
        if (user.getCreditScore() < creditThreshold) {
            throw new RuntimeException("信用分不足" + creditThreshold + "，无法借阅");
        }

        // 2. 检查图书库存
        Book book = bookService.getById(bookId);
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        if (book.getStock() <= 0) {
            throw new RuntimeException("库存不足，可以预约");
        }

        // 3. 检查是否已有未还记录
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowRecord::getUserId, userId)
                .eq(BorrowRecord::getBookId, bookId)
                .in(BorrowRecord::getStatus, "BORROWED", "OVERDUE");
        if (count(wrapper) > 0) {
            throw new RuntimeException("您已借阅此书且未归还");
        }

        // 4. 计算应还日期
        LocalDate dueDate;
        if (duration != null && unit != null) {
            long totalDays;
            switch (unit) {
                case "day":
                    totalDays = duration;
                    break;
                case "week":
                    totalDays = duration * 7L;
                    break;
                case "month":
                    totalDays = duration * 30L;
                    break;
                default:
                    throw new RuntimeException("不支持的借阅单位");
            }
            if (totalDays > 30) {
                throw new RuntimeException("最大借阅时长不能超过30天");
            }
            if (totalDays < 1) {
                throw new RuntimeException("借阅时长不能少于1天");
            }
            dueDate = LocalDate.now().plusDays(totalDays);
        } else {
            dueDate = LocalDate.now().plusDays(borrowDays);
        }

        // 5. 扣减库存
        book.setStock(book.getStock() - 1);
        book.setUpdateTime(LocalDateTime.now());
        bookService.updateById(book);

        // 6. 创建借阅记录
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(dueDate);
        record.setStatus("BORROWED");
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        save(record);

        return record;
    }

    @Override
    @Transactional
    public BorrowRecord borrowForReservation(Long userId, Long bookId, Integer duration, String unit) {
        // 预约领取时的借阅，跳过库存校验（因为已预留）

        // 1. 检查信用分
        User user = userService.getById(userId);
        if (user.getCreditScore() < creditThreshold) {
            throw new RuntimeException("信用分不足" + creditThreshold + "，无法借阅");
        }

        // 2. 扣减库存
        Book book = bookService.getById(bookId);
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        if (book.getStock() <= 0) {
            throw new RuntimeException("库存不足");
        }
        book.setStock(book.getStock() - 1);
        book.setUpdateTime(LocalDateTime.now());
        bookService.updateById(book);

        // 3. 计算应还日期
        LocalDate dueDate;
        if (duration != null && unit != null) {
            long totalDays;
            switch (unit) {
                case "day": totalDays = duration; break;
                case "week": totalDays = duration * 7L; break;
                case "month": totalDays = duration * 30L; break;
                default: throw new RuntimeException("不支持的借阅单位");
            }
            if (totalDays > 30) throw new RuntimeException("最大借阅时长不能超过30天");
            if (totalDays < 1) throw new RuntimeException("借阅时长不能少于1天");
            dueDate = LocalDate.now().plusDays(totalDays);
        } else {
            dueDate = LocalDate.now().plusDays(borrowDays);
        }

        // 4. 创建借阅记录
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(dueDate);
        record.setStatus("BORROWED");
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        save(record);

        return record;
    }

    @Override
    @Transactional
    public void applyReturn(Long userId, Long recordId) {
        BorrowRecord record = getById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new RuntimeException("借阅记录不存在");
        }
        if (!"BORROWED".equals(record.getStatus()) && !"OVERDUE".equals(record.getStatus())) {
            throw new RuntimeException("该记录状态无法申请归还");
        }
        record.setStatus("PENDING_RETURN");
        record.setUpdateTime(LocalDateTime.now());
        updateById(record);
    }

    @Override
    @Transactional
    public Map<String, Object> confirmReturn(Long recordId) {
        // 1. 查找借阅记录
        BorrowRecord record = getById(recordId);
        if (record == null) {
            throw new RuntimeException("借阅记录不存在");
        }
        if (!"PENDING_RETURN".equals(record.getStatus())) {
            throw new RuntimeException("该记录状态无法确认归还");
        }

        Map<String, Object> result = new HashMap<>();
        BigDecimal fine = BigDecimal.ZERO;
        int overdueDays = 0;

        // 2. 判断是否逾期
        LocalDate today = LocalDate.now();
        if (today.isAfter(record.getDueDate())) {
            overdueDays = (int) ChronoUnit.DAYS.between(record.getDueDate(), today);
            if (overdueDays == 0) overdueDays = 1;
            fine = BigDecimal.valueOf(overdueDays * finePerDay);

            // 扣除信用分
            User user = userService.getById(record.getUserId());
            int deductScore = overdueDays * creditDeductPerDay;
            int newScore = Math.max(0, user.getCreditScore() - deductScore);
            user.setCreditScore(newScore);
            user.setUpdateTime(LocalDateTime.now());
            userService.updateById(user);

            result.put("overdueDays", overdueDays);
            result.put("fine", fine);
            result.put("creditDeducted", deductScore);
            result.put("newCreditScore", newScore);
        }

        // 3. 更新借阅记录
        record.setReturnDate(LocalDate.now());
        record.setStatus("RETURNED");
        record.setFineAmount(fine);
        record.setOverdueDays(overdueDays);
        record.setUpdateTime(LocalDateTime.now());
        updateById(record);

        // 4. 增加库存
        Book book = bookService.getById(record.getBookId());
        book.setStock(book.getStock() + 1);
        book.setUpdateTime(LocalDateTime.now());
        bookService.updateById(book);

        // 5. 检查是否有预约排队，将最早的预约状态改为READY
        reserveService.processReturnReservation(record.getBookId());

        result.put("record", record);
        return result;
    }

    @Override
    public BorrowRecord getRecordDetail(Long recordId) {
        BorrowRecord record = getById(recordId);
        if (record == null) {
            throw new RuntimeException("借阅记录不存在");
        }
        return record;
    }

    @Override
    public IPage<BorrowRecord> getMyRecords(Long userId, String status, int page, int size) {
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowRecord::getUserId, userId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(BorrowRecord::getStatus, status);
        }
        wrapper.orderByDesc(BorrowRecord::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public IPage<BorrowRecord> getAllRecords(String username, String bookName, String status, int page, int size) {
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();

        // 按用户名查询：先查出用户ID列表
        if (StringUtils.hasText(username)) {
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.like(User::getUsername, username)
                    .or()
                    .like(User::getNickname, username);
            List<User> users = userService.list(userWrapper);
            if (users.isEmpty()) {
                return new Page<>(page, size); // 没有匹配用户，返回空
            }
            List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
            wrapper.in(BorrowRecord::getUserId, userIds);
        }

        // 按书名查询：先查出图书ID列表
        if (StringUtils.hasText(bookName)) {
            LambdaQueryWrapper<Book> bookWrapper = new LambdaQueryWrapper<>();
            bookWrapper.like(Book::getTitle, bookName);
            List<Book> books = bookService.list(bookWrapper);
            if (books.isEmpty()) {
                return new Page<>(page, size); // 没有匹配图书，返回空
            }
            List<Long> bookIds = books.stream().map(Book::getId).collect(Collectors.toList());
            wrapper.in(BorrowRecord::getBookId, bookIds);
        }

        // 按状态查询
        if (StringUtils.hasText(status)) {
            wrapper.eq(BorrowRecord::getStatus, status);
        }

        wrapper.orderByDesc(BorrowRecord::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        String[] statuses = {"BORROWED", "PENDING_RETURN", "RETURNED", "OVERDUE"};
        for (String s : statuses) {
            LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BorrowRecord::getStatus, s);
            stats.put(s.toLowerCase(), count(wrapper));
        }
        return stats;
    }
}
