import * as t from 'io-ts';
import { Filters } from 'stores/CargoRegistry/CargoRegistry.interface';
import { createPagination } from 'stores/Pagination/Pagination.interface';
import * as tt from 'utils/io-ts';

export const Task = t.type({
  id: tt.uuid,
  status: t.string,
  filterRequest: t.array(Filters),
  creationTime: t.string,
  actions: t.string,
});

export type Tasks = t.TypeOf<typeof Task>;
export const TasksSearchResponse = createPagination(Task);

export type TasksSearchResponse = t.TypeOf<typeof TasksSearchResponse>;
