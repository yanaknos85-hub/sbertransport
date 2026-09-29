export enum ContractorHandbooksNames {
  id = 'id',
  name = 'name',
  tin = 'tin',
  msrn = 'msrn',
}

export const ContractorHandbookTitles: Record<ContractorHandbooksNames, string> = {
  id: 'Идентификатор контрагента',
  name: 'Имя контрагента',
  tin: 'ИНН',
  msrn: 'ОГРН',
};

export type ExpandedFiltersType = keyof typeof ContractorHandbooksNames;
