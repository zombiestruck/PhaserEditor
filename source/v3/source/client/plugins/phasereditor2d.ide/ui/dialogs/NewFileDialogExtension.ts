namespace phasereditor2d.ide.ui.dialogs {

    import controls = colibri.ui.controls;
    import io = colibri.core.io;

    export class NewFileDialogExtension extends colibri.core.extensions.Extension {

        static POINT = "phasereditor2d.ide.ui.dialogs.NewFileDialogExtension";

        private _wizardName: string;
        private _icon: controls.IImage;
        private _initialFileName: string;
        private _fileExtension: string;
        private _fileContent: string;

        constructor(config: {
            id: string,
            wizardName: string,
            icon: controls.IImage,
            initialFileName: string,
            fileExtension: string,
            fileContent: string
        }) {
            super(config.id);

            this._wizardName = config.wizardName;
            this._icon = config.icon;
            this._initialFileName = config.initialFileName;
            this._fileExtension = config.fileExtension;
            this._fileContent = config.fileContent;
        }

        getFileContent() {
            return this._fileContent;
        }

        getInitialFileName() {
            return this._initialFileName;
        }

        getFileExtension() {
            return this._fileExtension;
        }

        getWizardName() {
            return this._wizardName;
        }

        getIcon() {
            return this._icon;
        }

        getInitialFileLocation(): io.FilePath {
            return colibri.ui.ide.Workbench.getWorkbench().getProjectRoot();
        }

        findInitialFileLocationBasedOnContentType(contentType: string) {

            const root = colibri.ui.ide.Workbench.getWorkbench().getProjectRoot();

            const files: io.FilePath[] = [];

            root.flatTree(files, false);

            const reg = colibri.ui.ide.Workbench.getWorkbench().getContentTypeRegistry()

            const targetFiles = files.filter(file => contentType === reg.getCachedContentType(file));

            if (targetFiles.length > 0) {

                targetFiles.sort((a, b) => {
                    return b.getModTime() - a.getModTime();
                });

                return targetFiles[0].getParent();
            }

            return root;
        }
    }
}