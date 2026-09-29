import React, { useEffect, useMemo, useState } from 'react';
import { AUTOPARK_ID, Roles } from 'constants/app.constants';
import { useRole } from 'hooks/useRole';

export interface Props {
  autoparkId?: string;
  setAutoparkId: React.Dispatch<React.SetStateAction<string | undefined>>;
}

const savedAutoparkId = localStorage.getItem(AUTOPARK_ID);

export const useAutopark = (): Props => {
  const roles = useRole();
  const isAdmin = roles.includes(Roles.ROLE_DISPATCHER_ROOM_ADMIN)
    || roles.includes(Roles.ROLE_FEDERAL_DISPATCHER_CONTRACTOR);

  const [autoparkId, setAutoparkId] = useState(isAdmin ? (savedAutoparkId ?? undefined) : undefined);

  useEffect(() => {
    if (autoparkId) {
      localStorage.setItem(AUTOPARK_ID, autoparkId);
    }
  }, [autoparkId]);

  return useMemo(() => ({
    autoparkId,
    setAutoparkId,
  }), [autoparkId]);
};

export const AutoparkContext = React.createContext({} as Props);

export const useAutoparkContext = () => {
  const context = React.useContext(AutoparkContext);

  if (context === undefined) {
    throw new Error('AutoparkContext must be used within a AutoparkContext.Provider');
  }

  return context;
};
