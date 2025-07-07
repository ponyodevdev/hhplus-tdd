package io.hhplus.tdd.domain.charge;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalTime;


@Getter
public class Charge {

    public static final LocalTime CARD_COMPANY_MAINTENANCE_START_TIME = LocalTime.of(23,50);
    public static final LocalTime CARD_COMPANY_MAINTENANCE_END_TIME = LocalTime.of(0,30);

    private int chargedPoint;

    public void addPoint(int amount) {

        if(amount < 1000){
            throw new IllegalArgumentException("충전 금액은 1,000원 이상이어야 합니다.");
        }
        if(amount > 500000){
            throw new IllegalArgumentException("충전은 최대 500,000원까지 가능합니다.");
        }
        if(amount % 1000 != 0){
            throw new IllegalArgumentException("충전 금액은 천원 단위여야 합니다.");
        }
        if(this.chargedPoint + amount > 1000000){
            throw new IllegalArgumentException("총 보유 포인트는 최대 1,000,000 포인트를 초과할 수 없습니다.");
        }

        this.chargedPoint += amount;
    }

    public void addPoint(LocalDateTime currentDateTime){

        LocalTime currentTime = currentDateTime.toLocalTime();

        boolean isDuringMaintenance;

        if (CARD_COMPANY_MAINTENANCE_START_TIME.isBefore(CARD_COMPANY_MAINTENANCE_END_TIME)) {
            isDuringMaintenance = !currentTime.isBefore(CARD_COMPANY_MAINTENANCE_START_TIME) &&
                    !currentTime.isAfter(CARD_COMPANY_MAINTENANCE_END_TIME);
        } else {
            isDuringMaintenance = !currentTime.isBefore(CARD_COMPANY_MAINTENANCE_START_TIME) ||
                    !currentTime.isAfter(CARD_COMPANY_MAINTENANCE_END_TIME);
        }

        if (isDuringMaintenance) {
            throw new IllegalArgumentException("지금은 카드사 점검 시간입니다. 잠시 후 다시 시도해주세요.");
        }
    }

}
