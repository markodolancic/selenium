// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements. See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership. The SFC licenses this file
// to you under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#ifndef WEBDRIVER_IE_SENDKEYSCOMMANDHANDLER_H_
#define WEBDRIVER_IE_SENDKEYSCOMMANDHANDLER_H_

#include "../IECommandHandler.h"

namespace webdriver {

class SendKeysCommandHandler : public IECommandHandler {
 public:
  struct FileNameData {
    HWND main;
    HWND hwnd;
    DWORD ieProcId;
    DWORD dialogTimeout;
    const wchar_t* text;
  };

  SendKeysCommandHandler(void);
  virtual ~SendKeysCommandHandler(void);

 protected:
  void ExecuteInternal(const IECommandExecutor& executor,
                       const ParametersMap& command_parameters,
                       Response* response);
 private:
  static unsigned int WINAPI SetFileValue(void *file_data);
  static bool SendKeysToFileUploadAlert(HWND dialog_window_handle,
                                        const wchar_t* value);

  bool VerifyPageHasFocus(HWND top_level_window_handle,
                          HWND browser_pane_window_handle);
  bool WaitUntilElementFocused(IHTMLElement *element);
  bool SetInsertionPoint(IHTMLElement* element);
};

} // namespace webdriver

#endif // WEBDRIVER_IE_SENDKEYSCOMMANDHANDLER_H_
