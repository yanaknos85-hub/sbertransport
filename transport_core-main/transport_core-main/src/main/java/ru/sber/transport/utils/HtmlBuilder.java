package ru.sber.transport.utils;

import java.util.List;
import java.util.Optional;

public class HtmlBuilder {

    public static final Integer DEFAULT_FONT_SIZE = 14;
    public static final String DEFAULT_FONT = "Arial";
    public static final String DEFAULT_COLOR = "#333";

    private final StringBuilder html;

    /**
     * Конструктор с дефолтными значениями
     */
    public HtmlBuilder() {
        this(DEFAULT_FONT_SIZE, DEFAULT_FONT, DEFAULT_COLOR);
    }

    /**
     * Конструктор
     * @param fontSize размер шрифта
     * @param font шрифт
     * @param color цвет текста
     */
    public HtmlBuilder(Integer fontSize, String font, String color) {
        this.html = new StringBuilder();
        initHtml(fontSize, font, color);
    }

    /**
     * Конструктор, который берет HTML из строки
     * @param raw HTML
     */
    private HtmlBuilder(String raw) {
        this.html = new StringBuilder();
        html.append(raw);
    }

    public HtmlBuilder h1(String text) {
        return wrapWithTag("h1", text);
    }

    public HtmlBuilder h2(String text) {
        return wrapWithTag("h2", text);
    }

    public HtmlBuilder h3(String text) {
        return wrapWithTag("h3", text);
    }

    public HtmlBuilder paragraph(String text) {
        return wrapWithTag("p", text);
    }

    public HtmlBuilder hr() {
        html.append("</hr>\n");
        return this;
    }

    public HtmlBuilder br() {
        html.append("</br>\n");
        return this;
    }

    public HtmlBuilder list(List<String> items) {
        if (items == null || items.isEmpty()) {
            return this;
        }

        html.append("<ul>\n");
        items.forEach(item -> wrapWithTag("li", item));
        html.append("</ul>\n");
        return this;
    }

    public HtmlBuilder orderedList(List<String> items) {
        if (items == null || items.isEmpty()) {
            return this;
        }

        html.append("<ol>\n");
        items.forEach(item -> wrapWithTag("li", item));
        html.append("</ol>\n");
        return this;
    }

    public HtmlBuilder bold(String text) {
        return wrapWithTag("b", text);
    }

    public HtmlBuilder wrapWithTag(String tag, String text) {
        if (text == null || tag == null) {
            return this;
        }

        html.append("<")
                .append(tag)
                .append(">")
                .append(escape(text))
                .append("</")
                .append(tag)
                .append(">\n");

        return this;
    }

    public String build() {
        html.append("</body>\n");
        return html.toString();
    }

    /**
     * Собирает билдер из HTML
     * @param html HTML
     * @return Билдер
     */
    public static HtmlBuilder fromHtml(String html) {
        // ищем <body>
        String bodyStartRegex = "(?i)<body[^>]*>";
        String bodyEndRegex = "(?i)</body>";

        // начало <body>
        java.util.regex.Matcher startMatcher = java.util.regex.Pattern
                .compile(bodyStartRegex)
                .matcher(html);

        // конец </body>
        java.util.regex.Matcher endMatcher = java.util.regex.Pattern
                .compile(bodyEndRegex)
                .matcher(html);

        if (!startMatcher.find() || !endMatcher.find()) {
            throw new IllegalArgumentException("В HTML нет корректного <body>...</body>");
        }

        int bodyEnd = endMatcher.start();

        // вытаскиваем HTML внутри body
        String extracted = html.substring(0, bodyEnd);

        return new HtmlBuilder(extracted);
    }

    private void initHtml(Integer fontSize, String font, String color) {
        html.append("<!DOCTYPE html>")
                .append("<html>\n")
                .append("<head>\n")
                .append("<meta charset=\"UTF-8\">\n")
                .append("<body style=\"font-family:%s; font-size:%spx; line-height:1.5; color:%s;\">".formatted(font, fontSize, color))
                .append("</head>\n")
                .append("<body>\n");
    }

    private String escape(String text) {
        return Optional.ofNullable(text)
                .map(t -> t.replace("&", "&amp;"))
                .map(t -> t.replace("<", "&lt;"))
                .map(t -> t.replace(">", "&lg;"))
                .orElse(null);
    }
}