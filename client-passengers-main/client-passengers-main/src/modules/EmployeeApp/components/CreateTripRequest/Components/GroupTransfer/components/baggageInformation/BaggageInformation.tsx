import '../../override.scss';
import { Form, Input, Switch } from 'antd';
import { observer } from 'mobx-react';
import React, { FC, useEffect, useState } from 'react';

import baggageInformationStyles from './baggageInformation.module.scss';
import { amountLuggage, fieldTitles, nonDimensionaLuggage } from '../../constants';
import { ValidationRules } from 'shared/fieldValidationRules';
import Group from 'shared/components/Images/Group.svg';
import { Information } from 'stores/Trip/Trip.interface';

export const BaggageInformation: FC<{ information: Information }> = observer(
  ({ information }): JSX.Element => {
    const [isContentVisible, setIsContentVisible] = useState(false);

    useEffect(() => {
      if (information) {
        information.bugsOversized && setIsContentVisible(information.bugsOversized);
      }
    }, []);

    return (
      <div className="wrapper_groupTransfer">
        <span className="groupTransfer_title_item">{amountLuggage}</span>
        <Form.Item
          key={fieldTitles.bugs}
          name={fieldTitles.bugs}
          initialValue={true}
          className={baggageInformationStyles.switch_amountLuggage}
        />
        <Form.Item
          key={fieldTitles.bugsComment}
          name={fieldTitles.bugsComment}
          rules={[ValidationRules.general.required]}
        >
          <Input className={baggageInformationStyles.baggage_item} placeholder="Укажите количество багажа" />
        </Form.Item>
        <div className={baggageInformationStyles.wrapper_switch}>
          <div className={baggageInformationStyles.wrapper_baggageInformation}>
            <img src={Group} alt="Group" />
            <span className={baggageInformationStyles.title}>{nonDimensionaLuggage}</span>
          </div>
          <Form.Item
            key={fieldTitles.bugsOversized}
            name={fieldTitles.bugsOversized}
          >
            <Switch checked={isContentVisible} onChange={() => setIsContentVisible(prev => !prev)} />
          </Form.Item>
        </div>
        {isContentVisible && (
          <Form.Item
            key={fieldTitles.bugsOversizedComment}
            name={fieldTitles.bugsOversizedComment}
          >
            <Input className={baggageInformationStyles.baggage_item} placeholder={nonDimensionaLuggage} />
          </Form.Item>
        )}
      </div>
    );
  }
);
