import { TripStatusesCancellable } from 'modules/EmployeeApp/TripRequestStatuses.constants';

import { TripRequestModel } from 'stores/Trip/models';

export const filterByAuthor = (item: TripRequestModel): boolean => item.passenger.id === item.author.id;

export const filterByPlannedStatus = (item: TripRequestModel): boolean => item.isMatched(TripStatusesCancellable);
