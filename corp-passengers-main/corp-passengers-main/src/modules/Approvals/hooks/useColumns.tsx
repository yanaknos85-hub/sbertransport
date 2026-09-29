import { LabeledValue } from 'antd/es/select';
import { RadioChangeEvent } from 'antd/lib/radio';
import { useTranslation } from 'i18n';
import React, {
  Dispatch, SetStateAction, useCallback, useMemo
} from 'react';
import { TableEditButtons } from 'shared/components/TableEditButtons.tsx/TableEditButtons';
import RadioGroupField from 'shared/models/ModelDetail/FieldTypes/RadioGroupField';
import SelectField from 'shared/models/ModelDetail/FieldTypes/SelectField';
import { ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { UUID } from 'utils/io-ts';
import uniqId from 'utils/uid';

import { ValidationRules } from 'shared/fieldValidationRules';
import { preventDefault } from 'utils';
import { MinimalSum, SpecificTerritory } from '../components/Fields';
import { ApprovalsRecord, Cell, CompositeRecord } from '../types/types';

export enum ApprovalsNamesSubstring {
  tripPurpose = 'tripPurpose_',
  territory = 'territory_',
  specificTerritory = 'specificTerritory_',
  minimalSum = 'minimalSum_',
}

export const useColumns = (
  purposes: LabeledValue[],
  geoZones: LabeledValue[],
  state: ApprovalsRecord[],
  setState: Dispatch<SetStateAction<ApprovalsRecord[]>>,
  deleteRow: (id: UUID) => void
): Cell<CompositeRecord>[] => {
  const { t } = useTranslation();

  const addSpecificTerritory = useCallback(
    (rowId: UUID) => {
      const updatedState = state.map(i => {
        const id = uniqId() as UUID;

        return i.rowId !== rowId
          ? i
          : {
            ...i,
            specificTerritory: [...i.specificTerritory, {
              id, territory: '', rowId: i.rowId,
            }],
            specificMinimalSum: [...i.specificMinimalSum, {
              id, minimalSum: 0, rowId: i.rowId,
            }],
          };
      });

      return updatedState && setState(updatedState);
    },
    [state, setState]
  );

  const deleteSpecificTerritory = useCallback(
    (rowId: UUID, cellId: UUID) => {
      const foundRow = state.find(i => i.id === rowId) as ApprovalsRecord;

      const updatedState = state.map(i => i.id !== rowId
        ? i
        : {
          id: i.id,
          transportType: i.transportType,
          rowId: i.rowId,
          territory: i.territory,
          tripPurpose: i.tripPurpose,
          // eslint-disable-next-line no-unsafe-optional-chaining
          specificTerritory: [...foundRow?.specificTerritory.filter(s => s.id !== cellId)],
          // eslint-disable-next-line no-unsafe-optional-chaining
          specificMinimalSum: [...foundRow?.specificMinimalSum.filter(s => s.id !== cellId)],
        }
      );

      return updatedState && setState(updatedState);
    },
    [state, setState]
  );

  const selectTerritoryType = (territoryType: 'specific' | 'any', record: ApprovalsRecord, rowId: UUID) => {
    let newSpecific = [];
    if (territoryType === 'any') {
      // @ts-ignore
      newSpecific = record.specificTerritory.map(item => (item.rowId === rowId ? { id: '', rowId } : item));
    } else {
      // @ts-ignore
      newSpecific = record.specificTerritory;
    }
    return newSpecific;
  };

  const selectHandle = useCallback(
    (event: RadioChangeEvent, record: ApprovalsRecord['territory']) => {
      setState(
        state.map(row => row.rowId === record?.rowId
          ? {
            ...row,
            id: row.id,
            territory: { ...row.territory, territory: event.target.value },
            specificMinimalSum: row.specificMinimalSum,
            specificTerritory: selectTerritoryType(event.target.value, row, record.rowId),
            tripPurpose: row.tripPurpose,
            transportType: row.transportType,
          }
          : row
        )
      );
    },
    [state, setState]
  );

  return useMemo(
    (): Cell<CompositeRecord>[] => [
      {
        title: 'Цель поездки',
        dataIndex: 'tripPurpose',
        key: 'tripPurpose',
        render: (record: ApprovalsRecord['tripPurpose']) => (
          <SelectField
            useParentContainer={false}
            onInputKeyDown={preventDefault}
            editable
            name={`${ApprovalsNamesSubstring.tripPurpose}${record?.rowId}`}
            fieldType={ModelFormFieldType.SELECT}
            options={purposes}
            rules={[ValidationRules.general.required]}
            initialValue={record?.purposeId as string}
            placeholder="Выберите цель поездки" // TODO перенести в i18n
          />
        ),
      },
      {
        title: 'Территория действия',
        dataIndex: 'territory',
        key: 'territory',
        width: 400,
        render: (record: ApprovalsRecord['territory']) => (
          <RadioGroupField
            editable
            className="radioGroupTerritory"
            name={`${ApprovalsNamesSubstring.territory}${record?.rowId}`}
            fieldType={ModelFormFieldType.RADIO_GROUP}
            options={[
              { value: 'any', label: 'Без ограничения' },
              { value: 'specific', label: 'Требует согласования' },
            ]}
            onChange={value => selectHandle(value, record)}
            initialValue={record.territory || 'any'}
          />
        ),
      },
      {
        title: '',
        dataIndex: 'specificTerritory',
        key: 'specificTerritory',
        width: 250,
        render: (record: ApprovalsRecord['specificTerritory']) => (
          <SpecificTerritory
            state={state}
            rowId={record[0]?.rowId}
            useParentContainer={false}
            addSpecificTerritory={addSpecificTerritory}
            deleteSpecificTerritory={deleteSpecificTerritory}
            geoZones={geoZones}
            rules={[ValidationRules.general.required]}
            initialValue={record[0]?.territory}
          />
        ),
      },
      {
        title: 'Согласование от (₽)',
        dataIndex: 'specificMinimalSum',
        key: 'specificMinimalSum',
        width: 160,
        render: (record: ApprovalsRecord['specificMinimalSum']) => (
          <MinimalSum
            state={state ?? []}
            rowId={record[0]?.rowId}
            rules={[ValidationRules.general.onlyDigits]}
            initialValue={record[0]?.minimalSum ? record[0]?.minimalSum : undefined}
          />
        ),
      },
      {
        width: 60,
        render: (record: CompositeRecord) => (
          <TableEditButtons
            isEdit={false}
            id={record?.rowId}
            onDelete={() => deleteRow(record?.rowId)}
            title={t.global.deleteConfirm}
            cancelText="Отмена"
            okText="Удалить"
            deleteIconColor="#262626"
          />
        ),
      },
    ],
    [t, state, geoZones, purposes, addSpecificTerritory, deleteRow, deleteSpecificTerritory, selectHandle]
  );
};
