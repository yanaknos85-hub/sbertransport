import { useState } from 'react';

const initialState = {
  page: 0,
  size: 20,
};

export const useFraudPagination = () => {
  const [pagination, setPagination] = useState(initialState);

  const resetPage = () => setPagination(prev => ({ ...prev, page: 0 }));

  const reset = () => setPagination(initialState);

  return {
    pagination, setPagination, resetPage, reset,
  };
};
