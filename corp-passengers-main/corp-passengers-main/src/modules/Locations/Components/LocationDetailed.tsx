import React, { useCallback, useState } from 'react';
import { observer } from 'mobx-react';
import { useCreateLocation, useUpdateLocation, useLocations } from 'api/locations';
import { formatAddress } from 'utils/formatAddress';
import { Location } from 'stores/Locations/Locations.interface';
import { LocationDetailedForm } from './LocationDetailedForm';

export const LocationDetailed: React.FC<{ locationId: string; handleClose: () => void }> = observer(
  ({ locationId, handleClose }): JSX.Element => {
    const [createLocation, { isLoading: isCreating }] = useCreateLocation();
    const [updateLocation, { isLoading: isUpdating }] = useUpdateLocation();
    const inProgress = isCreating || isUpdating;

    const [id, setId] = useState(locationId);

    const { byId: locations } = useLocations().data;

    const defaultLocation = {
      label: '',
      fullAddress: '',
      address: {},
    };
    const location: Location = locations[id] || defaultLocation;

    const initialValues = {
      label: location.label,
      fullAddress: formatAddress(location.address),
      address: { ...location.address },
    };

    const handleSave = useCallback(
      async values => {
        // Значения полей для нового местоположения
        const newLocationDetails = {
          address: { ...values.address, ...{ label: values.label } },
          label: values.label,
        };
        // Значения полей для обновления местоположения
        const updLocationDetails = { id, ...newLocationDetails };

        const isCreate = locationId === 'adding';
        if (isCreate) {
          // Получим id созданного местоположения
          createLocation({ location: newLocationDetails }).then(loc => {
            // Устанавливаем корректный id
            if (loc && loc.id) {
              setId(loc.id);
              handleClose();
            }
          });
        } else {
          updateLocation({ location: updLocationDetails }).then(() => {
            handleClose();
          });
        }
      },
      // eslint-disable-next-line react-hooks/exhaustive-deps
      [id, setId, createLocation, updateLocation]
    );

    return (
      <LocationDetailedForm
        initialValues={initialValues}
        handleSave={handleSave}
        inProgress={inProgress}
        handleClose={handleClose}
      />
    );
  }
);

export default LocationDetailed;
