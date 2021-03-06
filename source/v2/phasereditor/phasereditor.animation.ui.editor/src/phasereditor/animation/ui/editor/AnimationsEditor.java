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
package phasereditor.animation.ui.editor;

import static java.lang.System.out;
import static java.util.stream.Collectors.toList;
import static phasereditor.ui.IEditorSharedImages.IMG_PLAY;
import static phasereditor.ui.IEditorSharedImages.IMG_STOP;
import static phasereditor.ui.PhaserEditorUI.swtRun;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPersistableEditor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.operations.UndoRedoActionGroup;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.json.JSONException;
import org.json.JSONObject;

import phasereditor.animation.ui.AnimationActions;
import phasereditor.animation.ui.AnimationCanvas;
import phasereditor.animation.ui.IAnimationsEditor;
import phasereditor.animation.ui.editor.properties.AnimationsPropertyPage;
import phasereditor.animation.ui.editor.wizards.AssetsSplitter;
import phasereditor.animation.ui.editor.wizards.AssetsSplitter.AssetsGroup;
import phasereditor.assetpack.core.AssetPackCore;
import phasereditor.assetpack.core.AtlasAssetModel;
import phasereditor.assetpack.core.IAssetFrameModel;
import phasereditor.assetpack.core.IAssetKey;
import phasereditor.assetpack.core.ImageAssetModel;
import phasereditor.assetpack.core.MultiAtlasAssetModel;
import phasereditor.assetpack.core.SpritesheetAssetModel;
import phasereditor.assetpack.core.animations.AnimationFrameModel;
import phasereditor.assetpack.core.animations.AnimationModel;
import phasereditor.assetpack.ui.AssetPackUI;
import phasereditor.project.core.PhaserProjectBuilder;
import phasereditor.ui.EditorBlockProvider;
import phasereditor.ui.EditorSharedImages;
import phasereditor.ui.FilteredTreeCanvas;
import phasereditor.ui.IEditorBlock;
import phasereditor.ui.IEditorHugeToolbar;
import phasereditor.ui.IEditorSharedImages;
import phasereditor.ui.ImageCanvas_Zoom_1_1_Action;
import phasereditor.ui.ImageCanvas_Zoom_FitWindow_Action;
import phasereditor.ui.SelectionProviderImpl;
import phasereditor.ui.TreeCanvasViewer;
import phasereditor.ui.editors.EditorFileMoveHelper;
import phasereditor.ui.editors.EditorFileStampHelper;

/**
 * @author arian
 *
 */
public class AnimationsEditor extends EditorPart implements IPersistableEditor, IAnimationsEditor {

	private static final String ANIMATION_KEY = "animation";
	private static final String OUTLINER_TREE_STATE_KEY = "outliner.tree.state";
	public static final String ID = "phasereditor.animation.ui.editor.AnimationsEditor"; //$NON-NLS-1$
	private AnimationsModel_in_Editor _model;
	private AnimationCanvas _animCanvas;
	Outliner _outliner;
	ISelectionChangedListener _outlinerListener;
	private AnimationTimelineCanvas_in_Editor _timelineCanvas;
	private ImageCanvas_Zoom_1_1_Action _zoom_1_1_action;
	private ImageCanvas_Zoom_FitWindow_Action _zoom_fitWindow_action;
	private boolean _dirty;
	private String _initialAnimtionKey;
	JSONObject _initialOutlinerState;
	private Action _deleteAction;
	private Action _newAction;
	private AnimationActions _animationActions;
	private AnimationModel _initialAnimation;
	private EditorFileStampHelper _fileStampHelper;
	private AnimationsEditorBlockProvider _blocksProvider;
	private AnimationsEditorHugeToolbar _hugeToolbar;
	private Composite _stackComp;
	private StackLayout _stackLayout;
	private SashForm _singleAnimComp;
	private MultiAnimsComp _multiAnimationsComp;
	private Action _playAllAction;
	private Action _stopAllAction;
	private MenuManager _menuManager;
	private UndoRedoActionGroup _undoRedoGroup;
	private IPartListener _partListener;

	public static final IUndoContext UNDO_CONTEXT = new IUndoContext() {

		@Override
		public boolean matches(IUndoContext context) {
			return context == this;
		}

		@Override
		public String getLabel() {
			return "ANIMATIONS_EDITOR_CONTEXT";
		}
	};

	public void executeOperation(IUndoableOperation operation) {
		operation.addContext(UNDO_CONTEXT);
		IWorkbench workbench = getSite().getWorkbenchWindow().getWorkbench();
		try {
			IOperationHistory history = workbench.getOperationSupport().getOperationHistory();
			history.execute(operation, null, this);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void registerUndoRedoActions() {

		var site = getEditorSite();

		_undoRedoGroup = new UndoRedoActionGroup(site, UNDO_CONTEXT, true);

		var actionBars = site.getActionBars();

		_undoRedoGroup.fillActionBars(actionBars);

		actionBars.updateActionBars();

		_partListener = createUndoRedoPartListener();

		getEditorSite().getPage().addPartListener(_partListener);

	}

	public UndoRedoActionGroup getUndoRedoGroup() {
		return _undoRedoGroup;
	}

	private IPartListener createUndoRedoPartListener() {
		return new IPartListener() {

			@Override
			public void partOpened(IWorkbenchPart part) {
				//
			}

			@Override
			public void partDeactivated(IWorkbenchPart part) {
				//
			}

			@Override
			public void partClosed(IWorkbenchPart part) {
				//
			}

			@Override
			public void partBroughtToTop(IWorkbenchPart part) {
				//
			}

			@Override
			public void partActivated(IWorkbenchPart part) {
				if (part == AnimationsEditor.this) {
					var actionBars = getEditorSite().getActionBars();
					_undoRedoGroup.fillActionBars(actionBars);
					actionBars.updateActionBars();
				}
			}
		};
	}

	@Override
	public void dispose() {

		getEditorSite().getPage().removePartListener(_partListener);

		super.dispose();
	}

	public AnimationActions getAnimationActions() {
		return _animationActions;
	}

	public Action getPlayAllAction() {
		return _playAllAction;
	}

	public Action getStopAllAction() {
		return _stopAllAction;
	}

	public ImageCanvas_Zoom_1_1_Action getZoom_1_1_action() {
		return _zoom_1_1_action;
	}

	public ImageCanvas_Zoom_FitWindow_Action getZoom_fitWindow_action() {
		return _zoom_fitWindow_action;
	}

	public Action getDeleteAction() {
		return _deleteAction;
	}

	public Action getNewAction() {
		return _newAction;
	}

	public AnimationsEditor() {
		_fileStampHelper = new EditorFileStampHelper(this, this::reloadMethod, this::saveMethod);
	}

	@Override
	public void createPartControl(Composite parent) {
		createActions();

		_stackComp = new Composite(parent, 0);
		_stackLayout = new StackLayout();
		_stackComp.setLayout(_stackLayout);

		// animation

		createSingleAnimationComp();

		// multiple animations

		createMultiAnimationsComp();

		_stackLayout.topControl = _multiAnimationsComp;
		_stackComp.requestLayout();

		afterCreateWidgets();

		{
			var helper = new EditorFileMoveHelper<>(this) {

				@Override
				protected IFile getEditorFile(AnimationsEditor editor) {
					return getEditorInput().getFile();
				}

				@SuppressWarnings("synthetic-access")
				@Override
				protected void setEditorFile(AnimationsEditor editor, IFile file) {
					AnimationsEditor.super.setInput(new FileEditorInput(file));

					_model.setFile(file);

					setPartName(file.getName());

					firePropertyChange(PROP_TITLE);
				}
			};
			parent.addDisposeListener(e -> helper.dispose());
		}

		_animCanvas.setMenu(_menuManager.createContextMenu(_animCanvas));
		_timelineCanvas.setMenu(_menuManager.createContextMenu(_timelineCanvas));
	}

	private void createMultiAnimationsComp() {
		_multiAnimationsComp = new MultiAnimsComp(_stackComp, 0);
	}

	class MultiAnimsComp extends Composite implements PaintListener {

		public MultiAnimsComp(Composite parent, int style) {
			super(parent, style);

			var layout = new GridLayout(4, true);
			layout.marginWidth = layout.marginHeight = 20;
			setLayout(layout);
			addPaintListener(this);

			setMenu(_menuManager.createContextMenu(this));
		}

		@Override
		public void paintControl(PaintEvent e) {
			if (getChildren().length == 0) {

				String str;

				if (getModel().getAnimations().isEmpty()) {
					str = "Drop some frames here to create new animations.";
				} else {
					str = "Select the animations in the Outline view";
				}

				var size = e.gc.textExtent(str);
				e.gc.drawText(str, e.width / 2 - size.x / 2, e.height / 2 - size.y / 2, true);
			}
		}

		class MyAnimCanvas extends AnimationCanvas implements MouseTrackListener, MouseListener {

			private boolean _mouseOver;

			public MyAnimCanvas(Composite parent, int style) {
				super(parent, style);

				addControlListener(this);
				addMouseTrackListener(this);
				addMouseListener(this);
			}

			@Override
			protected void customPaintControl(PaintEvent e) {
				super.customPaintControl(e);
				if (_mouseOver) {
					e.gc.drawRectangle(1, 1, e.width - 2, e.height - 2);
				}
			}

			@Override
			public void controlResized(ControlEvent e) {
				resetZoom();
			}

			@Override
			public void controlMoved(ControlEvent e) {
				//
			}

			@Override
			public void mouseHover(MouseEvent e) {
				//
			}

			@Override
			public void mouseExit(MouseEvent e) {
				_mouseOver = false;
				redraw();
			}

			@Override
			public void mouseEnter(MouseEvent e) {
				_mouseOver = true;
				redraw();
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				mouseUp(e);
			}

			@Override
			public void mouseDown(MouseEvent e) {
				_mouseOver = false;
				setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
			}

			@Override
			public void mouseUp(MouseEvent e) {
				if (_mouseOver) {
					return;
				}

				setBackground(getParent().getBackground());

				if (e.button != 1) {
					return;
				}

				var sel = new StructuredSelection(getModel());
				if (_outliner == null) {
					setExternalSelection(sel);
				} else {
					_outliner.setSelection(sel);
				}
			}

		}

		public void updateContent(Object[] anims) {
			for (var c : getChildren()) {
				c.dispose();
			}

			var len = anims.length;
			var cols = (int) Math.round(Math.sqrt(len));

			if ((float) len / cols > cols) {
				cols++;
			}

			if (len < 4) {
				cols = 3;
			}

			setLayout(new GridLayout(cols, false));

			for (var anim : anims) {
				var animCanvas = new MyAnimCanvas(this, 0);
				animCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				animCanvas.setModel((AnimationModel) anim, false);
			}

			requestLayout();

		}

		public void disposeContent() {
			for (var c : getChildren()) {
				var canvas = (AnimationCanvas) c;
				canvas.stop();
				swtRun(10, canvas::dispose);
			}
		}

		public void play() {
			for (var c : getChildren()) {
				var canvas = (AnimationCanvas) c;
				canvas.play();
			}
		}

		public void stop() {
			for (var c : getChildren()) {
				var canvas = (AnimationCanvas) c;
				canvas.stop();
			}
		}
	}

	private void createSingleAnimationComp() {
		_singleAnimComp = new SashForm(_stackComp, SWT.VERTICAL);

		Composite topComp = new Composite(_singleAnimComp, SWT.BORDER);
		GridLayout layout = new GridLayout(1, false);
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		topComp.setLayout(layout);

		_animCanvas = new AnimationCanvas(topComp, SWT.NONE);
		_animCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		_timelineCanvas = new AnimationTimelineCanvas_in_Editor(_singleAnimComp, SWT.BORDER);
		_timelineCanvas.setEditor(this);

		_singleAnimComp.setWeights(new int[] { 2, 1 });

		_animationActions = new AnimationActions(_animCanvas, _timelineCanvas);
	}

	private void afterCreateWidgets() {

		getEditorSite().setSelectionProvider(new ISelectionProvider() {

			private ISelection _selection = StructuredSelection.EMPTY;
			private ListenerList<ISelectionChangedListener> _listeners = new ListenerList<>();

			@Override
			public void setSelection(ISelection selection) {
				_selection = selection;
				var event = new SelectionChangedEvent(this, selection);
				for (var l : _listeners) {
					l.selectionChanged(event);
				}
			}

			@Override
			public void removeSelectionChangedListener(ISelectionChangedListener listener) {
				_listeners.remove(listener);
			}

			@Override
			public ISelection getSelection() {
				return _selection;
			}

			@Override
			public void addSelectionChangedListener(ISelectionChangedListener listener) {
				_listeners.add(listener);
			}
		});

		_animCanvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				var anim = getTimelineCanvas().getModel();

				if (anim != null) {
					getTimelineCanvas().clearSelection();
				}
			}
		});
		_animCanvas.setStepCallback(_timelineCanvas::redraw);
		_animCanvas.setPlaybackCallback(_animationActions::animationStatusChanged);
		_animCanvas.addPaintListener(e -> {
			if (_animCanvas.getModel() != null) {
				e.gc.setAlpha(255);
				e.gc.setForeground(_animCanvas.getForeground());
				e.gc.drawText(_animCanvas.getModel().getKey(), 5, 5, true);
			}
		});
		_animCanvas.setZoomWhenShiftPressed(false);

		_timelineCanvas.setZoomWhenModifiedPressed(false);

		disableToolbar();

		if (_initialAnimtionKey != null) {
			var opt = _model.getAnimations().stream().filter(a -> a.getKey().equals(_initialAnimtionKey)).findFirst();
			if (opt.isPresent()) {
				_initialAnimation = opt.get();
			}
		}

		init_DND_Support();

		if (PhaserProjectBuilder.isStartupFinished()) {

			if (_initialAnimation == null) {
				setSelection(StructuredSelection.EMPTY);
			} else {
				setSelection(new StructuredSelection(_initialAnimation));
				_initialAnimation = null;
			}
		}

	}

	private void init_DND_Support() {
		{
			int options = DND.DROP_MOVE | DND.DROP_DEFAULT;
			for (var comp : new Composite[] { _animCanvas, _multiAnimationsComp }) {
				DropTarget target = new DropTarget(comp, options);
				Transfer[] types = { LocalSelectionTransfer.getTransfer() };
				target.setTransfer(types);
				target.addDropListener(new DropTargetAdapter() {

					@Override
					public void drop(DropTargetEvent event) {
						Object[] data = null;

						if (event.data instanceof Object[]) {
							data = (Object[]) event.data;
						}

						if (event.data instanceof IStructuredSelection) {
							data = ((IStructuredSelection) event.data).toArray();
						}

						if (data != null) {

							if (comp == _multiAnimationsComp) {
								createAnimationsWithDrop(data);
							} else {
								// now the animation canvas only handles the drop of frames, and append them at
								// the end of the timeline.

								_timelineCanvas.setDropIndex(-1);
								_timelineCanvas.selectionDropped(data);
							}
						}
					}
				});
			}
		}
	}

	protected final void openNewAnimationDialog(List<AnimationFrameModel> frames) {

		String initialName = "untitled";

		var msg = "Enter the name of the new animation:";
		var dlg = new InputDialog(getAnimationCanvas().getShell(), "Add Animation", msg, initialName,
				new IInputValidator() {

					@Override
					public String isValid(String newText) {
						if (getModel().getAnimations().stream().filter(a -> a.getKey().equals(newText)).findFirst()
								.isPresent()) {
							return "That name is used by other animation.";
						}
						return null;
					}
				});

		if (dlg.open() == Window.OK) {

			var anim = new AnimationModel(getModel());

			anim.setKey(dlg.getValue());
			if (frames != null) {
				anim.getFrames().addAll(frames);
			}

			getModel().getAnimations().add(anim);

			anim.buildTimeline();

			if (_outliner != null) {
				_outliner.refresh();
			}

			selectAnimation(anim);

			setDirty();
		}
	}

	private void disableToolbar() {
		_animationActions.setEnabled(false);

		_zoom_1_1_action.setEnabled(false);
		_zoom_fitWindow_action.setEnabled(false);
	}

	public void dirtyPropertyChanged() {

		setDirty();

		var running = !_animCanvas.isStopped();

		_animCanvas.stop();

		var anim = _timelineCanvas.getModel();

		anim.buildTimeline();

		_timelineCanvas.redraw();

		if (running) {
			_animCanvas.play();
		}
	}

	private void createActions() {

		_zoom_1_1_action = new ImageCanvas_Zoom_1_1_Action(_animCanvas);
		_zoom_fitWindow_action = new ImageCanvas_Zoom_FitWindow_Action(_animCanvas);

		_deleteAction = new Action("Delete",
				Workbench.getInstance().getSharedImages().getImageDescriptor(ISharedImages.IMG_ETOOL_DELETE)) {
			@Override
			public void run() {
				var elems = ((IStructuredSelection) getEditorSite().getSelectionProvider().getSelection()).toArray();

				if (elems.length > 0) {
					var list = List.of(elems);

					var list1 = list.stream()

							.filter(e -> e instanceof AnimationModel)

							.map(e -> (AnimationModel) e)

							.collect(toList());

					if (list1.isEmpty()) {
						var list2 = list.stream().filter(e -> e instanceof AnimationFrameModel)

								.map(e -> (AnimationFrameModel) e)

								.collect(toList());

						if (!list2.isEmpty()) {
							var animations = List.of(list2.get(0).getAnimation());
							var before = AnimationOperation.readState(animations);
							deleteFrames(list2);
							var after = AnimationOperation.readState(animations);
							executeOperation(new AnimationOperation("Delte frames", before, after, false));
						}
					} else {
						var before = AnimationsGlobalOperation.readState(AnimationsEditor.this);
						deleteAnimations(list1);
						var after = AnimationsGlobalOperation.readState(AnimationsEditor.this);
						executeOperation(new AnimationsGlobalOperation("Delete animations and frames", before, after));
					}
				}
			}
		};

		_newAction = new Action("Add Animation", EditorSharedImages.getImageDescriptor(IEditorSharedImages.IMG_ADD)) {
			@Override
			public void run() {
				openNewAnimationDialog(null);
			}
		};

		_playAllAction = new Action("", EditorSharedImages.getImageDescriptor(IMG_PLAY)) {
			@Override
			public void run() {
				_multiAnimationsComp.play();
			}
		};

		_stopAllAction = new Action("", EditorSharedImages.getImageDescriptor(IMG_STOP)) {
			@Override
			public void run() {
				_multiAnimationsComp.stop();
			}
		};

		_menuManager = new MenuManager();

		_menuManager.add(_newAction);
		_menuManager.add(_deleteAction);
		_menuManager.addMenuListener(m -> {
			var selectionProvider = getEditorSite().getSelectionProvider();
			var elems = ((IStructuredSelection) selectionProvider.getSelection()).toArray();
			var list = List.of(elems);
			var count = list.stream().filter(e -> e instanceof AnimationModel).count();
			count += list.stream().filter(e -> e instanceof AnimationFrameModel).count();
			_deleteAction.setEnabled(count > 0);
		});
	}

	@Override
	public void setFocus() {
		_animCanvas.setFocus();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		_fileStampHelper.helpDoSave(monitor);
	}

	private void saveMethod(IProgressMonitor monitor) {

		var file = getEditorInput().getFile();

		try (ByteArrayInputStream source = new ByteArrayInputStream(_model.toJSON().toString(2).getBytes())) {

			file.setContents(source, true, false, monitor);

			_dirty = false;

			firePropertyChange(PROP_DIRTY);

			refreshBlocksProvider();

		} catch (JSONException | CoreException | IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public void doSaveAs() {
		// Do the Save As operation
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);

		registerUndoRedoActions();
	}

	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);

		var file = ((IFileEditorInput) input).getFile();

		setPartName(file.getName());

		try {
			_model = new AnimationsModel_in_Editor(this);
			_model.build();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void reloadFile() {
		_fileStampHelper.helpReloadFile();
	}

	private void reloadMethod() {
		try {
			_model = new AnimationsModel_in_Editor(this);

			var anim = _animCanvas.getModel();

			if (anim != null) {
				var key = anim.getKey();
				anim = _model.getAnimation(key);
			}

			_initialAnimation = anim;

			if (_outliner != null) {
				_outliner._viewer.setInput(null);
			}

			build();

			selectAnimation(anim);

			setDirty(false);

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void build() {
		_model.build();

		refreshOutline();
		refreshBlocksProvider();

		var animModel = _animCanvas.getModel();

		if (animModel != null) {
			animModel = _model.getAnimation(animModel.getKey());
		}

		if (_initialAnimation != null) {
			animModel = _initialAnimation;
			_initialAnimation = null;
		}

		if (animModel == null) {
			setSelection(StructuredSelection.EMPTY);
		} else {
			setSelection(new StructuredSelection(animModel));
		}
	}

	public void refreshBlocksProvider() {
		if (_blocksProvider != null) {
			_blocksProvider.refresh();
		}
	}

	@Override
	public IFileEditorInput getEditorInput() {
		return (IFileEditorInput) super.getEditorInput();
	}

	public AnimationsModel_in_Editor getModel() {
		return _model;
	}

	@Override
	public boolean isDirty() {
		return _dirty;
	}

	public boolean isStopped() {
		return _animCanvas.isStopped();
	}

	public void setDirty() {
		setDirty(true);
	}

	public void setDirty(boolean dirty) {
		_dirty = dirty;
		firePropertyChange(PROP_DIRTY);
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	private List<AnimationsPropertyPage> _propertyPages = new ArrayList<>();

	public void updatePropertyPagesContentWithSelection() {
		for (var page : _propertyPages) {
			page.selectionChanged(this, getEditorSite().getSelectionProvider().getSelection());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getAdapter(Class adapter) {

		if (adapter == IContentOutlinePage.class) {
			return createOutliner();
		}

		if (adapter == IPropertySheetPage.class) {
			var page = new AnimationsPropertyPage(this) {
				@Override
				public void dispose() {
					_propertyPages.remove(this);
					super.dispose();
				}
			};
			_propertyPages.add(page);
			return page;
		}

		if (adapter == EditorBlockProvider.class) {
			if (_blocksProvider == null) {
				_blocksProvider = new AnimationsEditorBlockProvider();
			}
			return _blocksProvider;
		}

		if (adapter == IEditorHugeToolbar.class) {
			if (_hugeToolbar == null) {
				_hugeToolbar = new AnimationsEditorHugeToolbar();
			}
			return _hugeToolbar;
		}

		return super.getAdapter(adapter);
	}

	class AnimationsEditorHugeToolbar implements IEditorHugeToolbar {

		private List<ActionButton> _singleAnimationButtons;
		private List<ActionButton> _multiAnimationButtons;
		private Composite _parent;

		@SuppressWarnings("unused")
		@Override
		public void createContent(Composite parent) {
			_parent = parent;

			_singleAnimationButtons = new ArrayList<>();
			_multiAnimationButtons = new ArrayList<>();

			_singleAnimationButtons.add(new ActionButton(parent, getAnimationActions().getPlayAction()));
			_singleAnimationButtons.add(new ActionButton(parent, getAnimationActions().getPauseAction()));
			_singleAnimationButtons.add(new ActionButton(parent, getAnimationActions().getStopAction()));

			_multiAnimationButtons.add(new ActionButton(parent, getPlayAllAction()));
			_multiAnimationButtons.add(new ActionButton(parent, getStopAllAction()));

			new ActionButton(parent, getNewAction(), true);

			updateButtons();
		}

		public List<ActionButton> getSingleAnimationButtons() {
			return _singleAnimationButtons;
		}

		public void updateButtons() {
			var visible = isSingleAnimationMode();

			for (var btn : _singleAnimationButtons) {
				btn.getButton().setVisible(visible);
				btn.getButton().setLayoutData(new RowData(visible ? SWT.DEFAULT : 0, visible ? SWT.DEFAULT : 0));
			}

			visible = !visible;

			for (var btn : _multiAnimationButtons) {
				btn.getButton().setVisible(visible);
				btn.getButton().setLayoutData(new RowData(visible ? SWT.DEFAULT : 0, visible ? SWT.DEFAULT : 0));
			}

			_parent.requestLayout();

		}

	}

	class AnimationsEditorBlockProvider extends EditorBlockProvider {

		@Override
		public String getId() {
			return getClass().getSimpleName() + "$" + getEditorInput().getFile().getFullPath().toString();
		}

		@Override
		public List<IEditorBlock> getBlocks() {
			var packs = AssetPackCore.getAssetPackModels(getEditorInput().getFile().getProject());

			var list = packs.stream()

					.flatMap(pack -> pack.getAssets().stream())

					.filter(asset -> {
						return asset instanceof ImageAssetModel

								|| asset instanceof AtlasAssetModel

								|| asset instanceof MultiAtlasAssetModel

								|| asset instanceof IAssetFrameModel

								|| asset instanceof SpritesheetAssetModel;
					})

					.map(asset -> AssetPackUI.getAssetEditorBlock(asset))

					.collect(toList());

			return list;
		}

		@Override
		public IPropertySheetPage createPropertyPage() {
			return new AnimationBlocksPropertyPage(AnimationsEditor.this);
		}

	}

	public AnimationCanvas getAnimationCanvas() {
		return _animCanvas;
	}

	public AnimationTimelineCanvas_in_Editor getTimelineCanvas() {
		return _timelineCanvas;
	}

	private Object createOutliner() {
		if (_outliner == null) {
			_outliner = new Outliner();

			_outlinerListener = new ISelectionChangedListener() {

				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					outliner_selectionChanged(event);
				}
			};
			_outliner.addSelectionChangedListener(_outlinerListener);
		} else {
			_outliner.refresh();
		}

		if (_timelineCanvas.getModel() != null) {
			swtRun(() -> {
				if (_outliner != null) {
					_outliner.setSelection(new StructuredSelection(_timelineCanvas.getModel()));
				}
			});
		}

		return _outliner;
	}

	public Outliner getOutliner() {
		return _outliner;
	}

	protected void outliner_selectionChanged(SelectionChangedEvent event) {
		var sel = event.getStructuredSelection();

		setExternalSelection(sel);
	}

	public void setSelection(IStructuredSelection sel) {
		if (_outliner == null) {
			setExternalSelection(sel);
		} else {
			_outliner.refresh();
			_outliner.setSelection(sel);
		}
	}

	private void setExternalSelection(IStructuredSelection sel) {
		var elems = sel.toArray();

		Composite top;

		if (elems.length == 1) {
			top = _singleAnimComp;
			var elem = sel.getFirstElement();
			var anim = (AnimationModel) elem;
			loadAnimation(anim);
			_multiAnimationsComp.disposeContent();
		} else {
			top = _multiAnimationsComp;
			loadAnimation(null);
			if (elems.length == 0) {
				elems = _model.getAnimations().toArray();
			}
			_multiAnimationsComp.updateContent(elems);
		}

		_stackLayout.topControl = top;
		_stackComp.requestLayout();

		getEditorSite().getSelectionProvider().setSelection(sel);

		_deleteAction.setEnabled(!sel.isEmpty());

		if (_hugeToolbar != null) {
			_hugeToolbar.updateButtons();
		}

		getStopAllAction().run();
		getAnimationActions().getStopAction().run();
	}

	public void selectAnimation(AnimationModel anim) {
		StructuredSelection selection = anim == null ? StructuredSelection.EMPTY : new StructuredSelection(anim);

		if (_outliner == null) {

			loadAnimation(anim);

			getEditorSite().getSelectionProvider().setSelection(selection);

		} else {

			_outliner.setSelection(selection);

		}
	}

	class Outliner extends Page implements IContentOutlinePage, ISelectionChangedListener {
		private FilteredTreeCanvas _filteredTreeCanvas;
		private SelectionProviderImpl _selProvider;
		private TreeCanvasViewer _viewer;

		public Outliner() {
		}

		public FilteredTreeCanvas getFilteredTreeCanvas() {
			return _filteredTreeCanvas;
		}

		@Override
		public void createControl(Composite parent) {
			_filteredTreeCanvas = new FilteredTreeCanvas(parent, SWT.NONE);
			_viewer = new AnimationsTreeViewer(_filteredTreeCanvas.getTree());

			{
				int options = DND.DROP_MOVE | DND.DROP_DEFAULT;
				DropTarget target = new DropTarget(_filteredTreeCanvas.getTree(), options);
				Transfer[] types = { LocalSelectionTransfer.getTransfer() };
				target.setTransfer(types);
				target.addDropListener(new DropTargetAdapter() {

					@Override
					public void drop(DropTargetEvent event) {
						if (event.data instanceof Object[]) {
							createAnimationsWithDrop((Object[]) event.data);
						}

						if (event.data instanceof IStructuredSelection) {
							createAnimationsWithDrop(((IStructuredSelection) event.data).toArray());
						}
					}
				});
			}

			if (PhaserProjectBuilder.isStartedFirstTime()) {
				// if the builders are ready, then do this, else, do it on the first refresh.
				_viewer.setInput(getModel());
			}

			for (var l : _initialListeners) {
				_filteredTreeCanvas.getUtils().addSelectionChangedListener(l);
			}

			if (_initialSelection != null) {
				_filteredTreeCanvas.getUtils().setSelection(_initialSelection);
				_filteredTreeCanvas.redraw();
			}

			_initialListeners.clear();
			_initialSelection = null;

			if (_initialOutlinerState != null) {
				_outliner.getFilteredTreeCanvas().getTree().restoreState(_initialOutlinerState);
				_initialOutlinerState = null;
			}

			_viewer.getControl().setMenu(_menuManager.createContextMenu(_viewer.getControl()));

			registerUndoRedoActions();

		}

		private void registerUndoRedoActions() {
			getUndoRedoGroup().fillActionBars(getSite().getActionBars());
		}

		@Override
		public void dispose() {
			removeSelectionChangedListener(_outlinerListener);
			AnimationsEditor.this._outliner = null;
			super.dispose();
		}

		private ListenerList<ISelectionChangedListener> _initialListeners = new ListenerList<>();
		private ISelection _initialSelection;

		@Override
		public void addSelectionChangedListener(ISelectionChangedListener listener) {
			if (_filteredTreeCanvas == null) {
				_initialListeners.add(listener);
				return;
			}

			_filteredTreeCanvas.getUtils().addSelectionChangedListener(listener);
		}

		@Override
		public ISelection getSelection() {
			return _filteredTreeCanvas.getUtils().getSelection();
		}

		@Override
		public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			if (_filteredTreeCanvas == null) {
				_initialListeners.remove(listener);
				return;
			}

			_filteredTreeCanvas.getUtils().removeSelectionChangedListener(listener);
		}

		@Override
		public void setSelection(ISelection selection) {
			if (_filteredTreeCanvas == null) {
				_initialSelection = selection;
				return;
			}

			_filteredTreeCanvas.getUtils().setSelection(selection);
			_filteredTreeCanvas.redraw();
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			_selProvider.fireSelectionChanged();
		}

		@Override
		public Control getControl() {
			return _filteredTreeCanvas;
		}

		@Override
		public void setFocus() {
			_filteredTreeCanvas.setFocus();
		}

		public void refresh() {
			if (_filteredTreeCanvas != null) {
				if (_viewer.getInput() == null) {
					_viewer.setInput(getModel());
				} else {
					_viewer.refresh();
				}
			}
		}
	}

	protected void loadAnimation(AnimationModel anim) {
		_animationActions.setChecked(false);
		_animationActions.setEnabled(false);

		if (anim == null) {

			_zoom_1_1_action.setEnabled(false);
			_zoom_fitWindow_action.setEnabled(false);
			_deleteAction.setEnabled(false);

			_animCanvas.setModel(null);
			_timelineCanvas.setModel(null);

			return;
		}

		_animCanvas.setModel(anim, false);

		_animationActions.getPlayAction().setEnabled(true);

		if (_timelineCanvas.getModel() != anim) {
			_timelineCanvas.setModel(anim);
		}

		_zoom_1_1_action.setEnabled(true);
		_zoom_fitWindow_action.setEnabled(true);
		_deleteAction.setEnabled(true);

	}

	public void deleteAnimations(List<AnimationModel> animations) {
		if (animations.isEmpty()) {
			return;
		}

		_model.getAnimations().removeAll(animations);

		if (isSingleAnimationMode()) {
			if (_animCanvas.getModel() != null && animations.contains(_animCanvas.getModel())) {
				selectAnimation(null);
			}
		} else {
			var provider = getEditorSite().getSelectionProvider();
			provider.setSelection(StructuredSelection.EMPTY);
			_multiAnimationsComp.updateContent(new Object[] {});
		}

		if (_outliner != null) {
			_outliner.refresh();
		}

		setDirty();
	}

	public void deleteFrames(List<AnimationFrameModel> frames) {
		if (frames.isEmpty()) {
			return;
		}

		boolean running = !_animCanvas.isStopped();

		_animCanvas.stop();

		var animation = _animCanvas.getModel();
		animation.getFrames().removeAll(frames);
		animation.buildTimeline();

		if (running) {
			_animCanvas.play();
		} else {
			if (!animation.getFrames().isEmpty()) {
				_animCanvas.showFrame(0);
			}
		}

		_timelineCanvas.getSelectedFrames().clear();
		_timelineCanvas.redraw();

		setDirty();
	}

	public void playOrPause() {
		_animCanvas.playOrPause();
	}

	@Override
	public void saveState(IMemento memento) {
		AnimationModel anim = getAnimationCanvas().getModel();

		if (anim != null) {
			memento.putString(ANIMATION_KEY, anim.getKey());
		}

		if (_outliner != null) {
			var jsonSate = new JSONObject();
			_outliner.getFilteredTreeCanvas().getTree().saveState(jsonSate);
			memento.putString(OUTLINER_TREE_STATE_KEY, jsonSate.toString());
		}
	}

	@Override
	public void restoreState(IMemento memento) {
		_initialAnimtionKey = memento.getString(ANIMATION_KEY);
		{
			var str = memento.getString(OUTLINER_TREE_STATE_KEY);
			if (str != null) {
				_initialOutlinerState = new JSONObject(str);
			}
		}
	}

	public void refreshOutline() {
		if (_outliner != null) {
			_outliner.refresh();
		}
	}

	@SuppressWarnings("boxing")
	public void createAnimationsWithDrop(Object[] data) {

		var dlg = new InputDialog(getSite().getShell(), "Create Animations",
				"Enter a common prefix for all the animations:", "", prefix -> {
					return null;
				});

		if (dlg.open() != Window.OK) {
			return;
		}

		var prefix = dlg.getValue();
		
		var result = splitFramesByPrefix(prefix, data);

		if (result.isEmpty()) {
			return;
		}

		for (var group : result) {
			out.println(group.getPrefix());
			for (var asset : group.getAssets()) {
				out.println("  " + asset.getKey());
			}
		}

		var before = AnimationsGlobalOperation.readState(this);

		var anims = new ArrayList<AnimationModel>();

		for (var group : result) {
			var anim = new AnimationModel(_model);
			anims.add(anim);

			_model.getAnimation(group.getPrefix());

			anim.setKey(_model.getNewAnimationName(group.getPrefix()));

			for (var frame : group.getAssets()) {

				var animFrame = new AnimationFrameModel(anim);
				animFrame.setTextureKey(frame.getAsset().getKey());

				if (frame.getAsset() instanceof ImageAssetModel) {
					// nothing
				} else if (frame instanceof SpritesheetAssetModel.FrameModel) {
					animFrame.setFrameName(((SpritesheetAssetModel.FrameModel) frame).getIndex());
				} else {
					animFrame.setFrameName(frame.getKey());
				}

				anim.getFrames().add(animFrame);
			}

			anim.buildTimeline();
		}

		_model.getAnimations().addAll(anims);
		_model.getAnimations().sort((a, b) -> a.getKey().compareTo(b.getKey()));

		var after = AnimationsGlobalOperation.readState(this);

		executeOperation(new AnimationsGlobalOperation("Auto-create animations", before, after));

		if (!anims.isEmpty()) {
			var sel = new StructuredSelection(anims);
			setSelection(sel);
		}

		if (_outliner != null) {
			_outliner.refresh();
		}

		setDirty();
	}

	public static Collection<AssetsGroup> splitFramesByPrefix(String prefix, Object[] data) {
		var splitter = new AssetsSplitter(prefix);

		for (var obj : data) {
			if (obj instanceof AtlasAssetModel) {
				splitter.addAll(((AtlasAssetModel) obj).getSubElements());
			} else if (obj instanceof MultiAtlasAssetModel) {
				splitter.addAll(((MultiAtlasAssetModel) obj).getSubElements());
			} else if (obj instanceof SpritesheetAssetModel) {
				splitter.addAll(((SpritesheetAssetModel) obj).getFrames());
			} else if (obj instanceof IAssetFrameModel) {
				splitter.add((IAssetKey) obj);
			} else if (obj instanceof ImageAssetModel) {
				splitter.add(((ImageAssetModel) obj).getFrame());
			}
		}

		var result = splitter.split();

		return result;
	}

	@Override
	public void revealAnimation(String key) {
		var anim = getModel().getAnimation(key);
		if (anim != null) {
			selectAnimation(anim);
		}
	}

	public boolean isSingleAnimationMode() {
		return _stackLayout.topControl == _singleAnimComp;
	}

	public void resetPlayback() {
		resetPlayback(isStopped());
	}

	public void resetPlayback(boolean stopped) {
		if (isSingleAnimationMode()) {
			if (!stopped) {
				getAnimationActions().getStopAction().run();
				getAnimationActions().getPlayAction().run();
			}
		} else {
			getStopAllAction().run();
			getPlayAllAction().run();
		}
	}
}
