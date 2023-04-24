package com.mossflower.vod_service.enums;

/**
 * @author z's'b
 * @date 2023/3/5 星期日 23:12
 * @description
 */
public enum CdnSignTypeEnum {
    A("A"),
    B("B"),
    C("C"),
    D("D");

    private final String value;

    CdnSignTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
