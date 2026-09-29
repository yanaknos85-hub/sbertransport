import React, { FC, useState } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { Button, Form } from 'antd';
import cn from 'classnames';
import { StoreNames } from 'ioc/ioc.storeNames';
import { useUiContext } from 'shared/components/UI';
import { FieldType } from 'shared/form/Field/Field';
import FormField from 'shared/form/FormField/FormField';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { ReactComponent as CrossIcon } from '../../static/images/crossIcon.svg';
import { ReactComponent as SearchIcon } from '../../static/images/search.svg';

import styles from './FilterById.module.scss';

interface Props {
  isCrossVisible: boolean;
}

export const FilterById: FC<Props> = () => {
  const {
    [StoreNames.exchangeStore]: exchangeStore,
  } = useAppStoreContext();

  const { getRequestById, getAvailableList } = exchangeStore;

  const [form] = Form.useForm();
  const { isMobile } = useUiContext();
  const [isCrossVisible, setIsCrossVisible] = useState(false);
  const { params: { type } } = useRouteMatch();

  const handleSearch = async () => {
    const id = form.getFieldValue('humanReadableId');
    await getRequestById(type, id);
  };

  const handleReset = async () => {
    form.resetFields();
    setIsCrossVisible(false);
    await getAvailableList(type, exchangeStore.pageSetting, exchangeStore.sortSetting);
  };

  const field = {
    find: {
      label: '',
      name: 'humanReadableId',
      type: FieldType.input,
      index: 0,
      allowClear: true,
      params: {
        onChange: (value: string) => {
          setIsCrossVisible(!!value);
        },
        placeholder: 'Поиск по номеру заявки',
        style: {
          backgroundColor: '#F2F3F6',
          fontFamily: 'SB Sans Text Regular',
        },
        suffix: (
          <>
            {isCrossVisible && (
              <Button
                className={styles.findIcon}
                onClick={handleReset}
              >
                <CrossIcon />
              </Button>
            )}
            <Button
              className={styles.findIcon}
              onClick={handleSearch}
            >
              <SearchIcon />
            </Button>
          </>
        ),
      },
    },
  };

  return (
    <Form
      form={form}
      className={cn(styles.form, {
        [styles['formMobile']]: isMobile,
      })}
    >
      <FormField {...field.find} />
    </Form>
  );
};
