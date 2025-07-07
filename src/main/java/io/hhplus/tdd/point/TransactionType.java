package io.hhplus.tdd.point;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 포인트 트랜잭션 종류
 * - CHARGE : 충전
 * - USE : 사용
 */
@Getter
@RequiredArgsConstructor
public enum TransactionType {
    CHARGE("충전"),
    USE("사용"),
    EXPIRE("소멸");

    private final String text;


}