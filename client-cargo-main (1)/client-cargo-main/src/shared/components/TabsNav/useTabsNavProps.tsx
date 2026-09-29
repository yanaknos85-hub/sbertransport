import React from 'react';
import { useRouteMatch } from 'react-router-dom';

export const useTabsNavProps = (): {
  setActiveTab?: React.Dispatch<React.SetStateAction<string>>;
  activeTab: string;
} => {
  const match: { params: { filter: string } } = useRouteMatch();
  const [activeTab, setActiveTab] = React.useState(match.params.filter);

  React.useEffect(() => {
    if (setActiveTab) {
      setActiveTab(match.params.filter);
    }
  }, [match.params.filter]);

  return {
    activeTab,
    setActiveTab,
  };
};

export type UseTabsNavProps = Omit<ReturnType<typeof useTabsNavProps>, 'activeTab'>;
