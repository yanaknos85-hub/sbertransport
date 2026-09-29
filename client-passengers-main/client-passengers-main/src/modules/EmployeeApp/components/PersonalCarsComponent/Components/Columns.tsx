import { PersonalCar, carTypeDescriptions } from '@sber-sbertransport/mf-core';
import { ColumnsType } from 'antd/lib/table';
import React from 'react';

import { TableEditButtons } from 'shared/components/TableEditButtons.tsx/TableEditButtons';

import {
  PersonalCars,
  PersonalCarsCyrillicShort,
  PersonalCarsTexts,
  PersonalCarsTextsCyrillic,
  PersonalOwnerInformation,
  PersonalOwnerInformationCyrillic
} from '../PersonalCars.constants';

export const columns = (path: string, onDelete: (id: string) => void): ColumnsType<PersonalCar> => [
  {
    title: PersonalCarsCyrillicShort[PersonalCars.brandName],
    dataIndex: PersonalCars.brandName,
    key: PersonalCars.brandName,
  },
  {
    title: PersonalCarsCyrillicShort[PersonalCars.transportType],
    dataIndex: PersonalCars.transportType,
    key: PersonalCars.transportType,
    render: (_: unknown, record: PersonalCar): string => carTypeDescriptions[record.transportType as keyof typeof carTypeDescriptions],
  },
  {
    title: PersonalCarsCyrillicShort[PersonalCars.model],
    dataIndex: PersonalCars.model,
    key: PersonalCars.model,
  },
  {
    title: PersonalCarsCyrillicShort[PersonalCars.color],
    dataIndex: PersonalCars.color,
    key: PersonalCars.color,
  },
  {
    title: PersonalCarsCyrillicShort[PersonalCars.engineVolume],
    dataIndex: PersonalCars.engineVolume,
    key: PersonalCars.engineVolume,
  },
  {
    title: PersonalCarsCyrillicShort[PersonalCars.insuranceNumber],
    dataIndex: PersonalCars.insuranceNumber,
    key: PersonalCars.insuranceNumber,
  },
  {
    title: PersonalCarsCyrillicShort[PersonalCars.registrationCertificate],
    dataIndex: PersonalCars.registrationCertificate,
    key: PersonalCars.registrationCertificate,
  },
  {
    title: PersonalCarsCyrillicShort[PersonalCars.registrationNumber],
    dataIndex: PersonalCars.registrationNumber,
    key: PersonalCars.registrationNumber,
  },
  {
    title: PersonalCarsCyrillicShort[PersonalCars.passengerSeatsCount],
    dataIndex: PersonalCars.passengerSeatsCount,
    key: PersonalCars.passengerSeatsCount,
  },
  {
    title: PersonalCarsCyrillicShort[PersonalCars.ownerInfo],
    dataIndex: PersonalCars.ownerInfo,
    key: PersonalCars.ownerInfo,
    render: (_: unknown, record: PersonalCar): string => PersonalOwnerInformationCyrillic[record.ownerInfo as PersonalOwnerInformation],
  },
  {
    width: 60,
    render: (_: unknown, record: PersonalCar): JSX.Element => (
      <TableEditButtons
        path={path}
        id={record.id}
        onDelete={onDelete}
        title={PersonalCarsTextsCyrillic[PersonalCarsTexts.deleteConfirm]}
        okText={PersonalCarsTextsCyrillic[PersonalCarsTexts.remove]}
        cancelText={PersonalCarsTextsCyrillic[PersonalCarsTexts.cancel]}
      />
    ),
  },
];
