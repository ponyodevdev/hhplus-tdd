package io.hhplus.tdd.sevice;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.domain.model.TransactionType;
import io.hhplus.tdd.service.UseService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class UseServiceTest {

    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;
    private UseService usePointService;

    @BeforeEach
    void setUp(){
        userPointTable = new UserPointTable();
        pointHistoryTable = new PointHistoryTable();
        usePointService = new UseService(pointHistoryTable, userPointTable);
    }

    @Test
    @DisplayName("존재하지 않는 유저는 포인트를 사용할 수 없기에 예외가 발생한다.")
    void userNotFound() {
        Assertions.assertThatThrownBy(() ->
                        usePointService.use(999L, 1000, LocalDateTime.now())
                ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 유저입니다.");
    }

    @DisplayName("정상 사용 : 포인트를 사용하면 보유 포인트에서 차감된다.")
    @Test
    void successUsePoint(){
        long userId = 1L;
        userPointTable.insertOrUpdate(userId,5000);
        pointHistoryTable.insert(userId,0, TransactionType.CHARGE, System.currentTimeMillis());

        usePointService.use(userId, 1000, LocalDateTime.of(2025,7,9,10,0));

        long updated = userPointTable.selectById(userId).point();
        Assertions.assertThat(updated).isEqualTo(4000);
    }


    @DisplayName("보유하고 있는 포인트보다 많은 포인트를 사용하려 하면 예외가 발생한다.")
    @Test
    void insufficientBalance(){
        long userId = 2L;
        userPointTable.insertOrUpdate(userId,500);
        pointHistoryTable.insert(userId,0,TransactionType.CHARGE, System.currentTimeMillis());

        Assertions.assertThatThrownBy(() ->
                        usePointService.use(userId, 1000, LocalDateTime.now())
                ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("잔고가 부족합니다.");
    }

    @Test
    @DisplayName("100 단위가 아닌 포인트를 사용하면 예외 발생")
    void invalidUnitUse() {
        long userId = 3L;
        userPointTable.insertOrUpdate(userId, 1000);
        pointHistoryTable.insert(userId, 0, TransactionType.CHARGE, System.currentTimeMillis());

        Assertions.assertThatThrownBy(() ->
                        usePointService.use(userId, 150, LocalDateTime.now())
                ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("포인트는 100포인트 단위로만 사용할 수 있습니다.");
    }
}