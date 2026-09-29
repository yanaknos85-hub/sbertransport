import { Form, Select } from 'antd';
import { FormInstance } from 'antd/es/form/Form';
import React, { useMemo } from 'react';
import moment from 'moment';

import ChevronSmall from 'shared/components/Images/view/menu 2.0/ChevronSmall';
import { TripPurpose } from 'stores/Trip/Trip.interface';
import { purposeIcons } from 'shared/images/purposes';

import { validateSelectedDateByPurpose } from '../../../CreateTripRequest/utils/utils';
import { TripPurposeList } from '../../hooks/useTripPurposeList';
import { TripRequestPurpose } from '../../hooks/useTripRequestPurpose';
import { checkPurposeValidity, validatePurposeTime } from '../../utils/utils';

const { Option } = Select;

export default function Component({
  tripPurpose,
  form,
  disabled,
  tripPurposeList,
  defaultValue,
}: {
  tripPurpose: TripRequestPurpose;
  form: FormInstance;
  disabled?: boolean;
  tripPurposeList: TripPurposeList;
  defaultValue?: TripPurpose;
}): JSX.Element {
  const { purposes } = tripPurposeList;
  const { setPurposeValidity, purposeValidity } = tripPurpose;

  const purposesOptions = useMemo(() => purposes.filter(l => l.label !== 'Поездка в личных целях'), [purposes]);

  const handleChange = (value: string): void => {
    setPurposeValidity(validateSelectedDateByPurpose(form, purposes, purposeValidity.purposeId, value));
  };

  const isTimingTripDisabled = (purpose: TripPurpose) => !validatePurposeTime(purpose, moment());

  const timingTripTitle = (purpose: TripPurpose) => {
    const availablePeriods: string[] = [];
    (purpose.tripPurposeTimes || []).forEach(item => {
      const start = moment(item.startTime).format('HH:mm');
      const end = moment(item.endTime).format('HH:mm');
      availablePeriods.push(`с ${start} до ${end}`);
    });
    return availablePeriods.length
      ? `Данную цель поездки можно выбрать: ${availablePeriods.join('; ')}`
      : purpose.label;
  };

  return (
    <div className="purposes">
      <Form.Item
        name="purpose"
        // label={CreateRequestLinksTitles[CreateRequestLinks.purposePlaceholder]}
        rules={[
          { required: true, message: 'Пожалуйста, выберите цель поездки' },
          // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
          () => ({
            // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
            validator() {
              return checkPurposeValidity(purposeValidity.isValid, true);
            },
          }),
        ]}
        id="purposes"
      >
        <Select
          suffixIcon={(
            <div style={{ borderLeft: '1px solod grey', padding: '5px 18px' }}>
              <ChevronSmall />
            </div>
          )}
          getPopupContainer={trigger => trigger.parentNode}
          placeholder="Цель поездки"
          onChange={handleChange}
          disabled={disabled}
          defaultValue={defaultValue?.id}
        >
          {purposesOptions?.length > 0 ? (
            purposesOptions?.map(item => {
              const Icon = item.icon ? purposeIcons[item.icon] : purposeIcons.default;
              return (
                <Option
                  key={item.id}
                  title={form.getFieldValue('when') === 'now' ? timingTripTitle(item) : item.label}
                  disabled={form.getFieldValue('when') === 'now' ? isTimingTripDisabled(item) : disabled}
                  value={item.id}
                >
                  {item.icon && (
                    <span>
                      <Icon />
                    </span>
                  )}
                  {item.label}
                </Option>
              );
            })
          ) : (
            <Option disabled value="">
              Данных нет
            </Option>
          )}
        </Select>
      </Form.Item>
    </div>
  );
}
