import {
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  createContext
} from 'react';
import type { Dispatch, SetStateAction } from 'react';

import { useProfile } from 'api/profile';
import {
  ORGANIZATION_ID,
  ORGANIZATION_NAME,
  EXECUTOR_GROUP_ID_PASSENGERS,
  EXECUTOR_GROUP_ID_CARGO,
  EMPTY_EXECUTOR_GROUP_CARGO,
  IS_ORGANIZATION_FLAG
} from 'constants/constants.app';
import { useCargoRoute } from 'shared/hooks/useCargoRoute';

export interface IOrganizationContext {
  organizationId: string | undefined;
  setOrganizationId: Dispatch<SetStateAction<string | undefined>>;
  organizationName: string | undefined;
  setOrganizationName: Dispatch<SetStateAction<string | undefined>>;
  executorGroupId: string[];
  setExecutorGroupId: (value: string[]) => void;
  emptyExecutorGroup: boolean;
  setEmptyExecutorGroup: (value: boolean) => void;
  allExecutorGroups: boolean;
  setAllExecutorGroups: (value: boolean) => void;
  isOrganization: boolean;
  setIsOrganization: Dispatch<SetStateAction<boolean>>;
  isCargo: boolean;
}

const savedOrgId = localStorage.getItem(ORGANIZATION_ID);
const savedOrgName = localStorage.getItem(ORGANIZATION_NAME);
const savedFlag = localStorage.getItem(IS_ORGANIZATION_FLAG);
const savedEmptyFlag = localStorage.getItem(EMPTY_EXECUTOR_GROUP_CARGO);

export const useHook = (disabled?: boolean): IOrganizationContext => {
  const { organizationId, organizationName } = useProfile(disabled).data || {};
  const { isCargoAny: isCargo } = useCargoRoute();

  const [id, setId] = useState<string | undefined>(savedOrgId ?? organizationId);
  const [name, setName] = useState<string | undefined>(savedOrgName ?? organizationName);

  // Раздельные состояния для пассажиров и грузов
  const [passengersValue, setPassengersValue] = useState<string[]>(() => {
    const saved = localStorage.getItem(EXECUTOR_GROUP_ID_PASSENGERS);
    return saved ? JSON.parse(saved) : [];
  });

  const [cargoValue, setCargoValue] = useState<string[]>(() => {
    const saved = localStorage.getItem(EXECUTOR_GROUP_ID_CARGO);
    return saved ? JSON.parse(saved) : [];
  });

  // Спец-флаги для грузового раздела:
  // emptyExecutorGroup — «Без групп» (LS-персистентно, переживает перезагрузку)
  // allExecutorGroups — «Все группы» (только в памяти, обнуляется при F5)
  const [emptyFlag, setEmptyFlag] = useState<boolean>(
    savedEmptyFlag === 'true'
  );
  const [allFlag, setAllFlag] = useState<boolean>(false);

  const [flag, setFlag] = useState<boolean>(savedFlag ? JSON.parse(savedFlag) : true);

  // Возвращаем значение для текущего раздела
  const executorGroupId = isCargo ? cargoValue : passengersValue;

  const setExecutorGroupId = useCallback((value: string[]) => {
    if (isCargo) {
      setCargoValue(value);
    } else {
      setPassengersValue(value);
    }
  }, [isCargo]);

  const setEmptyExecutorGroup = useCallback((value: boolean) => {
    setEmptyFlag(value);
  }, []);

  const setAllExecutorGroups = useCallback((value: boolean) => {
    setAllFlag(value);
  }, []);

  // Сохраняем значения пассажиров в localStorage
  useEffect(() => {
    localStorage.setItem(EXECUTOR_GROUP_ID_PASSENGERS, JSON.stringify(passengersValue));
  }, [passengersValue]);

  // Сохраняем значения грузов в localStorage
  useEffect(() => {
    localStorage.setItem(EXECUTOR_GROUP_ID_CARGO, JSON.stringify(cargoValue));
  }, [cargoValue]);

  useEffect(() => {
    if (id) {
      localStorage.setItem(ORGANIZATION_ID, id);
    } else {
      localStorage.removeItem(ORGANIZATION_ID);
    }
    if (name) {
      localStorage.setItem(ORGANIZATION_NAME, name);
    } else {
      localStorage.removeItem(ORGANIZATION_NAME);
    }
  }, [id, name]);

  useEffect(() => {
    localStorage.setItem(IS_ORGANIZATION_FLAG, JSON.stringify(flag));
  }, [flag]);

  useEffect(() => {
    localStorage.setItem(EMPTY_EXECUTOR_GROUP_CARGO, String(emptyFlag));
  }, [emptyFlag]);

  return useMemo<IOrganizationContext>(
    () => ({
      organizationId: id,
      setOrganizationId: setId,
      organizationName: name,
      setOrganizationName: setName,
      executorGroupId,
      setExecutorGroupId,
      emptyExecutorGroup: emptyFlag,
      setEmptyExecutorGroup,
      allExecutorGroups: allFlag,
      setAllExecutorGroups,
      isOrganization: flag,
      setIsOrganization: setFlag,
      isCargo,
    }),
    [
      id,
      name,
      executorGroupId,
      emptyFlag,
      allFlag,
      flag,
      isCargo,
      setExecutorGroupId,
      setEmptyExecutorGroup,
      setAllExecutorGroups,
    ]
  );
};

export const OrganizationContext = createContext<IOrganizationContext>({} as IOrganizationContext);

export const useOrganizationContext = (): IOrganizationContext => {
  return useContext(OrganizationContext);
};
