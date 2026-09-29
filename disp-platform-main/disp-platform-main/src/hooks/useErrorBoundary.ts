import { useEffect, useRef } from 'react';
import { LocationListener } from 'history';
import ErrorBoundary from 'components/ErrorBoundary';
import { StoreNames, useAppStore } from 'ioc';

const useErrorBoundary = () => {
  const {
    [StoreNames.configStore]: { history },
  } = useAppStore();

  const errorBoundaryRef = useRef<ErrorBoundary | null>(null);

  // Сброс ErrorBoundary при смене роута
  useEffect(() => {
    const handleRouteChange: LocationListener = (_location, _action) => {
      if (errorBoundaryRef.current?.state.error) {
        errorBoundaryRef.current.reset();
      }
    };

    history.listen(handleRouteChange);
  }, [history]);

  return { errorBoundaryRef };
};

export default useErrorBoundary;
