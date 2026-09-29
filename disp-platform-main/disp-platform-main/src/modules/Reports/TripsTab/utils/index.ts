import moment from 'moment';

export const isValidDate = (date?: string) => !!date && moment(date).isValid();
