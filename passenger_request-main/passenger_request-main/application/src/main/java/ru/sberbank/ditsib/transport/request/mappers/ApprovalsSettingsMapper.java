package ru.sberbank.ditsib.transport.request.mappers;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import ru.sberbank.ditsib.transport.request.messaging.message.ApprovalsSettingsMessage;
import ru.sberbank.ditsib.transport.request.messaging.message.OtherTrTypesApprovalsSettingsMessage;
import ru.sberbank.ditsib.transport.request.messaging.message.PublicApprovalsSettingsMessage;
import ru.sberbank.ditsib.transport.request.messaging.message.TaxiApprovalsSettingsMessage;
import ru.sberbank.ditsib.transport.request.database.model.approvals.settings.OtherTrTypesApprovalsSettings;
import ru.sberbank.ditsib.transport.request.database.model.approvals.settings.PublicTrApprovalsSettings;
import ru.sberbank.ditsib.transport.request.database.model.approvals.settings.PurposeAndRegionApprovalSettingsItem;
import ru.sberbank.ditsib.transport.request.database.model.approvals.settings.TaxiApprovalsSettings;

import java.util.List;

@Mapper
public interface ApprovalsSettingsMapper {
    
    PurposeAndRegionApprovalSettingsItem toItem(ApprovalsSettingsMessage.PurposeAndRegionApprovalSettingsItem item);
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    List<PurposeAndRegionApprovalSettingsItem> toItems(
            List<ApprovalsSettingsMessage.PurposeAndRegionApprovalSettingsItem> items);
    
    TaxiApprovalsSettings toTaxiSettings(TaxiApprovalsSettingsMessage message);
    PublicTrApprovalsSettings toPublicSettings(PublicApprovalsSettingsMessage message);
    OtherTrTypesApprovalsSettings toOtherTrSettings(OtherTrTypesApprovalsSettingsMessage message);
}
