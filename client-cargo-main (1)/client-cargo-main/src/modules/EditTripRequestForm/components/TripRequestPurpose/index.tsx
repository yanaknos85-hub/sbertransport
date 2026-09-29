/* eslint-disable */
import {Form, Select} from 'antd';
import {FormInstance} from 'antd/es/form/Form';
import React from 'react';
import moment from 'moment'

import ChevronBig from 'shared/components/Images/view/menu 2.0/ChevronBig';

import {validateSelectedDateByPurpose} from '../../../CreateTripRequest/utils/utils';
import {TripPurposeList} from '../../hooks/useTripPurposeList';
import {TripRequestPurpose} from '../../hooks/useTripRequestPurpose';
import {checkPurposeValidity} from '../../utils/utils';

const { Option } = Select;

export default ({
  tripPurpose,
  form,
  disabled,
  tripPurposeList,
}: {
  tripPurpose: TripRequestPurpose;
  form: FormInstance;
  disabled?: boolean;
  tripPurposeList: TripPurposeList;
}): JSX.Element => {
  const { inProgress: purposesIsLoading, purposes } = tripPurposeList;
  const { setPurposeValidity, purposeValidity } = tripPurpose;
  // Todo:Исправить когда выйдем в пилот
  const purposesOptions = purposes
    .filter(l => l.label !== 'Поездка в личных целях')
    .map(el => ({ label: el.label, value: el.id, tripPurposeTimes: el.tripPurposeTimes }));

  const handleChange = (value: string): void => {
    setPurposeValidity(validateSelectedDateByPurpose(form, purposes, purposeValidity.purposeId, value));
  };

  const now = moment().format('HH:mm');

  const isTimingTrip = (item: any) => {
    return item.tripPurposeTimes.find(() => {
      const start = moment(item.tripPurposeTimes[0]?.startTime)
        .format('HH:mm')
      const end = moment(item.tripPurposeTimes[item.tripPurposeTimes?.length - 1]?.endTime)
        .format('HH:mm')
      return now < start || now > end
    })
  }

  const timingTripTitle = (item: any) => {
    const start = moment(item.tripPurposeTimes[0]?.startTime)
      .format('HH:mm')
    const end = moment(item.tripPurposeTimes[item.tripPurposeTimes?.length - 1]?.endTime)
      .format('HH:mm')
    return item.tripPurposeTimes?.length > 0
      ? `Данную цель поездки можно выбрать с ${start} до ${end}`
      : item.label
  }

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
              // FIXME @typescript-eslint/explicit-function-return-type
              return checkPurposeValidity(purposeValidity.isValid, true);
            },
          }),
        ]}
        id="purposes"
      >
        <Select
          suffixIcon={<ChevronBig />}
          getPopupContainer={trigger => trigger.parentNode}
          placeholder="Цель поездки"
          onChange={handleChange}
          loading={purposesIsLoading}
          disabled={disabled}
        >
          {purposesOptions?.length > 0
            ? purposesOptions?.map((item) => (
              <Option
                title={
                  form.getFieldValue('when') === 'now'
                    ? timingTripTitle(item)
                    : item.label
                }
                key={item.value}
                disabled={
                  form.getFieldValue('when') === 'now'
                    ? isTimingTrip(item)
                    : disabled
                }
                value={item.value}
              >
                {item.label}
              </Option>
            ))
            : <Option disabled value=''>Данных нет</Option>
          }
        </Select>
      </Form.Item>
    </div>
  );
};
