package com.example.ledger.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** 오늘 날짜 계산에 쓰는 Clock. 테스트에서 고정된 시계로 바꿔 끼울 수 있다. */
@Configuration
public class ClockConfig {

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
