package io.wispforest.gadget.client.resource;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.client.DialogUtil;
import io.wispforest.gadget.client.gui.braid.BraidGuiUtil;
import io.wispforest.gadget.client.gui.braid.BraidScreenWithParent;
import io.wispforest.gadget.client.gui.braid.VanillaTranslucent;
import io.wispforest.gadget.decompile.KnotUtil;
import io.wispforest.gadget.decompile.QuiltflowerHandler;
import io.wispforest.gadget.decompile.QuiltflowerManager;
import io.wispforest.gadget.early.GadgetMixinExtension;
import io.wispforest.gadget.util.ProgressToast;
import io.wispforest.gadget.util.ThrowableUtil;
import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.KeyboardInput;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.scroll.FlatScrollbar;
import io.wispforest.owo.braid.widgets.scroll.ScrollController;
import io.wispforest.owo.braid.widgets.scroll.ScrollableWithBars;
import io.wispforest.owo.braid.widgets.splitpane.SplitPane;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ForkJoinPool;

public class ViewClassesWidget extends StatefulWidget {
    private final boolean showAll;
    private ProgressToast toast;

    public ViewClassesWidget(boolean showAll, ProgressToast toast) {
        this.showAll = showAll;
        this.toast = toast;

        toast.step(Text.translatable("message.gadget.progress.loading_quiltflower"));
    }

    public static void openWithProgress(Screen parent) {
        ProgressToast toast = ProgressToast.create(Text.translatable("message.gadget.loading_classes"));
        MinecraftClient client = MinecraftClient.getInstance();
        boolean showAll = Screen.hasShiftDown();

        toast.follow(
            QuiltflowerManager.ensureInstalled(toast)
                .thenApplyAsync(unused -> {
                    BraidScreenWithParent screen = new BraidScreenWithParent(parent, new ViewClassesWidget(showAll, toast));

                    screen.init(client, parent.width, parent.height);

                    return screen;
                })
                .thenAcceptAsync(client::setScreen, client),
            true);
    }

    @Override
    public WidgetState<?> createState() {
        return new State();
    }

    public static class State extends WidgetState<ViewClassesWidget> {
        private QuiltflowerHandler decompiler;

        private String currentFileName = "";
        private String currentFileContents = "";

        private final List<Text> logMessages = new ArrayList<>();

        private final TreeEntry<String> treeRoot = new TreeEntry<>("");

        private final ScrollController scrollController = new ScrollController();

        @Override
        public void init() {
            this.decompiler = QuiltflowerManager.loadHandler(widget().toast, text -> {
                MinecraftClient.getInstance().execute(() -> {
                    setState(() -> logMessages.add(text));
                    schedulePostLayoutCallback(() -> scrollController.setOffset(scrollController.maxOffset()));
                });
            });

            Set<String> allClasses;

            if (widget().showAll) {
                allClasses = new TreeSet<>();

                for (Class<?> klass : KnotUtil.INSTRUMENTATION.getInitiatedClasses(Gadget.class.getClassLoader())) {
                    if (klass.isHidden()) continue;
                    if (klass.isArray()) continue;

                    klass = klass.getNestHost();
                    allClasses.add(klass.getName());
                }
            } else {
                allClasses = GadgetMixinExtension.DUMPED_CLASSES;
            }

            for (var name : allClasses) {
                String fullPath = decompiler.mapClass(name.replace('.', '/')) + ".class";
                String[] split = fullPath.split("/");

                treeRoot.addUnder(split, fullPath);
            }

            widget().toast = null;
        }

        private void switchToClass(String fullPath) {
            var client = MinecraftClient.getInstance();

            this.setState(() -> {
                this.currentFileName = "";
                this.currentFileContents = "";
                this.logMessages.clear();
            });

            ForkJoinPool.commonPool().execute(() -> {
                try {
                    var text = decompiler.decompileClass(Class.forName(
                        decompiler.unmapClass(
                            fullPath
                                .replace(".class", "")
                                .replace('/', '.')))
                    );

                    client.execute(() -> {
                        this.setState(() -> {
                            currentFileName = fullPath.substring(fullPath.lastIndexOf('/') + 1);
                            currentFileContents = text;
                        });

                        schedulePostLayoutCallback(() -> scrollController.setOffset(0));
                    });
                } catch (Exception e) {
                    client.execute(() -> {
                        this.setState(() -> {
                            logMessages.clear();
                            logMessages.add(Text.literal(ThrowableUtil.throwableToString(e)
                                    .replace("\t", "    "))
                                .formatted(Formatting.RED));
                        });
                    });
                }
            });
        }

        @Override
        public Widget build(BuildContext context) {
            Widget contents;

            if (currentFileContents.isEmpty()) {
                List<Widget> widgets = new ArrayList<>();

                for (var log : logMessages) {
                    widgets.add(new Label(
                        new LabelStyle(Alignment.LEFT, null, null, null),
                        true,
                        log
                    ));
                }

                contents = new Column(widgets);
            } else {
                contents = BraidGuiUtil.codeListing(this.currentFileContents);
            }

            // TODO: context menus.
            Widget w = new VanillaTranslucent(
                new Padding(
                    Insets.all(5),
                    new SplitPane(
                        new ScrollableWithBars(
                            null,
                            null,
                            3,
                            (axis, controller) -> new FlatScrollbar(axis, controller, Color.ofRgb(0xabb0bf), Color.ofRgb(0xabb0bf)),
                            new TreeEntryWidget<>(treeRoot, this::switchToClass)
                        ),
                        new ScrollableWithBars(
                            null,
                            scrollController,
                            3,
                            (axis, controller) -> new FlatScrollbar(axis, controller, Color.ofRgb(0xabb0bf), Color.ofRgb(0xabb0bf)),
                            new Padding(
                                Insets.all(5),
                                contents
                            )
                        ),
                        LayoutAxis.HORIZONTAL
                    )
                )
            );

            w = new KeyboardInput(
                widget -> widget.keyDownCallback((keyCode, modifiers) -> {
                    if (keyCode == GLFW.GLFW_KEY_S && modifiers.ctrl()) {
                        if (currentFileContents == null) return;

                        String path = DialogUtil.saveFileDialog(
                            "Save as .java",
                            currentFileName.replace(".class", ".java"),
                            List.of("*.java"),
                            "Java source files"
                        );

                        if (path != null) {
                            try {
                                Files.writeString(Path.of(path), currentFileContents, StandardCharsets.UTF_8);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }),
                w
            );

            return w;
        }
    }
}
