package ru.art.rsocket.model;

import io.rsocket.*;
import lombok.*;
import ru.art.entity.Value;

@Getter
@AllArgsConstructor
public class RsocketPayloadValue {
    private final Payload payload;
    private final Value value;
}
