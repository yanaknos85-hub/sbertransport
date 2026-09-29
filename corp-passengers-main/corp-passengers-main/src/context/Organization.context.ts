import React, { useEffect, useState } from 'react';

import { useProfile } from 'api/profile';
import { ORGANIZATION_ID, EXECUTOR_GROUP_ID, IS_ORGANIZATION_FLAG } from 'constants/constants.app';

export interface Props {
  organizationId: string;
  setOrganizationId: React.Dispatch<React.SetStateAction<string>>;
  executorGroupId: string[];
  setExecutorGroupId: React.Dispatch<React.SetStateAction<string[]>>;
  isOrganization: boolean;
  setIsOrganization: React.Dispatch<React.SetStateAction<boolean>>;
}

const savedOrgId = localStorage.getItem(ORGANIZATION_ID);
const savedExecutorGroup = localStorage.getItem(EXECUTOR_GROUP_ID);
const savedFlag = localStorage.getItem(IS_ORGANIZATION_FLAG);

export const useHook = (disabled?: boolean): Props => {
  const { organizationId } = useProfile(disabled).data;

  const [id, setId] = useState(savedOrgId ?? organizationId);
  const [executor, setExecutor] = useState(savedExecutorGroup ? JSON.parse(savedExecutorGroup) : []);
  const [flag, setFlag] = useState(savedFlag ? JSON.parse(savedFlag) : false);

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
    localStorage.setItem(IS_ORGANIZATION_FLAG, JSON.stringify(flag));
  }, [flag]);

  return {
    organizationId: id,
    setOrganizationId: setId,
    executorGroupId: executor,
    setExecutorGroupId: setExecutor,
    isOrganization: flag,
    setIsOrganization: setFlag,
  };
};

export const OrganizationContext = React.createContext({} as Props);

export const useOrganizationContext = () => {
  const context = React.useContext(OrganizationContext);

  if (context === undefined) {
    throw new Error('OrganizationContext must be used within a OrganizationContext.Provider');
  }

  return context;
};
