package io.hhplus.tdd.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.domain.model.PointHistory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointHistoryService {
    private final PointHistoryTable pointHistoryTable;


    public PointHistoryService(PointHistoryTable pointHistoryTable) {
        this.pointHistoryTable = pointHistoryTable;
    }

    public List<PointHistory> getHistories(long userId){
        return pointHistoryTable.selectAllByUserId(userId);
    }
}
