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

public class TimetableException extends Exception {

  public enum TimetableErrorType {
    GENERAL, CONNECTION, TIMEOUT, FORMAT, STORAGE, UNKNOWN_TIMETABLE, AUTHENTIFICATON
  }

  protected TimetableErrorType errorType = TimetableErrorType.GENERAL;

  private static final long serialVersionUID = -4169428160136560220L;

  public TimetableException() {
  }

  public TimetableException(String detailMessage) {
    super(detailMessage);
  }

  public TimetableException(Throwable throwable) {
    super(throwable);
  }

  public TimetableException(String detailMessage, Throwable throwable) {
    super(detailMessage, throwable);
  }

  public TimetableErrorType getType() {
    return this.errorType;
  }

}
