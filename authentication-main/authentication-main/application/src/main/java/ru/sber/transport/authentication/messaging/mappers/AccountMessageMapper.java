package ru.sber.transport.authentication.messaging.mappers;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.springframework.util.StringUtils;
import ru.sber.transport.authentication.business.dto.AccountDto;
import ru.sber.transport.authentication.business.dto.RoleDto;
import ru.sber.transport.authentication.business.dto.Scope;
import ru.sberbank.ditsib.transport.messaging.messages.UserMessage;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface AccountMessageMapper {

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    Collection<AccountDto> toBusiness(Collection<UserMessage> message);

    default AccountDto toBusiness(UserMessage message) {
        Map<String, Boolean> roles = null;
        if (message.roles() != null) {
            roles = message.roles().stream().collect(Collectors.toMap(Function.identity(), string -> false));
        }
        var login = Optional.ofNullable(message.login())
                .orElseGet(() -> generateUserLogin(message.firstName(), message.lastName(), message.patronymic()));
        return AccountDto.builder()
                .id(message.getId())
                .email(message.email())
                .phone(message.phone())
                .login(login)
                .hash(message.hash())
                .transferPassword(message.transportAccess())
                .scope(Optional.ofNullable(message.scope()).map(Enum::name).map(Scope::valueOf).orElse(Scope.EMPLOYEE))
                .roles(roles)
                .active(message.active())
                .build();
    }

    @Mapping(target = "code", source = "role")
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "description", ignore = true)
    RoleDto toBusiness(String role);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    Set<RoleDto> toBusiness(Set<String> roles);

    default String generateUserLogin(String firstname, String lastname, String patronymic) {
        if (lastname == null) {
            throw new NullPointerException("lastname is can not be null");
        } else {
            var transliteratedFirstName = firstname == null ? "" : StringUtils.capitalize(transliterate(firstname.trim().substring(0, 1)));
            var transliteratedLastName = StringUtils.capitalize(transliterate(lastname.trim().replace(" ", "")));
            var transliteratedPatronymic = Optional.ofNullable(patronymic)
                    .filter(source -> !source.isBlank())
                    .map(String::trim)
                    .map(source -> source.substring(0, 1))
                    .map(this::transliterate)
                    .map(source -> "-" + source)
                    .orElse("");
            return "%s%s%s".formatted(transliteratedLastName, transliteratedFirstName, transliteratedPatronymic);
        }
    }

    default String transliterate(String source) {
        char[] abcCyr = new char[]{'а', 'б', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'л', 'м', 'н', 'о', 'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь', 'э', 'ю', 'я', 'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ё', 'Ж', 'З', 'И', 'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', 'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь', 'Э', 'Ю', 'Я', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        String[] abcLat = new String[]{"a", "b", "v", "g", "d", "e", "e", "zh", "z", "i", "y", "k", "l", "m", "n", "o", "p", "r", "s", "t", "u", "f", "h", "ts", "ch", "sh", "sch", "", "i", "", "e", "ju", "ja", "A", "B", "V", "G", "D", "E", "E", "Zh", "Z", "I", "Y", "K", "L", "M", "N", "O", "P", "R", "S", "T", "U", "F", "H", "Ts", "Ch", "Sh", "Sch", "", "I", "", "E", "Ju", "Ja", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        StringBuilder builder = new StringBuilder();

        for(int i = 0; i < source.length(); ++i) {
            char srcChar = source.charAt(i);
            if (Character.isAlphabetic(srcChar)) {
                for(int x = 0; x < abcCyr.length; ++x) {
                    if (srcChar == abcCyr[x]) {
                        builder.append(abcLat[x]);
                    }
                }
            } else {
                builder.append(srcChar);
            }
        }

        return builder.toString();
    }

}
