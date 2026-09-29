import { FormInstance } from 'antd/es/form/Form';
import { useForm } from 'antd/lib/form/Form';

interface CreateTripeRequestHook {
  groupTransferForm: FormInstance;
}

export const useGroupTransferTrip = (): CreateTripeRequestHook => {
  const [groupTransferForm] = useForm();
  return {
    groupTransferForm,
  };
};
