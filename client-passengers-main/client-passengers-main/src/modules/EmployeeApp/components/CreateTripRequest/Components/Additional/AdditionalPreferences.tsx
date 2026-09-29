import { Form, Select } from 'antd';
import React from 'react';

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
  </>
);
