/*
 *    Copyright 2020 ART
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a recursiveCopy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.art.core.converter;

import lombok.experimental.*;
import static io.art.core.checker.EmptinessChecker.*;
import static io.art.core.constants.StringConstants.*;
import static io.art.core.determiner.SystemDeterminer.*;

@UtilityClass
public class WslPathConverter {
    public static String convertToWslPath(String windowsPath) {
        if (!isWindows()) {
            return windowsPath;
        }
        if (isEmpty(windowsPath)) {
            return windowsPath;
        }
        if (SLASH.equals(EMPTY_STRING + windowsPath.charAt(0)) || BACKWARD_SLASH.equals(EMPTY_STRING + windowsPath.charAt(0))) {
            windowsPath = windowsPath.substring(1);
        }
        if (windowsPath.contains(WINDOWS_DISK_PATH_SLASH) || windowsPath.contains(WINDOWS_DISK_PATH_BACKWARD_SLASH)) {
            windowsPath = windowsPath
                    .replaceAll(WINDOWS_DISK_PATH_SLASH, SLASH)
                    .replaceAll(WINDOWS_DISK_PATH_BACKWARD_SLASH_REGEX, SLASH)
                    .replaceAll(BACKWARD_SLASH_REGEX, SLASH);
            String firstLetter = EMPTY_STRING + windowsPath.charAt(0);
            return WSL_DISK_PREFIX + firstLetter.toLowerCase() + windowsPath.substring(1);
        }
        return windowsPath;
    }
}
