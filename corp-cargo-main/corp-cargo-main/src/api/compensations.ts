import * as t from 'io-ts';
import { useAPI } from './index';
import { PublicCompensation } from '../stores/Compensations/Compensations.interface';
import { GET_PUBLIC_COMPENSATIONS } from '../constants/constants.api';

declare module 'api' {
  interface Cache {
    publicCompensations: {
      key: ['publicCompensations'];
      value: { publicCompensations: PublicCompensation[] };
    };
    personalCompensations: {
      key: ['personalCompensations'];
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      value: { personalCompensations: any[] };
    };
  }
}

const publicRaw2Cache = (publicCompensations: PublicCompensation[]) => ({ publicCompensations });

export const usePublicCompensations = () => useAPI(['publicCompensations'], ({ http, process }) => http
  .get<PublicCompensation[]>(GET_PUBLIC_COMPENSATIONS)
  .then(process.decodeResponseData(t.array(PublicCompensation)))
  .then(publicRaw2Cache)
);
