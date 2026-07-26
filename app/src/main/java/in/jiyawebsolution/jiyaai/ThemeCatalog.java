package in.jiyawebsolution.jiyaai;

public final class ThemeCatalog {
    public static final class Theme {
        public final String name;
        public final int background, panel, panelAlt, primary, accent, success, pattern, radius;

        Theme(String name, String background, String panel, String panelAlt,
              String primary, String accent, String success, int pattern, int radius) {
            this.name = name;
            this.background = color(background);
            this.panel = color(panel);
            this.panelAlt = color(panelAlt);
            this.primary = color(primary);
            this.accent = color(accent);
            this.success = color(success);
            this.pattern = pattern;
            this.radius = radius;
        }
    }

    public static final Theme[] ALL = {
            t("Quantum Violet", "#050811", "#0C1422", "#101B2C", "#35F4FF", "#A96BFF", "#65FFB1", 0, 22),
            t("Cyber Rose", "#0B050C", "#1B0D19", "#281225", "#FF4FD8", "#9D6BFF", "#72FFD2", 1, 26),
            t("Matrix Green", "#020905", "#07150D", "#0B2114", "#38FF79", "#B7FF39", "#54FFD6", 2, 8),
            t("Solar Flare", "#0D0702", "#211007", "#32170A", "#FFB52E", "#FF5D31", "#FFF36A", 3, 18),
            t("Arctic Pulse", "#02090D", "#071820", "#0B2732", "#65E8FF", "#9FCBFF", "#7DFFD8", 4, 30),
            t("Blood Moon", "#0D0205", "#21070B", "#310B12", "#FF3B5C", "#FF8457", "#D4FF62", 5, 12),
            t("Royal Gold", "#090703", "#1C1608", "#2B210A", "#FFD45A", "#FF9D2E", "#FFF1A8", 0, 6),
            t("Ocean Reactor", "#020812", "#07182B", "#0B2440", "#26B9FF", "#316BFF", "#4DFFD2", 1, 24),
            t("Plasma Pink", "#0A0310", "#190923", "#281039", "#FF54F7", "#7D4DFF", "#4DFFF3", 2, 32),
            t("Toxic Lime", "#070A02", "#121A07", "#1D290A", "#B8FF35", "#5DFF68", "#F2FF6A", 3, 10),
            t("Titanium", "#06080A", "#11161B", "#1B222A", "#C9D5E2", "#7089A6", "#73E7FF", 4, 4),
            t("Inferno Core", "#0C0301", "#201008", "#35170A", "#FF6534", "#FFD23F", "#FF4365", 5, 20),
            t("Neon Sakura", "#0B050A", "#1B0F19", "#2A1625", "#FF8CCF", "#C77DFF", "#8FFFF0", 0, 28),
            t("Emerald Glass", "#020A08", "#071A16", "#0D2A23", "#44FFD1", "#23B98F", "#A1FF78", 1, 34),
            t("Ultraviolet", "#07030E", "#150A24", "#22103A", "#B260FF", "#6D3CFF", "#42E8FF", 2, 14),
            t("Amber Grid", "#0B0802", "#1B1407", "#2A1E0A", "#FFC247", "#FF7D2E", "#E8FF62", 3, 2),
            t("Ice Crystal", "#04090D", "#0A1720", "#102634", "#B9F3FF", "#5CBFFF", "#D6FFF4", 4, 16),
            t("Crimson Ops", "#0A0304", "#190A0D", "#291014", "#FF5365", "#B81835", "#FFB14D", 5, 5),
            t("Electric Indigo", "#03040D", "#0A0D20", "#11163A", "#6878FF", "#C05CFF", "#45F5FF", 0, 23),
            t("Mint Circuit", "#030A08", "#091B16", "#0E2A22", "#73FFD0", "#38DDA8", "#E4FF6B", 1, 19),
            t("Copper Engine", "#0A0603", "#1C110A", "#2D1A0E", "#E99952", "#FFCC72", "#7CFFF2", 2, 9),
            t("Blue Phantom", "#02050C", "#070F20", "#0B1934", "#498DFF", "#58E1FF", "#866BFF", 3, 27),
            t("White Nova", "#08090B", "#15181D", "#22262E", "#FFFFFF", "#86AFFF", "#79FFE1", 4, 36),
            t("Black Hacker", "#010302", "#050A07", "#0A120E", "#00FF66", "#00A843", "#B4FF38", 5, 0)
    };

    private ThemeCatalog() {}

    public static Theme get(int index) {
        return ALL[Math.max(0, Math.min(index, ALL.length - 1))];
    }

    private static Theme t(String name, String bg, String panel, String panelAlt,
                           String primary, String accent, String success, int pattern, int radius) {
        return new Theme(name, bg, panel, panelAlt, primary, accent, success, pattern, radius);
    }

    private static int color(String value) {
        return android.graphics.Color.parseColor(value);
    }
}
