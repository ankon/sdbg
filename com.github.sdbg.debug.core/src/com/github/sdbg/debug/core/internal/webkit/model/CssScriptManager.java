/*
 * Copyright (c) 2012, the Dart project authors.
 * 
 * Licensed under the Eclipse Public License v1.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.github.sdbg.debug.core.internal.webkit.model;

import com.github.sdbg.debug.core.SDBGDebugCorePlugin;
import com.github.sdbg.debug.core.internal.util.ResourceChangeManager;
import com.github.sdbg.debug.core.internal.util.ResourceChangeParticipant;
import com.github.sdbg.debug.core.internal.webkit.protocol.WebkitCallback;
import com.github.sdbg.debug.core.internal.webkit.protocol.WebkitResult;
import com.github.sdbg.debug.core.internal.webkit.protocol.WebkitStyleSheetRef;
import com.github.sdbg.utilities.IFileUtilities;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manage known css scripts loaded in the target browser. Listen for resource change events (using
 * the ResourceChangeManager class). When a css scripts changes on disk that the browser knows
 * about, send the new contents to the browser using the Webkit inspector protocol.
 */
class CssScriptManager implements ResourceChangeParticipant {
  private WebkitDebugTarget target;

  private List<WebkitStyleSheetRef> styleSheets = Collections.synchronizedList(new ArrayList<WebkitStyleSheetRef>());

  public CssScriptManager(WebkitDebugTarget target) {
    this.target = target;

    ResourceChangeManager.getManager().addChangeParticipant(this);
  }

  public void dispose() {
    ResourceChangeManager.removeChangeParticipant(this);
  }

  @Override
  public void handleFileAdded(IFile file) {
    handleFileChanged(file);
  }

  @Override
  public void handleFileChanged(IFile file) {
    if ("css".equals(file.getFileExtension())) {
      String fileUrl = target.getResourceResolver().getUrlForResource(file);

      if (fileUrl != null) {
        for (WebkitStyleSheetRef ref : styleSheets) {
          if (fileUrl.equals(ref.getSourceURL())) {
            uploadNewSource(ref, file);
          }
        }
      }
    }
  }

  @Override
  public void handleFileRemoved(IFile file) {

  }

  public void handleLoadEventFired() {
    // Flush everything.
    styleSheets.clear();

    // Re-load the list of scripts.
    try {
      target.getConnection().getCSS().getAllStyleSheets(
          new WebkitCallback<WebkitStyleSheetRef[]>() {
            @Override
            public void handleResult(WebkitResult<WebkitStyleSheetRef[]> result) {
              handleStyleSheetResults(result);
            }
          });
    } catch (IOException e) {
      SDBGDebugCorePlugin.logError(e);
    }
  }

  private void handleStyleSheetResults(WebkitResult<WebkitStyleSheetRef[]> result) {
    if (!result.isError()) {
      for (WebkitStyleSheetRef ref : result.getResult()) {
        styleSheets.add(ref);
      }
    }
  }

  private void uploadNewSource(WebkitStyleSheetRef ref, IFile file) {
    try {
      target.getConnection().getCSS().setStyleSheetText(
          ref.getStyleSheetId(),
          IFileUtilities.getContents(file));
    } catch (IOException e) {
      // We upload changed css scripts on a best-effort basis.
      //DartDebugCorePlugin.logError(e);
    } catch (CoreException e) {
      SDBGDebugCorePlugin.logError(e);
    }
  }

}
