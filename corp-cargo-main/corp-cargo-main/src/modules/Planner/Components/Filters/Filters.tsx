import React, { FC } from 'react';

import { FieldType } from 'shared/form/Field/Field';
import { colors, fontFamily } from 'shared/styles/styles';
import FormField from 'shared/form/FormField/FormField';
import * as S from './Filters.styles';
import { ReactComponent as SettingsIcon } from '../../images/settingsIcon.svg';

// @deprecated
interface FiltersProps {
  handleFilters?: () => void;
}

export const Filters: FC<FiltersProps> = props => {
  const { handleFilters } = props;

  const field = {
    find: {
      label: '',
      name: 'find',
      type: FieldType.input,
      index: 0,
      params: {
        placeholder: 'Поиск',
        style: {
          backgroundColor: colors.bgDark,
          fontFamily: fontFamily.SBSansTextRegular,
        },
        suffix: (
          <S.Button onClick={handleFilters}>
            <SettingsIcon />
          </S.Button>
        ),
      },
    },
  };
  return (
    <S.FormWrapper>
      <FormField {...field.find} />
    </S.FormWrapper>
  );
};
