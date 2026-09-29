import * as t from 'io-ts';
import {
  APIQueryResult, TypeAtKey, updateQueryCache, useAPI, useAPIMutation
} from 'api';
import {
  DELETE_POSITION,
  GET_ALL_POSITIONS,
  GET_POSITION,
  POSITION_ADD_PARAMS,
  POSITION_PARAMS
} from 'constants/constants.api';
import { UUID } from 'utils/io-ts';
import { Position } from 'stores/Position/Position.interface';
import indexById from 'utils/indexById';
import { ignore } from 'utils';
import { mkUseUploadEntity } from './upload';

declare module 'api' {
  interface Cache {
    positions: {
      key: ['positions', UUID];
      value: {
        positions: Position[];
        byId: Record<string, Position | undefined>;
      };
    };
    position: { key: ['position', UUID | null | undefined, UUID | null | undefined]; value: Position | undefined };
  }
}

type PositionsCacheItem = TypeAtKey<['positions', UUID]>;

const raw2cache = (positions: Position[]): PositionsCacheItem => ({ positions, byId: indexById(positions) });

export const usePositions = (orgId: UUID): APIQueryResult<PositionsCacheItem, unknown> => useAPI(['positions', orgId], ({ http, process }) => http
  .get<Position[]>(GET_ALL_POSITIONS, { urlParams: { orgId } })
  .then(process.decodeResponseData(t.array(Position)))
  .then(raw2cache)
);

export const usePosition = (
  orgId: UUID | null | undefined,
  posId: UUID | null | undefined
): APIQueryResult<Position | undefined, Error> => useAPI(['position', orgId, posId], ({ http, process }) => orgId && posId
  ? http
    .get<Position>(GET_POSITION, { urlParams: { orgId, posId } })
    .then(process.decodeResponseData(Position))
  : undefined
);

export const useCreatePosition = () => useAPIMutation(
  ({ http, process }, position: Omit<Position, 'id'>) => http
    .post(POSITION_ADD_PARAMS, position, { urlParams: { orgId: position.organizationId } })
  // @ts-ignore
    .then<Position>(process.getResponseData),
  {
    onSuccess: ({
      cache, result: position, process, t,
    }) => {
      process.processStatus(200, t.Positions.AddSuccess);
      updateQueryCache(cache, ['positions', position.organizationId], ({ positions }) => raw2cache([...positions, position])
      );
    },
  }
);

export const useUpdatePosition = () => useAPIMutation(
  ({ http }, position: Position) => http
    .put(POSITION_PARAMS, position, { urlParams: { orgId: position.organizationId, posId: position.id } })
    .then(ignore),
  {
    onSuccess: ({
      cache, variables: position, process, t,
    }) => {
      process.processStatus(200, t.Positions.EditSuccess);
      updateQueryCache(cache, ['positions', position.organizationId], ({ positions }) => raw2cache(positions.map(p => (p.id === position.id ? position : p)))
      );
    },
  }
);

export const useDeletePosition = () => useAPIMutation(
  ({ http }, { orgId, posId }: { orgId: UUID; posId: UUID }) => http
    .delete<number>(DELETE_POSITION, { urlParams: { orgId, posId } })
    .then(ignore),
  {
    onSuccess: ({
      cache, variables: { orgId, posId }, process, t,
    }) => {
      process.processStatus(200, t.Positions.EditSuccess);
      updateQueryCache(cache, ['positions', orgId], ({ positions, byId }) => {
        const position = byId[posId];
        if (position) {
          position.active = false;
        }
        return { positions, byId };
      });
    },
  }
);

export const useUploadPositions = mkUseUploadEntity('position', {
  onSuccess: ({ cache }) => cache.invalidateQueries(['positions']),
});
