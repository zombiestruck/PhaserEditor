// The MIT License (MIT)
//
// Copyright (c) 2015, 2017 Arian Fornaris
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
package phasereditor.canvas.core.codegen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import phasereditor.assetpack.core.AtlasAssetModel;
import phasereditor.assetpack.core.IAssetFrameModel;
import phasereditor.assetpack.core.IAssetKey;
import phasereditor.assetpack.core.ImageAssetModel;
import phasereditor.assetpack.core.SpritesheetAssetModel;
import phasereditor.canvas.core.AnimationModel;
import phasereditor.canvas.core.ArcadeBodyModel;
import phasereditor.canvas.core.AssetSpriteModel;
import phasereditor.canvas.core.AtlasSpriteModel;
import phasereditor.canvas.core.BaseObjectModel;
import phasereditor.canvas.core.BaseSpriteModel;
import phasereditor.canvas.core.BodyModel;
import phasereditor.canvas.core.ButtonSpriteModel;
import phasereditor.canvas.core.CanvasModel;
import phasereditor.canvas.core.CanvasType;
import phasereditor.canvas.core.CircleArcadeBodyModel;
import phasereditor.canvas.core.EditorSettings;
import phasereditor.canvas.core.GroupModel;
import phasereditor.canvas.core.ImageSpriteModel;
import phasereditor.canvas.core.PhysicsSortDirection;
import phasereditor.canvas.core.PhysicsType;
import phasereditor.canvas.core.RectArcadeBodyModel;
import phasereditor.canvas.core.SpritesheetSpriteModel;
import phasereditor.canvas.core.StateSettings;
import phasereditor.canvas.core.TileSpriteModel;
import phasereditor.canvas.core.WorldModel;
import phasereditor.inspect.core.InspectCore;
import phasereditor.lic.LicCore;
import phasereditor.project.core.codegen.BaseCodeGenerator;

/**
 * @author arian
 *
 */
public abstract class JSLikeCanvasCodeGenerator extends BaseCodeGenerator {

	protected final String PRE_INIT_CODE_BEGIN = "/* --- pre-init-begin --- */";
	protected final String PRE_INIT_CODE_END = "/* --- pre-init-end --- */";
	protected final String POST_INIT_CODE_BEGIN = "/* --- post-init-begin --- */";
	protected final String POST_INIT_CODE_END = "/* --- post-init-end --- */";
	protected final String START_GENERATED_CODE = "/* --- start generated code --- */";
	protected final String END_GENERATED_CODE = "/* --- end generated code --- */";
	protected final WorldModel _world;
	protected final CanvasModel _model;
	protected final EditorSettings _settings;

	public JSLikeCanvasCodeGenerator(CanvasModel model) {
		_world = model.getWorld();
		_settings = model.getSettings();
		_model = model;
	}

	public static class TextureArgs {
		public String key;
		public String frame = "null";
	}

	public static TextureArgs getTextureArgs(IAssetKey assetKey) {
		TextureArgs info = new TextureArgs();
		info.key = "'" + assetKey.getAsset().getKey() + "'";
		if (assetKey.getAsset() instanceof ImageAssetModel) {
			info.frame = "null";
		} else if (assetKey instanceof SpritesheetAssetModel.FrameModel) {
			info.frame = assetKey.getKey();
		} else if (assetKey instanceof AtlasAssetModel.Frame) {
			info.frame = "'" + assetKey.getKey() + "'";
		}
		return info;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * phasereditor.canvas.core.codegen.BaseCodeGenerator#internalGenerate()
	 */
	@Override
	protected void internalGenerate() {
		{
			String content = getReplaceContent();
			int i = content.indexOf(START_GENERATED_CODE);
			if (i >= 0) {
				line(content.substring(0, i) + START_GENERATED_CODE);
			} else {
				line(getYouCanInsertCodeHere("user code here"));
				line(START_GENERATED_CODE);
			}
		}

		line();
		line("// Generated by " + LicCore.PRODUCT_NAME + " (Phaser v" + InspectCore.PHASER_VERSION + ")");
		line();
		line();

		generateHeader();

		generateObjectCreation();

		trim(() -> {
			line();

			int mark1 = length();

			generatePublicFields();

			int mark2 = length();

			if (mark1 < mark2) {
				line("// public fields");
				line();
				append(cut(mark1, mark2));
			}

		});

		generateFooter();
	}

	protected void generatePublicFields() {
		getRootObjectsContainer().walk_skipGroupIfFalse(obj -> {

			generatePublicField(obj);

			return !obj.isPrefabInstance();
		});
	}

	protected void generatePublicField(BaseObjectModel obj) {
		if (!(obj instanceof WorldModel) && obj.isEditorGenerate()) {
			if (obj.isEditorPublic()) {
				String name = getVarName(obj);
				String localName = getLocalVarName(obj);
				String camel = getPublicFieldName(name);
				line("this." + camel + " = " + localName + ";");
			}

			if (obj instanceof BaseSpriteModel && obj.isOverriding(BaseSpriteModel.PROPSET_ANIMATIONS)) {
				List<AnimationModel> anims = ((BaseSpriteModel) obj).getAnimations();
				for (AnimationModel anim : anims) {
					if (anim.isPublic()) {
						String localAnimVar = getLocalAnimationVarName(obj, anim);
						String name = getPublicFieldName(getAnimationVarName(obj, anim));
						line("this." + name + " = " + localAnimVar + ";");
					}
				}
			}
		}
	}

	protected void generateObjectCreation() {
		trim(() -> {
			int i = 0;
			GroupModel root = getRootObjectsContainer();
			int last = root.getChildren().size() - 1;
			for (BaseObjectModel child : root.getChildren()) {
				generateObjectCreate(child);
				if (i < last) {
					line();
				}
				i++;
			}
		});
	}

	protected GroupModel getRootObjectsContainer() {
		return _world;
	}

	protected String getYouCanInsertCodeHere() {
		return getYouCanInsertCodeHere("user code here");
	}

	protected String getYouCanInsertCodeHere(String msg) {
		return "\n" + getIndentTabs() + "// -- " + msg + " --\n" + getIndentTabs();
	}

	protected abstract void generateFooter();

	protected abstract void generateHeader();

	protected static String getPublicFieldName(String name) {
		return "f" + name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	protected void generateObjectCreate(BaseObjectModel model) {
		if (!model.isEditorGenerate()) {
			return;
		}

		if (model instanceof GroupModel) {
			generateGroup((GroupModel) model);
		} else if (model instanceof BaseSpriteModel) {
			generateSprite((BaseSpriteModel) model);
		}
	}

	protected final String getSystemsContainerChain() {
		switch (_model.getType()) {
		case STATE:
			return "this";
		case GROUP:
		case SPRITE:
		default:
			return "this.game";
		}
	}

	private void generateSprite(BaseSpriteModel model) {

		// properties

		int mark1 = length();

		generateProperties(model);

		int mark2 = length();

		// create method

		String parVar = getLocalVarName(model.getParent());

		switch (_model.getType()) {
		case STATE:
			if (model.getParent().isWorldModel()) {
				parVar = null;
			}
			break;
		case GROUP:
			if (model.getParent() == model.getWorld().findGroupPrefabRoot()) {
				parVar = "this";
			}
			break;
		case SPRITE:
			// I hope we don't need this on sprite prefabs cause we do not allow
			// groups inside this kind of canvas
			parVar = "this.parent";
			break;
		default:
			break;
		}

		boolean isPreloadSprite = false;
		{
			StateSettings state = _model.getStateSettings();
			//@formatter:off
			if (
					_model.getType() == CanvasType.STATE 
					&& state.isPreloader()
					&& model.getId().equals(state.getPreloadSpriteId())
				) 
			//@formatter:on
			{
				isPreloadSprite = true;
			}
		}

		if (mark1 < mark2 || model.isEditorPublic() || model.isPrefabInstance() || isPreloadSprite) {
			append("var " + getLocalVarName(model) + " = ");
		}

		if (model.isPrefabInstance()) {
			String prefabName = model.isPrefabInstance() ? model.getPrefab().getClassName() : null;
			boolean writeTexture = model.isOverriding(BaseSpriteModel.PROPSET_TEXTURE);
			Call call = new Call("new " + prefabName);
			// a prefab instance only supports images, sprite atlas and sprite
			// sheet for now.

			if (model instanceof ImageSpriteModel) {

				ImageSpriteModel image = (ImageSpriteModel) model;
				call.value("this.game", round(image.getX()), round(image.getY()));
				call.valueOrUndefined(writeTexture, "'" + image.getAssetKey().getKey() + "'", "null");

			} else if (model instanceof SpritesheetSpriteModel || model instanceof AtlasSpriteModel) {

				AssetSpriteModel<?> sprite = (AssetSpriteModel<?>) model;
				IAssetKey frame = sprite.getAssetKey();
				String frameValue = frame instanceof SpritesheetAssetModel.FrameModel
						? Integer.toString(((SpritesheetAssetModel.FrameModel) frame).getIndex())
						: "'" + frame.getKey() + "'";

				call.value("this.game", round(sprite.getX()), round(sprite.getY()));
				call.valueOrUndefined(writeTexture, "'" + sprite.getAssetKey().getAsset().getKey() + "'", frameValue);
			}

			call.append();

		} else {

			append(getSystemsContainerChain() + ".add.");

			if (model instanceof ImageSpriteModel) {
				ImageSpriteModel image = (ImageSpriteModel) model;

				Call call = new Call("sprite");
				call.value(round(image.getX()));
				call.value(round(image.getY()));
				call.value("'" + image.getAssetKey().getKey() + "'");
				call.value("null");
				call.valueOrUndefined(parVar != null, parVar);

				call.append();

			} else if (model instanceof SpritesheetSpriteModel || model instanceof AtlasSpriteModel) {
				AssetSpriteModel<?> sprite = (AssetSpriteModel<?>) model;
				IAssetKey frame = sprite.getAssetKey();
				String frameValue = frame instanceof SpritesheetAssetModel.FrameModel
						? Integer.toString(((SpritesheetAssetModel.FrameModel) frame).getIndex())
						: "'" + frame.getKey() + "'";

				append("sprite");

				append("(" + // sprite
						round(sprite.getX())// x
						+ ", " + round(sprite.getY()) // y
						+ ", '" + sprite.getAssetKey().getAsset().getKey() + "'" // key
						+ ", " + frameValue // frame
						+ (parVar == null ? "" : ", " + parVar) // group
						+ ")");
			} else if (model instanceof ButtonSpriteModel) {
				ButtonSpriteModel button = (ButtonSpriteModel) model;
				String outFrameKey;
				if (button.getAssetKey().getAsset() instanceof ImageAssetModel) {
					// buttons based on image do not have outFrames
					outFrameKey = "null";
				} else {
					outFrameKey = frameKey((IAssetFrameModel) button.getAssetKey());
				}

				append("button(" + // sprite
						round(button.getX())// x
						+ ", " + round(button.getY()) // y
						+ ", '" + button.getAssetKey().getAsset().getKey() + "'" // key
						+ ", " + emptyStringToNull(button.getCallback()) // callback
						+ ", " + emptyStringToNull(button.getCallbackContext()) // context
						+ ", " + frameKey(button.getOverFrame())// overFrame
						+ ", " + outFrameKey// outFrame
						+ ", " + frameKey(button.getDownFrame())// downFrame
						+ ", " + frameKey(button.getUpFrame())// upFrame
						+ (parVar == null ? "" : ", " + parVar) // group
						+ ")");
			} else if (model instanceof TileSpriteModel) {
				TileSpriteModel tile = (TileSpriteModel) model;
				IAssetKey assetKey = tile.getAssetKey();
				String frame;
				if (assetKey instanceof SpritesheetAssetModel.FrameModel) {
					frame = assetKey.getKey();
				} else if (assetKey instanceof AtlasAssetModel.Frame) {
					frame = "'" + assetKey.getKey() + "'";
				} else {
					// like in case it is an image
					frame = "null";
				}

				append("tileSprite(" + // sprite
						round(tile.getX())// x
						+ ", " + round(tile.getY()) // y
						+ ", " + round(tile.getWidth()) // width
						+ ", " + round(tile.getHeight()) // height
						+ ", '" + tile.getAssetKey().getAsset().getKey() + "'" // key
						+ ", " + frame // frame
						+ (parVar == null ? "" : ", " + parVar) // group
						+ ")");
			}
		}
		line(";");

		String props = cut(mark1, mark2);
		append(props);

		if (model.isPrefabInstance()) {
			if (parVar == null) {
				line(getSystemsContainerChain() + ".add.existing(" + getLocalVarName(model) + ");");
			} else {
				line(parVar + ".add(" + getLocalVarName(model) + ");");
			}
		}
	}

	protected class Call {
		private List<String> _values = new ArrayList<>();
		private String _method;

		public Call(String method) {
			_method = method;
		}

		public Call valueOrUndefined(boolean cond, String... values) {
			for (String v : values) {
				value(cond ? v : "undefined");
			}
			return this;
		}

		public Call string(String... values) {
			for (String value : values) {
				value("'" + value + "'");
			}
			return this;
		}

		public Call value(String... values) {
			_values.addAll(Arrays.asList(values));
			return this;

		}

		public void append() {
			List<String> list = new ArrayList<>();
			for (int i = _values.size() - 1; i >= 0; i--) {
				String value = _values.get(i);
				if (value.equals("undefined") || value.equals("null")) {
					if (list.isEmpty()) {
						continue;
					}
				}

				list.add(0, value);
			}
			JSLikeCanvasCodeGenerator.this.append(_method + "(");
			for (int i = 0; i < list.size(); i++) {
				String sep = i > 0 ? ", " : "";
				JSLikeCanvasCodeGenerator.this.append(sep + list.get(i));
			}
			JSLikeCanvasCodeGenerator.this.append(")");
		}
	}

	protected void generateProperties(BaseSpriteModel model) {
		generateObjectProps(model);

		generateSpriteProps(model);

		if (model instanceof TileSpriteModel) {
			generateTileProps((TileSpriteModel) model);
		}
	}

	protected void generateObjectProps(BaseObjectModel model) {
		String varname = getLocalVarName(model);

		if (model.getName() != null) {
			line(varname + ".name = '" + model.getName() + "';");
		}

		if (model.isOverriding(BaseObjectModel.PROPSET_POSITION)) {
			if (model instanceof GroupModel) {
				if (model.getX() != 0 || model.getY() != 0) {
					line(varname + ".position.setTo(" + round(model.getX()) + ", " + round(model.getY()) + ");");
				}
			}
		}

		if (model.isOverriding(BaseObjectModel.PROPSET_ANGLE)) {
			if (model.getAngle() != 0) {
				line(varname + ".angle = " + model.getAngle() + ";");
			}
		}

		if (model.isOverriding(BaseObjectModel.PROPSET_SCALE)) {
			if (model.getScaleX() != 1 || model.getScaleY() != 1) {
				line(varname + ".scale.setTo(" + model.getScaleX() + ", " + model.getScaleY() + ");");
			}
		}

		if (model.isOverriding(BaseObjectModel.PROPSET_PIVOT)) {
			if (model.getPivotX() != 0 || model.getPivotY() != 0) {
				line(varname + ".pivot.setTo(" + model.getPivotX() + ", " + model.getPivotY() + ");");
			}
		}

		if (model.isOverriding(BaseObjectModel.PROPSET_ALPHA)) {
			if (model.getAlpha() != 1) {
				line(varname + ".alpha = " + model.getAlpha() + ";");
			}
		}
		
		if (model.isOverriding(BaseObjectModel.PROPSET_RENDERABLE)) {
			if (!model.isRenderable()) {
				line(varname + ".renderable = " + model.isRenderable() + ";");
			}
		}
	}

	@SuppressWarnings("static-method")
	protected String getVarName(BaseObjectModel model) {
		return model.getEditorName();
	}

	@SuppressWarnings("static-method")
	protected String getLocalVarName(BaseObjectModel model) {
		return "_" + model.getEditorName();
	}

	private void generateSpriteProps(BaseSpriteModel model) {
		String varname = getLocalVarName(model);

		if (model.isOverriding(BaseSpriteModel.PROPSET_ANCHOR)) {
			if (model.getAnchorX() != 0 || model.getAnchorY() != 0) {
				line(varname + ".anchor.setTo(" + model.getAnchorX() + ", " + model.getAnchorY() + ");");
			}
		}

		if (model.isOverriding(BaseSpriteModel.PROPSET_TINT)) {
			if (model.getTint() != null && !model.getTint().equals("0xffffff")) {
				line(varname + ".tint = " + model.getTint() + ";");
			}
		}

		if (model.isOverriding(BaseSpriteModel.PROPSET_ANIMATIONS)) {
			if (!model.getAnimations().isEmpty()) {
				for (AnimationModel anim : model.getAnimations()) {
					String animvar = null;
					if (anim.isPublic() || anim.isKillOnComplete() || anim.isAutoPlay()) {
						animvar = getLocalAnimationVarName(model, anim);
						append("var " + animvar + " = ");
					}

					append(varname + ".animations.add(");

					append("'" + anim.getName() + "', [");
					int i = 0;
					for (IAssetFrameModel frame : anim.getFrames()) {
						if (i++ > 0) {
							append(", ");
						}
						if (frame instanceof SpritesheetAssetModel.FrameModel) {
							append(frame.getKey());
						} else {
							append("'" + frame.getKey() + "'");
						}
					}
					line("], " + anim.getFrameRate() + ", " + anim.isLoop() + ");");

					if (anim.isKillOnComplete()) {
						line(animvar + ".killOnComplete = true;");
					}

					if (anim.isAutoPlay()) {
						line(animvar + ".play();");
					}
				}
			}
		}

		generateBodyProps(model);

		// always generate data at the end, because it can use previous
		// properties.

		String data = model.getData();
		if (data != null && data.trim().length() > 0) {
			data = data.replace("$$", varname);
			data = data.replace("\n", "\n" + getIndentTabs());
			line(varname + ".data = " + data + ";");
		}
	}

	protected String getAnimationVarName(BaseObjectModel obj, AnimationModel anim) {
		return getVarName(obj) + "_" + anim.getName();
	}

	protected String getLocalAnimationVarName(BaseObjectModel obj, AnimationModel anim) {
		return getLocalVarName(obj) + "_" + anim.getName();
	}

	private void generateBodyProps(BaseSpriteModel model) {
		if (model.isOverriding(BaseSpriteModel.PROPSET_PHYSICS)) {
			BodyModel body = model.getBody();
			if (body != null) {
				if (body instanceof ArcadeBodyModel) {
					generateArcadeBodyProps(model);
				}
			}
		}
	}

	private void generateArcadeBodyProps(BaseSpriteModel model) {
		String varname = getLocalVarName(model);

		if (!model.getParent().isPhysicsGroup() || model.getParent().getPhysicsBodyType() != PhysicsType.ARCADE) {
			line("this.game.physics.arcade.enable(" + varname + ");");
		}

		ArcadeBodyModel body = model.getArcadeBody();
		boolean hasOffset = body.getOffsetX() != 0 || body.getOffsetY() != 0;
		switch (body.getBodyType()) {
		case ARCADE_CIRCLE:
			CircleArcadeBodyModel circle = (CircleArcadeBodyModel) body;
			if (hasOffset) {
				line(varname + ".body.setCircle(" + circle.getRadius() + ", " + circle.getOffsetX() + ", "
						+ circle.getOffsetY() + ");");
			} else {
				line(varname + ".body.setCircle(" + circle.getRadius() + ");");
			}
			break;
		case ARCADE_RECT:
			RectArcadeBodyModel rect = (RectArcadeBodyModel) body;
			if (rect.getWidth() != -1 && rect.getHeight() != -1) {
				if (hasOffset) {
					line(varname + ".body.setSize(" + rect.getWidth() + ", " + rect.getHeight() + ", "
							+ rect.getOffsetX() + ", " + rect.getOffsetY() + ");");
				} else {
					line(varname + ".body.setSize(" + rect.getWidth() + ", " + rect.getHeight() + ");");
				}
			}
			break;
		default:
			break;
		}

		generateCommonArcadeProps(model);
	}

	@SuppressWarnings("boxing")
	private void generateCommonArcadeProps(BaseSpriteModel model) {
		String varname = getLocalVarName(model);
		ArcadeBodyModel body = model.getArcadeBody();

		class Prop {
			private String name;
			private Object def;
			private Function<ArcadeBodyModel, Object> get;

			public Prop(String name, Function<ArcadeBodyModel, Object> get, Object def) {
				super();
				this.name = name;
				this.def = def;
				this.get = get;
			}

			public void gen() {
				Object v = get.apply(body);
				if (!v.equals(def)) {
					line(varname + ".body." + name + " = " + v + ";");
				}
			}
		}

		Prop[] props = {

				new Prop("mass", ArcadeBodyModel::getMass, 1d),

				new Prop("moves", ArcadeBodyModel::isMoves, true),

				new Prop("immovable", ArcadeBodyModel::isImmovable, false),

				new Prop("collideWorldBounds", ArcadeBodyModel::isCollideWorldBounds, false),

				new Prop("allowRotation", ArcadeBodyModel::isAllowRotation, true),

				new Prop("allowGravity", ArcadeBodyModel::isAllowGravity, true),

				new Prop("bounce.x", ArcadeBodyModel::getBounceX, 0d),

				new Prop("bounce.y", ArcadeBodyModel::getBounceY, 0d),

				new Prop("velocity.x", ArcadeBodyModel::getVelocityX, 0d),

				new Prop("velocity.y", ArcadeBodyModel::getVelocityY, 0d),

				new Prop("maxVelocity.x", ArcadeBodyModel::getMaxVelocityX, 10_000d),

				new Prop("maxVelocity.y", ArcadeBodyModel::getMaxVelocityY, 10_000d),

				new Prop("acceleration.x", ArcadeBodyModel::getAccelerationX, 0d),

				new Prop("acceleration.y", ArcadeBodyModel::getAccelerationY, 0d),

				new Prop("drag.x", ArcadeBodyModel::getDragX, 0d),

				new Prop("drag.y", ArcadeBodyModel::getDragY, 0d),

				new Prop("gravity.x", ArcadeBodyModel::getGravityX, 0d),

				new Prop("gravity.y", ArcadeBodyModel::getGravityY, 0d),

				new Prop("friction.x", ArcadeBodyModel::getFrictionX, 1d),

				new Prop("friction.y", ArcadeBodyModel::getFrictionY, 0d),

				new Prop("angularVelocity", ArcadeBodyModel::getAngularVelocity, 0d),

				new Prop("maxAngular", ArcadeBodyModel::getMaxAngular, 1000d),

				new Prop("angularAcceleration", ArcadeBodyModel::getAngularAcceleration, 0d),

				new Prop("angularDrag", ArcadeBodyModel::getAngularDrag, 0d),

				new Prop("checkCollision.none", ArcadeBodyModel::isCheckCollisionNone, false),

				new Prop("checkCollision.up", ArcadeBodyModel::isCheckCollisionUp, true),

				new Prop("checkCollision.down", ArcadeBodyModel::isCheckCollisionDown, true),

				new Prop("checkCollision.left", ArcadeBodyModel::isCheckCollisionLeft, true),

				new Prop("checkCollision.right", ArcadeBodyModel::isCheckCollisionRight, true),

				new Prop("skipQuadTree", ArcadeBodyModel::isSkipQuadTree, false),

		};

		for (Prop prop : props) {
			prop.gen();
		}

	}

	private void generateTileProps(TileSpriteModel model) {
		String varname = getLocalVarName(model);

		if (model.getTilePositionX() != 0 || model.getTilePositionY() != 0) {
			line(varname + ".tilePosition.setTo(" + round(model.getTilePositionX()) + ", "
					+ round(model.getTilePositionY()) + ");");
		}

		if (model.getTileScaleX() != 1 || model.getTileScaleY() != 1) {
			line(varname + ".tileScale.setTo(" + model.getTileScaleX() + ", " + model.getTileScaleY() + ");");
		}

	}

	private void generateGroup(GroupModel group) {

		{
			String varname = getLocalVarName(group);
			String parVarname;
			if (group.getParent().isWorldModel()) {
				if (_model.getType() == CanvasType.STATE) {
					parVarname = "undefined";
				} else {
					parVarname = "this";
				}
			} else {
				parVarname = getLocalVarName(group.getParent());
			}

			append("var " + varname + " = ");

			Call call;
			if (group.isPrefabInstance()) {
				call = new Call("new " + group.getPrefab().getClassName());
				call.value("this.game");
				call.value(parVarname);

				if (group.isPhysicsGroup()) {
					call.string(group.getEditorName()); // name
					call.value("undefined"); // addToStage
					call.value("true"); // enableBody
					call.value(group.getPhysicsBodyType().getPhaserName()); // physicsBodyType
				}
			} else {
				if (group.isPhysicsGroup()) {
					call = new Call(getSystemsContainerChain() + ".add.physicsGroup");
					call.value(group.getPhysicsBodyType().getPhaserName());
					call.value(parVarname);
				} else {
					call = new Call(getSystemsContainerChain() + ".add.group");
					call.value(parVarname);
				}
			}

			call.append();
			line(";");
		}

		generateObjectProps(group);
		generateGroupProps(group);

		if (!group.isPrefabInstance() && !group.getChildren().isEmpty()) {
			line();
			int i = 0;
			int last = group.getChildren().size() - 1;

			for (BaseObjectModel child : group.getChildren()) {
				generateObjectCreate(child);
				if (i < last) {
					line();
				}
				i++;
			}
		}
	}

	protected void generateGroupProps(GroupModel model) {
		String varname = getLocalVarName(model);

		if (model.isOverriding(BaseSpriteModel.PROPSET_PHYSICS)) {
			if (model.getPhysicsSortDirection() != PhysicsSortDirection.NULL) {
				line(varname + ".physicsSortDirection = " + model.getPhysicsSortDirection().getPhaserName() + ";");
			}
		}
	}

	private static String frameKey(IAssetFrameModel frame) {
		if (frame == null) {
			return "null";
		}

		if (frame instanceof SpritesheetAssetModel.FrameModel) {
			return Integer.toString(((SpritesheetAssetModel.FrameModel) frame).getIndex());
		}

		return "'" + frame.getKey() + "'";
	}

}
