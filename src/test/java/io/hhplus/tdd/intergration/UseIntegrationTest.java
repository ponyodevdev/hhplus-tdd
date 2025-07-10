package io.hhplus.tdd.intergration;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.domain.model.PointHistory;
import io.hhplus.tdd.domain.model.TransactionType;
import io.hhplus.tdd.service.UseService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

class UseIntegrationTest {

    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;
    private UseService usePointService;

    private final long userId = 1L;


    @BeforeEach
    void setUp(){
        userPointTable = new UserPointTable(); // fake
        pointHistoryTable = new PointHistoryTable(); // fake
        usePointService = new UseService(pointHistoryTable, userPointTable);
        userPointTable.insertOrUpdate(userId, 5000L);
        pointHistoryTable.insert(userId, 0, TransactionType.CHARGE, System.currentTimeMillis());
    }

    @DisplayName("포인트 사용이 정상적으로 처리되면 포인트 잔액이 줄고 히스토리에 저장된다.")
    @Test
    void use_success() {
        LocalDateTime now = LocalDateTime.of(2025, 7, 9, 10, 0);
        usePointService.use(userId, 1000, now);

        long updated = userPointTable.selectById(userId).point();
        Assertions.assertThat(updated).isEqualTo(4000);

        List<PointHistory> histories = pointHistoryTable.selectAllByUserId(userId);
        Assertions.assertThat(histories).anyMatch(h ->
                h.amount() == 1000 &&
                        h.type() == TransactionType.USE
        );
    }

    @Test
    @DisplayName("포인트 사용시 포인트 잔액이 부족하면 예외가 발생한다.")
    void use_fail_insufficient() {
        Assertions.assertThatThrownBy(() ->
                usePointService.use(userId, 6000, LocalDateTime.of(2025, 7, 9, 10, 0))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("잔고가 부족합니다.");
    }

}