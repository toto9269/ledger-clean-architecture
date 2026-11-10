package com.example.ledger.dto.expense;

/** 이미지 한 장의 형식과 내용. 올릴 때와 내려받을 때 모두 쓴다. */
public record ImageFile(String contentType, byte[] data) {
}
