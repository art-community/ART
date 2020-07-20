/*
 * ART
 *
 * Copyright 2020 ART
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

package io.art.server.service.validation;

import static io.art.server.constants.ServerModuleConstants.ExceptionsMessages.NOT_BETWEEN_VALIDATION_ERROR;
import static io.art.server.constants.ServerModuleConstants.ValidationExpressionType.*;
import static java.text.MessageFormat.*;

class BetweenLongValidationExpression extends ValidationExpression<Long> {
    private final long lowerValue;
    private final long greaterValue;

    BetweenLongValidationExpression(long lowerValue, long greaterValue) {
        super(BETWEEN_LONG);
        this.lowerValue = lowerValue;
        this.greaterValue = greaterValue;
    }

    BetweenLongValidationExpression(long lowerValue, long greaterValue, String pattern) {
        super(BETWEEN_LONG);
        this.lowerValue = lowerValue;
        this.greaterValue = greaterValue;
        this.pattern = pattern;
    }

    @Override
    public boolean evaluate(String fieldName, Long value) {
        return super.evaluate(fieldName, value) && value > lowerValue && value < greaterValue;
    }

    @Override
    public String getValidationErrorMessage() {
        return format(NOT_BETWEEN_VALIDATION_ERROR, fieldName, value, lowerValue, greaterValue);
    }
}
