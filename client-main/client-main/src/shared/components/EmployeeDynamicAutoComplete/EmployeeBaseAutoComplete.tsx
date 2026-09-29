import React, {
  FC, useCallback, useEffect, useMemo, useRef, useState
} from 'react';
import {
  EmployeeModel, RequestCanceler, RequestParams, Requester
} from '@sber-sbertransport/mf-core';
import { Select, Spin } from 'antd';
import { LabeledValue, SelectProps } from 'antd/es/select';
import axios from 'axios';
import debounce from 'lodash/debounce';

import { ReactComponent as Icon } from 'shared/form/Select/images/selectArrow.svg';
import { ReactComponent as Close } from 'shared/components/Images/Close.svg';
import { formatName } from 'utils/formatName';

interface BaseProps
  extends Omit<SelectProps<string | string[]>, 'options' | 'children' | 'value' | 'onChange' | 'mode'> {
  debounceTimeout?: number;
  extraParams?: RequestParams;
  minSearchLength?: number;
}

interface SingleProps {
  multiple?: false;
  value?: EmployeeModel;
  onChange?: (value: EmployeeModel | undefined) => void;
}

interface MultipleProps {
  multiple: true;
  value?: EmployeeModel[];
  onChange?: (value: EmployeeModel[]) => void;
}

type SpecificProps = SingleProps | MultipleProps;

export interface SingleEmployeeProps extends BaseProps, SingleProps {}
export interface MultipleEmployeeProps extends BaseProps, MultipleProps {}
export type EmployeeAutoCompleteProps = SingleEmployeeProps | MultipleEmployeeProps;

export interface RequesterProps {
  requester: Requester<EmployeeModel[]>;
  paramsGetter: (search: string, extraParams?: RequestParams) => RequestParams;
}

const employeeToOption = (employee: EmployeeModel): LabeledValue => ({
  value: employee.id,
  label: `${formatName(employee)} (${employee.personnelNumber})`,
});

const getInitialValues = (props: SpecificProps): string | string[] => props.multiple ? (props.value || []).map(item => item.id) : props.value && (props.value as EmployeeModel).id;

const getInitialEmployees = (props: SpecificProps): EmployeeModel[] => props.multiple ? (props as MultipleProps).value || [] : (props.value && [(props as SingleProps).value]) || [];

const callOnChange = (props: SpecificProps, newValue: string | string[], employees: EmployeeModel[]) => {
  if (props.onChange) {
    if (props.multiple) {
      props.onChange(employees.filter(item => newValue.includes(item.id)));
    } else {
      (props.onChange as SingleProps['onChange'])(employees.find(item => item.id === newValue));
    }
  }
};

export const EmployeeBaseAutoComplete: FC<EmployeeAutoCompleteProps & RequesterProps> = ({
  placeholder = 'Выберите сотрудника',
  debounceTimeout = 750,
  value,
  multiple = false,
  onChange,
  extraParams = {},
  requester,
  paramsGetter = (): RequestParams => ({}),
  minSearchLength = 3,
  ...props
}) => {
  const [selected, setSelected] = useState(getInitialValues({ value, multiple } as SpecificProps));
  const [employees, setEmployees] = useState(getInitialEmployees({ value, multiple } as SpecificProps));
  useEffect(() => {
    const specProps = { value, multiple } as SpecificProps;
    setSelected(getInitialValues(specProps));
    setEmployees(getInitialEmployees(specProps));
  }, [value, multiple, setSelected, setEmployees]);

  const [isFetching, setIsFetching] = useState(false);
  const cancelRequestRef = useRef<RequestCanceler>();

  const handleSearch = useMemo(
    () => debounce((search: string) => {
      if (!extraParams.transportType || !extraParams.date) return;

      const searchValue = search.trim();

      const cancelRequest = cancelRequestRef.current;
      if (cancelRequest) {
        cancelRequest();
      }
      setIsFetching(true);
      (searchValue && searchValue.length >= minSearchLength
        ? requester(paramsGetter(searchValue, extraParams), cancel => {
          cancelRequestRef.current = cancel;
        })
        : Promise.resolve<EmployeeModel[]>([])
      )
        .then(list => setEmployees(list))
        .catch(error => {
          if (!axios.isCancel(error)) {
            throw error;
          }
        })
        .finally(() => {
          cancelRequestRef.current = undefined;
          setIsFetching(false);
        });
    }, debounceTimeout),
    [debounceTimeout, setEmployees, setIsFetching, minSearchLength, requester, extraParams, paramsGetter]
  );

  const handleChange = useCallback(
    (newValue: string | string[]) => callOnChange({ onChange, multiple } as SpecificProps, newValue, employees),
    [onChange, multiple, employees]
  );
  const options = useMemo(() => employees.map(employeeToOption), [employees]);

  return (
    <Select
      {...props}
      placeholder={placeholder}
      suffixIcon={<Icon />}
      clearIcon={<Close />}
      allowClear
      showSearch
      filterOption={false}
      mode={multiple ? 'multiple' : undefined}
      value={selected}
      onSearch={handleSearch}
      onChange={handleChange}
      notFoundContent={isFetching ? <Spin size="small" /> : null}
      options={options}
    />
  );
};
