package dev.teammenu.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public abstract class TmScreen extends Screen {
    protected static int BORDER, PANEL, HEADER, ACCENT, ROW, TEXT, DIM;
    protected static boolean SHADOW = true;
    protected static final int
            GOLD = 0xFFFFC857, GREEN = 0xFF7CE38B, RED = 0xFFFF7B72;

    protected record Act(String label, Runnable run) {}

    /** Берёт цвета из выбранного стиля. */
    static void applyStyle() {
        Ui u = Ui.get;
        int[] t = u.toneColors();
        BORDER = t[0]; PANEL = t[1]; HEADER = t[2]; ROW = t[3]; TEXT = t[4]; DIM = t[5];
        if (u.textColor() != 0) TEXT = u.textColor();
        ACCENT = TEXT;
        SHADOW = !u.light();
        if (u.style == 1) BORDER = 0;
        if (u.style == 2) { PANEL = PANEL & 0xFFFFFF | 0x90000000; HEADER = HEADER & 0xFFFFFF | 0xB0000000; }
    }

    private static String notice;
    private static long noticeUntil, creditsUntil;

    protected final Screen parent;
    protected int pw, ph, left, top, scroll;
    private int maxScroll;
    private String lastSig = "";
    private GuiGraphicsExtractor g;
    private final List<Runnable> back = new ArrayList<>(), front = new ArrayList<>();

    protected TmScreen(String title, Screen parent) {
        super(Component.literal(title));
        this.parent = parent;
    }

    protected int panelWidth() { return 300; }
    protected int panelHeight() { return 200; }
    protected abstract void build();
    /** Строка-«отпечаток» содержимого: если после синхронизации изменилась, экран перестраивается. */
    protected String sig() { return ""; }
    protected String heading() { return title.getString(); }

    public static void showNotice(String s) { notice = s; noticeUntil = System.currentTimeMillis() + 4000; }

    public void onSync() { if (!sig().equals(lastSig)) rebuildWidgets(); }

    @Override
    protected void init() {
        applyStyle();
        if (creditsUntil == 0) creditsUntil = System.currentTimeMillis() + 20_000;
        back.clear();
        front.clear();
        maxScroll = 0;
        pw = Math.min(panelWidth(), width - 16);
        ph = Math.min(panelHeight(), height - 16);
        left = (width - pw) / 2;
        top = (height - ph) / 2;
        lastSig = sig();
        // выбор языка в правом верхнем углу
        btn(Ui.get.english ? "EN" : "RU", left + pw - 26, top + 2, 24, 18, () -> {
            Ui.get.english = !Ui.get.english;
            Ui.save();
            rebuildWidgets();
        });
        build();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float pt) {
        super.extractBackground(g, mx, my, pt);
        this.g = g;
        g.fill(left - 1, top - 1, left + pw + 1, top + ph + 1, BORDER);
        g.fill(left, top, left + pw, top + ph, PANEL);
        g.fill(left, top, left + pw, top + 22, HEADER);
        g.fill(left, top + 22, left + pw, top + 23, ACCENT);
        back.forEach(Runnable::run);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float pt) {
        super.extractRenderState(g, mx, my, pt);
        this.g = g;
        centered(heading(), left + pw / 2, top + 7, TEXT);
        front.forEach(Runnable::run);
        if (notice != null && System.currentTimeMillis() < noticeUntil)
            g.centeredText(font, notice, width / 2, Math.min(height - 10, top + ph + 5), RED);
        if (System.currentTimeMillis() < creditsUntil) { // маленькая полупрозрачная метка, 20 секунд
            var pose = g.pose();
            pose.pushMatrix();
            pose.translate(4, height - 8);
            pose.scale(0.7f, 0.7f);
            g.text(font, Ui.tr("создатели") + ": Dekkabraz x Js_PsYY", 0, 0, 0x66FFFFFF, false);
            pose.popMatrix();
        }
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        int next = Math.max(0, Math.min(maxScroll, scroll - (int) Math.signum(dy)));
        if (next != scroll) { scroll = next; rebuildWidgets(); }
        return true;
    }

    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() { minecraft.gui.setScreen(parent); }
    protected void open(Screen s) { minecraft.gui.setScreen(s); }

    // ---------- рисование
    protected void text(String s, int x, int y, int color) { g.text(font, Ui.tr(s), x, y, color, SHADOW); }
    protected void label(String s, int x, int y, int color) { front.add(() -> text(s, x, y, color)); }
    protected void labelCentered(String s, int x, int y, int color) { front.add(() -> centered(s, x, y, color)); }
    protected void centered(String s, int x, int y, int color) {
        String t = Ui.tr(s);
        g.text(font, t, x - font.width(t) / 2, y, color, SHADOW);
    }
    protected void paint(Runnable r) { front.add(r); }
    protected void stripe(int y, int h) { back.add(() -> g.fill(left + 6, y, left + pw - 6, y + h, ROW)); }
    protected String fit(String raw, int w) { String s = Ui.tr(raw); return font.width(s) <= w ? s : font.plainSubstrByWidth(s, w - 6) + "…"; }

    /** Голова скина игрока 16x16. */
    protected void face(String uuid, int x, int y) {
        front.add(() -> {
            UUID id = UUID.fromString(uuid);
            PlayerInfo info = minecraft.getConnection() == null ? null : minecraft.getConnection().getPlayerInfo(id);
            var skin = info != null ? info.getSkin() : DefaultPlayerSkin.get(id);
            PlayerFaceExtractor.extractRenderState(g, skin, x, y, 16);
        });
    }

    /** Иконка команды: серый квадрат с первыми буквами названия. */
    protected void icon(String name, int x, int y) {
        front.add(() -> {
            int c = 0x50 + Math.floorMod(name.hashCode(), 0x70);
            g.fill(x, y, x + 16, y + 16, 0xFF000000 | c << 16 | c << 8 | c);
            String s = name.substring(0, Math.min(2, name.length())).toUpperCase();
            g.text(font, s, x + 8 - font.width(s) / 2, y + 4, c > 0x90 ? 0xFF000000 : 0xFFFFFFFF, false);
        });
    }

    /** Маленькая кнопка-палитра: открывает выбор стиля. */
    protected void palette(int x, int y) {
        btn("", x, y, 20, () -> open(new Screens.StyleScreen(this)));
        int[] c = {0xFFE5534B, 0xFFFFC857, 0xFF7CE38B, 0xFF4FC3F7};
        front.add(() -> {
            for (int i = 0; i < 4; i++)
                g.fill(x + 5 + i % 2 * 6, y + 5 + i / 2 * 6, x + 9 + i % 2 * 6, y + 9 + i / 2 * 6, c[i]);
        });
    }

    // ---------- виджеты
    protected Button btn(String label, int x, int y, int w, int h, Runnable action) {
        return addRenderableWidget(Button.builder(Component.literal(Ui.tr(label)), b -> action.run()).bounds(x, y, w, h).build());
    }
    protected Button btn(Component label, int x, int y, int w, Runnable action) {
        return addRenderableWidget(Button.builder(label, b -> action.run()).bounds(x, y, w, 20).build());
    }
    protected Button btn(String label, int x, int y, int w, Runnable action) {
        return btn(Component.literal(Ui.tr(label)), x, y, w, action);
    }
    protected EditBox box(int x, int y, int w, String value, int max, Consumer<String> onChange) {
        EditBox b = new EditBox(font, x, y, w, 20, Component.empty());
        b.setMaxLength(max);
        b.setValue(value);
        b.setResponder(onChange);
        return addRenderableWidget(b);
    }
    /** Ряд кнопок одинаковой ширины на всю панель. */
    protected void row(int y, List<Act> acts) {
        int n = acts.size(), gap = 4, w = (pw - 16 - gap * (n - 1)) / n;
        for (int i = 0; i < n; i++) btn(acts.get(i).label(), left + 8 + i * (w + gap), y, w, acts.get(i).run());
    }
    /** Сетка кнопок по две в строке; возвращает y под сеткой. */
    protected int grid(int y, List<Act> acts) {
        int gap = 4, w = (pw - 16 - gap) / 2;
        for (int i = 0; i < acts.size(); i++)
            btn(acts.get(i).label(), left + 8 + i % 2 * (w + gap), y + i / 2 * 24, w, acts.get(i).run());
        return y + (acts.size() + 1) / 2 * 24;
    }
    /** Нижний ряд: кнопки слева, справа маленькая палитра. */
    protected void bottom(List<Act> acts) {
        int n = acts.size(), gap = 4, w = (pw - 40 - gap * (n - 1)) / n, y = top + ph - 28;
        for (int i = 0; i < n; i++) btn(acts.get(i).label(), left + 8 + i * (w + gap), y, w, acts.get(i).run());
        palette(left + pw - 28, y);
    }
    protected static Component toggleText(String name, boolean on) {
        return Component.literal(Ui.tr(name) + ": ").append(
                Component.literal(Ui.tr(on ? "ВКЛ" : "ВЫКЛ")).withStyle(on ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    /** Прокручиваемый колесом список строк высотой 26. */
    protected void list(List<IntConsumer> items, int y0, int y1) {
        int rows = Math.max(1, (y1 - y0) / 26);
        maxScroll = Math.max(0, items.size() - rows);
        scroll = Math.min(scroll, maxScroll);
        for (int i = 0; i < rows && i + scroll < items.size(); i++) items.get(i + scroll).accept(y0 + i * 26);
        if (maxScroll > 0) {
            int h = y1 - y0, th = Math.max(8, h * rows / items.size()), ty = y0 + (h - th) * scroll / maxScroll, x = left + pw - 4;
            back.add(() -> { g.fill(x, y0, x + 2, y1, ROW); g.fill(x, ty, x + 2, ty + th, ACCENT); });
        }
    }
}