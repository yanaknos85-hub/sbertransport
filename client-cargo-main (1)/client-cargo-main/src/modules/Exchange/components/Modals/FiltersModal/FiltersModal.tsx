import React, { FC, useEffect, useRef } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { Button, Form } from 'antd';
import { useForm } from 'antd/lib/form/Form';
import cn from 'classnames';
import { StoreNames } from 'ioc/ioc.storeNames';
import { FieldType } from 'shared/form/Field/Field';
import FormField from 'shared/form/FormField/FormField';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import TModal from 'shared/ui/Modal/Modal';

import { useAPIQueryCache } from 'api';

import { useGetFilters } from '../../../api/filters';
import { KEYS } from '../../../api/types';
import { useExchangeFilters } from '../../../hooks/useExchangeFilters';
import { ReactComponent as BookmarkIcon } from '../../../static/images/bookmarkIcon.svg';
import { ReactComponent as CrossIcon } from '../../../static/images/crossIcon.svg';

import styles from './FiltersModal.module.scss';

interface Props {
  visible: boolean;
  handleClose: () => void;
  setDescriptionAddressFrom: (value: string) => void;
  setDescriptionAddressTo: (value: string) => void;
}

export const FiltersModal: FC<Props> = props => {
  const {
    visible, handleClose, setDescriptionAddressFrom, setDescriptionAddressTo,
  } = props;
  const [form] = useForm();
  const { params } = useRouteMatch();
  const type = params.type;

  const {
    [StoreNames.exchangeStore]: exchangeStore,
  } = useAppStoreContext();

  const { data: savedFilters } = useGetFilters();
  const { handleSave } = useExchangeFilters(form, setDescriptionAddressFrom, setDescriptionAddressTo);
  const { getFilterAvailableList } = exchangeStore;
  const cache = useAPIQueryCache();

  // Флаг для предотвращения повторной инициализации после ручного изменения
  const isInitialized = useRef(false);

  useEffect(() => {
    if (visible && !isInitialized.current && savedFilters && (savedFilters.addressFrom || savedFilters.addressTo)) {
      form.setFieldsValue(savedFilters);
      isInitialized.current = true;
    }
  }, [visible, savedFilters, form]);

  // Обновляем адреса в Controls при открытии модалки
  useEffect(() => {
    if (visible && savedFilters) {
      setDescriptionAddressFrom(savedFilters.addressFrom || '');
      setDescriptionAddressTo(savedFilters.addressTo || '');
    }
  }, [visible, savedFilters, setDescriptionAddressFrom, setDescriptionAddressTo]);

  // Сбрасываем флаг при закрытии модалки, чтобы при следующем открытии данные загрузились из useGetFilters
  useEffect(() => {
    if (!visible) {
      isInitialized.current = false;
    }
  }, [visible]);

  const handleReset = () => {
    form.resetFields();
    const {
      addressFrom,
      addressTo,
    } = form.getFieldsValue();
    setDescriptionAddressFrom(addressFrom || '');
    setDescriptionAddressTo(addressTo || '');
    getFilterAvailableList(type, addressFrom || '', addressTo || '', undefined);
  };

  const onFinish = async () => {
    const addressFrom = form.getFieldValue('addressFrom');
    const addressTo = form.getFieldValue('addressTo');
    const desiredDateRangeRaw = form.getFieldValue('desiredDateRange');

    const desiredDateRange = desiredDateRangeRaw
      ? {
        start: desiredDateRangeRaw[0]?.toISOString(),
        end: desiredDateRangeRaw[1]?.toISOString(),
      }
      : undefined;

    setDescriptionAddressFrom(addressFrom || '');
    setDescriptionAddressTo(addressTo || '');
    await getFilterAvailableList(type, addressFrom || '', addressTo || '', desiredDateRange);
    // Обновляем кэш с сохранёнными фильтрами данными из формы
    // @ts-ignore
    cache.setQueryData([KEYS.EXCHANGE_FILTERS], {
      addressFrom: addressFrom || undefined,
      addressTo: addressTo || undefined,
    });
    handleClose();
  };

  const onSaveClick = () => {
    const addressFrom = form.getFieldValue('addressFrom');
    const addressTo = form.getFieldValue('addressTo');

    handleSave(addressFrom || undefined, addressTo || undefined);
  };

  const fields = {
    addressFrom: {
      label: 'Адрес отправления',
      name: ['addressFrom'],
      type: FieldType.address,
      params: {
        dropdownMatchSelectWidth: false,
        dropdownStyle: {
          borderRadius: '12px',
        },
      },
    },
    addressTo: {
      label: 'Адрес доставки',
      name: ['addressTo'],
      type: FieldType.address,
      params: {
        dropdownMatchSelectWidth: false,
        dropdownStyle: {
          borderRadius: '12px',
        },
      },
    },
    desiredDateRange: {
      label: 'Дата доставки',
      name: 'desiredDateRange',
      type: FieldType.dateRange,
    },
  };

  return (
    <TModal
      footer={null}
      title="Фильтры"
      visible={visible}
      closeIcon={<CrossIcon />}
      onCancel={() => {
        handleClose();
      }}
    >
      <Form
        form={form}
        onFinish={() => onFinish()}
      >
        <FormField {...fields.addressFrom} />
        <FormField {...fields.addressTo} />
        <FormField {...fields.desiredDateRange} />
        <div className={styles.buttonsWrapper}>
          <div className={styles.saveButtonWrapper}>
            <Button
              className={styles.bookmarkButton}
              type="text"
              icon={<BookmarkIcon />}
              onClick={onSaveClick}
            >
              Сохранить
            </Button>
          </div>
          <Button
            className={cn(styles.button, styles.reset)}
            onClick={() => handleReset()}
          >
            Сбросить
          </Button>
          <Button
            className={cn(styles.button, styles.submit)}
            htmlType="submit"
          >
            Применить
          </Button>
        </div>
      </Form>
    </TModal>
  );
};
