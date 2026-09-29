import {
  Requester, RequestCanceler, RequestParams, EmployeeModel
} from '@sber-sbertransport/mf-core';
import React, {
  FC, useCallback, useEffect, useMemo, useRef, useState
} from 'react';
import axios from 'axios';
import debounce from 'lodash/debounce';
import { Select, Spin } from 'antd';
import { LabeledValue, SelectProps } from 'antd/es/select';
import { EmployeeStatus } from 'modules/EmployeeApp/EmployeeApp.constants';
import './override.scss';
import ChevronSmall from '../Images/view/menu 2.0/ChevronSmall';

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
  label: employee.fullNameWithCode,
});

const getInitialValues = (props: SpecificProps) => props.multiple ? (props.value || []).map(item => item.id) : props.value && props.value.id;

const getInitialEmployees = (props: SpecificProps) => props.multiple ? props.value || [] : (props.value && [props.value]) || [];

const callOnChange = (props: SpecificProps, newValue: string | string[], employees: EmployeeModel[]) => props.onChange
  && (props.multiple
    ? props.onChange(employees.filter(item => newValue.includes(item.id)))
    : props.onChange(employees.find(item => item.id === newValue)));

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
      const cancelRequest = cancelRequestRef.current;
      if (cancelRequest) {
        cancelRequest();
      }
      setIsFetching(true);
      (search && search.length >= minSearchLength
        ? requester(paramsGetter(search, extraParams), cancel => {
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

  const filterEmployees = useMemo(() => employees.filter(el => el.status === EmployeeStatus.ACTIVE), [employees]);
  const options = useMemo(() => filterEmployees.map(employeeToOption), [filterEmployees]);

  return (
    <Select
      suffixIcon={(
        <div style={{ borderLeft: '1px solod grey', padding: '5px 24px' }}>
          <ChevronSmall />
        </div>
      )}
      placeholder={placeholder}
      allowClear
      filterOption={false}
      showSearch
      mode={multiple ? 'multiple' : undefined}
      value={selected}
      onSearch={handleSearch}
      onChange={handleChange}
      className="EmployeeBaseAutoComplete"
      notFoundContent={isFetching ? <Spin size="small" /> : null}
      {...props}
      options={options}
    />
  );
};
