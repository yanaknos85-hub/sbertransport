import { useTasksWithParams } from 'api/cargo-registry-compensations-search';
import { useProfile } from 'api/profile';
import { PaginationParams } from 'stores/Pagination/Pagination.interface';

export const useTasks = ({ page = 0, size = 10 }: Partial<PaginationParams>) => {
  const { organizationId } = useProfile().data;

  const cargoData = useTasksWithParams(
    { pagination: { page, size } },
    organizationId
  );

  return cargoData;
};
