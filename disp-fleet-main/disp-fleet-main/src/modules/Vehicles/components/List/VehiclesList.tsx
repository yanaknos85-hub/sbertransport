import React, { FC, useState } from 'react';
import { useTranslation } from 'i18n';

import { useProfile } from 'api/profile/profile.api';
import { useSearchAllVehicles } from 'api/vehicles/vehicles.api';
import { Vehicle } from 'api/vehicles/vehicles.types';
import { DISPATCHER_ROOM, VEHICLES_LOAD_FILE } from 'api/vehicles/vehicles.constants';

import { TripTypes, vehicleTypeTitles } from 'constants/app.constants';
import { List, Props as ListProps } from 'components/List/List';
import { RowIconTypes } from 'components/List/types';
import { Props as RowProps } from 'components/List/components/Row/Row';
import { defaultPagination } from 'hooks/useQuery';

import { vehiclesFormJSX } from './formJSX';
import { useModalForm } from '../../context/ModalForm';
import { useActiveVehicle } from '../../context/ActiveVehicle';
import { useEditDelete } from '../../context/EditDeleteContext';
import { processRequestParams } from '../../utils';

export const VehiclesList: FC = () => {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [query, setQuery] = useState<Record<string, any>>(defaultPagination);
  const { t } = useTranslation();
  const { handleOpenAdd, handleOpenUpload } = useModalForm();

  const { contractorId, autoparkId } = useProfile().data;

  // eslint-disable-next-line no-unsafe-optional-chaining
  const { content: vehicles, totalElements } = useSearchAllVehicles({
    contractorId,
    data: {
      ...processRequestParams(query),
      autopark: query.autoparkId ?? autoparkId,
    },
  })?.data?.response || {};

  const { activeVehicle, toggleActiveVehicle } = useActiveVehicle();

  const { handleEditClick } = useEditDelete();

  const rows: ListProps['rows']
    = vehicles?.map(vehicle => ({
      rowData: {
        ...vehicle,
        modelInfo: [vehicle.model.brand, vehicle.model.name].filter(Boolean).join(' '),
      },
      activeRow: { ...activeVehicle, name: '' } as Vehicle,
      statuses: [
        { type: vehicle.inExploitation ? 'active' : 'inactive' },
        { title: t.Vehicles.vinAbbr, description: vehicle.vin },
        { description: vehicleTypeTitles[vehicle.vehicleType] },
      ] as RowProps['statuses'],
      iconType: vehicle.vehicleType === TripTypes.Cargo ? RowIconTypes.CargoTruck : RowIconTypes.PassengerCar,
      toggleActive: toggleActiveVehicle as RowProps['toggleActive'],
      onEdit: handleEditClick as RowProps['onEdit'],
    })) ?? [];

  return (
    <List
      autoSize
      onChangeQuery={setQuery}
      onAddClick={handleOpenAdd}
      onExportClick={handleOpenUpload}
      formJSX={vehiclesFormJSX(t)}
      rows={rows}
      totalElements={totalElements as number}
      searchPanelProps={{
        searchValue: query.stateNumber,
        searchField: 'stateNumber',
        placeholder: 'A123AA 123 RUS',
      }}
      downloadButtonProps={{
        directoryUrl: DISPATCHER_ROOM,
        url: VEHICLES_LOAD_FILE,
        query: { ...query, autoparkId: query.autoparkId ?? autoparkId },
      }}
    />
  );
};
