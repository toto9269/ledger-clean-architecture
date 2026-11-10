package com.example.ledger.application.port.in;

import java.util.Optional;

/** 지출 이미지 한 장 조회 유스케이스 */
public interface GetExpenseImageUseCase {

    Optional<Image> get(long imageId);

    record Image(String contentType, byte[] data) {
    }
}
