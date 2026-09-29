import { Contractor } from 'stores/Contractors/Contractors.interface';

export type ContractorMapped = Contractor & {
  contractorName: string;
  contractorRusName: string;
  integrationEmail: string;

  dispatcherFirstName: string;
  dispatcherLastName: string;
  dispatcherPatronymic: string;
  dispatcherPhone: string;
  dispatcherEmail: string;

  APIUrl: string;
  APILogin: string;
  APIPassword: string;
};
