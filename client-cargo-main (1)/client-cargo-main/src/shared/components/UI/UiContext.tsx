import React, {
  createContext, FC, ReactNode, useContext
} from 'react';
import { useMobile } from 'shared/hooks/useMobile';

interface UiContext {
  isMobile: boolean;
}

const UiContext = createContext<UiContext>({ isMobile: false });

export const UiContextProvider: FC<{ children: ReactNode }> = ({ children }) => {
  const isMobile = useMobile();
  return (
    <UiContext.Provider value={{ isMobile }}>{children}</UiContext.Provider>
  );
};

export const useUiContext = () => {
  return useContext(UiContext);
};
