package io.hhplus.tdd.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.domain.model.PointHistory;
import io.hhplus.tdd.domain.model.TransactionType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;


@Service
public class PointExpirationService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public PointExpirationService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    /**
     * 유저 목록을 파라미터로 넘기고, 그에 대해서만 소멸 처리
     */
    public void expirePointsForUsers(List<Long> userIds, LocalDateTime now) {
        long nowMillis = toEpochMillis(now);
        long expiredThreshold = toEpochMillis(now.minusYears(1));

        for (Long userId : userIds) {
            List<PointHistory> histories = pointHistoryTable.selectAllByUserId(userId);

            long expiredAmount = histories.stream()
                    .filter(h -> h.type() == TransactionType.CHARGE)
                    .filter(h -> h.updateMillis() < expiredThreshold)
                    .mapToLong(PointHistory::amount)
                    .sum();

            if (expiredAmount <= 0) continue;

            long current = userPointTable.selectById(userId).point();
            long newBalance = Math.max(0, current - expiredAmount);

            userPointTable.insertOrUpdate(userId, newBalance);
            pointHistoryTable.insert(userId, expiredAmount, TransactionType.EXPIRE, nowMillis);
        }
    }

    private long toEpochMillis(LocalDateTime time) {
        return time.toInstant(ZoneOffset.ofHours(9)).toEpochMilli();
    }
}
