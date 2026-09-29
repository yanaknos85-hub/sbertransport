
import React, { FC, useMemo } from 'react';
import { useTranslation } from 'i18n';

import { useSelfAutopark } from 'api/contractors/contractors.api';
import { TripTypes, vehicleTypeTitles } from 'constants/app.constants';
import { ListDetailed, Props as ListDetailedProps } from 'components/ListDetailed/ListDetailed';
import { IconTypes } from 'components/ListDetailed/types';

import { useActiveVehicle } from '../../context/ActiveVehicle';
import { useEditDelete } from '../../context/EditDeleteContext';
import { getEcoClassDescription } from '../../utils';

export const VehiclesDetailed: FC = () => {
  const { t } = useTranslation();

  const { isInternal } = useSelfAutopark().data;

  const { activeVehicle } = useActiveVehicle();

  const { handleEditClick } = useEditDelete();

  const marks = [
    {
      label: `${t.Registry.Labels.type}`,
      value: activeVehicle?.vehicleType ? vehicleTypeTitles[activeVehicle.vehicleType] : '',
    },
    {
      label: `${t.Registry.Labels.carMark}`,
      value: activeVehicle?.model?.brand ?? '',
    },
    {
      label: `${t.Registry.Labels.carModel}`,
      value: activeVehicle?.model?.name ?? '',
    },
    {
      label: `${t.Registry.Labels.carYear}`,
      value: activeVehicle?.model?.year?.toString() ?? '',
    },
  ];

  const items: ListDetailedProps['items'] = [
    {
      label: t.Registry.Labels.carSeries,
      value: activeVehicle?.insuranceNumber?.replace(/(^[A-Z]+)/, '$1 ') ?? '',
    },
    {
      type: activeVehicle?.inExploitation ? 'active' : 'inactive',
    },
    {
      label: t.Registry.Labels.vin,
      value: activeVehicle?.vin,
    },
    {
      label: t.Registry.Labels.ecoClass,
      value: getEcoClassDescription(activeVehicle?.ecoClass),
    },
  ];

  const iconType = useMemo(() => {
    if (isInternal) return IconTypes.SpecialCar;

    if (activeVehicle?.vehicleType) {
      return activeVehicle.vehicleType === TripTypes.Cargo
        ? IconTypes.CargoTruck
        : IconTypes.PassengerCar;
    }

    return undefined;
  }, [activeVehicle?.vehicleType, isInternal]);

  return (
    <ListDetailed
      activeRow={activeVehicle as ListDetailedProps['activeRow']}
      iconType={iconType}
      marks={marks}
      items={items}
      emptyProps={{
        title: 'Автомобиль не выбран',
        description: 'Выберите автомобиль для получения подробной информации',
      }}
      onEdit={handleEditClick as ListDetailedProps['onEdit']}
    />
  );
};
