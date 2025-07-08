package io.hhplus.tdd.domain.usepoint;

import lombok.Getter;

@Getter
public class Use {

    private int usedPoint;

    public void usePoint(int amount, int currentPoint){
        if (amount <= 0) {
            throw new IllegalArgumentException("사용 포인트는 100포인트 이상이어야 합니다.");
        }

        if(amount % 100 != 0){
            throw new IllegalArgumentException("포인트는 100포인트 단위로만 사용할 수 있습니다.");
        }
        if(amount > currentPoint){
            throw new IllegalArgumentException("잔고가 부족합니다.");
        }

        this.usedPoint += amount;
    }

}
