package io.hhplus.tdd.sevice;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.domain.model.PointHistory;
import io.hhplus.tdd.domain.model.TransactionType;
import io.hhplus.tdd.domain.model.UserPoint;
import io.hhplus.tdd.service.PointExpirationService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;


class PointExpirationServiceTest {

    private PointHistoryTable pointHistoryTable;
    private UserPointTable userPointTable;
    private PointExpirationService pointExpirationService;

    @BeforeEach
    void setUp() {
        pointHistoryTable = new PointHistoryTable();
        userPointTable = new UserPointTable();
        pointExpirationService = new PointExpirationService(userPointTable, pointHistoryTable);
    }

    @DisplayName("1년이 지난 포인트는 소멸된다.")
    @Test
    void expireOldPoints() {
        // given
        long userId = 1L;
        LocalDateTime now = LocalDateTime.of(2025, 7, 9, 12, 0);

        long oldMillis = now.minusYears(1).minusDays(1).toInstant(ZoneOffset.ofHours(9)).toEpochMilli();
        pointHistoryTable.insert(userId, 5000, TransactionType.CHARGE, oldMillis);

        long recentMillis = now.minusMonths(6).toInstant(ZoneOffset.ofHours(9)).toEpochMilli();
        pointHistoryTable.insert(userId, 2000, TransactionType.CHARGE, recentMillis);

        userPointTable.insertOrUpdate(userId, 7000);

        // when
        pointExpirationService.expirePointsForUsers(List.of(userId), now);

        // then
        UserPoint updated = userPointTable.selectById(userId);
        Assertions.assertThat(updated.point()).isEqualTo(2000);

        List<PointHistory> historyList = pointHistoryTable.selectAllByUserId(userId);
        Assertions.assertThat(historyList).anyMatch(h ->
                h.type() == TransactionType.EXPIRE &&
                        h.amount() == 5000
        );
    }
    @DisplayName("1년이 지나지 않은 포인트는 소멸되지 않는다.")
    @Test
    void recentPointsRemain() {
        // given
        long userId = 2L;
        LocalDateTime now = LocalDateTime.of(2025, 7, 9, 12, 0);

        long millis = now.minusMonths(3).toInstant(ZoneOffset.ofHours(9)).toEpochMilli();
        pointHistoryTable.insert(userId, 3000, TransactionType.CHARGE, millis);
        userPointTable.insertOrUpdate(userId, 3000);

        // when
        pointExpirationService.expirePointsForUsers(List.of(userId), now);

        // then
        UserPoint updated = userPointTable.selectById(userId);
        Assertions.assertThat(updated.point()).isEqualTo(3000);

        List<PointHistory> historyList = pointHistoryTable.selectAllByUserId(userId);
        Assertions.assertThat(historyList).noneMatch(h -> h.type() == TransactionType.EXPIRE);
    }
}