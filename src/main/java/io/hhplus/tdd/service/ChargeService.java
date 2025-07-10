package io.hhplus.tdd.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.domain.charge.Charge;
import io.hhplus.tdd.domain.model.TransactionType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class ChargeService {

    private final PointHistoryTable pointHistoryTable;
    private final UserPointTable userPointTable;

    public ChargeService(PointHistoryTable pointHistoryTable, UserPointTable userPointTable) {
        this.pointHistoryTable = pointHistoryTable;
        this.userPointTable = userPointTable;
    }


    public void charge(Long userId, long chargeAmount, LocalDateTime localDateTime){

        // 1. 유저 존재 여부를 확인한다.
        boolean exists = !pointHistoryTable.selectAllByUserId(userId).isEmpty();
        if (!exists) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }

        //2. 현재 포인트 조회
        long currentTotal = userPointTable.selectById(userId).point();

        //3. 도메인에서 충전 정책 검증 수행 -> 서비스의 책임을 테스트
        Charge charge = new Charge(currentTotal);
        charge.addPoint(chargeAmount);
        charge.validateMaintenanceTime(localDateTime);

        //4. 포인트 충전
        userPointTable.insertOrUpdate(userId, currentTotal + chargeAmount);

        // 5. 포인트의 히스토리를 저장한다.
        pointHistoryTable.insert(userId, chargeAmount, TransactionType.CHARGE, localDateTime.toInstant(ZoneOffset.ofHours(9)).toEpochMilli()); // UTC+9 → 한국 시간 기준

    }

}
