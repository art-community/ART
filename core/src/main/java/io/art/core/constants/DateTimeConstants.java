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

package io.art.core.constants;

import static java.time.format.DateTimeFormatter.*;
import java.time.format.*;

public interface DateTimeConstants {
    String YYYY_MM_DD_HH_MM_SS_24H_DASH = "yyyy-MM-dd HH:mm:ss";
    DateTimeFormatter YYYY_MM_DD_HH_MM_SS_24H_DASH_FORMAT = ofPattern(YYYY_MM_DD_HH_MM_SS_24H_DASH);

    String YYYY_MM_DD_T_HH_MM_SS_24H_DASH = "yyyy-MM-dd'T'HH:mm:ss";
    DateTimeFormatter YYYY_MM_DD_T_HH_MM_SS_24H_DASH_FORMAT = ofPattern(YYYY_MM_DD_T_HH_MM_SS_24H_DASH);

    String YYYY_MM_DD_T_HH_MM_SS_24H_Z_DASH = "yyyy-MM-dd'T'HH:mm:ssZ";
    DateTimeFormatter YYYY_MM_DD_T_HH_MM_SS_24H_Z_DASH_FORMAT = ofPattern(YYYY_MM_DD_T_HH_MM_SS_24H_Z_DASH);

    String YYYY_MM_DD_T_HH_MM_SS_24H_SSS_DASH = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    DateTimeFormatter YYYY_MM_DD_T_HH_MM_SS_24H_SSS_DASH_FORMAT = ofPattern(YYYY_MM_DD_T_HH_MM_SS_24H_SSS_DASH);

    String YYYY_MM_DD_T_HH_MM_SS_24H_SSS_Z_DASH = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    DateTimeFormatter YYYY_MM_DD_T_HH_MM_SS_24H_SSS_Z_DASH_FORMAT = ofPattern(YYYY_MM_DD_T_HH_MM_SS_24H_SSS_Z_DASH);

    String YYYY_MM_DD_T_HH_MM_SS_SSSXXX = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    DateTimeFormatter YYYY_MM_DD_T_HH_MM_SS_SSSXXX_FORMAT = ofPattern(YYYY_MM_DD_T_HH_MM_SS_SSSXXX);

    String YYYY_MM_DD_DASH = "yyyy-MM-dd";
    DateTimeFormatter YYYY_MM_DD_DASH_FORMAT = ofPattern(YYYY_MM_DD_DASH);

    String YYYY_MM_DD_HH_MM_SS_24H_Z_DOT = "yyyy.MM.dd HH:mm:ss Z";
    DateTimeFormatter YYYY_MM_DD_HH_MM_SS_24H_Z_DOT_FORMAT = ofPattern(YYYY_MM_DD_HH_MM_SS_24H_Z_DOT);

    String DD_MM_YYYY_HH_MM_24H_DOT = "dd.MM.yyyy HH:mm";
    DateTimeFormatter DD_MM_YYYY_HH_MM_24H_DOT_FORMAT = ofPattern(DD_MM_YYYY_HH_MM_24H_DOT);

    String DD_MM_YYYY_HH_MM_SS_DOT = "dd.MM.yyyy hh:mm:ss";
    DateTimeFormatter DD_MM_YYYY_HH_MM_SS_DOT_FORMAT = ofPattern(DD_MM_YYYY_HH_MM_SS_DOT);

    String DD_MM_YYYY_HH_MM_SS_24H_DOT = "dd.MM.yyyy HH:mm:ss";
    DateTimeFormatter DD_MM_YYYY_HH_MM_SS_24H_DOT_FORMAT = ofPattern(DD_MM_YYYY_HH_MM_SS_24H_DOT);

    String DD_MM_YYYY_HH_MM_SS_24H_SLASH = "dd/MM/yyyy HH:mm:ss";
    DateTimeFormatter DD_MM_YYYY_HH_MM_SS_24H_SLASH_FORMAT = ofPattern(DD_MM_YYYY_HH_MM_SS_24H_SLASH);

    String DD_MM_YYYY_HH_MM_SS_24H_DASH = "dd-MM-yyyy HH:mm:ss";
    DateTimeFormatter DD_MM_YYYY_HH_MM_SS_24H_DASH_FORMAT = ofPattern(DD_MM_YYYY_HH_MM_SS_24H_DASH);

    String DD_MM_YYYY_HH_MM_SS_24H_SSS_DASH = "dd-MM-yyyy HH:mm:ss.SSS";
    DateTimeFormatter DD_MM_YYYY_HH_MM_SS_24H_SSS_DASH_FORMAT = ofPattern(DD_MM_YYYY_HH_MM_SS_24H_SSS_DASH);

    String YYYYMM = "yyyyMM";
    DateTimeFormatter YYYYMM_FORMAT = ofPattern(YYYYMM);

    String YYYY = "yyyy";
    DateTimeFormatter YYYY_FORMAT = ofPattern(YYYY);

    String DD_MM_DOT = "dd.MM";
    DateTimeFormatter DD_MM_DOT_FORMAT = ofPattern(DD_MM_DOT);

    String DD_MM_YYYY_DOT = "dd.MM.yyyy";
    DateTimeFormatter DD_MM_YYYY_DOT_FORMAT = ofPattern(DD_MM_YYYY_DOT);

    String DD_MM_YYYY_EEEE_DOT = "dd.MM.yyyy EEEE";
    DateTimeFormatter DD_MM_YYYY_EEEE_DOT_FORMAT = ofPattern(DD_MM_YYYY_EEEE_DOT);

    String DD_MM_YYYY_DASH = "dd-MM-yyyy";
    DateTimeFormatter DD_MM_YYYY_DASH_FORMAT = ofPattern(DD_MM_YYYY_DASH);

    String DD_MM_DASH = "dd-MM";
    DateTimeFormatter DD_MM_DASH_FORMAT = ofPattern(DD_MM_DASH);

    String HH_MM_SS_24H = "HH:mm:ss";
    DateTimeFormatter HH_MM_SS_24H_FORMAT = ofPattern(HH_MM_SS_24H);

    String HH_MM_24H = "HH:mm";
    DateTimeFormatter HH_MM_24H_FORMAT = ofPattern(HH_MM_24H);

    String HH = "HH";
    DateTimeFormatter HH_FORMAT = ofPattern(HH);

    String MMMMM_YYYY = "MMMMM yyyy";
    DateTimeFormatter MMMMM_YYYY_FORMAT = ofPattern(MMMMM_YYYY);

    String EEE_MMM_D_HH_MM_SS_Z_YYYY = "EEE MMM d HH:mm:ss z yyyy";
    DateTimeFormatter EEE_MMM_D_HH_MM_SS_Z_YYYY_FORMAT = ofPattern(EEE_MMM_D_HH_MM_SS_Z_YYYY);

    int MONTHS_IN_YEAR = 12;
    int MONTHS_IN_QUARTER = 3;
    int QUARTERS_COUNT = 4;
    String TIME_ZONE_GMT = "GMT";
}
