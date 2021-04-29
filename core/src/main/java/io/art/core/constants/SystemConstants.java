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

package io.art.core.constants;

public interface SystemConstants {
    String WSL = "wsl";
    String BASH = "bash";
    int PROCESS_ERROR_CODE_OK = 0;
    int PROCESS_ERROR_CODE_FAIL = -1;
    String BASH_FIRST_ARGUMENT = "$1";
    String BASH_ALL_ARGUMENTS = "$@";
}
