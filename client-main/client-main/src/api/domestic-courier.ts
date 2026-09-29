import { MutationResultPair } from 'react-query';
import { useAPIMutation } from 'api';
import { GET_COURIER_ROLES } from 'constants/constants.env';
import { AxiosError } from 'axios';

// declare module 'api' { TODO Пока оставлю, но не думаю, что тут нужно кэшировать
//   interface Cache {
//     courierRoles: {
//       key: ['courierRoles']
//       value: unknown
//     }
//   }
// }

/** Подключение роли внутренний курьер */
export const useCourierRole = (): MutationResultPair<
  unknown,
  AxiosError,
  void,
  unknown
> => useAPIMutation(
  ({ http, process }) => http
    .put(GET_COURIER_ROLES, undefined)
    .then(process.getResponseData),
  {
    onSuccess: ({ process }) => {
      process.processStatus(200, 'Роль подключена');
    },
    onError: ({ logger }) => {
      logger.toMessage('error', 'Произошла ошибка при подключении роли');
    },
  }
);

/** Удаление роли внутренний курьер */
export const useDeleteCourierRole = (): MutationResultPair<
  unknown,
  AxiosError,
  void,
  unknown
> => useAPIMutation(
  ({ http, process }) => http
    .delete(GET_COURIER_ROLES)
    .then(process.getResponseData),
  {
    onSuccess: ({ process }) => {
      process.processStatus(200, 'Роль удалена');
    },
    onError: ({ logger }) => {
      logger.toMessage('error', 'Произошла ошибка при удалении роли');
    },
  }
);
