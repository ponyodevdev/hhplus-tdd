package io.hhplus.tdd.intergration;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.domain.model.PointHistory;
import io.hhplus.tdd.domain.model.TransactionType;
import io.hhplus.tdd.service.ChargeService;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

@DisplayName("포인트 충전 통합 테스트")
class ChargeIntegrationTest {


    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;
    private ChargeService chargePointService;

    private final long userId = 1L;


    @BeforeEach
    void setUp(){
        userPointTable = new UserPointTable(); // fake
        pointHistoryTable = new PointHistoryTable(); // fake
        chargePointService = new ChargeService(pointHistoryTable, userPointTable);
        userPointTable.insertOrUpdate(userId, 0L);
        pointHistoryTable.insert(userId,0, TransactionType.CHARGE, System.currentTimeMillis());
    }

    @DisplayName("정상 충전 시 포인트 적립 및 히스토리 저장이 완료된다.")
    @Test
    void chargeSuccess(){
        //when
        LocalDateTime now = LocalDateTime.of(2025,7,9,10,0);
        chargePointService.charge(userId,5000,now);

        //then
        long updatePoint = userPointTable.selectById(userId).point();
        Assertions.assertThat(updatePoint).isEqualTo(5000);

        List<PointHistory> histories = pointHistoryTable.selectAllByUserId(userId);
        Assertions.assertThat(histories).anyMatch(h ->
                h.amount() == 5000 &&
                h.type() == TransactionType.CHARGE
        );
    }

    @DisplayName("카드사 점검 시간(23:50 ~ 00:30)에 포인트 충전 시도 시 예외가 발생한다.")
    @Test
    void chargeFailMaintenanceTime(){
        //when
        LocalDateTime maintenanceTime = LocalDateTime.of(2025,7,10,23,50);

        //then
        AssertionsForClassTypes.assertThatThrownBy(() ->
                chargePointService.charge(userId, 1000, maintenanceTime)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지금은 카드사 점검 시간입니다. 잠시 후 다시 시도해주세요.");
    }

}