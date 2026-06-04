package com.example.librarybackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.librarybackend.entity.BorrowRecord;
import com.example.librarybackend.entity.ReserveRecord;

import java.util.List;

public interface ReserveService extends IService<ReserveRecord> {
    void reserveBook(Long userId, Long bookId);
    List<ReserveRecord> getMyReservations(Long userId);
    List<ReserveRecord> getReadyReservations(Long userId);
    BorrowRecord claimReservation(Long userId, Long reserveId, Integer duration, String unit);
    void cancelReservation(Long userId, Long reserveId);
    int getWaitingCount(Long bookId);
    void processReturnReservation(Long bookId);
    List<ReserveRecord> getAllReservations();
}
