import { FraudComment } from 'stores/Trip/Trip.interface';

export const getFraudComments = (fraudComments?: FraudComment[]) => fraudComments?.map(({ text }) => text).filter(Boolean) ?? [];
