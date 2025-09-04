package io.wispforest.gadget.client.gui;

import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;

import java.time.Duration;
import java.util.function.BiFunction;

public abstract class BaseGadgetToast<Self extends BaseGadgetToast<Self, R>, R extends ParentComponent> implements Toast {

    protected final R rootComponent;
    protected final VisibilityPredicate<Self> visibilityPredicate;

    public BaseGadgetToast(BiFunction<Sizing, Sizing, R> rootComponent, VisibilityPredicate<Self> visibilityPredicate) {
        this(rootComponent.apply(Sizing.fill(), Sizing.fill()), visibilityPredicate);
    }

    public BaseGadgetToast(R rootComponent, VisibilityPredicate<Self> visibilityPredicate) {
        this.rootComponent = rootComponent;
        this.visibilityPredicate = visibilityPredicate;
        this.rootComponent
                .allowOverflow(true)
                .surface(Surface.VANILLA_TRANSLUCENT.and(Surface.outline(0xFF5800FF)))
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)
                .padding(Insets.of(5));
    }

    public BaseGadgetToast(R rootComponent, Duration duration) {
        this(rootComponent, (toast, shownTime) -> shownTime <= duration.toMillis());
    }

    public BaseGadgetToast(BiFunction<Sizing, Sizing, R> rootComponent, Duration duration) {
        this(rootComponent, (toast, shownTime) -> shownTime <= duration.toMillis());
    }

    protected void inflateAndMount() {
        this.rootComponent.inflate(Size.of(BASE_WIDTH, BASE_HEIGHT));
        this.rootComponent.mount(null, 0, 0);
    }

    private Visibility visibility = Visibility.HIDE;

    @SuppressWarnings("unchecked")
    @Override
    public void update(ToastManager manager, long time) {
        final var delta = MinecraftClient.getInstance().getRenderTickCounter().getDynamicDeltaTicks();

        int mouseX = 0; //(int)(client.mouse.getX() * (double) window.getScaledWidth() / (double) window.getWidth());
        int mouseY = 0; //(int)(client.mouse.getY() * (double) window.getScaledHeight() / (double) window.getHeight());

        this.rootComponent.update(delta, mouseX, mouseY);

        this.visibility = this.visibilityPredicate.isVisible((Self) this, time) ? Visibility.SHOW : Visibility.HIDE;
    }

    @Override
    public Visibility getVisibility() {
        return this.visibility;
    }

    @Override
    public void draw(DrawContext context, TextRenderer textRenderer, long time) {
        if (!(context instanceof OwoUIDrawContext)) context = OwoUIDrawContext.of(context);
        var owoContext = (OwoUIDrawContext) context;

        var tickCounter = MinecraftClient.getInstance().getRenderTickCounter();

        this.rootComponent.draw(owoContext, 0, 0, tickCounter.getTickProgress(false), tickCounter.getDynamicDeltaTicks());
        owoContext.createNewRootLayer();
    }

    @Override
    public int getHeight() {
        return this.rootComponent.fullSize().height();
    }

    @Override
    public int getWidth() {
        return this.rootComponent.fullSize().width();
    }

    public interface VisibilityPredicate<Self extends BaseGadgetToast<Self, ?>> {
        boolean isVisible(Self toast, long shownTime);
    }

    public static abstract class VerticalFlow<Self extends VerticalFlow<Self>> extends BaseGadgetToast<Self, FlowLayout> {
        public VerticalFlow(VisibilityPredicate<Self> visibilityPredicate) {
            super(Containers::verticalFlow, visibilityPredicate);
        }

        public VerticalFlow(Duration duration) {
            super(Containers::verticalFlow, duration);
        }
    }
}
