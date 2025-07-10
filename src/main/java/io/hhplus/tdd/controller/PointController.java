package io.hhplus.tdd.controller;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.domain.model.PointHistory;
import io.hhplus.tdd.domain.model.UserPoint;
import io.hhplus.tdd.service.ChargeService;
import io.hhplus.tdd.service.UseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/point")
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    private final ChargeService chargePointService;
    private final UseService usePointService;
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public PointController(ChargeService chargePointService, UseService usePointService, UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.chargePointService = chargePointService;
        this.usePointService = usePointService;
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }


    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능
     */
    @GetMapping("{id}")
    public UserPoint point(@PathVariable long id) {
        log.info("포인트 조회 요청 - userId: {}", id);
        UserPoint point = userPointTable.selectById(id);
        log.info("포인트 조회 완료 - userId: {}, point {}", id, point.point());
        return point;
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능
     */
    @GetMapping("{id}/histories")
    public List<PointHistory> history(@PathVariable long id) {
       log.info("포인트 히스토리 조회 요청 - userId: {}",id);
       List<PointHistory> histories = pointHistoryTable.selectAllByUserId(id);
       log.info("포인트 히스토리 {}건 조회 - userId: {}", histories.size(), id);
       return histories;
    }

    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능
     */
    @PatchMapping("{id}/charge")
    public UserPoint charge(@PathVariable long id, @RequestBody long amount) {
        log.info("포인트 충전 요청 - userId: {}, amount: {}",id, amount);
        try{
            chargePointService.charge(id, amount, LocalDateTime.now());
            UserPoint result = userPointTable.selectById(id);
            log.info("포인트 충전 완료 - userId: {}, 잔액: {}", id, result.point());
            return result;
        }catch (Exception e){
            log.error("포인트 충전 실패 - userId: {}, reason: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능
     */
    @PatchMapping("{id}/use")
    public UserPoint use(@PathVariable long id, @RequestBody long amount) {
        log.info("포인트 사용 요청 - userId: {}, amount: {}", id, amount);
        try{
            usePointService.use(id, amount, LocalDateTime.now());
            UserPoint result = userPointTable.selectById(id);
            log.info("포인트 사용 완료 - userId: {}, 잔액: {}", id, result.point());
            return result;
        }catch (Exception e){
            log.error("포인트 사용 실패 - userId:{}, reason: {}",id,e.getMessage());
            throw e;
        }
    }
}
