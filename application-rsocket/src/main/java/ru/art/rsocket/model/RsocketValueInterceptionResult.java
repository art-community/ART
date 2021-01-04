package ru.art.rsocket.model;

import lombok.*;
import ru.art.entity.Value;
import ru.art.rsocket.constants.RsocketModuleConstants.*;

@Getter
@RequiredArgsConstructor(staticName = "intercepted")
@AllArgsConstructor(staticName = "intercepted")
public class RsocketValueInterceptionResult {
    private Value value;
    private final RsocketInterceptedResultAction action;
}