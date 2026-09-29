import { ChangeEvent, useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useSearchContracts } from 'api/contracts';
import { SelectOption } from '../../TripPurposes/types/types';
import { Contract } from 'stores/Contracts/Contracts.interface';
import { TariffTypes } from 'constants/constants.app';

interface ContractConditions {
  contractor?: string;
  serviceType?: string;
  organization?: string;
  transportType?: string;
  regionId?: string | null;
}

export const useContractsInput = (
  isFormUpdate: boolean,
  initialContractConditions: ContractConditions = {}
): {
    contractOptions: SelectOption[];
    contractsData: Contract[];
    isContractLoading: boolean;
    onSelect: (field: string) => (value?: string) => void;
    onChange: (field: string) => (value: ChangeEvent<HTMLInputElement>) => void;
  } => {
  const [contractConditions, setContractConditions] = useState<ContractConditions>(initialContractConditions);
  const [contractOptions, setContractOptions] = useState<SelectOption[]>([]);
  const [contractsData, setContractsData] = useState<Contract[]>([]);
  const [isContractLoading, setContractLoading] = useState(false);

  const { tariffType, routeId } = useParams<{ tariffType: TariffTypes; routeId: string }>();

  const {
    contractor, serviceType, organization, transportType, regionId,
  } = contractConditions;

  const [searchContracts] = useSearchContracts({
    contractorId: contractor,
    // serviceType, не надо передавать - не находит с ним
    organizationId: organization,
    transportType,
    region: tariffType === TariffTypes.INCOME || tariffType === TariffTypes.OUTCOME ? regionId : null,
    active: 'true',
    pagination: { page: 0, size: 1000 },
    contractType: tariffType,
  });

  useEffect(() => {
    setContractOptions([]);
  }, [isFormUpdate]);

  useEffect(() => {
    if (tariffType && routeId === 'all') {
      Object.keys(contractConditions).length && setContractConditions({});
      contractOptions.length && setContractOptions([]);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tariffType, routeId]);

  const getContracts = async () => {
    setContractLoading(true);
    const res = await searchContracts();
    if (res) {
      setContractsData(res.content || []);
      const data = (res.content || []).map(contract => ({ label: contract.contractNumber, value: contract.id }));
      setContractOptions(data);
    }
    setContractLoading(false);
  };

  useEffect(() => {
    if (serviceType && transportType && regionId && (tariffType === TariffTypes.OUTCOME ? true : !!organization)) {
      getContracts();
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [contractor, serviceType, organization, transportType, regionId]);

  function onSelect(field: string) {
    return (value?: string) => {
      setContractConditions({
        ...contractConditions,
        [field]: value,
      });
    };
  }

  function onChange(field: string) {
    return (event: ChangeEvent<HTMLInputElement>) => {
      setContractConditions({
        ...contractConditions,
        [field]: event.target.value,
      });
    };
  }

  return {
    onSelect,
    onChange,
    contractOptions,
    contractsData,
    isContractLoading,
  };
};
