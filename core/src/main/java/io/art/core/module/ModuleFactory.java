package io.art.core.module;

import java.util.function.*;

public interface ModuleFactory<T extends Module<?, ?>> extends Supplier<T> {
}
