import React, {
  FC, Suspense, useEffect, useMemo, useState
} from 'react';
import { useTranslation } from 'i18n';
import { PlusOutlined } from '@ant-design/icons';
import { EditableColumnType, EditableTable, EditableTableStore } from 'shared/components/EditableTable';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import {
  useCargoPackageSettings,
  useCreateCargoPackageSettings,
  useDeleteCargoPackageSettings,
  useUpdateCargoPackageSettings
} from 'api/cargo-package-settings';
import { CargoPackageSettingsRecord } from 'stores/CargoPackage/CargoPackageSettings.interface';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import { compareBy } from 'utils/sorting';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import SelectContractor from 'shared/components/SelectContractor';
import { Contractor } from 'stores/Contractors/Contractors.interface';
import uniqId from 'utils/uid';
import { UUID } from 'utils/io-ts';
import cn from 'classnames';
import { Button } from 'shared/components/Button/Button';
import { TextField } from 'shared/components/TextField/TextField';
import { NumericInputCell } from './components/NumericInputCell';

import styles from './styles.module.scss';

interface SaveSettingsResponse {
  id: UUID;
}

export const CargoPackageSettings: FC<{ className?: string }> = ({ className }) => (
  <ErrorBoundary>
    <Suspense fallback={<SpinWrapped />}>
      <CargoPackageSettingsInner className={className} />
    </Suspense>
  </ErrorBoundary>
);

const CargoPackageSettingsInner: FC<{ className?: string }> = ({ className }) => {
  const { t } = useTranslation();
  const { logger } = useAppStoreContext();
  const [contractor, setContractor] = useState<Contractor['name']>('');
  const [buttonDisabled, setButtonDisabled] = React.useState(true);

  const [tempIdMap, setTempIdMap] = useState<Record<string, UUID>>({});

  const { data: cargoPackageSettings = [], isLoading: isLoadingSettings } = useCargoPackageSettings(contractor);
  const [saveSettings, { isLoading: isSavingSettings }] = useCreateCargoPackageSettings(contractor);
  const [deleteSettings, { isLoading: isDeletingSettings }] = useDeleteCargoPackageSettings(contractor);
  const [updateSettings, { isLoading: isUpdatingSettings }] = useUpdateCargoPackageSettings(contractor);

  /**
   * Определяем функцию, по которой будет определяться id строки данных в таблице
   * @param record строка данных
   */
  const rowKeyFn = (record: CargoPackageSettingsRecord) => record.id;

  /**
   * Определяем тип данных и функцию для определения id для таблицы
   */
  const store = useMemo(
    () => new EditableTableStore<CargoPackageSettingsRecord>({
      rowKeyFn,
    }),
    []
  );

  /**
   * Заполнение данных таблицы данными с api
   */
  useEffect(() => {
    store.data = cargoPackageSettings.map(value => ({
      id: value.id,
      label: value.label,
      cost: value.cost,
      unit: value.unit,
      isNew: false,
    }));
  }, [store, cargoPackageSettings, contractor]);

  useEffect(() => {
    store.handleSave = async (record: CargoPackageSettingsRecord) => {
      const { id, label, cost, unit, isNew } = record;
      
      if (isNew) {
        const result = await saveSettings({
          settings: {
            label, cost, unit,
          },
        });
  
        if (result?.data) {
          const responseData = result.data as SaveSettingsResponse;
          const newId = responseData.id;
          setTempIdMap(prev => ({
            ...prev,
            [id]: newId
          }));
  
          const recordIndex = store.data.findIndex(item => item.id === id);
          if (recordIndex !== -1) {
            const updatedRecord: CargoPackageSettingsRecord = {
              id: newId,
              label: record.label,
              cost: record.cost,
              unit: record.unit,
              isNew: false
            };
            const newData = [...store.data];
            newData[recordIndex] = updatedRecord;
            store.data = newData;
          }
  
          return true;
        }
        return false;
      } else {
        const actualId = tempIdMap[id] || id;
        const result = await updateSettings({
          settings: {
            label, cost, unit,
          },
          packageId: actualId as UUID,
        });
        return !!result;
      }
    };
  }, [store, saveSettings, updateSettings, tempIdMap]);
  
  /**
   * Удаляем строку при нажатии на корзинку
   */
  useEffect(() => {
    store.handleDelete = async (record: CargoPackageSettingsRecord) => {
      const currentRecord = store.data.find(item => 
        item.id === record.id || 
        tempIdMap[record.id] === item.id
      );
      
      if (!currentRecord) {
        return false;
      }
  
      const { id, isNew } = currentRecord;
  
      if (isNew) {
        store.data = store.data.filter(item => item.id !== id);
        setTempIdMap(prev => {
          const newMap = { ...prev };
          delete newMap[record.id];
          return newMap;
        });
        return true;
      } else {
        const result = await deleteSettings({
          packageId: id as UUID
        });
        return !!result;
      }
    };
  }, [store, deleteSettings, tempIdMap]);
  
  const addRow = () => {
    if (contractor === '') {
      logger.toMessage('error', 'Сначала выберите контрагента');
    } else {
      const item: CargoPackageSettingsRecord = {
        id: uniqId() as UUID,
        label: '',
        cost: 0,
        unit: '',
        isNew: true,
      };
      store.data.unshift(item);
      store.startEditing(item.id);
    }
  };

  /**
   * После выбора контрагента подтягиваем данные в таблицу, и делаем активной кнопку Добавить упаковку
   */
  const handleChange = (value: Contractor['name']) => {
    setContractor(value);
    setButtonDisabled(false);
    setTempIdMap({});
  };

  /**
   * Определяем колонки, которые будут отображены в таблице
   */
  const useColumns = (): EditableColumnType<CargoPackageSettingsRecord>[] => useMemo(
    (): EditableColumnType<CargoPackageSettingsRecord>[] => [
      {
        title: t.CargoPackageSettings.Columns.label,
        dataIndex: 'label',
        key: 'label',
        width: 300,
        className: styles.editCell,
        sorter: compareBy('label'),
        render: props => <TextField {...props} />,
      },
      {
        title: t.CargoPackageSettings.Columns.cost,
        dataIndex: 'cost',
        key: 'cost',
        width: 120,
        className: styles.editCell,
        render: props => <NumericInputCell {...props} />,
      },
      {
        title: t.CargoPackageSettings.Columns.unit,
        dataIndex: 'unit',
        key: 'unit',
        width: 120,
        className: styles.editCell,
        render: props => <TextField {...props} />,
      },
    ],
    []
  );

  const columns = useColumns();

  return (
    <div className={cn(styles.cargoPackageSettings, className)}>
      <div className={styles.contractorLabel}>{t.contractors.contractor}</div>
      <SelectContractor
        value={contractor}
        onChange={handleChange}
        className={styles.contractorSelect}
      />
      <EditableTable
        className={styles.cargoPackageTable}
        store={store}
        columns={columns}
        pagination={{ position: ['bottomCenter'] }}
        footer={() => (
          <Footer
            addRow={addRow}
            disabled={buttonDisabled}
            buttonCaption={t.CargoPackageSettings.createAttribute}
          />
        )}
      />

      {(isLoadingSettings || isSavingSettings || isDeletingSettings || isUpdatingSettings) && <SpinWrapped mask />}
    </div>
  );
};

const Footer: FC<{ addRow: () => void; disabled: boolean; buttonCaption: string }> = ({
  addRow,
  disabled,
  buttonCaption,
}) => (
  <div className={styles.footerButtonContainer}>
    <Button
      icon={<PlusOutlined />}
      type="primary"
      size="middle"
      className={styles.createButton}
      onClick={addRow}
      disabled={disabled}
    >
      {buttonCaption}
    </Button>
  </div>
);

export default CargoPackageSettings;
