import { useTasksWithParams } from 'api/business-reports';
import { PaginationParams } from 'stores/Pagination/Pagination.interface';

export const useTasks = ({ page = 0, size = 10 }: Partial<PaginationParams>) => {
  const cargoData = useTasksWithParams({
    pagination: { page, size },
  });
  return cargoData;
};

