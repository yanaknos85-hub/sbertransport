import { useCreateCompensationTask } from 'api/cargo-registry-compensations-search';
import { useCallback } from 'react';
import { TransformedFilterValues } from '../types';

export const useCreateTaskMutation = () => {
  const [createTaskMutation, { isLoading, error }] = useCreateCompensationTask();

  const createTask = useCallback(async (filters: TransformedFilterValues) => {
    try {
      const result = await createTaskMutation(filters);
      return result;
    } catch (err) {
      console.error('Ошибка создания задачи на компенсацию:', err);
      throw err;
    }
  }, [createTaskMutation]);

  return {
    createTask,
    isLoading,
    error,
  };
};
