// The MIT License (MIT)
//
// Copyright (c) 2015, 2018 Arian Fornaris
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
package phasereditor.atlas.ui.editor;

import java.util.ArrayList;
import java.util.List;

import phasereditor.ui.properties.FormPropertyPage;
import phasereditor.ui.properties.FormPropertySection;

/**
 * @author arian
 *
 */
public class TexturePackerPropertyPage extends FormPropertyPage {

	private TexturePackerEditor _editor;

	public TexturePackerPropertyPage(TexturePackerEditor editor) {
		super();
		_editor = editor;
	}

	@Override
	protected Object getDefaultModel() {
		return _editor.getModel();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected List<FormPropertySection> createSections(Object obj) {
		var list = new ArrayList<FormPropertySection>();

		if (obj instanceof TexturePackerEditorModel) {
			list.add(new SettingsSection(_editor));
		}

		if (obj instanceof EditorPage) {
			list.add(new PageSection(_editor));
		}

		if (obj instanceof TexturePackerEditorFrame) {
			list.add(new FrameSection(_editor));
		}

		return list;
	}

}