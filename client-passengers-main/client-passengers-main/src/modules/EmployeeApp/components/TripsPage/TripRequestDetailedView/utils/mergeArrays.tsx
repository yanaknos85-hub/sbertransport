import { ITripHistory } from 'stores/Trip/Trip.interface';
import { IstatusStepper } from '../StepperDetailed/StepperDetailed';
import { TripRequestStatusesTitles } from 'modules/EmployeeApp/TripRequestStatuses.constants';

interface Ititle {
  title: string;
  description: string;
}

export type TitleMap = {
  [K in string]: Ititle
};

export const mergeArrays = (historyArray: ITripHistory[], statusArray: IstatusStepper[]) => {
  const titleMap: TitleMap = {};
  const uniqueHistoryArray = historyArray.filter(
    (obj, idx, arr) => arr.findIndex(t => JSON.stringify(t.status) === JSON.stringify(obj.status)) === idx
  );

  statusArray.forEach(item => {
    titleMap[item.title] = item;
  });

  const resultArray: IstatusStepper[] = [];
  let lastMatchIndex = -1;

  uniqueHistoryArray.forEach(item => {
    const matchingItem = titleMap[TripRequestStatusesTitles[item.status]];
    if (matchingItem) {
      resultArray.push(matchingItem);
      lastMatchIndex = statusArray.indexOf(matchingItem);
    }
  });

  statusArray.slice(lastMatchIndex + 1).forEach(item => {
    if (!resultArray.includes(item)) {
      resultArray.push(item);
    }
  });

  return resultArray;
};
