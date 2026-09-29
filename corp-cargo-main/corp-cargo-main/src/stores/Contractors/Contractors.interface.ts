import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { IntegrationTypes } from 'constants/constants.app';
import { createPagination } from 'stores/Pagination/Pagination.interface';
import { ContractorDispatcher } from '../ContractorDispatchers/ContractorDispatchers.interface';

export const XMLIntegrationsParams = t.partial({
  contractorName: t.string,
  contractorRusName: t.string,
  integrationEmail: t.string,
});

export const JsonIntegrationParams = t.partial({
  url: t.string,
  login: t.string,
  password: t.string,
});

const IntegrationTypeEnum = ioTypeFromEnum<IntegrationTypes>('IntegrationTypes', IntegrationTypes);

export const Contractor = t.intersection([
  t.type({
    id: t.string,
    name: t.string,
    msrn: t.string,
    tin: t.string,
    humanReadableId: t.string,
  }),
  t.partial({
    contactPersonInfo: t.string,
    contactPersonPhone: t.string,
    integrationParams: XMLIntegrationsParams,

    mainDispatcherId: tt.uuid,
    mainDispatcher: ContractorDispatcher,

    integrationType: IntegrationTypeEnum,
    jsonIntegrationParams: JsonIntegrationParams,
  }),
]);

export const ContractorSelect = t.type({
  id: t.string,
  name: t.string,
});

export type ContractorSelect = t.TypeOf<typeof ContractorSelect>;

export type Contractor = t.TypeOf<typeof Contractor>;

export const ContractorsResponse = createPagination(Contractor);

export type ContractorsResponse = t.TypeOf<typeof ContractorsResponse>;

export const PaginationParams = t.type({
  page: t.number,
  size: t.number,
});

export type PaginationParams = t.TypeOf<typeof PaginationParams>;

export const ContractorsFilters = t.partial({
  name: t.string,
  tim: t.number,
  msrn: t.number,
});

export type ContractorsFilters = t.TypeOf<typeof ContractorsFilters>;
