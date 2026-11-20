package dev.teammenu.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Ui {
    public int style, tone, text;
    public boolean english;

    public static Ui get = new Ui();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    static final String[] STYLES = {"С рамкой", "Плоский", "Прозрачный"};
    static final String[] TONES = {"Чёрный", "Серый", "Светлый"};
    static final String[] TEXTS = {"Обычный", "Серый", "Жёлтый", "Голубой", "Зелёный"};
    // рамка, панель, шапка, строка списка, текст, приглушённый текст
    private static final int[][] TONE = {
            {0xFF3A3A3A, 0xF0101010, 0xFF1C1C1C, 0x22FFFFFF, 0xFFFFFFFF, 0xFFA0A0A0},
            {0xFF5A5A5A, 0xF02B2B2B, 0xFF3A3A3A, 0x22FFFFFF, 0xFFFFFFFF, 0xFFB4B4B4},
            {0xFF8A8A8A, 0xF0D8D8D8, 0xFFBDBDBD, 0x22000000, 0xFF151515, 0xFF4A4A4A}};
    private static final int[] TEXT = {0, 0xFFB0B0B0, 0xFFFFC857, 0xFF4FC3F7, 0xFF7CE38B};

    int[] toneColors() { return TONE[tone]; }
    int textColor() { return TEXT[text]; }
    boolean light() { return tone == 2; }

    private static Path file() { return FabricLoader.getInstance().getConfigDir().resolve("teammenu-client.json"); }

    static void load() {
        try {
            if (Files.exists(file())) {
                Ui u = GSON.fromJson(Files.readString(file()), Ui.class);
                if (u != null) get = u;
            }
        } catch (Exception ignored) { }
        get.style = Math.floorMod(get.style, STYLES.length);
        get.tone = Math.floorMod(get.tone, TONES.length);
        get.text = Math.floorMod(get.text, TEXTS.length);
    }

    static void save() {
        try { Files.writeString(file(), GSON.toJson(get)); } catch (Exception ignored) { }
    }

    // ---------- перевод: русская фраза -> английская
    private static final String[] D = {
            "Меню команд", "Team menu",
            "Вы пока без команды: вступите или создайте свою", "You have no team yet: join one or create your own",
            "Команды и игроки", "Teams and players", "Команды", "Teams", "Команда", "Team", "команда", "team",
            "Приглашения команды", "Team invites", "Приглашения", "Invites", "Приглашений пока нет", "No invites yet",
            "Создать команду", "Create team", "Создать", "Create",
            "Доступные чат-команды", "Available chat commands",
            "Закрыть", "Close", "Назад", "Back", "Готово", "Done", "Отмена", "Cancel", "Да", "Yes", "Сохранить", "Save",
            "Название команды", "Team name", "Название роли", "Role name", "Название", "Name", "название", "name",
            "Доступ", "Access", "Скрытая", "Hidden", "Приватная", "Private", "Публичная", "Public",
            "Видна только участникам и админам", "Visible only to members and admins",
            "Видна всем, вступление по заявке или приглашению", "Visible to all, join by request or invite",
            "Вступить может любой игрок", "Anyone can join",
            "Настроить", "Configure", "Настройки команды", "Team settings", "Настройки", "Settings",
            "Чат команды у меня", "Team chat for me", "Чат команды", "Team chat",
            "Координаты участников", "Member coordinates",
            "Ники видны только своей команде", "Name tags only for own team",
            "Урон по своим", "Friendly fire", "Видеть невидимых союзников", "See invisible teammates",
            "ВКЛ", "ON", "ВЫКЛ", "OFF", "отключено на сервере", "disabled on this server",
            "Команд пока нет", "No teams yet", "Участники", "Members", "участников", "members",
            "Открыть", "Open", "Вступить", "Join", "Отправлено", "Sent", "Запрос", "Request",
            "Игроки без команды", "Players without a team", "Приглашён", "Invited", "Пригласить", "Invite",
            "Принять", "Accept", "Отклонить", "Decline", "Отменить", "Cancel",
            "Удалить команду", "Delete team", "Удалить", "Delete", "Войс команды", "Team voice",
            "Все команды", "All teams", "Выйти из команды", "Leave team", "Выйти", "Leave",
            "Скрытно: в команде", "Hidden: in team", "Войти скрытно", "Join hidden",
            "Вы уверены, что хотите удалить", "Are you sure you want to remove",
            "Роль", "Role", "Участник", "Member", "Создатель", "Owner",
            "Приватность", "Privacy", "Скрыть мои координаты", "Hide my coordinates",
            "Админ сервера видит координаты всегда", "Server admins always see coordinates",
            "координаты скрыты", "coordinates hidden", "не в сети", "offline", "мут", "muted",
            "Мир", "Overworld", "Ад", "Nether", "Энд", "End", "Другое", "Other",
            "Пригласить игрока по нику", "Invite a player by name",
            "Запросы на вступление", "Join requests", "Уже приглашены", "Already invited",
            "Место в списке участников", "Position in the member list", "из", "of",
            "Подтверждение", "Confirmation",
            "Стиль меню", "Menu style", "Стиль", "Style", "Тон", "Tone", "Текст", "Text",
            "С рамкой", "Framed", "Плоский", "Flat", "Прозрачный", "Transparent",
            "Чёрный", "Black", "Серый", "Grey", "Светлый", "Light",
            "Обычный", "Default", "Жёлтый", "Yellow", "Голубой", "Blue", "Зелёный", "Green",
            "создатели", "creators", "текст", "text", "ник", "player",
            "написать в чат команды", "write to team chat", "выйти из команды", "leave the team",
            "скрыть / показать свои координаты", "hide / show your coordinates",
            "выключить / включить чат команды у себя", "turn team chat off / on for yourself",
            "создать приватную команду", "create a private team",
            "вступить или отправить запрос", "join or send a request",
            "принять приглашение", "accept an invite", "отклонить приглашение", "decline an invite",
            "список команд", "list of teams", "пригласить игрока", "invite a player",
            "удалить участника", "remove a member", "мут / размут в чате команды", "mute / unmute in team chat",
            "передать права создателя", "transfer ownership",
            "удалить команду", "delete the team", "с подтверждением", "with confirmation",
            "Панель админа", "Admin panel", "Админ", "Admin",
            "Изменения применяются сразу для всех", "Changes apply to everyone immediately",
            "Мод обязателен", "Mod required", "Создание команд", "Team creation",
            "Скрытые команды", "Hidden teams", "Приватные команды", "Private teams", "Публичные команды", "Public teams",
            "Чат команд", "Team chats", "Координаты", "Coordinates", "Ники только своим", "Own-team name tags",
            "Видеть невидимых", "See invisible", "Войс команд", "Team voice groups",
            "Приватность игроков", "Player privacy", "Скрыть все ники", "Hide all name tags",
            "Лимит участников", "Member limit", "нет", "none", "Цвет чата", "Chat colour",
            "Золотой", "Gold", "Розовый", "Pink", "Белый", "White",
            "Объявление в меню", "Menu announcement", "Пустое поле — без объявления", "Empty field — no announcement",
            "Пароль войса", "Voice password", "Пустое поле — без пароля", "Empty field — no password",
            "Участники входят кнопкой без пароля", "Members join with the button, no password"};

    private static final List<String[]> PAIRS = new ArrayList<>();
    private static final Map<String, String> cache = new HashMap<>();
    static {
        for (int i = 0; i + 1 < D.length; i += 2) PAIRS.add(new String[]{D[i], D[i + 1]});
        PAIRS.sort((a, b) -> b[0].length() - a[0].length()); // длинные фразы раньше коротких
    }

    /** Переводит строку, если выбран английский. Результат запоминается, чтобы не считать каждый кадр. */
    static String tr(String s) {
        if (!get.english || s.isEmpty()) return s;
        String c = cache.get(s);
        if (c != null) return c;
        String out = s;
        for (String[] p : PAIRS) out = swap(out, p[0], p[1]);
        if (cache.size() > 2000) cache.clear();
        cache.put(s, out);
        return out;
    }

    /** Замена только целых слов и фраз. */
    private static String swap(String s, String a, String b) {
        int i = s.indexOf(a);
        while (i >= 0) {
            int e = i + a.length();
            boolean whole = (i == 0 || !Character.isLetter(s.charAt(i - 1))) && (e == s.length() || !Character.isLetter(s.charAt(e)));
            if (whole) { s = s.substring(0, i) + b + s.substring(e); i = s.indexOf(a, i + b.length()); }
            else i = s.indexOf(a, i + 1);
        }
        return s;
    }
}