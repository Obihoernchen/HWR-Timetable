/*******************************************************************************
 * Copyright 2012 Marc Prengemann
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
 *******************************************************************************/
package de.mprengemann.hwr.timetabel.exceptions;

public class IPoolFormatException extends TimetableException {
  private static final long serialVersionUID = 4786981922310199924L;

  public IPoolFormatException() {
    super.errorType = TimetableErrorType.FORMAT;
  }

  public IPoolFormatException(String detailMessage) {
    super(detailMessage);
    super.errorType = TimetableErrorType.FORMAT;
  }

  public IPoolFormatException(Throwable throwable) {
    super(throwable);
    super.errorType = TimetableErrorType.FORMAT;
  }

  public IPoolFormatException(String detailMessage, Throwable throwable) {
    super(detailMessage, throwable);
    super.errorType = TimetableErrorType.FORMAT;
  }

}
