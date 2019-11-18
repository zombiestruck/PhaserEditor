namespace colibri.ui.ide {

    export declare type CreateWindowFunc = ()=> WorkbenchWindow; 

    export class WindowExtension extends core.extensions.Extension {

        static ID = "colibri.ui.ide.WindowExtension";

        private _createWindowFunc : CreateWindowFunc;

        constructor(id : string, priority : number, createWindowFunc : CreateWindowFunc) {
            super(id, priority);

            this._createWindowFunc = createWindowFunc;
        }

        createWindow() {
            return this._createWindowFunc();
        }
    }
}