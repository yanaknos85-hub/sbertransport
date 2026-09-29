import React, { FC } from 'react';
import { Form } from 'antd';

import { FieldType } from 'shared/form/Field/Field';
import { colors, fontFamily } from 'shared/styles/styles';
import FormField from 'shared/form/FormField/FormField';

import { FormInstance } from 'antd/lib/form/Form';
import { useForm } from 'antd/es/form/Form';
import { ReactComponent as SearchIcon } from '../../../images/search.svg';
import { ReactComponent as CrossIcon } from '../../../images/crossIcon.svg';
import { RoutesFiltersType } from '../../../types';
import { ReactComponent as FilterIcon } from '../../../images/filterIcon.svg';

import * as S from './FilterInput.styles';

interface FiltersProps {
  filtersForm: FormInstance;
  getFilteredRoutes: (request: RoutesFiltersType) => void;
  handleFilters?: () => void;
}

export const FilterOrderInput: FC<FiltersProps> = props => {
  const { filtersForm, getFilteredRoutes, handleFilters } = props;

  const [form] = useForm();

  const isCrossVisible = !!form.getFieldValue('routeId')?.length;

  const field = {
    find: {
      label: '',
      name: 'routeId',
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
                  form.resetFields();
                  getFilteredRoutes({
                    ...filtersForm.getFieldsValue(),
                    humanReadableId: form.getFieldValue('routeId'),
                  });
                }}
              >
                <CrossIcon />
              </S.Button>
            )}
            <S.Button onClick={handleFilters}>
              <FilterIcon />
            </S.Button>
            {/* <S.Button
              onClick={() => getFilteredRoutes({ ...filtersForm.getFieldsValue(), humanReadableId: form.getFieldValue('routeId') })}
            >
              <SearchIcon />
            </S.Button> */}
          </>
        ),
      },
    },
  };

  return (
    <S.FormWrapper>
      <Form
        form={form}
        onKeyDown={e => {
          if (e.key === 'Enter') {
            e.preventDefault();
            getFilteredRoutes({ ...filtersForm.getFieldsValue(), humanReadableId: form.getFieldValue('routeId') });
          }
        }}
      >
        <FormField {...field.find} />
      </Form>
    </S.FormWrapper>
  );
};
