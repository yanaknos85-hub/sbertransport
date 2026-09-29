import { ApprovalSettingsQuery } from '../stores/ApprovalSettings/ApprovalSettings.interface';
import { MutationResultPair } from 'react-query';
import { useAPIMutation } from './index';
import { SEND_ORDER_TO_CONTRACTOR } from '../constants/constants.api';

declare module 'api' {
  interface Cache {
    forceOrder: { key: ['forceOrder']; value: { id: string } };
  }
}

export const useForceOrder = (): MutationResultPair<
  unknown,
  unknown,
  { orderId: string },
  unknown
  > => useAPIMutation(
  ({ http, process }, { orderId }) => http
    .put<ApprovalSettingsQuery>(SEND_ORDER_TO_CONTRACTOR, {}, {
      urlParams: { orderId: orderId }, hush: [404],
    })
    .then(process.decodeResponseData(ApprovalSettingsQuery)),
  {
    onSuccess: ({
      cache, process,
    }) => {
      process.processStatus(200, 'Данные по заявке отправлены');
      cache.refetchQueries(['forceOrder']);
    },
  }
);
