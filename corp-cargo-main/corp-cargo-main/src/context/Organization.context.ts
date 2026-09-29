import {
  useEffect,
  useMemo,
  useCallback,
  useState,
  createContext,
  useContext
} from 'react';
import { useProfile } from 'api/profile';

import {
  ORGANIZATION_ID,
  EXECUTOR_GROUP_ID,
  EMPTY_EXECUTOR_GROUP_CARGO,
  IS_ORGANIZATION_FLAG
} from 'constants/constants.app';

export interface Props {
  organizationId: string;
  setOrganizationId: (value: string) => void;
  executorGroupId: string[];
  setExecutorGroupId: (value: string[]) => void;
  emptyExecutorGroup: boolean;
  setEmptyExecutorGroup: (value: boolean) => void;
  allExecutorGroups: boolean;
  setAllExecutorGroups: (value: boolean) => void;
  isOrganization: boolean;
  setIsOrganization: (value: boolean) => void;
}

const savedOrgId = localStorage.getItem(ORGANIZATION_ID);
const savedExecutorGroup = localStorage.getItem(EXECUTOR_GROUP_ID);
const savedEmptyExecutorGroup = localStorage.getItem(EMPTY_EXECUTOR_GROUP_CARGO);
const savedFlag = localStorage.getItem(IS_ORGANIZATION_FLAG);

export const useHook = (disabled?: boolean): Props => {
  const { organizationId: defaultOrgId } = useProfile(disabled).data;

  const [id, setId] = useState<string>(savedOrgId ?? defaultOrgId);
  const [executor, setExecutor] = useState<string[]>(
    savedExecutorGroup ? JSON.parse(savedExecutorGroup) : []
  );
  const [flag, setFlag] = useState<boolean>(
    savedFlag ? JSON.parse(savedFlag) : false
  );

  const [empty, setEmpty] = useState<boolean>(
    savedEmptyExecutorGroup === 'true'
  );
  const [allGroups, setAllGroups] = useState<boolean>(false);

  const setOrganizationId = useCallback((value: string) => {
    setId(value);
  }, []);

  const setExecutorGroupId = useCallback((value: string[]) => {
    setExecutor(value);
  }, []);

  const setIsOrganization = useCallback((value: boolean) => {
    setFlag(value);
  }, []);

  const setEmptyExecutorGroup = useCallback((value: boolean) => {
    setEmpty(value);
  }, []);

  const setAllExecutorGroups = useCallback((value: boolean) => {
    setAllGroups(value);
  }, []);

  useEffect(() => {
    if (id) {
      localStorage.setItem(ORGANIZATION_ID, id);
    }
  }, [id]);

  useEffect(() => {
    if (executor) {
      localStorage.setItem(EXECUTOR_GROUP_ID, JSON.stringify(executor));
    }
  }, [executor]);

  useEffect(() => {
    localStorage.setItem(EMPTY_EXECUTOR_GROUP_CARGO, String(empty));
  }, [empty]);

  useEffect(() => {
    localStorage.setItem(IS_ORGANIZATION_FLAG, JSON.stringify(flag));
  }, [flag]);

  return useMemo<Props>(
    () => ({
      organizationId: id,
      setOrganizationId,
      executorGroupId: executor,
      setExecutorGroupId,
      emptyExecutorGroup: empty,
      setEmptyExecutorGroup,
      allExecutorGroups: allGroups,
      setAllExecutorGroups,
      isOrganization: flag,
      setIsOrganization,
    }),
    [
      id,
      setOrganizationId,
      executor,
      setExecutorGroupId,
      empty,
      setEmptyExecutorGroup,
      allGroups,
      setAllExecutorGroups,
      flag,
      setIsOrganization,
    ]
  );
};

export const OrganizationContext = createContext({} as Props);

export const useOrganizationContext = () => {
  const context = useContext(OrganizationContext);

  if (context === undefined) {
    throw new Error('OrganizationContext must be used within a OrganizationContext.Provider');
  }

  return context;
};
