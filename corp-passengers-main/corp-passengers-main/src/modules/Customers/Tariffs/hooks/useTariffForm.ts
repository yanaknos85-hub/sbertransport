import { useProfile } from 'api/profile';
import { SelectOption } from 'modules/Employees/EditEmployee';
import { useState } from 'react';
import { TariffJson } from 'stores/Tariffs/Tariffs.interface';

export const useTariffForm = (initialValues: TariffJson) => {
  const initialOrganizationId = useProfile().data;

  const [contractorTariffId, setContractorTariffId] = useState(initialValues.contractorId);
  const [contractId, setContractId] = useState(initialValues.contractId);
  const [regionId, setRegionId] = useState<SelectOption[] | undefined>([]);
  const [organizationId, setOrganizationId] = useState<string>(initialOrganizationId.id);

  return ({
    contractorTariffId,
    setContractorTariffId,
    contractId,
    setContractId,
    regionId,
    setRegionId,
    organizationId,
    setOrganizationId,
  });
};
