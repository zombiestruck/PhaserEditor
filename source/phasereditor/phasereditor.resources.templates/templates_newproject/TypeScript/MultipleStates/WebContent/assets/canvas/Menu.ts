
// -- user code here --

/* --- start generated code --- */

// Generated by  1.4.3 (Phaser v2.6.2)


/**
 * Menu.
 */
class Menu extends Phaser.State {
	
	constructor() {
		
		super();
		
	}
	
	init() {
		
		this.stage.backgroundColor = '#8080ff';
		
	}
	
	preload () {
		
	}
	
	create() {
		this.add.text(332, 292, 'Click to start...', {"font":"bold 20px Arial"});
		
		
		
		
	}
	
	
	/* state-methods-begin */
	
	initObjects() {
		this.add.text(100, 100, "Click to play", { fill:"#000" });
	}

	update() {
		if (this.input.activePointer.isDown) {
			this.game.state.start("Level");
		}
	}
	
	/* state-methods-end */
	
}
/* --- end generated code --- */
// -- user code here --
