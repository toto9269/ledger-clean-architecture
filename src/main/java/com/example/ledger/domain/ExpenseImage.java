package com.example.ledger.domain;

import java.util.Objects;
import java.util.Set;

/**
 * 지출에 붙이는 이미지 한 장(영수증 사진 등). 허용 형식·크기·장수 규칙을 스스로 지킨다.
 */
public class ExpenseImage {

    public static final int MAX_COUNT = 5;
    public static final int MAX_BYTES = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

    private final String contentType;
    private final byte[] data;

    public ExpenseImage(String contentType, byte[] data) {
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("이미지는 JPG, PNG, GIF, WEBP만 올릴 수 있습니다");
        }
        Objects.requireNonNull(data, "이미지 내용은 필수입니다");
        if (data.length == 0) {
            throw new IllegalArgumentException("빈 이미지는 올릴 수 없습니다");
        }
        if (data.length > MAX_BYTES) {
            throw new IllegalArgumentException("이미지는 한 장에 5MB 이하여야 합니다");
        }
        this.contentType = contentType;
        this.data = data;
    }

    /** 지출 한 건에 붙일 수 있는 장수를 넘으면 거부한다. */
    public static void checkCount(int count) {
        if (count > MAX_COUNT) {
            throw new IllegalArgumentException("이미지는 지출 한 건에 " + MAX_COUNT + "장까지 올릴 수 있습니다");
        }
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getData() {
        return data;
    }
}
