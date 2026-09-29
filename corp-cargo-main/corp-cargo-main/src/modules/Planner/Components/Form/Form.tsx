import React, { FC, useState, useEffect } from 'react';
import { Form as FormAntd } from 'antd';
import FormField from 'shared/form/FormField/FormField';
import { useTranslation } from 'i18n';
import moment, { Moment } from 'moment';
import { useGetContractorsTypes } from 'api/planner';
import { Modal } from 'shared/components/Modal/Modal';
import { FormInstance } from 'antd/lib/form';
import { notification } from 'antd';
import { fields } from '../RouteDetailed/fields';
import { ContractorType, RouteStatusEnum, RouteType } from '../../types';
import { DATE_FORMAT } from 'constants/constants.app';

import * as S from './Form.style';

interface Props {
  form?: FormInstance;
  route: RouteType;
  updateRoute: (route: RouteType | { tariffId?: string }) => void;
  maxDate?: number;
  minDate?: number;
}
interface Options {
  value: string;
  label: string;
}

const NOTIFICATION_DURATION = 5;

export const Form: FC<Props> = props => {
  const { form, route, updateRoute } = props;
  const {
    desiredDate,
    contractorInfo,
    auto,
    regionId,
    status,
    author: { organizationId },
    requests,
    auto: { cargoCategory },
  } = route;
  const [contractorNameList, setContractorNameList] = useState<ContractorType[]>([]);
  const [listNameOptions, setListNameOptions] = useState<Options[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [humanReadableIdList, setHumanReadableIdList] = useState('');
  const { t } = useTranslation();

  const { name: departmentName } = contractorInfo || '';
  const formattedDate = moment(desiredDate).format('YYYY-MM-DD');
  const { name } = auto;
  const { data: contractorsList } = useGetContractorsTypes(regionId, formattedDate, organizationId, cargoCategory);

  // Если не приходит requests, ставим это условие, иначе получаем ошибку
  const min = requests?.length > 0 ? moment.min(requests?.map(({ desiredDate }) => moment(desiredDate))).valueOf() : moment(desiredDate).valueOf();
  const max = requests?.length > 0 ? moment.max(requests?.map(({ desiredDate }) => moment(desiredDate))).valueOf() : moment(desiredDate).valueOf();

  useEffect(() => {
    const list: string[] = [];
    contractorsList.forEach(contractor => {
      if (contractor.auto.name === name) {
        contractor.contractors.forEach(item => {
          list.push(item.name);
        });
        setContractorNameList(contractor.contractors);
      }
    });

    const listOptions = list.reduce((acc: Options[], cur: string) => [...acc, { value: cur, label: cur }], []);
    setListNameOptions(listOptions);

    form?.setFieldsValue({
      transportType: auto.name, desiredDate: moment(desiredDate),
      contractor: departmentName || form?.getFieldValue('contractor'),
    });
  }, [contractorsList, auto]);

  const isDisabled = status === RouteStatusEnum.CARGO_PLANNING_FINISHED;

  const vehicleTypeOptions = contractorsList.reduce((acc: Options[], { auto }) => {
    const option = {
      label: String(auto.name),
      value: String(auto.name),
    };

    return [...acc, option];
  }, []);

  const handleContractorsList = (cargoType: string) => {
    const list: string[] = [];

    contractorsList.forEach(contractor => {
      if (contractor.auto.name === cargoType) {
        contractor.contractors.forEach(item => {
          list.push(item.name);
        });
        setContractorNameList(contractor.contractors);
      }
    });

    const listOptions = list.reduce((acc: Options[], cur: string) => [...acc, { value: cur, label: cur }], []);

    setListNameOptions(listOptions);
  };

  const updateContractorList = (value: any) => {
    handleContractorsList(value);
    form?.setFieldsValue({ contractor: '' });
  };

  const isInRange = (date: string) => {
    const diffMin = moment(min).diff(moment(date), 'days');
    const diffMax = moment(max).diff(moment(date), 'days');

    if (diffMax > 20) {
      notification.error({
        description: 'Возможное отклонение от плановой даты доставки: ± 20 календарных дней',
        message: 'Установка даты маршрута ранее 20 дней запрещена',
        duration: NOTIFICATION_DURATION,
      });
      return false;
    }

    if (diffMin < -20) {
      notification.error({
        description: 'Возможное отклонение от плановой даты доставки: ± 20 календарных дней',
        message: 'Установка даты маршрута позднее 20 дней запрещена',
        duration: NOTIFICATION_DURATION,
      });
      return false;
    }
    return true;
  };

  const getControlDateList = (date: string) => {
    return requests?.filter(request => {
      const diff = moment(date).startOf('day').diff(moment(request.controlDate).startOf('day'), 'days');
      if (diff > 0) {
        return request;
      }
    })
  };

  const handleChangeDate = async (date: Moment | string) => {
    const dateString = typeof date === 'string' ? date : date.format(DATE_FORMAT.DATE_WITH_TIME_ISO_SECONDS);
    const formattedDate = dateString;
    const isRange = isInRange(dateString);
    const controlDateList = getControlDateList(dateString);
    const list = controlDateList?.map(item => item.humanReadableId).join(', ');
    setHumanReadableIdList(list)

    if (isRange && controlDateList.length > 0) {
      setModalVisible(true);
    }

    if (isRange && controlDateList.length === 0) {
      await updateRoute({ ...route, desiredDate: formattedDate });
    }
  };

  const submitForm = async () => {
    setModalVisible(false);
    await updateRoute({
      ...route,
      desiredDate: moment(form?.getFieldValue('desiredDate')).format(DATE_FORMAT.DATE_WITH_TIME_ISO_SECONDS)
    });
  };

  return (
    <S.FormWrapper>
      <FormAntd
        form={form}
        name="dataRoute"
      >
        <S.InputWrapper>
          <S.FieldWrapper>
            <FormField
              {...fields.transportType}
              params={{
                disabled: isDisabled,
                allowClear: true,
                options: vehicleTypeOptions,
                onChange: (value: string) => {
                  updateContractorList(value);
                },
              }}
            />
          </S.FieldWrapper>
          <S.FieldWrapper>
            <FormField
              {...fields.contractor}
              params={{
                disabled: isDisabled,
                allowClear: true,
                options: listNameOptions,
                onSelect: async () => {
                  const contractorName = form?.getFieldValue('contractor');

                  const contractor = contractorNameList.find(contractor => contractor.name === contractorName);

                  await updateRoute({ ...route, tariffId: contractor?.tariffId });
                },
              }}
            />
          </S.FieldWrapper>
        </S.InputWrapper>
        <S.FieldWrapper>
          <FormField
            {...fields.desiredDate}
            params={{
              disabled: isDisabled,
              showNow: false,
              showTime: { format: 'HH:mm' },
              onChange: (value: Moment | null) => {
                if (value) {
                  handleChangeDate(value);
                }
              },
              disabledDate: (currentDate: Moment) => {
                // Если пришёл requests, делаем интервал 20 дней от минимальной и максимальной даты
                if (requests?.length > 0) {
                  const minDate = moment(min);
                  const maxDate = moment(max);
                  const disableDate = moment(min).add(20, 'days')
                    return (
                      currentDate && (currentDate < minDate || (currentDate > minDate && currentDate < maxDate) || currentDate > disableDate)
                    );
                }
                // Если requests не пришёл, делаем возможность выбора даты, начиная с завтра
                return currentDate && currentDate < moment().add(1, 'days');
              },
              value: moment(requests?.length > 0 ? desiredDate : moment().add(1, 'days')),
            }}
          />
        </S.FieldWrapper>
      </FormAntd>
      <Modal
        centered
        visible={modalVisible}
        onOk={submitForm}
        onCancel={() => setModalVisible(false)}
        destroyOnClose
        okText={t.global.save}
      >
        <p>Установленная дата маршрута влечет за собой нарушение SLA по заявке</p>
        <p>{`${humanReadableIdList} нарушается SLA по сроку доставки`}</p>
        <p>Подтвердите дату отправления</p>
      </Modal>
    </S.FormWrapper>
  );
};
