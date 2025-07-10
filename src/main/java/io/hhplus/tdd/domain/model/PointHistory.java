package io.hhplus.tdd.domain.model;

public record PointHistory(
        long id,
        long userId,
        long amount,
        TransactionType type,
        long updateMillis
) {
    public record UserPoint(long id, long point, long updateMillis){
        public static UserPoint empty(long id){
            return new UserPoint(id, 0, System.currentTimeMillis());
        }
    }
}
