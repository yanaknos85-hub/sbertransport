import { Form, Select } from 'antd';
import React, { FC } from 'react';

import { SpinWrapped } from 'shared/components';

import { CreateRequestLinks, CreateRequestLinksTitles } from '../constants/CreateRequest.constants';
import { PurposesProps } from '../types/types';
import { validateSelectedDateByPurpose } from '../utils/utils';

export const Purposes: FC<PurposesProps> = ({
  isLoadingPurposes,
  setCurrentDateAndPurpose,
  currentDateAndPurpose,
  purposes,
  form,
}) => {
  const purposesOptions = purposes.map(el => ({ label: el.label, value: el.id }));
  return (
    <Form.Item
      name="purpose"
      rules={[{ required: true, message: CreateRequestLinksTitles[CreateRequestLinks.purposeMessage] }]}
    >
      {isLoadingPurposes ? (
        <SpinWrapped />
      ) : (
        <Select
          getPopupContainer={trigger => trigger.parentNode}
          options={purposesOptions}
          placeholder={CreateRequestLinksTitles[CreateRequestLinks.purposePlaceholder]}
          onChange={(e): void => setCurrentDateAndPurpose(
            validateSelectedDateByPurpose(form, purposes, currentDateAndPurpose.purposeId, e as string)
          )}
        />
      )}
    </Form.Item>
  );
};
