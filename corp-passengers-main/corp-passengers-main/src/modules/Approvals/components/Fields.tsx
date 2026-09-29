import Form from 'antd/lib/form';
import React, { FC } from 'react';
import NumericField from 'shared/models/ModelDetail/FieldTypes/NumericField';
import SelectField from 'shared/models/ModelDetail/FieldTypes/SelectField';
import { ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import uniqId from 'utils/uid';

import { preventDefault } from 'utils';
import { ApprovalsNamesSubstring } from '../hooks/useColumns';
import styles from '../Approvals.module.scss';
import { ApprovalFieldBase, ApprovalsRecord, SpecificTerritoryProps } from '../types/types';

const SpecificTerritory: FC<SpecificTerritoryProps> = ({
  state,
  rowId,
  addSpecificTerritory,
  deleteSpecificTerritory,
  geoZones,
  rules,
  initialValue,
  ...rest
}) => {
  const row = state.find(i => i.rowId === rowId) as ApprovalsRecord;
  const isSpecific = row?.territory?.territory === 'specific';

  return (
    <div style={{ display: 'flex', flexDirection: 'column' }}>
      <Form.List name={`${ApprovalsNamesSubstring.specificTerritory}${rowId}`}>
        {() => (
          <div>
            {isSpecific
            && row?.specificTerritory?.map(item => (
              <div className={styles.specific_territory} key={uniqId()}>
                <SelectField
                  onInputKeyDown={preventDefault}
                  fieldType={ModelFormFieldType.SELECT}
                  name={item.id}
                  options={geoZones}
                  editable
                  rules={rules}
                  initialValue={initialValue}
                  placeholder="Выберите регион"
                  {...rest}
                />

                {/* Добавить во второй версии компонента */}
                {/* <Popconfirm
                    placement="left"
                    title="Отменить внесённые изменения?"
                    onConfirm={() => deleteSpecificTerritory(rowId, item.id)}
                    okText="Ok"
                    cancelText="Не отменять"
                    style={{ width: 300 }}
                  >
                    <CloseOutlined className={styles.cancelEditing} key="cancel" />
                  </Popconfirm> */}
              </div>
            ))}
          </div>
        )}
      </Form.List>

      {/* вернем в следующей версии продукта */}

      {/* {isSpecific && (
        <Button size="middle" onClick={() => addSpecificTerritory(rowId)}>
          Добавить
        </Button>
      )} */}
    </div>
  );
};

const MinimalSum: FC<ApprovalFieldBase> = ({
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  state, rowId, rules, initialValue, ...rest
}) => {
  const row = state.find(i => i.rowId === rowId) as ApprovalsRecord;

  return (
    <div className={styles.minimal_sum}>
      <Form.List name={`${ApprovalsNamesSubstring.minimalSum}${rowId}`}>
        {() => (
          <div>
            {row?.specificMinimalSum?.map(item => (
              <div className="inner_cell" key={uniqId()}>
                <NumericField
                  onPressEnter={preventDefault}
                  fieldType={ModelFormFieldType.NUMBER}
                  name={item.id}
                  editable
                  rules={rules}
                  initialValue={initialValue}
                />
              </div>
            ))}
          </div>
        )}
      </Form.List>
    </div>
  );
};

export { SpecificTerritory, MinimalSum };
