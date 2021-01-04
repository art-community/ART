package ru.art.test.specification.core;

import lombok.*;
import ru.art.core.module.*;
import static ru.art.core.factory.CollectionsFactory.*;
import java.util.*;

@Getter
public class TestModuleState implements ModuleState {
    public List<String> collection = dynamicArrayOf();

    public void add(String element) {
        collection.add(element);
    }
}