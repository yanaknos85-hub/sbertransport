import { FormInstance } from 'antd/lib/form';
import moment from 'moment';
import { useEffect, useMemo } from 'react';

import { FILL_FORM, Ownership } from '../../../constants/vehicles.constants';

import { FieldsEnum } from '../constants/vehiclesForm.fields';

const useFill = (form: FormInstance) => {
  const initialValues = useMemo(
    () => ({
      [FieldsEnum.ownership]: Ownership.USER,

      [FieldsEnum.vcRegistrationNumber]: 'О368СК29',
      [FieldsEnum.vcColor]: 'красная',
      [FieldsEnum.vcPassengerSeatsCount]: 3,

      [FieldsEnum.docs_mc_series]: '12345',
      [FieldsEnum.docs_mc_number]: '12345',
      [FieldsEnum.docs_mc_dateOfIssue]: moment(new Date()),

      [FieldsEnum.docs_dl_fio]: 'Иванов Инван Иванович',
      [FieldsEnum.docs_dl_dateOfIssue]: moment(new Date()),
      [FieldsEnum.docs_dl_validUntil]: moment(new Date()),
      [FieldsEnum.docs_dl_series]: 'ABC',
      [FieldsEnum.docs_dl_number]: '12345',
      [FieldsEnum.docs_dl_issuedBy]: 'Отедом ГАИ по Москве',
      [FieldsEnum.docs_dl_whereIssued]: 'Россия. Москва',
      [FieldsEnum.docs_dl_category]: 'B',
      [FieldsEnum.docs_dl_file]: [
        {
          name: 'file.png',
          status: 'done',
        },
      ],

      [FieldsEnum.docs_pts_vin]: '12345',
      [FieldsEnum.docs_pts_brandName]: 'TOYOTA',
      [FieldsEnum.docs_pts_model]: 'CAMRY',
      [FieldsEnum.docs_pts_engineVolume]: 12345,
      [FieldsEnum.docs_pts_enginePower]: '12345',
      [FieldsEnum.docs_pts_file]: [
        {
          name: 'file.png',
          status: 'done',
        },
      ],

      [FieldsEnum.docs_osago_series]: '12345',
      [FieldsEnum.docs_osago_number]: '12345',
      [FieldsEnum.docs_osago_startTime]: moment(new Date()),
      [FieldsEnum.docs_osago_endTime]: moment(new Date()),
      [FieldsEnum.docs_osago_file]: [
        {
          name: 'file.png',
          status: 'done',
        },
      ],

      // [FieldsEnum.docs_pdn_file]: [
      //   {
      //     name: 'file.png',
      //     status: 'done',
      //   },
      // ],

      [FieldsEnum.confirmaDataAccuracy]: true,
      [FieldsEnum.agreementPersonalData]: true,
    }),
    []
  );

  useEffect(() => {
    if (FILL_FORM) {
      const newValues = {
        ...initialValues,
      };

      // eslint-disable-next-line no-console
      console.log('Warn! Used fill form with values: ', newValues);

      form.setFieldsValue(newValues);
    }
  }, [form, initialValues]);

  return {
    initialValues: FILL_FORM ? initialValues : {},
  };
};

export default useFill;
