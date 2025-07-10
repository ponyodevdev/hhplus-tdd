package io.hhplus.tdd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.domain.model.PointHistory;
import io.hhplus.tdd.domain.model.TransactionType;
import io.hhplus.tdd.domain.model.UserPoint;
import io.hhplus.tdd.service.ChargeService;
import io.hhplus.tdd.service.UseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ChargeService chargePointService;

    @MockBean
    UseService usePointService;

    @MockBean
    UserPointTable userPointTable;

    @MockBean
    PointHistoryTable pointHistoryTable;

    private final ObjectMapper objectMapper =new ObjectMapper();

    @DisplayName("GET /point/{id} - 유저 포인트 조회")
    @Test
    void getUserPoint() throws Exception {
        long userId = 1L;
        UserPoint userPoint = new UserPoint(userId, 10000, System.currentTimeMillis());

        given(userPointTable.selectById(userId)).willReturn(userPoint);

        // when & then
        mockMvc.perform(get("/point/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(10000));
    }

    @DisplayName("GET /point/{id}/histories - 유저 포인트 히스토리 조회")
    @Test
    void getPointHistories() throws Exception{
        //given
        long userId = 1L;
        List<PointHistory> histories = List.of(
                new PointHistory(1L, userId, 1000, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2L, userId, 500,TransactionType.USE, System.currentTimeMillis())
        );

        given(pointHistoryTable.selectAllByUserId(userId)).willReturn(histories);

        mockMvc.perform(get("/point/{id}/histories", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));

    }

    @DisplayName("PATCH /point/{id}/charge - 유저 포인트 충전")
    @Test
    void chargePoint() throws Exception {
        //given
        long userId = 1L;
        long amount = 5000;
        UserPoint result = new UserPoint(userId, 5000, System.currentTimeMillis());

        doNothing().when(chargePointService).charge(eq(userId), eq(amount), any());

        given(userPointTable.selectById(userId)).willReturn(result);

        // when & then
        mockMvc.perform(patch("/point/{id}/charge",userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(amount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(5000));
    }

    @DisplayName("PATCH /point/{id}/use - 유저 포인트 사용")
    @Test
    void usePoint() throws Exception{
        //given
        long userId = 1L;
        long amount = 1000;
        UserPoint result = new UserPoint(userId, 4000, System.currentTimeMillis());

        doNothing().when(usePointService).use(eq(userId), eq(amount), any());

        given(userPointTable.selectById(userId)).willReturn(result);

        //when& then
        mockMvc.perform(patch("/point/{id}/use", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(amount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.point").value(4000));
    }







}