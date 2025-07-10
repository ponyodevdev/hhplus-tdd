package io.hhplus.tdd.sevice;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.domain.model.PointHistory;
import io.hhplus.tdd.domain.model.TransactionType;
import io.hhplus.tdd.service.PointHistoryService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;


class PointHistoryServiceTest {

    @DisplayName("유저의 포인트 히스토리를 모두 조회한다.")
    @Test
    void getAllPointHistories(){
        //given
        long userId = 1L;
        PointHistoryTable pointHistoryTable = new PointHistoryTable();
        PointHistoryService pointHistoryService = new PointHistoryService(pointHistoryTable);

        long now = System.currentTimeMillis();
        pointHistoryTable.insert(userId, 1000, TransactionType.CHARGE, now);
        pointHistoryTable.insert(userId, 500, TransactionType.USE, now);
        pointHistoryTable.insert(userId, 300, TransactionType.EXPIRE, now);


        //when
        List<PointHistory> histories = pointHistoryService.getHistories(userId);

        //then
        Assertions.assertThat(histories).hasSize(3);
        Assertions.assertThat(histories)
                .extracting(PointHistory::type)
                .containsExactlyInAnyOrder(TransactionType.CHARGE, TransactionType.USE, TransactionType.EXPIRE);

    }

}