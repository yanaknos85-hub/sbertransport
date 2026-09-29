import { UUID } from 'utils/io-ts';
import { RequestParams } from '@sber-sbertransport/mf-core';
import { ISelect } from '@sber-sbertransport/ui-kit/src';

export interface DepartmentAutoCompleteProps extends ISelect {
  value?: UUID[];
  debounceTimeout?: number;
  extraParams?: RequestParams;
  minSearchLength?: number;
  multiple?: boolean;
  clearOnBlur?: boolean;
}
