import * as t from 'io-ts';

export const CheckSplitMutationVarialbes = t.intersection([
  t.type({
    desiredDate: t.number,
    employeeId: t.string,
    expectedCost: t.number,
    expectedDuration: t.number,
  }),
  t.partial({
    timeZone: t.string,
  }),
]);

export type CheckSplitMutationVariables = t.TypeOf<typeof CheckSplitMutationVarialbes>;

export interface CheckSplitMutationErrorPayload {
  humanReadableId: string;
  id: string;
  status: string;
}

export interface CheckAbsenceMutationVariables {
  desiredDate: number;
  timeZone: string;
  personnelNumber: string;
  purpose: string;
  expectedDuration?: number;
}

export interface CheckAbsenceResponse {
  absence: {
    type: string;
    startDate: string;
    endDate: string;
  } | null;
}
