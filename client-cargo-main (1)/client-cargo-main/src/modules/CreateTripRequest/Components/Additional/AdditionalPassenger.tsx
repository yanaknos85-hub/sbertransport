import React from 'react';
import { AutoComplete, Form, Radio } from 'antd';
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { StoreNames } from 'stores/StoreNames.enum';

import { CreateRequestLinks, CreateRequestLinksTitles } from '../../constants/CreateRequest.constants';

export const AdditionalPassenger = observer(({ disabled }: { disabled: boolean }) => {
  const { [StoreNames.employeeStore]: employeeStore } = useAppStoreContext();

  const { Option: OptionAutoComplete } = AutoComplete;

  return (
    <>
      <Form.Item name="passenger">
        <Radio.Group disabled={disabled}>
          <Radio value="me">{CreateRequestLinksTitles[CreateRequestLinks.iWillGo]}</Radio>
          <Radio value="notme">{CreateRequestLinksTitles[CreateRequestLinks.coworkerWillGo]}</Radio>
        </Radio.Group>
      </Form.Item>
      <Form.Item
        noStyle={true}
        shouldUpdate={(prevValues, currentValues): boolean => prevValues.passenger !== currentValues.passenger}
      >
        {({ getFieldValue }): JSX.Element | null => getFieldValue('passenger') === 'notme' ? (
          <Form.Item name="employee">
            <AutoComplete
              placeholder={CreateRequestLinksTitles[CreateRequestLinks.coworkersName]}
              filterOption={true}
              backfill={true}
              allowClear={true}
              disabled={disabled}
              getPopupContainer={trigger => trigger.parentNode}
            >
              {employeeStore.employeeListByOrg.map(x => (
                <OptionAutoComplete
                  value={`${x.fullNameWithCode}`}
                  key={x.id}
                  getPopupContainer={(trigger: any) => trigger.parentNode}
                >
                  {x.fullNameWithCode}
                </OptionAutoComplete>
              ))}
            </AutoComplete>
          </Form.Item>
        ) : null}
      </Form.Item>
    </>
  );
});
