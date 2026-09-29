import { useEffect, useRef } from 'react';
import ErrorBoundary from 'components/ErrorBoundary';
import { useLocation } from 'react-router';

const useErrorBoundary = () => {
  const location = useLocation();

  const errorBoundaryRef = useRef<ErrorBoundary | null>(null);

  // Сброс ErrorBoundary при смене роута
  useEffect(() => {
    if (errorBoundaryRef.current?.state.error) {
      errorBoundaryRef.current.reset();
    }
  }, [location.pathname]);

  return { errorBoundaryRef };
};

export default useErrorBoundary;
