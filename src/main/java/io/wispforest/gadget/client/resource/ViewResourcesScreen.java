package io.wispforest.gadget.client.resource;

import io.wispforest.gadget.client.gui.braid.BraidGuiUtil;
import io.wispforest.gadget.client.gui.braid.BraidScreenWithParent;
import io.wispforest.gadget.client.gui.braid.HexDump;
import io.wispforest.gadget.client.gui.braid.VanillaTranslucent;
import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Align;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.scroll.FlatScrollbar;
import io.wispforest.owo.braid.widgets.scroll.ScrollableWithBars;
import io.wispforest.owo.braid.widgets.splitpane.SplitPane;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.resource.InputSupplier;
import net.minecraft.util.Identifier;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ViewResourcesScreen extends BraidScreenWithParent {
    private final Contents contents;

    public ViewResourcesScreen(Screen parent, Map<Identifier, Integer> resourcePaths) {
        super(parent, new Contents(resourcePaths));
        contents = (Contents) rootWidget;
    }

    public void resRequester(BiConsumer<Identifier, Integer> resRequester) {
        contents.resRequester(resRequester);
    }

    public void openFile(Identifier id, InputSupplier<InputStream> is) {
        contents.resAcceptor.accept(id, is);
    }

    public static class Contents extends StatefulWidget {
        private final Map<Identifier, Integer> resourcePaths;
        private BiConsumer<Identifier, Integer> resRequester;

        private BiConsumer<Identifier, InputSupplier<InputStream>> resAcceptor;

        public Contents(Map<Identifier, Integer> resourcePaths) {
            this.resourcePaths = resourcePaths;
        }

        public void resRequester(BiConsumer<Identifier, Integer> resRequester) {
            assertMutable();
            this.resRequester = resRequester;
        }


        @Override
        public WidgetState<?> createState() {
            return new State();
        }

    }

    public static class State extends WidgetState<Contents> {
        private final TreeEntry<ResourceKey> treeRoot = new TreeEntry<>("");

        private Identifier currentResourceId = null;
        private InputSupplier<InputStream> currentResourceGetter = null;

        @Override
        public void init() {
            for (var pair : widget().resourcePaths.entrySet()) {
                String fullPath = pair.getKey().getNamespace() + "/" + pair.getKey().getPath();
                List<String> split = new ArrayList<>(List.of(fullPath.split("/")));

                if (pair.getValue() > 1) {
                    for (int i = 0; i < pair.getValue(); i++) {
                        split.add(String.valueOf(i));

                        treeRoot.addUnder(split, new ResourceKey(pair.getKey(), i));

                        split.removeLast();
                    }
                } else {
                    treeRoot.addUnder(split, new ResourceKey(pair.getKey(), 0));
                }
            }

            widget().resAcceptor = (id, getter) -> {
                this.setState(() -> {
                    this.currentResourceId = id;
                    this.currentResourceGetter = getter;
                });
            };
        }

        @Override
        public Widget build(BuildContext context) {
            Widget contents;

            if (currentResourceId == null) {
                contents = new Padding(Insets.none());
            } else {
                if (currentResourceId.getPath().endsWith(".png")) {
                    contents = new PngTexture(currentResourceGetter);
                } else {
                    try {
                        InputStream is = new BufferedInputStream(currentResourceGetter.get());

                        boolean isText = currentResourceId.getPath().endsWith(".txt")
                            || currentResourceId.getPath().endsWith(".json")
                            || currentResourceId.getPath().endsWith(".fsh")
                            || currentResourceId.getPath().endsWith(".vsh")
                            || currentResourceId.getPath().endsWith(".snbt");

                        if (!isText) {
                            try {
                                is.mark(128);
                                byte[] bytes = is.readNBytes(128);

                                var chars = StandardCharsets.UTF_8
                                    .newDecoder()
                                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                                    .onMalformedInput(CodingErrorAction.REPORT)
                                    .decode(ByteBuffer.wrap(bytes));

                                isText = true;

                                for (int i = 0; i < chars.length(); i++) {
                                    int codepoint = chars.charAt(i);

                                    if (codepoint > 127) continue;

                                    if (!Character.isDigit(codepoint)
                                        && !Character.isAlphabetic(codepoint)
                                        && !Character.isSpaceChar(codepoint)) {
                                        isText = false;
                                        break;
                                    }
                                }
                            } catch (CharacterCodingException cce) {
                                // ...
                            }

                            is.reset();
                        }

                        if (isText) {
                            contents = BraidGuiUtil.codeListing(new String(is.readAllBytes(), StandardCharsets.UTF_8));
                        } else {
                            // Display as bytes.
                            contents = new HexDump(is.readAllBytes(), false);
                        }
                    } catch (IOException e) {
                        // TODO: show exception.
                        contents = BraidGuiUtil.showException(e);
                    }
                }
            }

            return new VanillaTranslucent(
                new Padding(
                    Insets.all(5),
                    new SplitPane(
                        new ScrollableWithBars(
                            null,
                            null,
                            3,
                            (axis, controller) -> new FlatScrollbar(axis, controller, Color.ofRgb(0xabb0bf), Color.ofRgb(0xabb0bf)),
                            new TreeEntryWidget<>(treeRoot, k -> {
                                widget().resRequester.accept(k.id(), k.index());
                            })
                        ),
                        new ScrollableWithBars(
                            null,
                            null,
                            3,
                            (axis, controller) -> new FlatScrollbar(axis, controller, Color.ofRgb(0xabb0bf), Color.ofRgb(0xabb0bf)),
                            new Padding(
                                Insets.all(5),
                                new Align(
                                    Alignment.TOP_LEFT,
                                    contents
                                )
                            )
                        ),
                        LayoutAxis.HORIZONTAL
                    )
                )
            );
        }
    }

    private record ResourceKey(Identifier id, int index) { }
}
