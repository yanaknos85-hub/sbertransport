import { useEffect, useState } from 'react';

const NAV_EXPANDED = 'NAV_EXPANDED';

export const useExpander = () => {
  const [isExpanded, setIsExpanded] = useState(localStorage.getItem(NAV_EXPANDED) === 'true');

  const toggleNav = () => {
    setIsExpanded(prev => !prev);
  };

  useEffect(() => {
    localStorage.setItem(NAV_EXPANDED, String(isExpanded));
  }, [isExpanded]);

  return {
    isExpanded,
    toggleNav,
  };
};
