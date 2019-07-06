// The MIT License (MIT)
//
// Copyright (c) 2015, 2019 Arian Fornaris
//
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to permit
// persons to whom the Software is furnished to do so, subject to the
// following conditions: The above copyright notice and this permission
// notice shall be included in all copies or substantial portions of the
// Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
// OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
// NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
// DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
// OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
// USE OR OTHER DEALINGS IN THE SOFTWARE.
package phasereditor.ide.ui.toolbar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.NewWizardDropDownAction;

import phasereditor.ide.ui.wizards.NewWizardLauncherDialog;

class NewMenuWrapper {

	private Button _btn;

	public NewMenuWrapper(Composite parent) {
		_btn = new Button(parent, SWT.PUSH);
		_btn.setText("New");
		_btn.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			var dlg = new NewWizardLauncherDialog(_btn.getShell());
			dlg.open();
		}));
		var action = new NewWizardDropDownAction(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		_btn.setImage(action.getImageDescriptor().createImage());
	}

	public Button getButton() {
		return _btn;
	}

}