package com.example.librarybackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.librarybackend.entity.BorrowRecord;

import java.util.Map;

public interface BorrowService extends IService<BorrowRecord> {
    BorrowRecord borrowBook(Long userId, Long bookId, Integer duration, String unit);
    BorrowRecord borrowForReservation(Long userId, Long bookId, Integer duration, String unit);
    void applyReturn(Long userId, Long recordId);
    Map<String, Object> confirmReturn(Long recordId);
    BorrowRecord getRecordDetail(Long recordId);
    IPage<BorrowRecord> getMyRecords(Long userId, String status, int page, int size);
    IPage<BorrowRecord> getAllRecords(String username, String bookName, String status, int page, int size);
}
