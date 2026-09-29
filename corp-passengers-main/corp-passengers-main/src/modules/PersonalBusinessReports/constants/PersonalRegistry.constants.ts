import moment from 'moment';

export const initialFormValues = {
  orderPaymentFormationStartRange: { mode: 'range', value: [moment().startOf('month'), moment()] },
};
