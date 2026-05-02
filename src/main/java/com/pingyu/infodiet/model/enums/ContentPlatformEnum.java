package com.pingyu.infodiet.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 内容平台枚举
 */
@Getter
public enum ContentPlatformEnum {

    GITHUB("GitHub", "github"),
    YOUTUBE("YouTube", "youtube"),
    X("X", "x");

    private final String text;

    private final String value;

    ContentPlatformEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static ContentPlatformEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (ContentPlatformEnum anEnum : ContentPlatformEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
