/* eslint-disable @typescript-eslint/no-explicit-any */
import React, { Dispatch } from 'react';

import { Form } from 'antd';
import { ModelFormFieldProps, FormSectionProps } from 'shared/models/ModelDetail/ModelFormField';
import { FilterRequest } from 'modules/CargoBusinessReports/types/types';

export type TransportType = 'public' | 'personal' | 'taxi' | 'carsharing';

export interface FilterPanelProps {
  onApplyFilters: Dispatch<any>;
  fields: (ModelFormFieldProps | FormSectionProps)[];
  setForm?: Dispatch<any>;
  className?: string;
  isStatusChangeActive?: boolean;
  setStatusChangeActive?: Dispatch<any>;
  onClearButtonActivator?: (event: any) => void;
  initialValues?: React.ComponentProps<typeof Form>['initialValues'];
  setFilterValues?(values: any): void;
  filterValues?: any;
  resetPagination?(): void;
  initialSearch?: boolean;
  onReset?: () => void;
  onSave?: () => void;
  setIsVisible?: Dispatch<React.SetStateAction<boolean>>;
  withFormSection?: boolean;
  submitBtnName?: string;
  showSaveBtn?: boolean;
  isNewDesign?: boolean;
  initialFilters?: FilterRequest;
}
