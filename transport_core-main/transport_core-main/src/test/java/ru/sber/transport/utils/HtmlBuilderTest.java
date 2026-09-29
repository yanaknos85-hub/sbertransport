package ru.sber.transport.utils;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("lib_transport_core")
@DisplayName("Тест билдера HTML")
class HtmlBuilderTest {

    @Test
    @DisplayName("Тест билдера HTML")
    void testBuild() {
        var expected = """
                <!DOCTYPE html><html>
                <head>
                <meta charset="UTF-8">
                <body style="font-family:Arial; font-size:14px; line-height:1.5; color:#333;"></head>
                <body>
                <h1>h1</h1>
                <h2>h2</h2>
                <h3>h3</h3>
                <b>bold</b>
                <p>paragraph</p>
                </br>
                </hr>
                <ol>
                <li>Пункт 1</li>
                <li>Пункт 2</li>
                </ol>
                <ul>
                <li>Пункт 1</li>
                <li>Пункт 2</li>
                </ul>
                <p>&lt;&lg;&amp;</p>
                <p>appended</p>
                </body>
                """;

        var actual = new HtmlBuilder()
                .h1("h1")
                .h2("h2")
                .h3("h3")
                .bold("bold")
                .paragraph("paragraph")
                .br()
                .hr()
                .orderedList(List.of("Пункт 1", "Пункт 2"))
                .list(List.of("Пункт 1", "Пункт 2"))
                .paragraph("<>&")
                .build();

        actual = HtmlBuilder.fromHtml(actual)
                .paragraph("appended")
                .build();

        assertThat(actual)
                .isEqualTo(expected);
    }
}