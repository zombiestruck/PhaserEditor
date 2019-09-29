/// <reference path="./TreeViewerRenderer.ts" />

namespace phasereditor2d.ui.controls.viewers {

    export const TREE_RENDERER_GRID_PADDING = 5;

    export class GridTreeViewerRenderer extends TreeViewerRenderer {

        private _center: boolean;

        constructor(viewer: TreeViewer, center: boolean = false) {
            super(viewer);
            viewer.setCellSize(128);
            this._center = center;
        }

        protected paintItems(objects: any[], treeIconList: TreeIconInfo[], paintItems: PaintItem[], x: number, y: number) {
            const viewer = this.getViewer();

            if (viewer.getCellSize() <= 48) {
                return super.paintItems(objects, treeIconList, paintItems, x, y);
            }

            const b = viewer.getBounds();

            const offset = this._center ? Math.floor(b.width % (viewer.getCellSize() + TREE_RENDERER_GRID_PADDING) / 2) : TREE_RENDERER_GRID_PADDING;

            return this.paintItems2(objects, treeIconList, paintItems, x + offset, y + TREE_RENDERER_GRID_PADDING, offset, 0);
        }

        private paintItems2(objects: any[], treeIconList: TreeIconInfo[], paintItems: PaintItem[], x: number, y: number, offset: number, depth: number) {

            const viewer = this.getViewer();
            const cellSize = Math.max(ROW_HEIGHT, viewer.getCellSize());
            const context = viewer.getContext();

            const b = viewer.getBounds();

            for (let obj of objects) {

                const children = viewer.getContentProvider().getChildren(obj);
                const expanded = viewer.isExpanded(obj);

                if (viewer.isFilterIncluded(obj)) {

                    const renderer = viewer.getCellRendererProvider().getCellRenderer(obj);

                    const args = new RenderCellArgs(context, x, y, cellSize, cellSize, obj, viewer, true);

                    this.renderGridCell(args, renderer, depth);

                    if (y > -cellSize && y < b.height) {
                        // render tree icon
                        if (children.length > 0) {
                            const iconY = y + (cellSize - TREE_ICON_SIZE) / 2;

                            const icon = Controls.getIcon(expanded ? ICON_CONTROL_TREE_COLLAPSE : ICON_CONTROL_TREE_EXPAND);
                            icon.paint(context, x + 5, iconY, ICON_SIZE, ICON_SIZE, false);

                            treeIconList.push({
                                rect: new Rect(x, iconY, TREE_ICON_SIZE, TREE_ICON_SIZE),
                                obj: obj
                            });
                        }

                    }
                    
                    const item = new PaintItem(paintItems.length, obj);
                    item.set(args.x, args.y, args.w, args.h);
                    paintItems.push(item);


                    x += cellSize + TREE_RENDERER_GRID_PADDING;

                    if (x + cellSize > b.width) {
                        y += cellSize + TREE_RENDERER_GRID_PADDING;
                        x = 0 + offset;
                    }
                }

                if (expanded) {
                    const result = this.paintItems2(children, treeIconList, paintItems, x, y, offset, depth + 1);
                    y = result.y;
                    x = result.x;
                }
            }

            return {
                x: x,
                y: y
            };
        }

        private renderGridCell(args: RenderCellArgs, renderer: ICellRenderer, depth: number) {
            const cellSize = args.viewer.getCellSize();
            const b = args.viewer.getBounds();
            const lineHeight = 20;
            let x = args.x;

            const ctx = args.canvasContext;

            const label = args.viewer.getLabelProvider().getLabel(args.obj);
            let line = "";
            for (const c of label) {
                const test = line + c;
                const m = ctx.measureText(test);
                if (m.width > args.w) {
                    if (line.length > 2) {
                        line = line.substring(0, line.length - 2) + "..";
                    }
                    break;
                } else {
                    line += c;
                }
            }

            const selected = args.viewer.isSelected(args.obj);

            let labelHeight: number;
            let visible: boolean;

            {

                labelHeight = lineHeight;

                visible = args.y > -(cellSize + labelHeight) && args.y < b.height;

                if (visible) {

                    if (depth > 0) {
                        const space = args.h / (depth + 1);
                        const arrowH = space / 2;
                        let arrowY = args.y + space;

                        ctx.save();
                        ctx.lineWidth = 1;
                        ctx.strokeStyle = Controls.theme.treeItemForeground;

                        for (let i = 0; i < depth; i++) {
                            ctx.beginPath();
                            ctx.moveTo(args.x - 5, arrowY - arrowH);
                            ctx.lineTo(args.x, arrowY);
                            ctx.lineTo(args.x - 5, arrowY + arrowH);
                            ctx.stroke()
                            arrowY += space;
                        }
                        ctx.restore();
                    }

                    this.renderCellBack(args, selected);

                    const args2 = new RenderCellArgs(args.canvasContext,
                        args.x + 3, args.y + 3,
                        args.w - 6, args.h - 6 - lineHeight,
                        args.obj, args.viewer, args.center
                    );

                    if (selected) {
                        ctx.save();
                        ctx.globalAlpha = 0.5;
                        renderer.renderCell(args2);
                        ctx.restore();
                    } else {
                        renderer.renderCell(args2);
                    }

                    this.renderCellFront(args, selected);

                    args.viewer.paintItemBackground(args.obj, args.x, args.y + args.h - lineHeight, args.w, labelHeight, 10);
                }
            }

            if (visible) {
                ctx.save();

                if (selected) {
                    ctx.fillStyle = Controls.theme.treeItemSelectionForeground;
                } else {
                    ctx.fillStyle = Controls.theme.treeItemForeground;
                }


                const m = ctx.measureText(line);
                const x2 = Math.max(x, x + args.w / 2 - m.width / 2);
                ctx.fillText(line, x2, args.y + args.h - 5);
                ctx.restore();
            }
        }


        protected renderCellBack(args: RenderCellArgs, selected: boolean) {
            // if (selected) {
            //     const ctx = args.canvasContext;
            //     ctx.save();
            //     ctx.fillStyle = Controls.theme.treeItemSelectionBackground;
            //     ctx.globalAlpha = 0.5;
            //     ctx.fillRect(args.x, args.y, args.w, args.h + labelHeight);
            //     ctx.restore();
            // }
        }

        protected renderCellFront(args: RenderCellArgs, selected: boolean) {
            // if (selected) {
            //     const ctx = args.canvasContext;
            //     ctx.save();
            //     ctx.globalAlpha = 0.3;
            //     ctx.fillRect(args.x, args.y, args.w, args.h + labelHeight);
            //     ctx.restore();
            // }
        }
    }


}