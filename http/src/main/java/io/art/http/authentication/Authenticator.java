/*
 * ART
 *
 * Copyright 2019-2021 ART
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.art.http.authentication;

import java.util.function.*;
import lombok.*;
import static io.art.core.constants.EmptyFunctions.*;
import static io.art.http.constants.HttpModuleConstants.ExceptionMessages.*;

@RequiredArgsConstructor
@Builder
public class Authenticator <T, R>{
    @Builder.Default
    private final Function<T, AuthenticationStatus> authenticationChecker = ignored -> AuthenticationStatus.allow;
    @Builder.Default
    private final UnaryOperator<R> onUnauthorized = emptyUnaryOperator();
    @Builder.Default
    private final UnaryOperator<R> onDeny = emptyUnaryOperator();
    @Builder.Default
    private final UnaryOperator<R> onAllow = emptyUnaryOperator();
    private final ThreadLocal<AuthenticationStatus> status = new ThreadLocal<>();

    public static <M, N> AuthenticatorBuilder<M, N> authenticatorBuilder(){
        return Authenticator.builder();
    }



    public Boolean check(T request){
        status.set(authenticationChecker.apply(request));
        return AuthenticationStatus.allow.equals(status.get());
    }

    public R apply(R response){
        R result;
        switch (status.get()){
            case unauthenticated:
                result = onUnauthorized.apply(response);
                break;
            case allow:
                result = onAllow.apply(response);
                break;
            case deny:
                result = onDeny.apply(response);
                break;
            default:
                throw new IllegalStateException(NULL_AUTHENTICATION_STATUS);
        }
        status.remove();
        return result;
    }

    private R allow(R response){
        return onAllow.apply(response);
    }

    private R deny(R response){
        return onDeny.apply(response);
    }

    private R requestAuthentication(R response) {
        return onUnauthorized.apply(response);
    }

    public enum AuthenticationStatus {
        unauthenticated,
        allow,
        deny
    }
}
