import { TripRequestModel } from 'stores/Trip/models';
import { TripStatusesCancellable } from 'constants/TripRequestStatuses.constants';

export const filterByAuthor = (item: TripRequestModel): boolean => item.passenger.id === item.author.id;

export const filterByPlannedStatus = (item: TripRequestModel): boolean => item.isMatched(TripStatusesCancellable);
