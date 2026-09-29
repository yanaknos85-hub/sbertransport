package ru.sber.transport.cargo.exchange.request.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.cargo.exchange.request.database.model.CarrierReply;
import ru.sber.transport.cargo.exchange.request.database.model.Organization;
import ru.sber.transport.cargo.exchange.request.database.model.Request;
import ru.sber.transport.cargo.exchange.request.dto.CarrierReplyDto;
import ru.sber.transport.cargo.exchange.request.dto.CarrierReplyShortDto;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Убедитесь, что реализация доступна
class ReplyMapperTest {
    private final ReplyMapperImpl mapper = new ReplyMapperImpl();

    private UUID requestId;
    private UUID orgId;
    private Organization organization;

    @BeforeEach
    void setUp() {
        requestId = UUID.randomUUID();
        orgId = UUID.randomUUID();

        var request = new Request();
        request.setId(requestId);

        organization = new Organization();
        organization.setId(orgId);
        organization.setName("ТрансЛогистик");
        organization.setContactPhone("+79991234567");
    }

    @Test
    void toModel_ShouldMapDtoToEntityWithCorrectFields() {
        // given
        var dto = new CarrierReplyDto(
                new CarrierReplyDto.Auto("Volvo FH16", "Volvo", "FH16", "2020", "16","A123AA777"),
                new CarrierReplyDto.Trailer("КрАЗ Т-2020", "Т-2020", "КрАЗ", "B456BB777"),
                new CarrierReplyDto.Driver("Иван Иванов", "+79161234567", "1234 567890"),
                50000.0,
                null,
                "Готов к отправке"
        );

        // when
        CarrierReply reply = mapper.toModel(requestId, orgId, dto);

        // then
        assertThat(reply).isNotNull();
        assertThat(reply.getRequest().getId()).isEqualTo(requestId);
        assertThat(reply.getOrganization().getId()).isEqualTo(orgId);
        assertThat(reply.getCreatedAt()).isNotNull();
        assertThat(reply.getReply()).usingRecursiveComparison().isEqualTo(dto);
    }

    @Test
    void toDto_ShouldMapReplyToDto() {
        // given
        var dto = new CarrierReplyDto(null, null, null, 45000.0, null, "Срочная доставка");
        var reply = new CarrierReply();
        reply.setReply(dto);

        // when
        CarrierReplyDto result = mapper.toDto(reply);

        // then
        assertThat(result).usingRecursiveComparison().isEqualTo(dto);
    }

    @Test
    void toShortDto_ShouldMapToShortDtoWithCorrectValues() {
        // given
        var auto = new CarrierReplyDto.Auto("Mercedes Actros", "Mercedes", "Actros", "2021", "10", "C789CC777");
        var replyEntity = mock(CarrierReply.class);
        var replyDto = new CarrierReplyDto(auto, null, null, 60000.0, null, "Доступен сегодня");

        when(replyEntity.getOrganization()).thenReturn(organization);
        when(replyEntity.getReply()).thenReturn(replyDto);

        // when
        CarrierReplyShortDto shortDto = mapper.toShortDto(replyEntity);

        // then
        assertThat(shortDto).isNotNull();
        assertThat(shortDto.carrier()).isEqualTo(organization.getName());
        assertThat(shortDto.phone()).isEqualTo(organization.getContactPhone());
        assertThat(shortDto.rate()).isEqualTo(4.5);
        assertThat(shortDto.auto().type()).isEqualTo(auto.mark() + " " + auto.year()); // Проверка @Named("toAutoType")
        assertThat(shortDto.auto().name()).isEqualTo(auto.name());        // Проверка @Named("toAutoName")
        assertThat(shortDto.cost()).isEqualTo(replyDto.cost());
        assertThat(shortDto.comment()).isEqualTo(replyDto.comment());
    }

    @Test
    void toShortDto_ShouldHandleNullAuto() {
        // given
        var replyEntity = mock(CarrierReply.class);
        var replyDto = new CarrierReplyDto(null, null, null, 50000.0, null, null);

        when(replyEntity.getOrganization()).thenReturn(organization);
        when(replyEntity.getReply()).thenReturn(replyDto);

        // when
        CarrierReplyShortDto shortDto = mapper.toShortDto(replyEntity);

        // then
        assertThat(shortDto.auto().type()).isNull();
        assertThat(shortDto.auto().name()).isNull();
    }

    @Test
    void toShortDtos_ShouldMapListToShortDtos() {
        // given
        var reply1 = mock(CarrierReply.class);
        var reply2 = mock(CarrierReply.class);

        when(reply1.getOrganization()).thenReturn(organization);
        when(reply2.getOrganization()).thenReturn(organization);

        var dto1 = new CarrierReplyDto(
                new CarrierReplyDto.Auto("Scania R500", "Scania", "R500", "2019", "15","D001DD777"),
                null, null, 55000.0, null, null);
        var dto2 = new CarrierReplyDto(
                new CarrierReplyDto.Auto("DAF XF", "DAF", "XF", "2020", "10", "E002EE777"),
                null, null, 58000.0, null, null);

        when(reply1.getReply()).thenReturn(dto1);
        when(reply2.getReply()).thenReturn(dto2);

        List<CarrierReply> replies = List.of(reply1, reply2);

        // when
        List<CarrierReplyShortDto> result = mapper.toShortDtos(replies);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).auto().type()).isEqualTo(dto1.auto().mark() + " " + dto1.auto().year());
        assertThat(result.get(1).auto().type()).isEqualTo(dto2.auto().mark() + " " + dto2.auto().year());
        assertThat(result.get(0).cost()).isEqualTo(dto1.cost());
        assertThat(result.get(1).cost()).isEqualTo(dto2.cost());
    }
}