import React, { FC, useEffect, useState } from 'react';
import { Form } from 'antd';

import { FieldType } from 'shared/form/Field/Field';
import { colors, fontFamily } from 'shared/styles/styles';
import FormField from 'shared/form/FormField/FormField';

import { FormInstance } from 'antd/lib/form/Form';
import { useForm } from 'antd/es/form/Form';
import { ReactComponent as SearchIcon } from '../../../images/search.svg';
import { ReactComponent as CrossIcon } from '../../../images/crossIcon.svg';
import { ReactComponent as FiltersIcon } from '../../../images/filterIcon.svg';
import { MonitorFiltersType } from '../../../types';

import * as S from './FilterInput.styles';

interface FiltersProps {
  filtersForm: FormInstance;
  filterIdForm?: FormInstance;
  getFilteredRoutes: (request: MonitorFiltersType) => void;
  handleFilters?: () => void;
  showFiltersIcon?: boolean;
}

export const FilterInput: FC<FiltersProps> = props => {
  const { filtersForm, getFilteredRoutes, handleFilters, showFiltersIcon = true, filterIdForm } = props;
  const [isCrossVisible, setIsCrossVisible] = useState(false);

  const sourceForm = filterIdForm ?? filtersForm;

  useEffect(() => {
    const routeIdValue = filtersForm.getFieldValue('humanReadableId');
    const sourceRouteId = sourceForm.getFieldValue('routeNumber');
    if (routeIdValue !== sourceRouteId) {
      sourceForm.setFieldsValue({ routeNumber: routeIdValue });
    }
    setIsCrossVisible(!!sourceRouteId?.length);
  }, [filtersForm.getFieldValue('humanReadableId')]);

  const handleGetFilteredRoutes = () => {
    const routeIdValue = sourceForm.getFieldValue('routeNumber');
    getFilteredRoutes({
      ...filtersForm.getFieldsValue(),
      humanReadableId: routeIdValue
    });
    filtersForm.setFieldsValue({ humanReadableId: routeIdValue });
  };

  const field = {
    find: {
      label: '',
      name: 'routeNumber',
      type: FieldType.input,
      index: 0,
      allowClear: true,
      params: {
        placeholder: 'Поиск по ID',
        style: {
          backgroundColor: colors.bgDark,
          fontFamily: fontFamily.SBSansTextRegular,
        },
        suffix: (
          <>
            {isCrossVisible && (
              <S.Button
                onClick={() => {
                  sourceForm.resetFields();
                  handleGetFilteredRoutes();
                }}
              >
                <CrossIcon />
              </S.Button>
            )}
             {showFiltersIcon ? (
              <S.Button onClick={() => handleFilters?.()}>
                <FiltersIcon />
              </S.Button>
            ) : (
              <S.Button onClick={handleGetFilteredRoutes}>
                <SearchIcon />
              </S.Button>
            )}
          </>
        ),
      },
    },
  };

  return (
    <S.FormWrapper>
      <Form
        form={sourceForm as FormInstance}
        onValuesChange={({ routeNumber }) => {
          setIsCrossVisible(!!routeNumber);
          filtersForm.setFieldsValue({ humanReadableId: routeNumber });
        }}
        onKeyDown={e => {
          if (e.key === 'Enter') {
            e.preventDefault();
            getFilteredRoutes({ ...filtersForm.getFieldsValue(), humanReadableId: sourceForm.getFieldValue('routeNumber') });
          }
        }}
      >
        <FormField {...field.find} />
      </Form>
    </S.FormWrapper>
  );
};
