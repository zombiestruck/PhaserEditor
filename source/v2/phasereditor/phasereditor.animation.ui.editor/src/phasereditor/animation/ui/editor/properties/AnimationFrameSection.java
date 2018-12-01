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
package phasereditor.animation.ui.editor.properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import phasereditor.animation.ui.editor.AnimationsEditor;
import phasereditor.assetpack.core.IAssetFrameModel;
import phasereditor.assetpack.core.ImageAssetModel;
import phasereditor.assetpack.core.animations.AnimationFrameModel;
import phasereditor.inspect.core.InspectCore;
import phasereditor.ui.ExplainFrameDataCanvas;
import phasereditor.ui.properties.TextToIntListener;

/**
 * @author arian
 *
 */
@SuppressWarnings("boxing")
public class AnimationFrameSection extends BaseAnimationSection<AnimationFrameModel> {

	private Text _durationText;
	private Label _computedDurationLabel;
	private ExplainFrameDataCanvas _frameCanvas;
	private Label _frameLabel;

	public AnimationFrameSection(AnimationsEditor editor) {
		super(editor, "Animation Frame");

		setFillSpace(true);
	}

	@Override
	public boolean canEdit(Object obj) {
		return obj instanceof AnimationFrameModel;
	}

	@SuppressWarnings("unused")
	@Override
	public Control createContent(Composite parent) {

		var comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));

		// duration

		{
			var label = new Label(comp, SWT.NONE);
			label.setText("Duration");
			label.setToolTipText(InspectCore.getPhaserHelp().getMemberHelp("AnimationFrameConfig.duration"));

			_durationText = new Text(comp, SWT.BORDER);
			_durationText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			new TextToIntListener(_durationText) {

				@SuppressWarnings("synthetic-access")
				@Override
				protected void accept(int value) {
					getModels().stream().forEach(model -> {

						model.setDuration(value);
						var animation = model.getAnimation();
						animation.buildTimeline();

					});

					updateTotalDuration();

					var editor = getEditor();
					editor.getTimelineCanvas().redraw();
					editor.setDirty();
					restartPlayback();

				}
			};
		}

		{
			new Label(comp, SWT.NONE);
			_computedDurationLabel = new Label(comp, SWT.NONE);
			_computedDurationLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			_computedDurationLabel.setToolTipText(
					"The computed duration of the frame. It is the frameRate-based duration plus the extra duration set in the 'duration' property.\nNOTE: This is not part of the Phaser API.");

		}

		// texture

		{
			label(comp, "Frame", InspectCore.getPhaserHelp().getMemberHelp("AnimationFrameConfig.frame"),
					new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		}

		{
			_frameCanvas = new ExplainFrameDataCanvas(comp, SWT.BORDER);
			var gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
			_frameCanvas.setLayoutData(gd);
		}

		{
			_frameLabel = new Label(comp, SWT.WRAP);
			_frameLabel.setText("");
			_frameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		}

		update_UI_from_Model();

		return comp;
	}

	@Override
	public void update_UI_from_Model() {
		var models = getModels();

		// duration

		_durationText.setText(flatValues_to_String(models.stream().map(model -> model.getDuration())));

		// computed duration

		updateTotalDuration();

		// texture
		{

			var frame = (IAssetFrameModel) flatValues_to_Object(models.stream().map(model -> model.getFrameAsset()));

			if (frame == null) {
				_frameLabel.setText("Frame");
				_frameCanvas.removeImage();
			} else {

				var file = frame.getImageFile();
				var fd = frame.getFrameData();

				// duration
				{
					var sb = new StringBuilder();
					if (frame instanceof ImageAssetModel.Frame) {
						sb.append("key: " + frame.getKey());
					} else {
						sb.append("key: " + frame.getAsset().getKey() + "\nframe: " + frame.getKey());
					}

					sb.append("\n");
					sb.append("size: " + fd.srcSize.x + "x" + fd.srcSize.y);
					sb.append("\n");

					if (file != null) {
						sb.append("file: " + file.getName());
					}

					_frameLabel.setText(sb.toString());
				}

				// texture

				_frameCanvas.setImageFile(file, fd);
				_frameCanvas.resetZoom();
			}

		}
	}

	private void updateTotalDuration() {
		{
			var total = 0;
			for (var model : getModels()) {
				total += model.getComputedDuration();
			}
			_computedDurationLabel.setText("Real duration: " + total);
		}
	}

}