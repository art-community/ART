package ru.art.test.specification.core;

import lombok.*;
import static ru.art.config.extensions.ConfigExtensions.configString;

@Getter
public class TestModuleFileConfiguration implements TestModuleConfiguration {
    private String value = configString("test", "value");
}