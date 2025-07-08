package io.hhplus.tdd.domain.usepoint;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 사용 정책
 *  - 포인트는 100포인트 단위로만 사용 가능하다.
 *  - 잔고가 부족한 경우 포인트 사용은 실패한다.
 *
 */

class UseTest {

    Use use;

    @BeforeEach
    void setUp(){
        use = new Use();
    }


    @DisplayName("100포인트를 사용하면 정상적으로 사용된다.")
    @Test
    void useValidPoint(){
        use.usePoint(100, 1000);
        Assertions.assertEquals(100, use.getUsedPoint());

    }

    @DisplayName("포인트 사용시 100포인트 단위가 아닌 경우 예외가 발생한다.")
    @Test
    void invalidUnitPoint(){
        assertChargeThrows(99, "포인트는 100포인트 단위로만 사용할 수 있습니다.");
    }

    @DisplayName("잔고를 초과한 포인트를 사용하려고 하면 예외가 발생한다.")
    @Test
    void insufficientBalance(){
        assertChargeThrows(1100, "잔고가 부족합니다.");
    }

    @DisplayName("0 포인트를 사용하려고 하면 예외가 발생한다.")
    @Test
    void useZeroPoint(){
        assertChargeThrows(0, "사용 포인트는 100포인트 이상이어야 합니다.");
    }

    @DisplayName("0 미만의 포인트를 사용하려고 하면 예외가 발생한다.")
    @Test
    void useNegativePoint(){
        assertChargeThrows(-100, "사용 포인트는 100포인트 이상이어야 합니다.");
    }


    private void assertChargeThrows(int amount, String expectedMessage){
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, ()->{
            use.usePoint(amount, 1000);
        });
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }



}