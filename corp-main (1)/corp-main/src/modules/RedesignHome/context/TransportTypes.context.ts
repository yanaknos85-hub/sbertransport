import { useProfile } from 'api/profile';
import { useOrganizationTransportTypes } from 'api/transport-types';
import { createCallableCtx } from 'utils/createCallableContext';

const useHook = () => {
  const { organizationId } = useProfile().data;

  const organizationTransportTypes = useOrganizationTransportTypes(organizationId).data;

  return {
    organizationTransportTypes,
  };
};

export const [useTransportTypes, TransportTypesProvider] = createCallableCtx(useHook, {
  name: 'TransportTypesProvider',
});
