import * as React from 'react';
import { FC, useEffect, useRef } from 'react';
import { observer } from 'mobx-react';
import { Button, Form } from 'antd';
import ErrorBoundary from 'antd/lib/alert/ErrorBoundary';
import { useCargoStatuses } from 'api/engineer';
import { searchSymbol } from 'utils/searchSymbol';
import { useForm } from 'antd/lib/form/Form';
import { RequestType } from "shared/constants/forms.constants";
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import SelectStatus from '../components/SelectStatus';
import FormItem from '../../FormItem';
import Input from '../../Inputs/Input';
import { DatePickerRange } from '../../Inputs/DatePickerRange';
import { SelectDepartmentFilter } from '../../Inputs/SelectDepartmentFilter';
import { SelectTemplateFilter } from '../../Inputs/SelectTemplateFilter';
import moment from 'moment';

import styles from '../search.module.scss';

const FIELD_LABELS = {
  id: 'ID заявки',
  status: 'Статус заявки',
  corpClient: 'Корпоративный клиент',
  creationTime: 'Дата и время создания заявки',
  department: 'Подразделение',
  scheduleCreationTime: 'Дата и время создания расписания',
  deadline: 'Контрольный срок',
  desiredDate: 'Дата отправки',
  deliveryType: 'Тип доставки',

  sender: 'ФИО отправителя',
  senderPhone: 'Телефон',
  senderPositionName: 'Должность',
  senderAddressType: 'Тип адреса',
  addressStart: 'Адрес отправления',
  cargoType: 'Тип груза',

  recipient: 'ФИО получателя',
  recipientPhone: 'Телефон',
  recipientPositionName: 'Должность',
  recipientAddressType: 'Тип адреса',
  cityEnd: 'Город получения',
  addressEnd: 'Адрес получения',
  requestType: 'Тип заявки',
};

interface ISearchProps {
  onClose: () => void;
  category: string;
}

export const CargoSearch: FC<ISearchProps> = observer(({ onClose, category }): JSX.Element => {
  const { cargoStore } = useAppStoreContext();
  const [form] = useForm<Fields>();
  type Fields = any; // TODO FeedSearchQuery | null;
  const prevCategoryRef = useRef(category);

  const templateOptions = [
    { label: 'Обычная заявка', value: RequestType.SINGLE },
    { label: 'Регулярная заявка', value: RequestType.REGULAR },
    { label: 'Переезд', value: RequestType.RELOCATION },
  ];

  // Флаг для предотвращения двойного вызова при инициализации
  const isInitializing = useRef(false);

  useEffect(() => {
    // Сброс флага при смене категории
    if (prevCategoryRef.current !== category) {
      form.resetFields();
      prevCategoryRef.current = category;
    }

    // Сброс флага инициализации перед установкой значений
    isInitializing.current = true;

    const filters = { ...cargoStore.cargoQueryFilters };

    if (filters.creationTimeFrom && filters.creationTimeTo) {
      filters.creationTime = {
        value: [
          moment(filters.creationTimeFrom),
          moment(filters.creationTimeTo)
        ]
      };
    }

    if (filters.controlTimeFrom && filters.controlTimeTo) {
      filters.controlTime = {
        value: [
          moment(filters.controlTimeFrom),
          moment(filters.controlTimeTo)
        ]
      };
    }

    if (filters.desiredTimeFrom && filters.desiredTimeTo) {
      filters.desiredTime = {
        value: [
          moment(filters.desiredTimeFrom),
          moment(filters.desiredTimeTo)
        ]
      };
    }

    form.setFieldsValue(filters);

    // Сброс флага после установки значений, чтобы обработчики заработали
    // Флаг сбрасывается синхронно, чтобы onChange, вызванные setFieldsValue, были проигнорированы
    isInitializing.current = false;

  }, [cargoStore.cargoQueryFilters, category, form]);

  const handleSubmit = (queryProps: Fields) => {
    const targetQueryProps = {
      ...cargoStore.cargoQueryFilters,
      ...queryProps,
      transportType: cargoStore.activeCategory !== 'all' ? cargoStore.activeCategory.toUpperCase() : undefined,
    };

    // Смотрим именно в queryProps (то, что пришло с формы)
    const formCreationTime = queryProps.creationTime;

    // Проверяем валидность
    const hasValidCreationTime = formCreationTime &&
      formCreationTime.value &&
      formCreationTime.value.filter(Boolean).length === 2;

    if (hasValidCreationTime) {
      targetQueryProps.creationTimeFrom = formCreationTime.value[0].toISOString();
      targetQueryProps.creationTimeTo = formCreationTime.value[1].toISOString();
    } else {
      // Явно ставим undefined, чтобы перезаписать значение в Store
      targetQueryProps.creationTimeFrom = undefined;
      targetQueryProps.creationTimeTo = undefined;
    }
    // Служебное поле формы можно удалить, оно в сторе не нужно
    delete targetQueryProps.creationTime;

    // Обработка controlTime
    const formControlTime = queryProps.controlTime;
    const hasValidControlTime = formControlTime &&
      formControlTime.value &&
      formControlTime.value.filter(Boolean).length === 2;

    if (hasValidControlTime) {
      targetQueryProps.controlTimeFrom = formControlTime.value[0].toISOString();
      targetQueryProps.controlTimeTo = formControlTime.value[1].toISOString();
    } else {
      targetQueryProps.controlTimeFrom = undefined;
      targetQueryProps.controlTimeTo = undefined;
    }
    delete targetQueryProps.controlTime;

    // Обработка desiredTime
    const formDesiredTime = queryProps.desiredTime;

    const hasValidDesiredTime = formDesiredTime &&
      formDesiredTime.value &&
      formDesiredTime.value.filter(Boolean).length === 2;

    if (hasValidDesiredTime) {
      targetQueryProps.desiredTimeFrom = formDesiredTime.value[0].toISOString();
      targetQueryProps.desiredTimeTo = formDesiredTime.value[1].toISOString();
    } else {
      targetQueryProps.desiredTimeFrom = undefined;
      targetQueryProps.desiredTimeTo = undefined;
    }
    delete targetQueryProps.desiredTime;

    cargoStore.setCargoFilterQueryProps(targetQueryProps);

    if (category === 'template') {
      cargoStore.getCargoSchedulerList();
    } else {
      cargoStore.getCargoOrderListDeferredPost();
    }
    onClose();
  };

  const saveFilters = () => {
    cargoStore.setCargoFilterQueryProps(form.getFieldsValue());
  };

  const handleReset = () => {
    cargoStore.resetFilters();
    form.resetFields();
  };

  const cargoStatuses = useCargoStatuses();

  return (
    <ErrorBoundary>
      <Form
        form={form}
        onFinish={handleSubmit}
        className={styles.form}
      >
        <div className={styles.blockTitle}>Заявка</div>
        <div className={styles.blockRow}>
          <FormItem name="id" label={`${FIELD_LABELS.id}`}>
            <Input allowClear placeholder={`${FIELD_LABELS.id}`} />
          </FormItem>

          <FormItem name="statuses" label={`${FIELD_LABELS.status}`}>
            <SelectStatus
              className={styles.select}
              allowClear
              showSearch
              optionFilterProp="label"
              filterOption={searchSymbol}
              placeholder={`${FIELD_LABELS.status}`}
              statusHook={cargoStatuses}
              mode={'multiple'}
              onChange={(value) => {
                // Игнорируем onChange при инициализации формы
                if (isInitializing.current) return;
                cargoStore.setCargoFilterQueryProps({
                  statuses: value,
                });
              }}
            />
          </FormItem>

          <FormItem name="creationTime" label={category === 'template' ? FIELD_LABELS.scheduleCreationTime : FIELD_LABELS.creationTime}>
            <DatePickerRange rangeTimeShow />
          </FormItem>
          {category !== 'template' && (
            <FormItem name="controlTime" label={`${FIELD_LABELS.deadline}`}>
              <DatePickerRange rangeTimeShow />
            </FormItem>
          )}
          {category !== 'template' && (
            <FormItem name="desiredTime" label={`${FIELD_LABELS.desiredDate}`}>
              <DatePickerRange rangeTimeShow />
            </FormItem>
          )}
        </div>

        <div className={styles.blockTitle}>Отправитель</div>
        <div className={styles.blockRow}>
          <FormItem name="senderName" label={`${FIELD_LABELS.sender}`}>
            <Input allowClear placeholder={`${FIELD_LABELS.sender}`} />
          </FormItem>

          <FormItem name="senderAddress" label={`${FIELD_LABELS.addressStart}`}>
            <Input allowClear placeholder={`${FIELD_LABELS.addressStart}`} />
          </FormItem>
        </div>

        <div className={styles.blockTitle}>Получатель</div>
        <div className={styles.blockRow}>
          <FormItem name="recipientName" label={`${FIELD_LABELS.recipient}`}>
            <Input allowClear placeholder={`${FIELD_LABELS.recipient}`} />
          </FormItem>

          <FormItem name="recipientAddress" label={`${FIELD_LABELS.addressEnd}`}>
            <Input allowClear placeholder={`${FIELD_LABELS.addressEnd}`} />
          </FormItem>
        </div>

        <div className={styles.blockTitle} />
        <div className={styles.blockRow}>
          <FormItem name="authorDepartment" label={FIELD_LABELS.department}>
            <SelectDepartmentFilter allowClear placeholder={FIELD_LABELS.department} />
          </FormItem>
          {category !== 'template' && (
            <FormItem name="requestType" label={FIELD_LABELS.requestType}>
              <SelectTemplateFilter
                className={styles.select}
                mode="multiple"
                options={templateOptions}
                allowClear
                showSearch
                placeholder={FIELD_LABELS.requestType}
                onChange={(value) => {
                  // Игнорируем onChange при инициализации формы
                  if (isInitializing.current) return;
                  cargoStore.setCargoFilterQueryProps({
                    requestType: value,
                  });
                }}
              />
            </FormItem>
          )}
        </div>

        <div className={styles.formFooter}>
          <div className={styles.saveFilter} onClick={() => saveFilters()}>
            Сохранить фильтр
          </div>
          <div className={styles.actionButtons}>
            <div className={styles.resetFilter} onClick={handleReset}>
              Сбросить
            </div>
            <Button
              type="primary"
              className={styles.acceptFilter}
              onClick={form.submit}
            >
              Подтвердить
            </Button>
          </div>
        </div>
      </Form>
    </ErrorBoundary>
  );
});
