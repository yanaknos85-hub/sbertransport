import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';

interface Props {
  to: string;
  from: string;
}

const RouterContext = React.createContext({} as Props);

export const RouterProvider = ({ children }) => {
  const location = useLocation();

  const [route, setRoute] = useState({
    to: location.pathname,
    from: location.pathname,
  });

  useEffect(() => {
    setRoute(prev => {
      const state = { from: prev.to, to: location.pathname };
      // eslint-disable-next-line no-console
      console.log('Route: ', state);
      return state;
    });
  }, [location]);

  return (
    <RouterContext.Provider value={route}>
      {children}
    </RouterContext.Provider>
  );
};

export const useContextRouter = () => {
  const context = React.useContext(RouterContext);

  if (context === undefined) {
    throw new Error('RouterContext must be used within a RouterContext.Provider');
  }

  return context;
};
