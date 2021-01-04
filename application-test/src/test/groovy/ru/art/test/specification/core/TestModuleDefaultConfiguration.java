package ru.art.test.specification.core;

import lombok.*;

@Getter
public class TestModuleDefaultConfiguration implements TestModuleConfiguration {
    private String value = DEFAULT;
}