import React from 'react';
import { Form, Input, Select } from 'antd';

import { CreateRequestLinks, CreateRequestLinksTitles } from '../../constants/CreateRequest.constants';
import { TripPrefTitlesEnum } from '../../constants/preferences.enum';

export const AdditionalPreferences = ({ disabled }: { disabled: boolean }): JSX.Element => (
  <>
    <Form.Item name="preferences">
      <Select
        mode="multiple"
        placeholder={CreateRequestLinksTitles[CreateRequestLinks.choosePreferences]}
        options={Object.keys(TripPrefTitlesEnum).map(x => ({
          value: x,
          label: TripPrefTitlesEnum[x as keyof typeof TripPrefTitlesEnum],
        }))}
        disabled={disabled}
        getPopupContainer={trigger => trigger.parentNode}
      />
    </Form.Item>
    <Form.Item
      noStyle={true}
      shouldUpdate={(prevValues, currentValues): boolean => prevValues.preferences !== currentValues.preferences}
    >
      {({ getFieldValue }): JSX.Element | null => getFieldValue('preferences')?.includes('commentary') && (
      <Form.Item
        name="commentary"
        label={CreateRequestLinksTitles[CreateRequestLinks.enterCommentary]}
        rules={[{ required: true, message: 'Поле "Комментарий для водителя" обязательно для ввода' }]}
        required={true}
      >
        <Input.TextArea disabled={disabled} rows={4} />
      </Form.Item>
      )}
    </Form.Item>
  </>
);
