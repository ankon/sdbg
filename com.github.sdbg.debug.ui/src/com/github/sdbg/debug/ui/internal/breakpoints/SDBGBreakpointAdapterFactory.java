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

package com.github.sdbg.debug.ui.internal.breakpoints;

import com.github.sdbg.core.DartCore;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * This adapter factory adapts the Dart compilation unit editor to an
 * {@link IToggleBreakpointsTarget}.
 */
public class SDBGBreakpointAdapterFactory implements IAdapterFactory {

  /**
   * Create a new DartBreakpointAdapterFactory.
   */
  public SDBGBreakpointAdapterFactory() {

  }

  @SuppressWarnings("rawtypes")
  @Override
  public Object getAdapter(Object adaptableObject, Class adapterType) {
    if (adaptableObject instanceof ITextEditor) {
      ITextEditor editorPart = (ITextEditor) adaptableObject;

      IResource resource = (IResource) editorPart.getEditorInput().getAdapter(IResource.class);

      if (resource != null) {
        String name = resource.getName().toLowerCase();

        if (DartCore.isHtmlLikeFileName(name) || DartCore.isJSLikeFileName(name)) {
          return new SDBGBreakpointAdapter();
        }
      }
    }

    return null;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Class[] getAdapterList() {
    return new Class[] {IToggleBreakpointsTarget.class};
  }

}
