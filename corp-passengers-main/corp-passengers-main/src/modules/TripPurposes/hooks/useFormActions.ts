import { useHistory } from 'react-router-dom';
import { FormInstance } from 'antd/lib/form/Form';
import { useCallback } from 'react';
import { useCreateTripPurpose, useDeleteTripPurpose, useUpdateTripPurpose } from 'api/purposes';
import { UUID } from 'utils/io-ts';
import { SubmitTripPurposeData, TripPurposeTimes } from '../types/types';
import { processFieldsForSubmitting } from '../utils/utils';

interface Export {
  busy: boolean;
  goBack: () => void;
  handleDelete: () => void;
  onFinish: () => Promise<void>;
}

export const useFormActions = ({
  form,
  organizationId,
  purposeId,
}: {
  form: FormInstance;
  organizationId: UUID;
  purposeId?: UUID;
}): Export => {
  const [createTripPurpose, { isLoading: isCreating }] = useCreateTripPurpose();
  const [updateTripPurpose, { isLoading: isUpdating }] = useUpdateTripPurpose();
  const [deleteTripPurpose, { isLoading: isDeleting }] = useDeleteTripPurpose();

  const { validateFields } = form;

  const history = useHistory();
  const goBack = useCallback((): void => history.push('./'), [history]);

  const handleSave = (data: SubmitTripPurposeData) => {
    const purposes = {
      ...data,
      tripPurposeTimes: data.tripPurposeTimes?.map((item: TripPurposeTimes) => ({
        startTime: item.startTime.format('YYYY-MM-DDTHH:mm:ss'),
        endTime: item.endTime.format('YYYY-MM-DDTHH:mm:ss'),
      })),
    };
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    createTripPurpose({ purpose: purposes as any, orgId: organizationId }).then(() => goBack());
  };

  const handleUpdate = (data: SubmitTripPurposeData) => {
    const purposes = {
      ...data,
      tripPurposeTimes: data.tripPurposeTimes?.map((item: TripPurposeTimes) => ({
        startTime: item.startTime.format('YYYY-MM-DDTHH:mm:ss'),
        endTime: item.endTime.format('YYYY-MM-DDTHH:mm:ss'),
      })),
    };
    updateTripPurpose({
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      purpose: purposes as any, orgId: organizationId, purId: purposeId!,
    }).then(() => goBack());
  };

  const handleDelete = useCallback(() => {
    deleteTripPurpose({ orgId: organizationId, purId: purposeId! }).then(() => goBack());
  }, [organizationId, purposeId, deleteTripPurpose, goBack]);

  const handleSubmit = () => {
    const data = processFieldsForSubmitting(form.getFieldsValue(), purposeId);
    purposeId ? handleUpdate(data) : handleSave(data);
  };

  const onFinish = async () => {
    await validateFields();
    const validationErrors = Object.values(form.getFieldsError());
    if (validationErrors.every(e => e.errors.length === 0)) {
      handleSubmit();
    }
  };

  return {
    busy: isCreating || isUpdating || isDeleting,
    goBack,
    handleDelete,
    onFinish,
  };
};
