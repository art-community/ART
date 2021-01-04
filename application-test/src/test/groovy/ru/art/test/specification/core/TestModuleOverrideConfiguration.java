package ru.art.test.specification.core;

import lombok.*;

@Getter
public class TestModuleOverrideConfiguration extends TestModuleFileConfiguration {
    private String value = OVERRIDE;
}