import { useProfile } from 'api/profile';
import { useOrganizationTransportTypes, useTransportTypes as useQueryTransportTypes } from 'api/transport-types';
import { createCallableCtx } from 'utils/createCallableContext';

const useHook = () => {
  const { organizationId } = useProfile().data;
  const allTransportTypes = useQueryTransportTypes().data;

  const organizationTransportTypes = useOrganizationTransportTypes(organizationId)
    .data.filter(({ active }) => active)
    .map(({ transportType }) => transportType);

  const activeTransportTypesSet = new Set(organizationTransportTypes);

  const allActiveTransportTypes = allTransportTypes.filter(({ name }) => activeTransportTypesSet.has(name));

  return {
    allTransportTypes,
    allActiveTransportTypes,
  };
};

export const [useTransportTypes, TransportTypesProvider] = createCallableCtx(useHook, {
  name: 'TransportTypesProvider',
});
