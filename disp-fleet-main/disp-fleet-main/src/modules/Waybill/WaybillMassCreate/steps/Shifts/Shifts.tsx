import React, { FC } from 'react';
import { useHistory } from '@sber-sbertransport/mf-core';
import moment from 'moment';
import {
  Row, Col, Table, Divider, PageHeader, Tooltip
} from 'antd';

import { useAppStore } from 'ioc';
import { useTranslation } from 'i18n';
import { DATE_FORMAT } from 'constants/app.constants';
import * as routes from 'constants/routes.constants';

import { Button } from 'components/Button';
import { FormItem } from 'components/FormItem';
import DatePicker from 'components/DatePicker/DatePicker';
import { SearchPanel } from 'components/SearchPanel/SearchPanel';

import { useColumns } from './hooks/useColumns';
import { useShiftsFilter } from './hooks/useShiftsFilter';
import { useShiftSelection } from './hooks/useShiftSelection';
import { useShiftErrorHandling } from './hooks/useShiftErrorHandling';
import { useCleanup } from './hooks/useCleanup';
import { Shift as ShiftType, TMassCreateFirstTitleItem, TMassCreateFirstTitleRequest } from 'api/shifts/shifts.types';
import { useMassCreateFirstTitle } from 'api/shifts/shifts.api';
import { ignore } from 'utils/utils';
import styles from './Shifts.module.scss';
import { StepsEnum } from '../steps.enum';

export interface ShiftsProps {
  selectedShifts: ShiftType[];
  setSelectedShifts: React.Dispatch<React.SetStateAction<ShiftType[]>>;
  shiftsData: ShiftType[] | undefined;
  isLoading: boolean;
  query: { startDate?: string };
  setQuery: (query: { startDate?: string }) => void;
  firstTitleResult: TMassCreateFirstTitleItem[] | undefined;
  setFirstTitleResult: React.Dispatch<React.SetStateAction<TMassCreateFirstTitleItem[] | undefined>>;
  setCurrentStep: React.Dispatch<React.SetStateAction<StepsEnum>>;
  maxSelectionLimit?: number;
}

const Shifts: FC<ShiftsProps> = ({
  selectedShifts,
  setSelectedShifts,
  shiftsData,
  isLoading,
  query,
  setQuery,
  firstTitleResult,
  setFirstTitleResult,
  setCurrentStep,
  maxSelectionLimit = 100,
}) => {
  const history = useHistory();
  const { createMass: i18 } = useTranslation().t.Waybill;
  const { logger } = useAppStore();

  const isMounted = useCleanup();

  // Хуки для фильтрации данных
  const { filteredShiftsData, handleSearch } = useShiftsFilter({ shiftsData });

  // Хуки для работы с ошибками
  const { errorShiftIds, getRowClassName } = useShiftErrorHandling({
    firstTitleResult,
    setSelectedShifts,
  });

  // Хуки для управления выбором
  const {
    isShiftDisabled, handleRowSelectionChange,
  } = useShiftSelection({
    errorShiftIds,
    maxSelectionLimit,
    selectedShifts,
    setSelectedShifts,
  });

  // Хуки для колонок таблицы
  const { columns } = useColumns({ firstTitleResult, errorShiftIds });

  const [mutate, { isLoading: isMutationLoading }] = useMassCreateFirstTitle();

  const renderRowSelectionCell = (
    _checked: boolean,
    record: ShiftType,
    index: number,
    originNode: React.ReactNode
  ) => {
    const isDisabled = isShiftDisabled(record.id);
    const title = firstTitleResult?.find(item => item.shiftId === record.id)?.errorText || i18.maxSelectionLimitText;

    if (isDisabled) {
      return (
        <Tooltip title={title}>
          {originNode}
        </Tooltip>
      );
    }

    return originNode;
  };

  const handleBack = () => {
    history.push(routes.RELEASE_ON_LINE_LINK);
  };

  const handleDateChange = (date: moment.Moment | null) => {
    const startDate = date?.format(DATE_FORMAT.BASE);
    setQuery({ startDate });
  };

  const handleNext = () => {
    if (!selectedShifts || selectedShifts.length === 0) {
      logger.toMessage('warning', 'Выберите хотя бы одну смену');
      return;
    }

    if (selectedShifts.length > maxSelectionLimit) {
      logger.toMessage('error', i18.maxSelectionLimitText);
      return;
    }

    const shiftIds = selectedShifts.map(shift => shift.id);

    const existingShiftIds = new Set(firstTitleResult?.map(item => item.shiftId) ?? []);
    const allShiftsAlreadyProcessed = shiftIds.every(id => existingShiftIds.has(id));

    if (allShiftsAlreadyProcessed) {
      // Случай 1 и 2: все выбранные смены уже есть в firstTitleResult
      // Убираем смены с ошибками и смены, которые больше не выбраны
      const filteredResult = firstTitleResult?.filter(
        item => item.errorText === '' && shiftIds.includes(item.shiftId)
      );
      if (isMounted) {
        setFirstTitleResult(filteredResult);
        setCurrentStep(StepsEnum.SignStep);
      }
      return;
    }

    // Случай 3: some shifts are missing in firstTitleResult
    // Находим недостающие смены
    const missingShiftIds = shiftIds.filter(id => !existingShiftIds.has(id));

    if (missingShiftIds.length > 0) {
      const request: TMassCreateFirstTitleRequest = {
        shiftIds: missingShiftIds,
        timeZone: moment().format('Z'),
      };
      mutate(request)
        .then(result => {
          if (isMounted) {
            // Проверяем, есть ли ошибки в новом результате
            const hasErrorsInNewResult = result && result.some(item => item.errorText !== '');

            if (hasErrorsInNewResult) {
              // Если есть ошибки в новом результате, объединяем с существующими (сохраняем старые ошибки)
              const mergedResult = [...(firstTitleResult || []), ...(result || [])];
              setFirstTitleResult(mergedResult);
            } else {
              // Если ошибок нет, очищаем firstTitleResult от ошибок и объединяем с новым результатом
              const existingItems = firstTitleResult?.filter(item => item.errorText === '') ?? [];
              const mergedResult = [...existingItems, ...(result || [])];
              setFirstTitleResult(mergedResult);

              // Переход на SignStep
              setCurrentStep(StepsEnum.SignStep);
            }
          }
        })
        .catch(() => {
          logger.toMessage('error', 'Не удалось создать файл');
          return ignore;
        });
      return;
    }
  };

  return (
    <>
      <PageHeader
        onBack={handleBack}
        title={i18.title}
        className={styles.pageHeader}
      />
      <Divider className={styles.divider} />

      <Row gutter={[24, 0]}>
        <Col span={12}>
          <FormItem label={i18.startDate}>
            <DatePicker
              format={DATE_FORMAT.BASE_REVERTED_DOTS}
              disabledDate={date => date.isBefore(moment(), 'day')}
              size="large"
              className={styles.datePicker}
              allowClear={false}
              value={query.startDate ? moment(query.startDate, DATE_FORMAT.BASE) : null}
              onChange={handleDateChange}
            />
          </FormItem>
        </Col>
      </Row>

      <div className={styles.helperText}>
        {i18.helperText}
      </div>

      <SearchPanel
        onSearch={handleSearch}
        placeholder="Поиск по Госномеру и ФИО"
        containerClassName={styles.searchPanel}
        allowClear
        minWidth={286}
      />

      <Table
        columns={columns}
        dataSource={filteredShiftsData}
        rowKey={record => String(record.id)}
        loading={isLoading}
        pagination={false}
        scroll={{ x: 950 }}
        className={styles.table}
        rowClassName={getRowClassName}
        rowSelection={{
          selectedRowKeys: selectedShifts.map(shift => String(shift.id)),
          onChange: handleRowSelectionChange,
          preserveSelectedRowKeys: true,
          getCheckboxProps: (record: ShiftType) => ({
            disabled: isShiftDisabled(record.id),
          }),
          renderCell: renderRowSelectionCell,
        }}
      />

      <Divider className={styles.divider} />

      <Row justify="end" gutter={[12, 0]}>
        <Col>
          <Button
            type="text"
            onClick={handleBack}
            className={styles.backButton}
          >
            {i18.backButton}
          </Button>
        </Col>
        <Col>
          <Button
            type="primary"
            onClick={handleNext}
            loading={isMutationLoading}
            disabled={selectedShifts.length === 0 || selectedShifts.length > maxSelectionLimit}
          >
            {i18.nextButton}
          </Button>
        </Col>
      </Row>
    </>
  );
};

export default Shifts;
