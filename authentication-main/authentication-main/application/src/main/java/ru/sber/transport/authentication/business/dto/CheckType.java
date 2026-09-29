package ru.sber.transport.authentication.business.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * Типы проверок.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum CheckType {
    
    /**
     * Эквивалентность логина и пароля.
     */
    PASSWORD_EQUALS_LOGIN("Password equals login", String::equalsIgnoreCase),
    
    /**
     * Длина пароля.
     */
    SHORT_PASSWORD("Short password", (login, password) -> password.length() < 8),
    
    /**
     * Символы пароля.
     */
    SYMBOLS("Letters or numbers only", (login, password) -> !isAlphaNumeric(password)),
    
    /**
     * Разнообразие символов.
     */
    SAME_SYMBOLS("Same symbols", (login, password) -> countUniqueCharacters(password) < 6),
    
    
    /**
     * Простые сочетания символов.
     */
    WEAK_SEQUENCE("Weak password", (login, password) -> isIncludeForbiddenSubstring(password));
    
    /**
     * Название проверки.
     */
    private final String checkName;
    
    /**
     * Предикат проверки.
     */
    private final BiPredicate<String, String> check;
    
    /**
     *  Список простых сочетаний.
     */
    private static final List<String> stringList = Arrays.asList("1234567890-=",
                                                                 "йцукенгшщзхъфывапролджэячсмитьбю",
                                                                 "qwertyuiop[]asdfghjkl,’zxcvbnm,./",
                                                                 "qazwsxedcrfvtgbyhnujmik,ol.p,/[‘]",
                                                                 "йфяцычувскамепинртгоьшлбщдюзж.хэъ",
                                                                 "741852963",
                                                                 "ЙЦУКЕНГШЩЗХЪФЫВАПРОЛДЖЭЯЧСМИТЬБЮ",
                                                                 "QWERTYUIOP[]ASDFGHJKL,’ZXCVBNM,./",
                                                                 "QAZWSXEDCRFVTGBYHNUJMIK,OL.P,/[‘]",
                                                                 "ЙФЯЦЫЧУВСКАМЕПИНРТГОЬШЛБЩДЮЗЖ.ХЭЪ.",
                                                                 "1йфя2цыч3увс4кам5епи6нрт7гоь8шлб9щдю0зж.-хэ=ъ",
                                                                 "1ЙФЯ2ЦЫЧ3УВС4КАМ5ЕПИ6НРТ7ГОЬ8ШЛБ9ЩДЮ0ЗЖ.-ХЭ",
                                                                 "1QAZ2WSX3EDC4RFV5TGB6YHN7UJM8IK,9OL.0P,/-[‘=]",
                                                                 "1qaz2wsx3edc4rfv5tgb6yhn7ujm8ik,9ol.0p,/-[‘=]",
                                                                 "1q2w3e4r5t6y7u8i9o0p-[=]azsxdcfvgbhnjmk,l.,/’qawsedrftgyhujikolp,[‘]",
                                                                 "1Q2W3E4R5T6Y7U8I9O0P-[=]AZSXDCFVGBHNJMK,L.,/’QAWSEDRFTGYHUJIKOLP,[‘]",
                                                                 "1Й2Ц3У4К5Е6Н7Г8Ш9Щ0З-Х=ЪФЯЫЧВСАМПИРТОЬЛБДЮЖ.ЭЙФЦЫУВКАЕПНРГОШЛЩДЗЖХЭЪ",
                                                                 "1й2ц3у4к5е6н7г8ш9щ0з-х=ъфяычвсампиртоьлбдюж.эйфцыувкаепнргошлщдзжхэъ.");
    
    /**
     * Проверка наличия букв.
     *
     * @param source исходная строка.
     * @return наличие символов.
     */
    private static boolean isAlphaNumeric(String source) {
        var chars = source.toCharArray();
        
        for (var c : chars) {
            if (!Character.isLetter(c) && !Character.isDigit(c)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Подсчет уникальных символов.
     *
     * @param input исходная строка.
     * @return количество уникальных символов.
     */
    public static long countUniqueCharacters(String input) {
        return input.chars()
                    .distinct()
                    .count();
    }
    
    /**
     * Проверка включения в пароль простых комбинаций.
     *
     * @param password исходная строка.
     * @return наличие простых комбинаций.
     */
    private static boolean isIncludeForbiddenSubstring(String password) {
        for (String s : stringList) {
            if (substringExists(s, password)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Поиск вхождений простых комбинаций в пароль.
     *
     * @param sequence набор комбинаций.
     * @param password пароль.
     * @return вхождение простых комбинаций.
     */
    private static boolean substringExists(String sequence, String password) {
        for (var i = 0; i < sequence.length() - 3; i++) {
            var substring = sequence.substring(i, i + 3);
            if (password.contains(substring)) {
                return true;
            }
        }
        return false;
    }
}
