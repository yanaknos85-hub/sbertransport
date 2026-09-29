import React, { useEffect, useState } from 'react';
import { Redirect } from 'react-router-dom';

interface DelayedRedirectProps {
  to: string;
  delay?: number;
}

export const DelayedRedirect = ({ to, delay = 0 }: DelayedRedirectProps): JSX.Element | null => {
  const [navigateReady, setNavigateReady] = useState(false);

  useEffect(() => {
    const t = setTimeout(() => {
      setNavigateReady(true);
    }, delay);
    return (): void => {
      clearTimeout(t);
    };
  }, [delay]);

  if (navigateReady) {
    return <Redirect to={to} />;
  }
  return null;
};
