package io.hhplus.tdd.sevice;


import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.domain.model.PointHistory;
import io.hhplus.tdd.domain.model.TransactionType;
import io.hhplus.tdd.service.ChargeService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ChargeServiceTest {

    private ChargeService chargePointService;
    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;

    @BeforeEach
    void setUp() {
        userPointTable = new UserPointTable();
        pointHistoryTable = new PointHistoryTable();
        chargePointService = new ChargeService(pointHistoryTable, userPointTable);

    }

    private final long userId = 1L;

    // 서비스가 히스토리 기반으로 존재 여부를 판단하기에 기준에 따라 예외가 던져지는지 확인
    @DisplayName("존재하지 않는 유저는 충전할 수 없기에 예외가 발생한다.")
    @Test
    void chargeFailUserNotFound(){
        Assertions.assertThatThrownBy(() -> chargePointService.charge(99L, 10000, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 유저입니다.");
    }


    @DisplayName("정상 충전 : 10,000원을 충전하면 10,000포인트가 적립된다.")
    @Test
    void successCharge(){
        //given
        pointHistoryTable.insert(userId, 0, TransactionType.CHARGE, System.currentTimeMillis());

        //when
        chargePointService.charge(userId, 10000, LocalDateTime.of(2025,7,9,10,0));

        //then
        long result = userPointTable.selectById(userId).point();
        Assertions.assertThat(result).isEqualTo(10000);
    }

    @DisplayName("포인트 충전 시 보유 포인트가 1,000,000 포인트 초과되면 예외가 발생한다.")
    @Test
    void chargePointExceedMax(){
        userPointTable.insertOrUpdate(userId,999000);
        pointHistoryTable.insert(userId,0, TransactionType.CHARGE, System.currentTimeMillis());

        Assertions.assertThatThrownBy(()->
                chargePointService.charge(userId, 2000, LocalDateTime.now())
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("총 보유 포인트는 최대 1,000,000 포인트를 초과할 수 없습니다.");
    }

    @Test
    @DisplayName("카드사 점검시간에는 충전할 수 없다")
    void chargePoint_maintenanceTime() {
        long userId = 1L;
        userPointTable.insertOrUpdate(userId, 0);
        pointHistoryTable.insert(userId, 0, TransactionType.CHARGE, System.currentTimeMillis());

        LocalDateTime maintenanceTime = LocalDateTime.of(2025, 7, 10, 23, 50);

        Assertions.assertThatThrownBy(() ->
                        chargePointService.charge(userId, 1_000, maintenanceTime)
                )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지금은 카드사 점검 시간입니다. 잠시 후 다시 시도해주세요.");
    }

    @DisplayName("사용자가 충전 시 히스토리가 기록된다.")
    @Test
    void chargePoint_historySaved(){
        //given
        userPointTable.insertOrUpdate(userId, 0);
        pointHistoryTable.insert(userId,0, TransactionType.CHARGE,System.currentTimeMillis());

        //when
        LocalDateTime now = LocalDateTime.of(2025,7,9,10,0);
        chargePointService.charge(userId, 5000, now);

        //then
        List<PointHistory> historyList = pointHistoryTable.selectAllByUserId(userId);
        Assertions.assertThat(historyList).anyMatch(h->
                h.amount() == 5000 &&
                        h.type() == TransactionType.CHARGE
        );
    }
}