import { useOrganizationTransportTypes } from 'api/transport-types';
import { TransportTypes, TransportTypeCategories, TransportTypeRuTitles } from '../constants/tripSettings';

export const useServiceOptionsByOrganization = (orgId: string) => {
  const serviceOptions = useOrganizationTransportTypes(orgId).data;

  // так как бэк присылает TransportTypes плоским списком без категорий сервисов, то вручную добавляем поле с категорией сервиса - пассажиры/грузы/флит.
  // исправить, когда бэк будет возвращать категории.
  const serviceOptionsWithCategory = serviceOptions.map(({ transportType, active }) => ({
    transportType,
    active,
    category: TransportTypeCategories[transportType as TransportTypes],
    title: TransportTypeRuTitles[transportType as TransportTypes],
  }));

  return serviceOptionsWithCategory;
};
