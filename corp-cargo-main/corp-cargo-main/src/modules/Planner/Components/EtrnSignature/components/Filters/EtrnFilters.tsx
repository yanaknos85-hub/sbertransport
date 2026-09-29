import React, { FC } from 'react';
import { Button, Form, Tooltip } from 'antd';
import { FieldType } from 'shared/form/Field/Field';
import { colors, fontFamily } from 'shared/styles/styles';
import FormField from 'shared/form/FormField/FormField';
import { FilterIcon } from 'modules/OrderExecution/components/Icon';
import { ReactComponent as SearchIcon } from 'modules/Planner/images/search.svg';

import styles from './styles.module.scss';

export const EtrnFilters: FC = () => {
  const field = {
    find: {
      label: '',
      name: 'routeNumber',
      type: FieldType.input,
      index: 0,
      allowClear: true,
      params: {
        placeholder: 'Поиск по номеру ЭТрН',
        disabled: true,
        style: {
          height: '40px',
          backgroundColor: colors.bgDark,
          fontFamily: fontFamily.SBSansTextRegular,
        },
        suffix: <SearchIcon />,
      },
    },
  };

  return (
    <div className={styles.etrnFilterContainer}>
      <div className={styles.etrnFilterHandlers}>
        <Tooltip title="В разработке">
          <div className={styles.formWrapper}>
            <Form component={false}>
              <FormField {...field.find} />
            </Form>
          </div>
        </Tooltip>
        <Tooltip title="В разработке">
          <span style={{ display: 'inline-flex' }}>
            <Button
              className={styles.etrnFilterSection__filterButton}
              type="primary"
              icon={<FilterIcon />}
              size="large"
              disabled
            >
              Фильтры
            </Button>
          </span>
        </Tooltip>
      </div>
    </div>
  );
};