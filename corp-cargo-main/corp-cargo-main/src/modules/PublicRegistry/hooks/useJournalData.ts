import { TripInfoForReporting } from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { useGettingAllTravelStatuses } from 'api/travel-status';
import { processJournalData } from '../utils/utils';

export const useJournalData = (data: TripInfoForReporting[]) => {
  const tripStatus = useGettingAllTravelStatuses().data;
  const processedJournalData = processJournalData(data, tripStatus);

  return { processedJournalData };
};
