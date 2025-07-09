package io.hhplus.tdd.domain.charge;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

/**
 * 포인트 충전 정책
 *
 * 1. 포인트는 1포인트 = 1원의 가치를 가진다.
 *
 * 2. 충전 정책
 *    - 충전은 1,000원 단위로만 가능하다.
 *    - 1회 충전 가능 금액은 최소 1,000원 이상, 최대 500,000원 이하이다.
 *    - 누적 보유 포인트는 1,000,000 포인트까지 가능하다.
 *    - 카드사 점검 시간(매일 23:50 ~ 00:30)에는 충전이 불가능하다.
 */

public class ChargeTest {

    Charge charge;

    @BeforeEach
    void setUp(){
        charge = new Charge();
    }


    @DisplayName("1,000원을 충전하면 1,000 포인트가 충전된다.")
    @Test
    void successWhenChargingMinBoundaryAmount(){
        charge.addPoint(1000);
        Assertions.assertEquals(1000, charge.getChargedPoint());
    }

    @DisplayName("충전 요청 금액이 1,000원 미만 금액인 경우 예외가 발생한다.")
    @Test
    void throwExceptionWhenLessThanMinAmount(){
        assertChargeThrows(999, "충전 금액은 1,000원 이상이어야 합니다.");
    }

    @DisplayName("충전 요청 금액이 0원인 경우 예외가 발생한다.")
    @Test
    void throwExceptionWhenZeroAmount() {
        assertChargeThrows(0, "충전 금액은 1,000원 이상이어야 합니다.");
    }

    @DisplayName("충전 요청 금액이 음수인 경우 예외가 발생한다.")
    @Test
    void throwExceptionWhenNegativeAmount() {
        assertChargeThrows(-1000, "충전 금액은 1,000원 이상이어야 합니다.");
    }


    @DisplayName("충전 요청 금액이 1,000원 단위가 아닌 경우 예외가 발생한다.")
    @Test
    void chargeUnderOneThousand(){
        assertChargeThrows(1500, "충전 금액은 천원 단위여야 합니다.");

    }

    @DisplayName("충전 요청 금액이 정확히 500,000원이면 충전에 성공한다.")
    @Test
    void chargeMaxAmount() {
        charge.addPoint(500000);
        Assertions.assertEquals(500000, charge.getChargedPoint());
    }


    @DisplayName("충전 요청 금액이 500,000원 초과 금액인 경우 예외가 발생한다.")
    @Test
    void throwExceptionWhenMoreThanFiveHundredThousand(){
        assertChargeThrows(6000000, "충전은 최대 500,000원까지 가능합니다.");
    }


    @DisplayName("기존 보유 포인트와 충전 요청 포인트의 합이 정확히 1,000,000 포인트이면 충전에 성공한다.")
    @Test
    void chargeUntilMaxPoint() {
        charge.addPoint(500000);
        charge.addPoint(500000);
        Assertions.assertEquals(1000000, charge.getChargedPoint());
    }

    @DisplayName("기존 보유 포인트와 충전 요청 포인트의 합이 최대 보유 한도인 1,000,000 포인트를 초과하면 예외가 발생한다.")
    @Test
    void throwExceptionWhenMoreThanOneMillionPoint(){
        charge.addPoint(400000);
        charge.addPoint(400000);

        assertChargeThrows(300000, "총 보유 포인트는 최대 1,000,000 포인트를 초과할 수 없습니다.");


    }

    @DisplayName("카드사 점검시간 (23:50 ~ 00:30)에 충전을 시도하면 예외가 발생한다.")
    @Test
    void cannotChargeDuringMaintenance(){

       IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, ()->{
           charge.validateMaintenanceTime(LocalDateTime.of(2025,7,8,23,50));
       });
        Assertions.assertEquals("지금은 카드사 점검 시간입니다. 잠시 후 다시 시도해주세요.", exception.getMessage());

    }


    private void assertChargeThrows(int amount, String expectedMessage){
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, ()->{
            charge.addPoint(amount);
        });
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }
}
