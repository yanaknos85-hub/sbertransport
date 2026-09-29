import moment from 'moment';

export const initialFormValues = {
  desiredDate: { mode: 'range', value: [moment().startOf('month'), moment()] },
};
