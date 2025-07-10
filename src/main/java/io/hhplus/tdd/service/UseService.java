package io.hhplus.tdd.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.domain.use.Use;
import io.hhplus.tdd.domain.model.TransactionType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class UseService {

    private final PointHistoryTable pointHistoryTable;
    private final UserPointTable userPointTable;

    public UseService(PointHistoryTable pointHistoryTable, UserPointTable userPointTable) {
        this.pointHistoryTable = pointHistoryTable;
        this.userPointTable = userPointTable;
    }

    public void use(long userId, long useAmount, LocalDateTime now){

        // 1. 유저 존재 여부를 확인한다.
        boolean exists = !pointHistoryTable.selectAllByUserId(userId).isEmpty();
        if (!exists) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }

        //2. 현재 보유 포인트를 조회한다,
        long currentPoint = userPointTable.selectById(userId).point();

        //3. 도메인 정책 적용
        Use use = new Use();
        use.usePoint(useAmount, currentPoint);

        // 4. 포인트 차감 및 히스토리 적용 (같이 처리해서 비즈니스 트랜잭션의 일관성 유지하기)
        userPointTable.insertOrUpdate(userId, currentPoint - useAmount);
        pointHistoryTable.insert(
                userId,
                useAmount,
                TransactionType.USE,
                now.toInstant(ZoneOffset.ofHours(9)).toEpochMilli()
        );
    }
}
