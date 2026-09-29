import { APIQueryResult, useAPI } from 'api';
import { GET_USER_AVATAR } from 'constants/constants.api';

declare module 'api' {
  interface Cache {
    userAvatar: { key: ['userAvatar', string]; value: string };
  }
}

export const useGetUserAvatar = (userId: string): APIQueryResult<string, Error> => useAPI(['userAvatar', userId], ({ http }) => http.get<Blob>(GET_USER_AVATAR, {
  urlParams: { userId },
  responseType: 'arraybuffer',
})
  .then(response => {
    const file = new Blob([response.data]);
    return URL.createObjectURL(file);
  }),
{ suspense: false }
);
