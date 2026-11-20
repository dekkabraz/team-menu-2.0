package dev.teammenu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Config {
    /** true — игрока без мода не пускает на сервер; false — мод необязателен. */
    public boolean required = false;
    /** Могут ли обычные игроки создавать команды (админ может всегда). */
    public boolean allowCreate = true;
    /** Какие типы доступа можно выбирать. */
    public boolean allowHidden = true, allowPrivate = true, allowPublic = true;
    /** Какие настройки команды доступны. Выключенная здесь настройка выключена у всех команд. */
    public boolean allowChat = true, allowCoords = true, allowNametags = true, allowFriendlyFire = true, allowSeeInvisible = true;
    public boolean allowVoice = true, allowPrivacy = true;
    /** true — ники над головой скрыты у всех; свои видит только команда с настройкой «ники только для своих». */
    public boolean hideAllNametags = false;
    /** Максимум участников в команде, 0 — без ограничения. */
    public int maxMembers = 0;
    /** Цвет метки команды в чате: номер из COLORS. Меняется в панели админа. */
    public int chatColor = 0;
    /** Объявление над меню у всех игроков, пустое — не показывать. */
    public String menuNote = "";

    public static final String[] COLORS = {"AQUA", "GOLD", "GREEN", "LIGHT_PURPLE", "YELLOW", "WHITE"};
    public static final String[] COLOR_NAMES = {"Голубой", "Золотой", "Зелёный", "Розовый", "Жёлтый", "Белый"};

    public static Config get = new Config();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Маска разрешённых настроек, порядок битов как в Model.Settings. */
    public int bits() {
        return (allowChat ? 1 : 0) | (allowCoords ? 2 : 0) | (allowNametags ? 4 : 0) | (allowFriendlyFire ? 8 : 0) | (allowSeeInvisible ? 16 : 0);
    }
    /** Значение переключателя по имени поля (для панели админа). */
    public boolean on(String key) {
        try {
            java.lang.reflect.Field f = Config.class.getField(key);
            return f.getType() == boolean.class && f.getBoolean(this);
        } catch (ReflectiveOperationException e) { return false; }
    }
    /** Переключает поле по имени; false — такого переключателя нет. */
    public boolean flip(String key) {
        try {
            java.lang.reflect.Field f = Config.class.getField(key);
            if (f.getType() != boolean.class) return false;
            f.setBoolean(this, !f.getBoolean(this));
            return true;
        } catch (ReflectiveOperationException e) { return false; }
    }
    static void save() {
        try {
            Files.writeString(FabricLoader.getInstance().getConfigDir().resolve("teammenu.json"), GSON.toJson(get));
        } catch (Exception e) {
            TeamMenu.LOG.error("Не удалось сохранить config/teammenu.json", e);
        }
    }
    public boolean allows(Model.Access a) {
        return switch (a) { case HIDDEN -> allowHidden; case PRIVATE -> allowPrivate; case PUBLIC -> allowPublic; };
    }

    static void load() {
        Path f = FabricLoader.getInstance().getConfigDir().resolve("teammenu.json");
        try {
            if (Files.exists(f)) {
                Config c = GSON.fromJson(Files.readString(f), Config.class);
                if (c != null) get = c;
            }
            Files.writeString(f, GSON.toJson(get));
        } catch (Exception e) {
            TeamMenu.LOG.error("Не удалось прочитать config/teammenu.json", e);
        }
    }
}